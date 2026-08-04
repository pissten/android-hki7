package com.jimz011apps.hki7.sharedui.icons

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jimz011apps.hki7.resources.Res
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.FontResource

/**
 * The same four bundled icon packs used by the Android app. The fonts and lookup tables are copied
 * byte-for-byte into Compose resources so browser and Android can render identical glyphs from the
 * same persisted qualified slugs (`mdi:`, `si:`, `tb:`, and `ph:`).
 */
enum class Hki7IconPack(
    val id: String,
    val displayName: String,
    internal val codepointsPath: String,
    internal val keywordsPath: String,
    val hasCategories: Boolean,
) {
    MDI("mdi", "Material Design Icons", "files/icons/mdi_codepoints.txt", "files/icons/mdi_keywords.txt", true),
    SIMPLE("si", "Simple Icons", "files/icons/simple_codepoints.txt", "files/icons/simple_keywords.txt", false),
    TABLER("tb", "Tabler", "files/icons/tabler_codepoints.txt", "files/icons/tabler_keywords.txt", true),
    PHOSPHOR("ph", "Phosphor", "files/icons/phosphor_codepoints.txt", "files/icons/phosphor_keywords.txt", true);

    companion object {
        val DEFAULT = MDI

        fun fromId(id: String?): Hki7IconPack = entries.firstOrNull { it.id == id } ?: DEFAULT

        fun parse(name: String?): Pair<Hki7IconPack, String> {
            val raw = name?.trim().orEmpty()
            val colon = raw.indexOf(':')
            if (colon > 0) {
                val prefix = raw.substring(0, colon).lowercase()
                entries.firstOrNull { it.id == prefix }?.let { pack ->
                    return pack to raw.substring(colon + 1)
                }
            }
            return MDI to raw
        }

        fun qualify(pack: Hki7IconPack, slug: String): String =
            if (pack == MDI) slug else "${pack.id}:$slug"
    }
}

private fun Hki7IconPack.fontResource(): FontResource = when (this) {
    Hki7IconPack.MDI -> Res.font.mdi_icons
    Hki7IconPack.SIMPLE -> Res.font.simple_icons
    Hki7IconPack.TABLER -> Res.font.tabler_icons
    Hki7IconPack.PHOSPHOR -> Res.font.phosphor_icons
}

private data class IconTables(
    val glyphs: Map<String, String>,
    val order: List<String>,
    val keywords: Map<String, String>,
)

/** Browser-safe asynchronous loader for the existing HKI 7 icon tables. */
object Hki7IconCatalog {
    private val mutex = Mutex()
    private val tables = mutableMapOf<Hki7IconPack, IconTables>()

    suspend fun load(pack: Hki7IconPack): Unit = ensureTables(pack).let { }

    suspend fun glyphOf(name: String?): Pair<Hki7IconPack, String>? {
        if (name.isNullOrBlank()) return null
        val (pack, rawSlug) = Hki7IconPack.parse(name)
        val table = ensureTables(pack)
        val slug = rawSlug.lowercase()
        table.glyphs[slug]?.let { return pack to it }

        if (pack == Hki7IconPack.MDI) {
            val mapped = com.jimz011apps.hki7.ui.utils.LEGACY_ICON_MAP[rawSlug]
                ?: com.jimz011apps.hki7.ui.utils.LEGACY_ICON_MAP[slug]
            mapped?.let { table.glyphs[it] }?.let { return pack to it }
        }
        return null
    }

    suspend fun allNames(pack: Hki7IconPack = Hki7IconPack.MDI): List<String> =
        ensureTables(pack).order

    suspend fun search(query: String, pack: Hki7IconPack = Hki7IconPack.MDI): List<String> {
        val table = ensureTables(pack)
        val normalized = query.trim().lowercase()
        if (normalized.isEmpty()) return table.order
        return table.order.filter { slug ->
            slug.contains(normalized) || table.keywords[slug]?.contains(normalized) == true
        }
    }

    suspend fun byCategory(tag: String, pack: Hki7IconPack = Hki7IconPack.MDI): List<String> {
        val table = ensureTables(pack)
        val needle = "#${tag.lowercase()}"
        return table.order.filter { table.keywords[it]?.contains(needle) == true }
    }

    private suspend fun ensureTables(pack: Hki7IconPack): IconTables {
        tables[pack]?.let { return it }
        return mutex.withLock {
            tables[pack] ?: loadTables(pack).also { tables[pack] = it }
        }
    }

    private suspend fun loadTables(pack: Hki7IconPack): IconTables {
        val glyphs = LinkedHashMap<String, String>(9000)
        val order = ArrayList<String>(8000)
        Res.readBytes(pack.codepointsPath).decodeToString().lineSequence().forEach { line ->
            val separator = line.indexOf(' ')
            if (separator <= 0) return@forEach
            val codePoint = line.substring(separator + 1).trim().toIntOrNull(16) ?: return@forEach
            val slug = line.substring(0, separator)
            glyphs[slug] = codePointToString(codePoint)
            order += slug
        }

        val keywords = LinkedHashMap<String, String>(8000)
        Res.readBytes(pack.keywordsPath).decodeToString().lineSequence().forEach { line ->
            val separator = line.indexOf('\t')
            if (separator <= 0) return@forEach
            keywords[line.substring(0, separator)] = line.substring(separator + 1).lowercase()
        }
        return IconTables(glyphs = glyphs, order = order, keywords = keywords)
    }
}

/**
 * Shared renderer for HKI 7's persisted icon slugs. Until the table is loaded, the box keeps its
 * final dimensions; once loaded, the original bundled glyph appears without a layout jump.
 */
@Composable
fun Hki7Icon(
    name: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    size: Dp = 24.dp,
    contentDescription: String? = null,
) {
    var resolved by remember(name) { mutableStateOf<Pair<Hki7IconPack, String>?>(null) }
    LaunchedEffect(name) {
        resolved = Hki7IconCatalog.glyphOf(name)
            ?: Hki7IconCatalog.glyphOf("lightbulb")
    }

    val descriptionModifier = if (contentDescription != null) {
        Modifier.semantics { this.contentDescription = contentDescription }
    } else {
        Modifier
    }

    Box(
        modifier = modifier.then(descriptionModifier).size(size),
        contentAlignment = Alignment.Center,
    ) {
        resolved?.let { (pack, glyph) ->
            val family = FontFamily(Font(pack.fontResource()))
            val fontSize = with(LocalDensity.current) { size.toSp() }
            BasicText(
                text = glyph,
                style = TextStyle(
                    color = tint,
                    fontSize = fontSize,
                    lineHeight = fontSize,
                    fontFamily = family,
                    textAlign = TextAlign.Center,
                ),
            )
        }
    }
}

private fun codePointToString(codePoint: Int): String {
    if (codePoint <= 0xFFFF) return codePoint.toChar().toString()
    val adjusted = codePoint - 0x10000
    val high = ((adjusted ushr 10) + 0xD800).toChar()
    val low = ((adjusted and 0x3FF) + 0xDC00).toChar()
    return "$high$low"
}

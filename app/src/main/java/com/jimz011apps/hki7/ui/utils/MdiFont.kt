package com.jimz011apps.hki7.ui.utils

import android.content.Context

/**
 * Android-only synchronous search index retained for the existing icon picker while the picker UI
 * is still in the Android module. Icon rendering itself now goes through the shared [MdiIcon]
 * composable and the same Compose resources used by the web target.
 */
object MdiIconStore {
    private class Tables {
        @Volatile var glyphs: Map<String, String>? = null
        @Volatile var order: List<String>? = null
        @Volatile var keywords: Map<String, String>? = null
    }

    private val tables: Map<IconPack, Tables> = IconPack.entries.associateWith { Tables() }

    private fun ensureGlyphs(context: Context, pack: IconPack): Map<String, String> {
        val table = tables.getValue(pack)
        table.glyphs?.let { return it }
        return synchronized(table) {
            table.glyphs ?: run {
                val glyphs = HashMap<String, String>(9000)
                val order = ArrayList<String>(7500)
                context.applicationContext.assets.open(pack.codepointsAsset)
                    .bufferedReader().useLines { lines ->
                        for (line in lines) {
                            val separator = line.indexOf(' ')
                            if (separator <= 0) continue
                            val codePoint = line.substring(separator + 1).trim().toIntOrNull(16) ?: continue
                            val name = line.substring(0, separator)
                            glyphs[name] = String(Character.toChars(codePoint))
                            order.add(name)
                        }
                    }
                table.order = order
                table.glyphs = glyphs
                glyphs
            }
        }
    }

    private fun ensureKeywords(context: Context, pack: IconPack): Map<String, String> {
        val table = tables.getValue(pack)
        table.keywords?.let { return it }
        return synchronized(table) {
            table.keywords ?: run {
                val keywords = HashMap<String, String>(7000)
                context.applicationContext.assets.open(pack.keywordsAsset)
                    .bufferedReader().useLines { lines ->
                        for (line in lines) {
                            val separator = line.indexOf('\t')
                            if (separator <= 0) continue
                            keywords[line.substring(0, separator)] = line.substring(separator + 1).lowercase()
                        }
                    }
                table.keywords = keywords
                keywords
            }
        }
    }

    /** Resolves any pack-qualified slug to its glyph, including legacy HKI 7 identifiers. */
    fun glyphOf(context: Context, name: String?): String? {
        if (name.isNullOrBlank()) return null
        val (pack, bare) = IconPack.parse(name)
        val glyphs = ensureGlyphs(context, pack)
        val slug = bare.lowercase()
        glyphs[slug]?.let { return it }
        if (pack == IconPack.MDI) {
            (LEGACY_ICON_MAP[bare] ?: LEGACY_ICON_MAP[slug])?.let { mapped ->
                glyphs[mapped]?.let { return it }
            }
        }
        return null
    }

    /** All icon slugs in [pack], alphabetically ordered. */
    fun allNames(context: Context, pack: IconPack = IconPack.MDI): List<String> {
        ensureGlyphs(context, pack)
        return tables.getValue(pack).order ?: emptyList()
    }

    /** Slugs whose name or keywords contain [query], case-insensitively. */
    fun search(context: Context, query: String, pack: IconPack = IconPack.MDI): List<String> {
        val order = allNames(context, pack)
        val normalized = query.trim().lowercase()
        if (normalized.isEmpty()) return order
        val keywords = ensureKeywords(context, pack)
        return order.filter { it.contains(normalized) || keywords[it]?.contains(normalized) == true }
    }

    /** Slugs tagged with category [tag], matched on the existing `#tag` keyword convention. */
    fun byCategory(context: Context, tag: String, pack: IconPack = IconPack.MDI): List<String> {
        val order = allNames(context, pack)
        val keywords = ensureKeywords(context, pack)
        val needle = "#${tag.lowercase()}"
        return order.filter { keywords[it]?.contains(needle) == true }
    }
}

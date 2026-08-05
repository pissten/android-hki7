@file:OptIn(InternalResourceApi::class)

package com.jimz011apps.hki7.resources

import kotlin.OptIn
import kotlin.String
import kotlin.collections.MutableMap
import org.jetbrains.compose.resources.FontResource
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.ResourceContentHash
import org.jetbrains.compose.resources.ResourceItem

private const val MD: String = "composeResources/com.jimz011apps.hki7.resources/"

@delegate:ResourceContentHash(-264_906_637)
public val Res.font.mdi_icons: FontResource by lazy {
      FontResource("font:mdi_icons", setOf(
        ResourceItem(setOf(), "${MD}font/mdi_icons.ttf", -1, -1),
      ))
    }

@delegate:ResourceContentHash(1_595_816_707)
public val Res.font.phosphor_icons: FontResource by lazy {
      FontResource("font:phosphor_icons", setOf(
        ResourceItem(setOf(), "${MD}font/phosphor_icons.ttf", -1, -1),
      ))
    }

@delegate:ResourceContentHash(1_942_781_832)
public val Res.font.simple_icons: FontResource by lazy {
      FontResource("font:simple_icons", setOf(
        ResourceItem(setOf(), "${MD}font/simple_icons.ttf", -1, -1),
      ))
    }

@delegate:ResourceContentHash(1_867_436_879)
public val Res.font.tabler_icons: FontResource by lazy {
      FontResource("font:tabler_icons", setOf(
        ResourceItem(setOf(), "${MD}font/tabler_icons.ttf", -1, -1),
      ))
    }

@InternalResourceApi
internal fun _collectCommonMainFont0Resources(map: MutableMap<String, FontResource>) {
  map.put("mdi_icons", Res.font.mdi_icons)
  map.put("phosphor_icons", Res.font.phosphor_icons)
  map.put("simple_icons", Res.font.simple_icons)
  map.put("tabler_icons", Res.font.tabler_icons)
}

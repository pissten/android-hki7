@file:OptIn(org.jetbrains.compose.resources.InternalResourceApi::class)

package com.jimz011apps.hki7.resources

import kotlin.OptIn
import kotlin.String
import kotlin.collections.Map
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.FontResource
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringArrayResource
import org.jetbrains.compose.resources.StringResource

public actual val Res.allDrawableResources: Map<String, DrawableResource> by lazy {
  val map = mutableMapOf<String, DrawableResource>()
  return@lazy map
}

public actual val Res.allStringResources: Map<String, StringResource> by lazy {
  val map = mutableMapOf<String, StringResource>()
  _collectCommonMainString0Resources(map)
  _collectCommonMainString10Resources(map)
  _collectCommonMainString11Resources(map)
  _collectCommonMainString12Resources(map)
  _collectCommonMainString13Resources(map)
  _collectCommonMainString14Resources(map)
  _collectCommonMainString15Resources(map)
  _collectCommonMainString16Resources(map)
  _collectCommonMainString17Resources(map)
  _collectCommonMainString18Resources(map)
  _collectCommonMainString19Resources(map)
  _collectCommonMainString1Resources(map)
  _collectCommonMainString20Resources(map)
  _collectCommonMainString21Resources(map)
  _collectCommonMainString22Resources(map)
  _collectCommonMainString23Resources(map)
  _collectCommonMainString24Resources(map)
  _collectCommonMainString25Resources(map)
  _collectCommonMainString26Resources(map)
  _collectCommonMainString27Resources(map)
  _collectCommonMainString28Resources(map)
  _collectCommonMainString29Resources(map)
  _collectCommonMainString2Resources(map)
  _collectCommonMainString30Resources(map)
  _collectCommonMainString31Resources(map)
  _collectCommonMainString3Resources(map)
  _collectCommonMainString4Resources(map)
  _collectCommonMainString5Resources(map)
  _collectCommonMainString6Resources(map)
  _collectCommonMainString7Resources(map)
  _collectCommonMainString8Resources(map)
  _collectCommonMainString9Resources(map)
  return@lazy map
}

public actual val Res.allStringArrayResources: Map<String, StringArrayResource> by lazy {
  val map = mutableMapOf<String, StringArrayResource>()
  _collectCommonMainArray0Resources(map)
  return@lazy map
}

public actual val Res.allPluralStringResources: Map<String, PluralStringResource> by lazy {
  val map = mutableMapOf<String, PluralStringResource>()
  _collectCommonMainPlurals0Resources(map)
  return@lazy map
}

public actual val Res.allFontResources: Map<String, FontResource> by lazy {
  val map = mutableMapOf<String, FontResource>()
  _collectCommonMainFont0Resources(map)
  return@lazy map
}

package com.immichframe.immichframe.helpers

import android.content.Context

object TextSizeHelper {
    fun textSizeMultiplier(context: Context, currentSizeSp: Float, multiplier: Float): Float {
        val resources = context.resources
        val fontScale = resources.configuration.fontScale
        val density = resources.displayMetrics.density
        val currentSizePx = currentSizeSp * density * fontScale
        val newSizePx = currentSizePx * multiplier

        return newSizePx / (density * fontScale)
    }

    fun cssFontSizeToSp(cssSize: String, context: Context, baseFontSizePx: Float = 16f): Float {
        val resources = context.resources
        val displayMetrics = resources.displayMetrics
        val fontScale = resources.configuration.fontScale
        val density = displayMetrics.density

        return when {
            cssSize.equals("xx-small", ignoreCase = true) -> 8f * fontScale
            cssSize.equals("x-small", ignoreCase = true) -> 10f * fontScale
            cssSize.equals("small", ignoreCase = true) -> 12f * fontScale
            cssSize.equals("medium", ignoreCase = true) -> 16f * fontScale
            cssSize.equals("large", ignoreCase = true) -> 20f * fontScale
            cssSize.equals("x-large", ignoreCase = true) -> 24f * fontScale
            cssSize.equals("xx-large", ignoreCase = true) -> 32f * fontScale

            cssSize.endsWith("px", ignoreCase = true) -> {
                val px = cssSize.removeSuffix("px").toFloatOrNull() ?: baseFontSizePx
                px / (density * fontScale)
            }

            cssSize.endsWith("pt", ignoreCase = true) -> {
                val pt = cssSize.removeSuffix("pt").toFloatOrNull() ?: baseFontSizePx
                val px = pt * (density * 160f / 72f)
                px / (density * fontScale)
            }

            cssSize.endsWith("em", ignoreCase = true) -> {
                val em = cssSize.removeSuffix("em").toFloatOrNull() ?: 1f
                val px = em * baseFontSizePx
                px / (density * fontScale)
            }

            else -> 16f * fontScale
        }
    }
}
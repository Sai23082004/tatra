package com.mahiya.safegas.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun getScreenWidth(): Dp {
    return LocalConfiguration.current.screenWidthDp.dp
}

@Composable
fun getScreenHeight(): Dp {
    return LocalConfiguration.current.screenHeightDp.dp
}

@Composable
fun isTablet(): Boolean {
    return getScreenWidth() > 600.dp
}

@Composable
fun isSmallScreen(): Boolean {
    return getScreenHeight() < 700.dp
}

@Composable
fun getResponsivePadding(): Dp {
    return if (isTablet()) 32.dp else 16.dp
}

@Composable
fun getResponsiveTextSize(baseSize: Float): Float {
    return if (isTablet()) baseSize * 1.2f else baseSize
}

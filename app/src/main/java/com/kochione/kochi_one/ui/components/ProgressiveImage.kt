package com.kochione.kochi_one.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest

@Composable
fun ProgressiveImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    shape: Shape = RectangleShape,
    alpha: Float = 1.0f,
    colorFilter: ColorFilter? = null,
    showLoadingCircle: Boolean = false,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    SubcomposeAsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(model)
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        alpha = alpha,
        colorFilter = colorFilter,
        loading = {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                SkeletonBox(
                    modifier = Modifier.fillMaxSize(),
                    shape = shape,
                    isDarkTheme = isDarkTheme
                )
                if (showLoadingCircle) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White, // White color
                        strokeWidth = 2.dp
                    )
                }
            }
        }
    )
}

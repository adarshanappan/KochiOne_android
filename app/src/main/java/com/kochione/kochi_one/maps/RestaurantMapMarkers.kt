package com.kochione.kochi_one.maps

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Precision
import coil.size.Scale
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberMarkerState
import com.kochione.kochi_one.R
import com.kochione.kochi_one.models.Restaurant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
@GoogleMapComposable
fun RestaurantMapMarkers(
    restaurants: List<Restaurant>,
    visible: Boolean,
    onRestaurantMarkerClick: (Restaurant) -> Unit = {}
) {
    if (!visible) return

    val context = LocalContext.current
    val density = LocalDensity.current
    val sizePx = remember(density) {
        with(density) { 36.dp.roundToPx() }
    }
    val imageLoader = remember(context) { ImageLoader(context) }

    for (restaurant in restaurants) {
        val lat = restaurant.location.latitude
        val lng = restaurant.location.longitude
        if (!lat.isFinite() || !lng.isFinite()) continue
        if (lat == 0.0 && lng == 0.0) continue

        key(restaurant.bizId) {
            RestaurantLogoMarker(
                restaurant = restaurant,
                sizePx = sizePx,
                imageLoader = imageLoader,
                onMarkerClick = onRestaurantMarkerClick
            )
        }
    }
}

@Composable
@GoogleMapComposable
private fun RestaurantLogoMarker(
    restaurant: Restaurant,
    sizePx: Int,
    imageLoader: ImageLoader,
    onMarkerClick: (Restaurant) -> Unit
) {
    val context = LocalContext.current
    val state = rememberMarkerState(
        key = restaurant.bizId,
        position = LatLng(restaurant.location.latitude, restaurant.location.longitude)
    )
    var icon by remember(restaurant.bizId) {
        mutableStateOf(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
    }

    LaunchedEffect(restaurant.bizId, restaurant.logo?.url, sizePx) {
        val custom = withContext(Dispatchers.Default) {
            try {
                buildRestaurantMarkerDescriptor(
                    context = context,
                    imageLoader = imageLoader,
                    logoUrl = restaurant.logo?.url,
                    sizePx = sizePx,
                    fallbackRes = R.drawable.ic_eat
                )
            } catch (_: Exception) {
                null
            }
        }
        if (custom != null) icon = custom
    }

    Marker(
        state = state,
        title = restaurant.name,
        icon = icon,
        anchor = Offset(0.5f, 1f),
        snippet = restaurant.address.city,
        onClick = {
            onMarkerClick(restaurant)
            true
        }
    )
}

private suspend fun buildRestaurantMarkerDescriptor(
    context: Context,
    imageLoader: ImageLoader,
    logoUrl: String?,
    sizePx: Int,
    @DrawableRes fallbackRes: Int
): BitmapDescriptor {
    val w = sizePx.coerceAtLeast(24)
    val h = (w * 1.4f).roundToInt().coerceAtLeast(w + 8)
    val headCx = w / 2f
    val headR = w * 0.38f
    val headCy = headR + 3f
    val tipY = (h - 2).toFloat()
    // Thin ring so the logo reads as “filled” inside the pin head (still clears the pin outline).
    val headInset = max(1.2f, headR * 0.055f)
    val innerR = (headR - headInset).coerceAtLeast(4f)
    val innerD = (innerR * 2f).roundToInt().coerceAtLeast(8)
    val decodeSide = (innerD * 2).coerceAtLeast(64)

    val innerBitmap: Bitmap = if (!logoUrl.isNullOrBlank()) {
        val request = ImageRequest.Builder(context)
            .data(logoUrl)
            .size(decodeSide, decodeSide)
            .scale(Scale.FILL)
            .precision(Precision.EXACT)
            .allowHardware(false)
            .build()
        when (val result = imageLoader.execute(request)) {
            is SuccessResult -> result.drawable.toBitmap(decodeSide, decodeSide)
            else -> loadFallbackBitmap(context, fallbackRes, decodeSide)
        }
    } else {
        loadFallbackBitmap(context, fallbackRes, decodeSide)
    }

    val output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)

    val cone = Path().apply {
        moveTo(headCx, tipY)
        lineTo(headCx - headR * 0.95f, headCy + headR * 0.22f)
        lineTo(headCx + headR * 0.95f, headCy + headR * 0.22f)
        close()
    }
    val head = Path().apply {
        addCircle(headCx, headCy, headR + 1f, Path.Direction.CW)
    }
    val pinOutline = Path().apply {
        op(head, cone, Path.Op.UNION)
    }

    val pinFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.WHITE
        style = Paint.Style.FILL
    }
    canvas.drawPath(pinOutline, pinFill)

    val pinStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.argb(70, 0, 0, 0)
        style = Paint.Style.STROKE
        strokeWidth = (w * 0.035f).coerceIn(0.8f, 2.2f)
    }
    canvas.drawPath(pinOutline, pinStroke)

    val side = innerR * 2f
    val shader = BitmapShader(innerBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    shader.setLocalMatrix(shaderMatrixCenterCropInSquare(innerBitmap, side))
    val imagePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isFilterBitmap = true
        this.shader = shader
    }
    canvas.save()
    canvas.translate(headCx - innerR, headCy - innerR)
    canvas.drawCircle(innerR, innerR, innerR, imagePaint)
    canvas.restore()

    return BitmapDescriptorFactory.fromBitmap(output)
}

/**
 * Fills a square [dstSide]×[dstSide] with center-cropped bitmap (like ImageView centerCrop).
 * Used in local coords where the circle is drawn at (innerR, innerR).
 */
private fun shaderMatrixCenterCropInSquare(bitmap: Bitmap, dstSide: Float): Matrix {
    val m = Matrix()
    val sw = bitmap.width.toFloat()
    val sh = bitmap.height.toFloat()
    if (sw <= 0f || sh <= 0f || dstSide <= 0f) return m
    val scale = max(dstSide / sw, dstSide / sh)
    m.postScale(scale, scale)
    m.postTranslate((dstSide - sw * scale) / 2f, (dstSide - sh * scale) / 2f)
    return m
}

private fun loadFallbackBitmap(context: Context, @DrawableRes res: Int, size: Int): Bitmap {
    val drawable = ContextCompat.getDrawable(context, res)!!
    return drawable.toBitmap(size, size)
}

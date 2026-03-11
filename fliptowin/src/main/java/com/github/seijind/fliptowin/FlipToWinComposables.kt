package com.github.seijind.fliptowin

import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import kotlinx.coroutines.isActive
import kotlin.math.floor
import kotlin.math.roundToInt

@Composable
internal fun FlipToWinGrid(
    uiState: FlipToWinUiState,
    onCardClicked: (Int) -> Unit,
    onMoveInCenterAnimationEnded: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val size = remember { mutableStateOf(IntSize(0, 0)) }
    val columns = uiState.config.gridColumns
    val rows = if (columns > 0) (uiState.items.size + columns - 1) / columns else 1

    val width = remember(density, size.value, columns) {
        with(density) {
            when {
                size.value.width <= size.value.height -> floor((size.value.width - 16.dp.toPx()).div(columns)).toDp()
                else -> floor((size.value.height - 16.dp.toPx()).div(columns)).toDp()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { intSize -> size.value = intSize },
        contentAlignment = Alignment.Center
    ) {
        FlowRow(
            modifier = Modifier.padding(vertical = 8.dp),
            maxItemsInEachRow = columns,
            maxLines = rows,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            uiState.items.forEachIndexed { index, item ->
                FlipToWinCard(
                    item = item,
                    onCardClicked = { onCardClicked(index) },
                    onMoveInCenterAnimationEnded = { onMoveInCenterAnimationEnded(index) },
                    modifier = Modifier
                        .width(width)
                        .aspectRatio(1f)
                        .padding(horizontal = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun FlipToWinCard(
    item: FlipToWinUiCardData,
    onCardClicked: () -> Unit,
    onMoveInCenterAnimationEnded: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val screenSize = LocalWindowInfo.current.containerSize

    val cardPosition = remember { mutableStateOf(Offset.Zero) }
    val cardSize = remember { mutableIntStateOf(0) }

    val wiggleAnim by rememberWiggleAnim(item.isWiggling)
    val scaleAnim by rememberScaleAnim(item.isScaling)
    val offsetAnim by rememberOffsetAnim(item.moveInCenter, cardPosition, cardSize, screenSize) { onMoveInCenterAnimationEnded() }
    val rotationAnim by rememberRotationAnim(item.isFlipped)

    Box(
        modifier = modifier
            .onSizeChanged { size -> cardSize.intValue = size.width }
            .onGloballyPositioned { coordinates -> cardPosition.value = coordinates.positionInRoot() }
            .absoluteOffset { IntOffset(offsetAnim.x.roundToInt(), offsetAnim.y.roundToInt()) }
            .zIndex(if (item.isSelected) 1f else 0f)
            .graphicsLayer {
                rotationY = rotationAnim
                rotationZ = wiggleAnim
                scaleX = scaleAnim
                scaleY = scaleAnim
                cameraDistance = 12 * density.density
            }
            .clip(RoundedCornerShape(8.dp))
            .drawBehind {
                val brush = item.brush ?: Brush.verticalGradient(colors = listOf(Color.White, Color.White))
                drawRoundRect(
                    brush = brush,
                    cornerRadius = CornerRadius(8.dp.toPx())
                )
            }
            .clickable(enabled = item.clickable) { onCardClicked() },
        contentAlignment = Alignment.Center
    ) {
        FlipToWinCardImageItem(
            image = item.image,
            bitmap = item.bitmap,
            isFlipped = item.isFlipped,
        )
    }
}

@Composable
private fun FlipToWinCardImageItem(
    image: String,
    bitmap: Bitmap?,
    isFlipped: Boolean,
) {
    val modifier = Modifier
        .fillMaxSize(if (bitmap == null) 0.5f else 1f)
        .graphicsLayer {
            if (isFlipped) scaleX = -1f
        }

    if (bitmap == null) {
        AsyncImage(
            model = image.loadImageWithCrossfade(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier,
        )
    } else {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = modifier
        )
    }
}

@Composable
private fun rememberWiggleAnim(isWiggling: Boolean): State<Float> {
    val wiggleAnim = remember { Animatable(0f) }
    LaunchedEffect(isWiggling) {
        if (isWiggling) {
            while (isActive) {
                wiggleAnim.animateTo(-10f, tween(700))
                wiggleAnim.animateTo(10f, tween(700))
            }
        } else {
            wiggleAnim.snapTo(0f)
        }
    }
    return wiggleAnim.asState()
}

@Composable
private fun rememberScaleAnim(isScaling: Boolean): State<Float> {
    val scaleAnim = remember { Animatable(1f) }
    LaunchedEffect(isScaling) {
        val target = if (isScaling) 2.5f else 1f
        scaleAnim.animateTo(target, tween(500))
    }
    return scaleAnim.asState()
}

@Composable
private fun rememberOffsetAnim(
    moveInCenter: Boolean,
    cardPosition: State<Offset>,
    cardSize: MutableIntState,
    screenSize: IntSize,
    onAnimationEnded: () -> Unit,
): State<Offset> {
    val offsetAnim = remember { Animatable(initialValue = Offset.Zero, typeConverter = Offset.VectorConverter) }

    LaunchedEffect(key1 = moveInCenter) {
        val targetOffset = if (moveInCenter) {
            val x = screenSize.width / 2f - cardSize.intValue.div(2) - cardPosition.value.x
            val y = screenSize.height / 2f - cardSize.intValue.div(2) - cardPosition.value.y
            Offset(x, y)
        } else {
            Offset.Zero
        }
        offsetAnim.animateTo(targetOffset, tween(500))
        if (moveInCenter) {
            onAnimationEnded()
        }
    }

    return offsetAnim.asState()
}

@Composable
private fun rememberRotationAnim(isFlipped: Boolean): State<Float> {
    val rotationAnim = remember { Animatable(0f) }
    LaunchedEffect(isFlipped) {
        val target = if (isFlipped) 180f else 0f
        rotationAnim.animateTo(
            targetValue = target,
            animationSpec = tween(durationMillis = 700)
        )
    }
    return rotationAnim.asState()
}

@Composable
@ReadOnlyComposable
fun String.loadImageWithCrossfade(@DrawableRes placeholder: Int? = null): ImageRequest {
    return if (placeholder != null) {
        ImageRequest.Builder(LocalContext.current)
            .placeholder(placeholder)
            .error(placeholder)
            .data(this)
            .crossfade(true)
            .build()
    } else {
        ImageRequest.Builder(LocalContext.current)
            .data(this)
            .crossfade(true)
            .build()
    }
}

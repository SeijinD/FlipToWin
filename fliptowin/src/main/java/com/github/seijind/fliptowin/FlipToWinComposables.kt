package com.github.seijind.fliptowin

import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
internal fun LoyaltyFlipGrid(
    uiState: FlipToWinUiState,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val size = remember { mutableStateOf(IntSize(0, 0)) }
    val width = with(density) {
        when {
            size.value.width <= size.value.height -> floor((size.value.width - 16.dp.toPx()).div(3)).toDp()
            else -> floor((size.value.height - 16.dp.toPx()).div(3)).toDp()
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
            maxItemsInEachRow = 3,
            maxLines = 3,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            uiState.items.forEach { cardItem ->
                LoyaltyFlipCard(
                    item = cardItem,
                    onCardClicked = { item -> uiState.onCardClicked(item) },
                    onMoveInCenterAnimationEnded = uiState.onMoveInCenterAnimationEnded,
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
private fun LoyaltyFlipCard(
    item: FlipToWinUiItem,
    onCardClicked: (FlipToWinUiItem) -> Unit,
    onMoveInCenterAnimationEnded: (FlipToWinUiItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val context = LocalContext.current

    val cardPosition = remember { mutableStateOf(Offset.Zero) }
    val cardSize = remember { mutableIntStateOf(0) }

    val screenSize = remember {
        val metrics = context.resources.displayMetrics
        IntSize(metrics.widthPixels, metrics.heightPixels)
    }

    val wiggleAnim by rememberWiggleAnim(item.isWiggling)
    val scaleAnim by rememberScaleAnim(item.isScaling)
    val offsetAnim by rememberOffsetAnim(item.moveInCenter, cardPosition, cardSize, screenSize) {
        onMoveInCenterAnimationEnded(item)
    }
    val rotationAnim by rememberRotationAnim(item.isFlipped)

    Box(
        modifier = modifier
            .onSizeChanged { size -> cardSize.intValue = size.width }
            .onGloballyPositioned { coordinates -> cardPosition.value = coordinates.positionInRoot() }
            .absoluteOffset { IntOffset(offsetAnim.x.roundToInt(), offsetAnim.y.roundToInt()) }
            .zIndex(if (item.isSelected.value) 1f else 0f)
            .graphicsLayer {
                rotationY = rotationAnim
                rotationZ = wiggleAnim
                scaleX = scaleAnim
                scaleY = scaleAnim
                cameraDistance = 12 * density.density
            }
            .clip(RoundedCornerShape(8.dp))
            .background(
                brush = item.brush.value ?: Brush.verticalGradient(colors = listOf(Color.White, Color.White)),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(enabled = item.clickable.value) { onCardClicked(item) },
        contentAlignment = Alignment.Center
    ) {
        LoyaltyFlipCardImageItem(
            image = item.image.value,
            bitmap = item.bitmap,
        )
    }
}

@Composable
private fun LoyaltyFlipCardImageItem(
    image: String,
    bitmap: State<Bitmap?>,
) {
    if (bitmap.value == null) {
        AsyncImage(
            model = image.loadImageWithCrossfade(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(0.5f),
        )
    } else {
        bitmap.value?.asImageBitmap()?.let {
            Image(
                bitmap = it,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = -1f
                    }
            )
        }
    }
}

@Composable
private fun rememberWiggleAnim(isWiggling: State<Boolean>): State<Float> {
    val wiggleAnim = remember { Animatable(0f) }
    LaunchedEffect(isWiggling.value) {
        if (isWiggling.value) {
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
private fun rememberScaleAnim(isScaling: State<Boolean>): State<Float> {
    val scaleAnim = remember { Animatable(1f) }
    LaunchedEffect(isScaling.value) {
        val target = if (isScaling.value) 2.5f else 1f
        scaleAnim.animateTo(target, tween(500))
    }
    return scaleAnim.asState()
}

@Composable
private fun rememberOffsetAnim(
    moveInCenter: State<Boolean>,
    cardPosition: State<Offset>,
    cardSize: MutableIntState,
    screenSize: IntSize,
    onAnimationEnded: () -> Unit,
): State<Offset> {
    val offsetAnim = remember { Animatable(initialValue = Offset.Zero, typeConverter = Offset.VectorConverter) }

    LaunchedEffect(key1 = moveInCenter.value) {
        val targetOffset = if (moveInCenter.value) {
            val x = screenSize.width / 2f - cardSize.intValue.div(2) - cardPosition.value.x
            val y = screenSize.height / 2f - cardSize.intValue.div(2) - cardPosition.value.y
            Offset(x, y)
        } else {
            Offset.Zero
        }
        offsetAnim.animateTo(targetOffset, tween(500))
        if (moveInCenter.value) {
            onAnimationEnded()
        }
    }

    return offsetAnim.asState()
}

@Composable
private fun rememberRotationAnim(isFlipped: State<Boolean>): State<Float> {
    val rotationAnim = remember { Animatable(0f) }
    LaunchedEffect(isFlipped.value) {
        val target = if (isFlipped.value) 180f else 0f
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

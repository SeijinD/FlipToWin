package com.github.seijind.fliptowin.ui.composable

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import coil3.imageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.github.seijind.fliptowin.ui.model.FlipToWinUiCard
import com.github.seijind.fliptowin.ui.model.FlipToWinUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt

/** Centralised animation defaults used by card composables. */
internal object AnimationDefaults {
    const val WIGGLE_ANGLE = 10f
    const val WIGGLE_DURATION_MS = 700
    const val ZOOM_TARGET = 2.5f
    const val ZOOM_DURATION_MS = 500
    const val OFFSET_DURATION_MS = 500
    const val FLIP_DURATION_MS = 700
    const val CARD_CORNER_RADIUS_DP = 8
    const val CAMERA_DISTANCE_FACTOR = 12
    const val CARD_IMAGE_FILL_FRACTION = 0.5f
    /** Half the flip animation duration — used to time haptic at the flip midpoint. */
    const val FLIP_MIDPOINT_MS = 350L
}

@Composable
internal fun FlipToWinGrid(
    uiState: FlipToWinUiState,
    onCardClicked: (Int) -> Unit,
    onCenteringAnimationEnded: (Int) -> Unit,
    cardContentDescription: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val size = remember { mutableStateOf(IntSize.Zero) }
    val columns = uiState.config.gridColumns
    val rows = if (columns > 0) (uiState.cards.size + columns - 1) / columns else 1

    val width = remember(density, size.value, columns) {
        if (columns <= 0 || size.value.width <= 0) return@remember 0.dp
        
        with(density) {
            val paddingPx = 16.dp.toPx()
            val availablePx = if (size.value.width <= size.value.height) {
                size.value.width - paddingPx
            } else {
                size.value.height - paddingPx
            }
            
            val cardSizePx = floor(availablePx / columns)
            cardSizePx.toDp()
        }
    }

    LaunchedEffect(uiState.cards, uiState.rewardCatalog) {
        val allUrls = (uiState.cards.map { it.imageUrl } + uiState.rewardCatalog.map { it.imageUrl })
            .filter { it.isNotEmpty() }
            .distinct()

        allUrls.forEach { url ->
            val request = ImageRequest.Builder(context)
                .data(url)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .build()
            context.imageLoader.enqueue(request)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
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
            uiState.cards.forEachIndexed { index, card ->
                FlipToWinCard(
                    card = card,
                    onCardClicked = { onCardClicked(index) },
                    onCenteringAnimationEnded = { onCenteringAnimationEnded(index) },
                    contentDescription = "$cardContentDescription ${index + 1}",
                    modifier = Modifier
                        .width(width)
                        .aspectRatio(1f)
                        .padding(horizontal = 4.dp),
                )
            }
        }
    }
}

/**
 * An individual FlipToWin card that handles its own animation state.
 *
 * It combines wiggle (shake), scale, translation (offset), and rotation (flip) animations.
 * Position is tracked via [onGloballyPositioned] to calculate the target translation to center.
 */
@Composable
private fun FlipToWinCard(
    card: FlipToWinUiCard,
    onCardClicked: () -> Unit,
    onCenteringAnimationEnded: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val screenSize = LocalWindowInfo.current.containerSize
    val haptic = LocalHapticFeedback.current

    val cardPosition = remember { mutableStateOf(Offset.Zero) }
    val cardSize = remember { mutableIntStateOf(0) }

    val wiggleAnim by rememberWiggleAnim(card.isWiggling)
    val zoomAnim by rememberZoomAnim(card.isZoomed)
    val offsetAnim by rememberOffsetAnim(card.isCentered, cardPosition, cardSize, screenSize) { onCenteringAnimationEnded() }
    val rotationAnim by rememberRotationAnim(card.isFlipped)

    HapticRevealEffect(card.isFlipped, card.isSelected, haptic)

    val cornerRadius = AnimationDefaults.CARD_CORNER_RADIUS_DP.dp

    Box(
        modifier = modifier
            .onSizeChanged { size -> cardSize.intValue = size.width }
            .onGloballyPositioned { coordinates -> cardPosition.value = coordinates.positionInRoot() }
            .absoluteOffset { IntOffset(offsetAnim.x.roundToInt(), offsetAnim.y.roundToInt()) }
            .zIndex(if (card.isSelected) 1f else 0f)
            .graphicsLayer {
                rotationY = rotationAnim
                rotationZ = wiggleAnim
                scaleX = zoomAnim
                scaleY = zoomAnim
                cameraDistance = AnimationDefaults.CAMERA_DISTANCE_FACTOR * density.density
            }
            .clip(RoundedCornerShape(cornerRadius))
            .drawBehind {
                val brush = card.brush ?: Brush.verticalGradient(colors = listOf(Color.White, Color.White))
                drawRoundRect(
                    brush = brush,
                    cornerRadius = CornerRadius(cornerRadius.toPx())
                )
            }
            .clickable(enabled = card.isClickable) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onCardClicked()
            },
        contentAlignment = Alignment.Center
    ) {
        FlipToWinCardImageItem(
            imageUrl = card.imageUrl,
            rotationYProvider = { rotationAnim },
            contentDescription = contentDescription,
        )
    }
}

@Composable
private fun FlipToWinCardImageItem(
    imageUrl: String,
    rotationYProvider: () -> Float,
    contentDescription: String,
) {
    AsyncImage(
        model = imageUrl.loadImageWithCrossfade(),
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxSize(AnimationDefaults.CARD_IMAGE_FILL_FRACTION)
            .graphicsLayer {
                val rotation = rotationYProvider()
                scaleX = if (abs(rotation) > 90f) -1f else 1f
            },
    )
}

@Composable
private fun HapticRevealEffect(isFlipped: Boolean, isSelected: Boolean, haptic: HapticFeedback) {
    LaunchedEffect(isFlipped) {
        if (isFlipped && isSelected) {
            delay(AnimationDefaults.FLIP_MIDPOINT_MS)
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }
}

@Composable
private fun rememberWiggleAnim(isWiggling: Boolean): State<Float> {
    val wiggleAnim = remember { Animatable(0f) }
    LaunchedEffect(isWiggling) {
        if (isWiggling) {
            while (isActive) {
                wiggleAnim.animateTo(-AnimationDefaults.WIGGLE_ANGLE, tween(AnimationDefaults.WIGGLE_DURATION_MS))
                wiggleAnim.animateTo(AnimationDefaults.WIGGLE_ANGLE, tween(AnimationDefaults.WIGGLE_DURATION_MS))
            }
        } else {
            wiggleAnim.snapTo(0f)
        }
    }
    return wiggleAnim.asState()
}

@Composable
private fun rememberZoomAnim(isZoomed: Boolean): State<Float> {
    val zoomAnim = remember { Animatable(1f) }
    LaunchedEffect(isZoomed) {
        val target = if (isZoomed) AnimationDefaults.ZOOM_TARGET else 1f
        zoomAnim.animateTo(target, tween(AnimationDefaults.ZOOM_DURATION_MS))
    }
    return zoomAnim.asState()
}

/**
 * Remembers and calculates the translation offset required to move a card to the screen center.
 *
 * @param isCentered Trigger for the animation.
 * @param cardPosition The absolute position of the card in the root coordinate system.
 * @param cardSize The width/height of the card.
 * @param screenSize Total available window size.
 * @param onAnimationEnded Callback triggered once the card reaches the target center position.
 *   Guaranteed to be called at most once per [isCentered] = true transition,
 *   even if [screenSize] changes during the animation (e.g. device rotation).
 */
@Composable
private fun rememberOffsetAnim(
    isCentered: Boolean,
    cardPosition: State<Offset>,
    cardSize: MutableIntState,
    screenSize: IntSize,
    onAnimationEnded: () -> Unit,
): State<Offset> {
    val offsetAnim = remember { Animatable(initialValue = Offset.Zero, typeConverter = Offset.VectorConverter) }
    val hasNotified = remember { mutableStateOf(false) }

    // screenSize is a key so that we recalculate the center position on rotation
    LaunchedEffect(key1 = isCentered, key2 = screenSize) {
        if (!isCentered) hasNotified.value = false
        val targetOffset = if (isCentered) {
            val x = screenSize.width / 2f - cardSize.intValue.div(2) - cardPosition.value.x
            val y = screenSize.height / 2f - cardSize.intValue.div(2) - cardPosition.value.y
            Offset(x, y)
        } else {
            Offset.Zero
        }
        offsetAnim.animateTo(targetOffset, tween(AnimationDefaults.OFFSET_DURATION_MS))
        if (isCentered && !hasNotified.value) {
            hasNotified.value = true
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
            animationSpec = tween(durationMillis = AnimationDefaults.FLIP_DURATION_MS)
        )
    }
    return rotationAnim.asState()
}

@Composable
internal fun String.loadImageWithCrossfade(@DrawableRes placeholder: Int? = null): ImageRequest {
    val context = LocalContext.current
    return remember(this, placeholder) {
        ImageRequest.Builder(context)
            .data(this)
            .crossfade(true)
            .apply {
                placeholder?.let {
                    placeholder(it)
                    error(it)
                }
            }
            .build()
    }
}
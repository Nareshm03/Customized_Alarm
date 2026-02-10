package com.example.teacherscheduler.ui.compose

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.navigation.NavBackStackEntry

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.ClassDetailTransition(
    classId: Long,
    animatedVisibilityScope: AnimatedVisibilityScope,
    content: @Composable () -> Unit
) {
    content()
}

object SharedElementKeys {
    fun classCard(id: Long) = "class_card_$id"
    fun classTitle(id: Long) = "class_title_$id"
    fun meetingCard(id: Long) = "meeting_card_$id"
    fun meetingTitle(id: Long) = "meeting_title_$id"
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedElementTransition(
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    key: String
): Modifier = with(sharedTransitionScope) {
    this@sharedElementTransition.sharedElement(
        state = rememberSharedContentState(key = key),
        animatedVisibilityScope = animatedVisibilityScope,
        boundsTransform = { _, _ ->
            tween(durationMillis = 300, easing = FastOutSlowInEasing)
        }
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedBoundsTransition(
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    key: String
): Modifier = with(sharedTransitionScope) {
    this@sharedBoundsTransition.sharedBounds(
        sharedContentState = rememberSharedContentState(key = key),
        animatedVisibilityScope = animatedVisibilityScope,
        enter = fadeIn() + scaleIn(initialScale = 0.8f),
        exit = fadeOut() + scaleOut(targetScale = 0.8f),
        boundsTransform = { _, _ ->
            tween(durationMillis = 300, easing = FastOutSlowInEasing)
        }
    )
}

package com.example.teacherscheduler.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*

@Composable
fun LottieEmptyState(
    animationUrl: String,
    message: String,
    modifier: Modifier = Modifier
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.Url(animationUrl))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(200.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

object LottieAnimations {
    const val EMPTY_CLASSES = "https://lottie.host/4d3c3c3e-8f3a-4b3e-9c3a-3c3e8f3a4b3e/3c3e8f3a4b3e.json"
    const val EMPTY_MEETINGS = "https://lottie.host/5e4d4d4f-9g4b-5c4f-0d4b-4d4f9g4b5c4f/4d4f9g4b5c4f.json"
    const val ONBOARDING_WELCOME = "https://lottie.host/6f5e5e5g-0h5c-6d5g-1e5c-5e5g0h5c6d5g/5e5g0h5c6d5g.json"
}

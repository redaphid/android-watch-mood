package com.loqwai.hypnomood.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.loqwai.hypnomood.presentation.theme.HypnoMoodTheme
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            WearApp()
        }
    }
}

var fontSize = 64.sp
fun mapRange(value: Float, fromLow: Float, fromHigh: Float, toLow: Float, toHigh: Float): Float {
    return toLow + (value - fromLow) / (fromHigh - fromLow) * (toHigh - toLow)
}

fun mapOffSetToMood(offset: Float): Int {
    return mapRange(offset, -2 * fontSize.value, 2 * fontSize.value, -5f, 5f).toInt() + 5
}

@Composable
fun WearApp() {
    HypnoMoodTheme {
        var moodScore by remember { mutableStateOf(5) }
        var dragOffset by remember { mutableStateOf(0f) }
        val animatableOffset = remember { Animatable(0f) }
        val scope = rememberCoroutineScope()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            // Snap to the nearest mood score

                            val targetScore = (moodScore + (animatableOffset.value * fontSize.value).toInt()).coerceIn(0, 10)
                            scope.launch {
                                animatableOffset.animateTo(
                                    targetScore.toFloat() ,
                                    animationSpec = tween(durationMillis = 500)
                                )
                            }

                        },
                        onDrag = { change, dragAmount ->
                            dragOffset -= dragAmount.x;
                            dragOffset = min(dragOffset,2 * fontSize.value)
                            dragOffset = max(dragOffset,-2 * fontSize.value)
                            moodScore = mapOffSetToMood(dragOffset)
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .wrapContentWidth()
                    .offset(x = animatableOffset.value.dp)
            ) {
                for (i in 0..10) {
                    val distanceFromCenter = abs(animatableOffset.value / 200 + i - moodScore)
                    val scaleFactor = max(1f - 0.1f * distanceFromCenter, 0.5f)
                    Text(
                        text = i.toString(),
                        fontSize = (fontSize * scaleFactor),  // Dynamically scale the font size
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .graphicsLayer(scaleX = scaleFactor, scaleY = scaleFactor)
                    )
                }
            }
            Row(
                modifier = Modifier.wrapContentWidth()
                    .offset(y = 30.dp)
            ){
                Text(
                    text = dragOffset.toString(),
                    fontSize = 24.sp,  // Dynamically scale the font size
                    textAlign = TextAlign.Center
                )
            }
            Row(
                modifier = Modifier.wrapContentWidth()
                    .offset(y = 50.dp)
            ){
                Text(
                    text = moodScore.toString(),
                    fontSize = 24.sp,  // Dynamically scale the font size
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp()
}

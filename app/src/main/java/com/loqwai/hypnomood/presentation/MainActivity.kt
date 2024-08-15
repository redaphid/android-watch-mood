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
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.loqwai.hypnomood.presentation.theme.HypnoMoodTheme
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

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
val fontSpacing = 20.dp
fun mapRange(value: Float, fromLow: Float, fromHigh: Float, toLow: Float, toHigh: Float): Float {
    return toLow + (value - fromLow) / (fromHigh - fromLow) * (toHigh - toLow)
}

fun mapOffSetToMood(offset: Float): Int {
    return mapRange(offset, -4 * fontSize.value, 4 * fontSize.value, -5f, 5f).roundToInt() + 5
}

/*
    This function should map the drag offset (which is measured from -2 * fontSize to 2 * fontSize)
    to a font size that is between 20% and 100% of the original font size.
    The function should not map linearly, but should instead scale the font size based on the distance from the center in an exponential way.
 */
fun mapOffsetToFontSize(screenWidth: Dp, mood:Int, score: Int = 5): TextUnit {
    if(mood == score) return fontSize;
    var diff = abs(mood - score);
    // the further away from the center, the smaller the font size. exponential scaling
    return fontSize * (0.2f + 0.8f * (1 - 0.5f.pow(diff.toFloat() / 5)))
}

@Composable
fun WearApp() {
    HypnoMoodTheme {
        var moodScore by remember { mutableStateOf(5) }

        val animatableOffset = remember { Animatable(0f) }
        val screenWidth = LocalConfiguration.current.screenWidthDp.dp
        val scope = rememberCoroutineScope()
        var targetScore by remember { mutableStateOf(0f) }
        val moodLineSize = 10 * (fontSize.value + fontSpacing.value)
        var dragOffset by remember { mutableStateOf(moodLineSize/2) }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            //make dragOffset snap to the nearest score. remember the numbers are 20dp apart
                            moodScore = min(moodScore,9)
                            moodScore = max(moodScore,0)
                            targetScore = (moodLineSize * (moodScore.toFloat() /10f)) - (moodLineSize/2)
                            scope.launch {
                                animatableOffset.animateTo(
                                    targetScore,
                                    animationSpec = tween(durationMillis = 500)
                                )
                            }

                        },
                        onDrag = { change, dragAmount ->
                            dragOffset -= dragAmount.x;


                            moodScore = ((dragOffset-(fontSize.value/2))/moodLineSize * 10).roundToInt()
                            scope.launch {
                            animatableOffset.animateTo(dragOffset - (moodLineSize/2), initialVelocity = animatableOffset.velocity)
                                }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .wrapContentWidth(unbounded = true)
                    .offset(x = -animatableOffset.value.dp, y=0.dp)
            ) {
                for (i in 0..9) {
                    val distanceFromCenter = abs(animatableOffset.value / screenWidth.value + i - moodScore)

                    Text(
                        text = i.toString(),
                        fontSize = fontSize,  // Dynamically scale the font size
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(horizontal = fontSpacing)
                    )
                }
            }
            Row(
                modifier = Modifier.wrapContentWidth()
                    .offset(y = 30.dp)
            ){
                Text(
                    text = dragOffset.toString(),
                    fontSize = 18.sp,  // Dynamically scale the font size
                    textAlign = TextAlign.Center
                )
            }
            Row(
                modifier = Modifier.wrapContentWidth()
                    .offset(y = 40.dp)
            ){
                Text(
                    text = moodScore.toString(),
                    fontSize = 18.sp,  // Dynamically scale the font size
                    textAlign = TextAlign.Center
                )
            }
            Row(
                modifier = Modifier.wrapContentWidth()
                    .offset(y = 60.dp, x = 0.dp)
            ){
                Text(
                    text = targetScore.toString(),
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

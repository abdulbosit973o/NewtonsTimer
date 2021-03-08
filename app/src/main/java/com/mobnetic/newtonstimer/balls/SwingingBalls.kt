/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mobnetic.newtonstimer.balls

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobnetic.newtonstimer.configuration.ConfigurationHint
import com.mobnetic.newtonstimer.configuration.configurationDragModifier
import com.mobnetic.newtonstimer.timer.TimerState
import com.mobnetic.newtonstimer.timer.TimerViewModel
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.isActive

@Composable
fun SwingingBalls(modifier: Modifier = Modifier) {
    Row(modifier) {
        val viewModel: TimerViewModel = viewModel()
        val angles = animateAngles(viewModel)
        val ballSize = remember { mutableStateOf(BallSize()) }

        val state = viewModel.state
        val isConfigured = state is TimerState.Configured
        val firstBallAngle = if (isConfigured) angles.first() else state.startAngle
        val draggable = if (isConfigured) Modifier else Modifier.configurationDragModifier(ballSize, viewModel::configureAngle, viewModel::play)
        val otherBallsAlpha by animateFloatAsState(targetValue = if (isConfigured) 1f else 0.15f)

        if (!isConfigured) {
            ConfigurationHint(firstBallAngle, ballSize.value)
        }
        BallOnStringWithShadow(
            angle = firstBallAngle,
            modifier = draggable,
            onSizeChanged = { ballSize.value = it }
        )
        (1..angles.lastIndex).forEach { index ->
            BallOnStringWithShadow(
                angle = angles[index],
                modifier = Modifier.alpha(otherBallsAlpha)
            )
        }
    }
}


@Composable
private fun animateAngles(viewModel: TimerViewModel): FloatArray {
    val state = viewModel.state as? TimerState.Configured.Running ?: return viewModel.getAnimationAngles()

    var angles by remember { mutableStateOf(viewModel.getAnimationAngles()) }
    LaunchedEffect(state) {
        do {
            angles = viewModel.getAnimationAngles()
            awaitFrame()
        } while (isActive)
    }
    return angles
}

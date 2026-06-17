package com.weatherwise.util

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * Extension function untuk menambahkan Haptic Feedback pada klik.
 * Gunakan ini pada Modifier.clickable atau Modifier.combinedClickable.
 */
@Composable
fun Modifier.hapticClick(
    hapticFeedbackType: HapticFeedbackType = HapticFeedbackType.TextHandleMove,
    onClick: () -> Unit
): Modifier = composed {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val indication = LocalIndication.current

    this.then(
        this.clickable(
            interactionSource = interactionSource,
            indication = indication,
            onClick = {
                haptic.performHapticFeedback(hapticFeedbackType)
                onClick()
            }
        )
    )
}

/**
 * Fungsi helper untuk memicu haptic feedback secara manual (misalnya saat refresh atau error).
 */
@Composable
fun triggerHapticFeedback(type: HapticFeedbackType = HapticFeedbackType.LongPress) {
    val haptic = LocalHapticFeedback.current
    haptic.performHapticFeedback(type)
}

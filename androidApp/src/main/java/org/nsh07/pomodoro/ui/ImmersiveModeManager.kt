/*
 * Copyright (c) 2025-2026 Nishant Mishra
 *
 * This file is part of Tomato - a minimalist pomodoro timer for Android.
 *
 * Tomato is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Tomato is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Tomato.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.ui

import android.content.res.Configuration
import android.view.View
import android.view.Window
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ImmersiveModeManager(window: Window, decorView: View) {

    private val controller = WindowInsetsControllerCompat(window, decorView).apply {
        systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    private val _isImmersive = MutableStateFlow(false)
    val isImmersive: StateFlow<Boolean> = _isImmersive.asStateFlow()

    fun applyImmersiveMode(config: Configuration, isAodActive: Boolean, isInMultiWindow: Boolean) {
        val isLandscape = config.orientation == Configuration.ORIENTATION_LANDSCAPE
        val shouldImmerse = isLandscape && !isAodActive && !isInMultiWindow

        // Atomic state transition - avoid TOCTOU race
        if (shouldImmerse != _isImmersive.value) {
            if (shouldImmerse) {
                controller.hide(WindowInsetsCompat.Type.systemBars())
            } else {
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
            _isImmersive.value = shouldImmerse
        }
    }

    fun onConfigurationChanged(newConfig: Configuration, isAodActive: Boolean, isInMultiWindow: Boolean) {
        applyImmersiveMode(newConfig, isAodActive, isInMultiWindow)
    }

    fun resetOnDestroy() {
        if (_isImmersive.value) {
            controller.show(WindowInsetsCompat.Type.systemBars())
            _isImmersive.value = false
        }
    }
}

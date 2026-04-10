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

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.zIndex
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.nsh07.pomodoro.utils.onTopLevelNavigate

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LandscapeNavMenu(
    backStack: NavBackStack<NavKey>,
    mainScreens: List<NavItem>,
    modifier: Modifier = Modifier
) {
    var drawerExpanded by rememberSaveable { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        // Hamburger FAB trigger — always visible at bottom-left
        androidx.compose.material3.FloatingActionButton(
            onClick = { drawerExpanded = true },
            containerColor = colorScheme.primaryContainer,
            contentColor = colorScheme.onPrimaryContainer,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .size(48.dp)
                .zIndex(2f)
                .semantics { contentDescription = "Open navigation" }
        ) {
            HamburgerIcon(
                color = colorScheme.onPrimaryContainer,
                modifier = Modifier.size(24.dp)
            )
        }

        // Expandable overlay menu
        if (drawerExpanded) {
            // Dismiss backdrop
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(3f)
                    .clickable { drawerExpanded = false }
                    .background(colorScheme.scrim.copy(alpha = 0.32f))
            )

            BackHandler(enabled = drawerExpanded) {
                drawerExpanded = false
            }

            // Menu card
            AnimatedVisibility(
                visible = drawerExpanded,
                enter = fadeIn() + slideInHorizontally { -it / 4 },
                exit = fadeOut() + slideOutHorizontally { -it / 4 },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .zIndex(4f)
            ) {
                RoundedCard {
                    Column {
                        mainScreens.fastForEach { item ->
                            val selected by remember { derivedStateOf { backStack.lastOrNull() == item.route } }
                            val label = stringResource(item.label)
                            ToggleButton(
                                checked = selected,
                                onCheckedChange = if (!selected) {
                                    {
                                        backStack.onTopLevelNavigate(item.route)
                                        drawerExpanded = false
                                    }
                                } else {
                                    {
                                        item.onNavigateHome()
                                        drawerExpanded = false
                                    }
                                },
                                colors = ToggleButtonDefaults.toggleButtonColors(
                                    containerColor = colorScheme.primaryContainer,
                                    contentColor = colorScheme.onPrimaryContainer,
                                    checkedContainerColor = colorScheme.primary,
                                    checkedContentColor = colorScheme.onPrimary
                                ),
                                shapes = ToggleButtonDefaults.shapes(CircleShape, CircleShape, CircleShape),
                                modifier = Modifier
                                    .height(56.dp)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .semantics { contentDescription = label }
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Crossfade(selected) {
                                        if (it) Icon(
                                            painterResource(item.selectedIcon),
                                            stringResource(item.label)
                                        )
                                        else Icon(
                                            painterResource(item.unselectedIcon),
                                            stringResource(item.label)
                                        )
                                    }
                                    AnimatedVisibility(
                                        visible = selected,
                                        enter = expandHorizontally(motionScheme.defaultSpatialSpec()),
                                        exit = shrinkHorizontally(motionScheme.defaultSpatialSpec())
                                    ) {
                                        Text(
                                            text = stringResource(item.label),
                                            fontSize = 16.sp,
                                            lineHeight = 24.sp,
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = TextOverflow.Clip,
                                            modifier = Modifier.padding(start = ButtonDefaults.IconSpacing)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Simple hamburger icon drawn with Canvas (three horizontal lines).
 */
@Composable
private fun HamburgerIcon(color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val lineThickness = size.height / 8f
        val gap = (size.height - lineThickness * 3) / 2f
        val cornerRadius = CornerRadius(lineThickness / 2f)

        listOf(
            0f,
            lineThickness + gap,
            (lineThickness + gap) * 2
        ).forEach { y ->
            val rect = androidx.compose.ui.geometry.Rect(0f, y, width, y + lineThickness)
            val path = Path().apply { addRoundRect(RoundRect(rect, cornerRadius)) }
            drawPath(path, color = color)
        }
    }
}

/**
 * Rounded card background for the landscape nav menu.
 */
@Composable
private fun RoundedCard(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .background(colorScheme.surfaceContainer, shape = RoundedCornerShape(28.dp))
            .padding(vertical = 8.dp)
    ) {
        content()
    }
}

package com.calculator.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.calculator.app.data.preferences.LocalNumberingSystem
import com.calculator.app.ui.CalculatorAction
import com.calculator.app.ui.CalculatorViewModel
import com.calculator.app.ui.navigation.AppScreen
import com.calculator.app.ui.screens.CalculatorScreen
import com.calculator.app.ui.screens.tools.AgeScreen
import com.calculator.app.ui.screens.tools.BmiScreen
import com.calculator.app.ui.screens.tools.DateCalculatorScreen
import com.calculator.app.ui.screens.tools.DiscountCalculatorScreen
import com.calculator.app.ui.screens.tools.GstCalculatorScreen
import com.calculator.app.ui.screens.tools.InvestmentSipScreen
import com.calculator.app.ui.screens.tools.LoanEmiScreen
import com.calculator.app.ui.screens.tools.NumberSystemScreen
import com.calculator.app.ui.screens.tools.TimeCalculatorScreen
import com.calculator.app.ui.screens.tools.ToolsHubScreen
import com.calculator.app.ui.screens.tools.UnitConverterScreen
import com.calculator.app.ui.theme.CalculatorTheme

class MainActivity : ComponentActivity() {

    private val viewModel: CalculatorViewModel by viewModels {
        CalculatorViewModel.Factory((application as CalculatorApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val state by viewModel.state.collectAsState()
            var screenStack by remember { mutableStateOf(listOf<AppScreen>(AppScreen.Calculator)) }
            val currentScreen = screenStack.lastOrNull() ?: AppScreen.Calculator
            var isNavigatingBack by remember { mutableStateOf(false) }

            BackHandler(enabled = screenStack.size > 1) {
                isNavigatingBack = true
                screenStack = screenStack.dropLast(1)
            }

            fun navigateTo(screen: AppScreen) {
                isNavigatingBack = false
                screenStack = screenStack + screen
            }

            fun navigateBack() {
                if (screenStack.size > 1) {
                    isNavigatingBack = true
                    screenStack = screenStack.dropLast(1)
                }
            }

            CalculatorTheme(themeMode = state.themeMode) {
                CompositionLocalProvider(LocalNumberingSystem provides state.numberingSystem) {
                    AnimatedContent(
                        targetState = currentScreen,
                    transitionSpec = {
                        val enterEasing = FastOutSlowInEasing
                        val exitEasing = FastOutLinearInEasing

                        if (!isNavigatingBack) {
                            // Forward navigation (e.g. into Tools Hub, or Tool screen):
                            // Incoming screen slides in from right with fade & subtle scale
                            (slideInHorizontally(
                                initialOffsetX = { fullWidth -> (fullWidth * 0.20f).toInt() },
                                animationSpec = tween(300, easing = enterEasing)
                            ) + fadeIn(
                                animationSpec = tween(260, easing = LinearOutSlowInEasing)
                            ) + scaleIn(
                                initialScale = 0.96f,
                                animationSpec = tween(300, easing = enterEasing)
                            )).togetherWith(
                                slideOutHorizontally(
                                    targetOffsetX = { fullWidth -> -(fullWidth * 0.10f).toInt() },
                                    animationSpec = tween(240, easing = exitEasing)
                                ) + fadeOut(
                                    animationSpec = tween(180, easing = exitEasing)
                                ) + scaleOut(
                                    targetScale = 0.98f,
                                    animationSpec = tween(240, easing = exitEasing)
                                )
                            )
                        } else {
                            // Backward navigation (pop to parent / Calculator):
                            // Current screen slides away to right, previous screen slides in from left
                            (slideInHorizontally(
                                initialOffsetX = { fullWidth -> -(fullWidth * 0.10f).toInt() },
                                animationSpec = tween(260, easing = enterEasing)
                            ) + fadeIn(
                                animationSpec = tween(240, easing = LinearOutSlowInEasing)
                            ) + scaleIn(
                                initialScale = 0.98f,
                                animationSpec = tween(260, easing = enterEasing)
                            )).togetherWith(
                                slideOutHorizontally(
                                    targetOffsetX = { fullWidth -> (fullWidth * 0.20f).toInt() },
                                    animationSpec = tween(240, easing = enterEasing)
                                ) + fadeOut(
                                    animationSpec = tween(180, easing = exitEasing)
                                ) + scaleOut(
                                    targetScale = 0.96f,
                                    animationSpec = tween(240, easing = enterEasing)
                                )
                            )
                        }
                    },
                    label = "ScreenTransition"
                ) { screen ->
                    when (screen) {
                        is AppScreen.Calculator -> {
                            CalculatorScreen(
                                state = state,
                                onAction = viewModel::onAction,
                                onOpenToolsHub = { navigateTo(AppScreen.ToolsHub) }
                            )
                        }
                        is AppScreen.ToolsHub -> {
                            ToolsHubScreen(
                                onNavigate = { target -> navigateTo(target) },
                                onBack = { navigateBack() },
                                numberingSystem = state.numberingSystem,
                                onSelectNumberingSystem = { sys -> viewModel.onAction(CalculatorAction.SelectNumberingSystem(sys)) }
                            )
                        }
                        is AppScreen.UnitConverter -> {
                            UnitConverterScreen(
                                initialCategory = screen.category,
                                onBack = { navigateBack() }
                            )
                        }
                        is AppScreen.NumberSystem -> {
                            NumberSystemScreen(
                                onBack = { navigateBack() }
                            )
                        }
                        is AppScreen.LoanEmi -> {
                            LoanEmiScreen(
                                onBack = { navigateBack() }
                            )
                        }
                        is AppScreen.InvestmentSip -> {
                            InvestmentSipScreen(
                                onBack = { navigateBack() }
                            )
                        }
                        is AppScreen.Gst -> {
                            GstCalculatorScreen(
                                onBack = { navigateBack() }
                            )
                        }
                        is AppScreen.Discount -> {
                            DiscountCalculatorScreen(
                                onBack = { navigateBack() }
                            )
                        }
                        is AppScreen.Bmi -> {
                            BmiScreen(
                                onBack = { navigateBack() }
                            )
                        }
                        is AppScreen.Age -> {
                            AgeScreen(
                                onBack = { navigateBack() }
                            )
                        }
                        is AppScreen.DateCalculator -> {
                            DateCalculatorScreen(
                                onBack = { navigateBack() }
                            )
                        }
                        is AppScreen.TimeCalculator -> {
                            TimeCalculatorScreen(
                                onBack = { navigateBack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
}

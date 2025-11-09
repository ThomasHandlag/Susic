package com.thugbn.susic.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.MutableWindowInsets
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.onConsumedWindowInsetsChanged
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SuScaffold(
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = contentColorFor(containerColor),
    contentWindowInsets: WindowInsets = WindowInsets.systemBars,
    content: @Composable (PaddingValues) -> Unit,
) {
    val safeInsets = remember(contentWindowInsets) { MutableWindowInsets(contentWindowInsets) }
    Surface(
        modifier =
            modifier.onConsumedWindowInsetsChanged { consumedWindowInsets ->
                // Exclude currently consumed window insets from user provided contentWindowInsets
                safeInsets.insets = contentWindowInsets.exclude(consumedWindowInsets)
            },
        color = containerColor,
        contentColor = contentColor,
    ) {
        SuScaffoldLayout(
            fabPosition = floatingActionButtonPosition,
            bottomBar = bottomBar,
            content = content,
            snackbar = snackbarHost,
            contentWindowInsets = safeInsets,
            fab = floatingActionButton,
        )
    }
}


@Composable
private fun SuScaffoldLayout(
    fabPosition: FabPosition,
    content: @Composable (PaddingValues) -> Unit,
    snackbar: @Composable () -> Unit,
    fab: @Composable () -> Unit,
    contentWindowInsets: WindowInsets,
    bottomBar: @Composable () -> Unit,
) {

    val contentPadding = remember {
        object : PaddingValues {
            var paddingHolder by mutableStateOf(PaddingValues(0.dp))

            override fun calculateLeftPadding(layoutDirection: LayoutDirection): Dp =
                paddingHolder.calculateLeftPadding(layoutDirection)

            override fun calculateTopPadding(): Dp = paddingHolder.calculateTopPadding()

            override fun calculateRightPadding(layoutDirection: LayoutDirection): Dp =
                paddingHolder.calculateRightPadding(layoutDirection)

            override fun calculateBottomPadding(): Dp = paddingHolder.calculateBottomPadding()
        }
    }

    val snackbarContent: @Composable () -> Unit = remember(snackbar) { { Box { snackbar() } } }
    val fabContent: @Composable () -> Unit = remember(fab) { { Box { fab() } } }
    val bodyContent: @Composable () -> Unit =
        remember(content, contentPadding) { { Box { content(contentPadding) } } }
    val bottomBarContent: @Composable () -> Unit = remember(bottomBar) { { Box { bottomBar() } } }
    SubcomposeLayout { constraints ->
        val layoutWidth = constraints.maxWidth
        val layoutHeight = constraints.maxHeight

        val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)

        // respect only bottom and horizontal for snackbar and fab
        val leftInset = contentWindowInsets.getLeft(this@SubcomposeLayout, layoutDirection)
        val rightInset = contentWindowInsets.getRight(this@SubcomposeLayout, layoutDirection)
        val bottomInset = contentWindowInsets.getBottom(this@SubcomposeLayout)


        val snackbarPlaceable =
            subcompose(SuScaffoldLayoutContent.Snackbar, snackbarContent)
                .first()
                .measure(looseConstraints.offset(-leftInset - rightInset, -bottomInset))

        val fabPlaceable =
            subcompose(SuScaffoldLayoutContent.Fab, fabContent)
                .first()
                .measure(looseConstraints.offset(-leftInset - rightInset, -bottomInset))

        val isFabEmpty = fabPlaceable.width == 0 && fabPlaceable.height == 0
        val fabPlacement =
            if (!isFabEmpty) {
                val fabWidth = fabPlaceable.width
                val fabHeight = fabPlaceable.height
                // FAB distance from the left of the layout, taking into account LTR / RTL
                val fabLeftOffset =
                    when (fabPosition) {
                        FabPosition.Start -> {
                            if (layoutDirection == LayoutDirection.Ltr) {
                                FabSpacing.roundToPx() + leftInset
                            } else {
                                layoutWidth - FabSpacing.roundToPx() - fabWidth - rightInset
                            }
                        }

                        FabPosition.End,
                        FabPosition.EndOverlay -> {
                            if (layoutDirection == LayoutDirection.Ltr) {
                                layoutWidth - FabSpacing.roundToPx() - fabWidth - rightInset
                            } else {
                                FabSpacing.roundToPx() + leftInset
                            }
                        }

                        else -> (layoutWidth - fabWidth + leftInset - rightInset) / 2
                    }

                FabPlacement(left = fabLeftOffset, width = fabWidth, height = fabHeight)
            } else {
                null
            }

        val bottomBarPlaceable =
            subcompose(SuScaffoldLayoutContent.BottomBar, bottomBarContent)
                .first()
                .measure(looseConstraints)

        val isBottomBarEmpty = bottomBarPlaceable.width == 0 && bottomBarPlaceable.height == 0

        val fabOffsetFromBottom =
            fabPlacement?.let {
                if (isBottomBarEmpty || fabPosition == FabPosition.EndOverlay) {
                    it.height +
                            FabSpacing.roundToPx() +
                            contentWindowInsets.getBottom(this@SubcomposeLayout)
                } else {
                    // Total height is the bottom bar height + the FAB height + the padding
                    // between the FAB and bottom bar
                    bottomBarPlaceable.height + it.height + FabSpacing.roundToPx()
                }
            }

        val snackbarHeight = snackbarPlaceable.height
        val snackbarOffsetFromBottom =
            if (snackbarHeight != 0) {
                snackbarHeight +
                        (fabOffsetFromBottom
                            ?: bottomBarPlaceable.height.takeIf { !isBottomBarEmpty }
                            ?: contentWindowInsets.getBottom(this@SubcomposeLayout))
            } else {
                0
            }

        // Update the backing state for the content padding before subcomposing the body
        val insets = contentWindowInsets.asPaddingValues(this)
        contentPadding.paddingHolder =
            PaddingValues(
                bottom =
                    if (isBottomBarEmpty) {
                        insets.calculateBottomPadding()
                    } else {
                        bottomBarPlaceable.height.toDp()
                    },
                start = insets.calculateStartPadding(layoutDirection),
                end = insets.calculateEndPadding(layoutDirection),
            )

        val bodyContentPlaceable =
            subcompose(SuScaffoldLayoutContent.MainContent, bodyContent)
                .first()
                .measure(looseConstraints)

        layout(layoutWidth, layoutHeight) {
            // Placing to control drawing order to match default elevation of each placeable
            bodyContentPlaceable.place(0, 0)
            snackbarPlaceable.place(
                (layoutWidth - snackbarPlaceable.width +
                        contentWindowInsets.getLeft(this@SubcomposeLayout, layoutDirection) -
                        contentWindowInsets.getRight(this@SubcomposeLayout, layoutDirection)) / 2,
                layoutHeight - snackbarOffsetFromBottom,
            )
            // The bottom bar is always at the bottom of the layout
            bottomBarPlaceable.place(0, layoutHeight - (bottomBarPlaceable.height))
            // Explicitly not using placeRelative here as `leftOffset` already accounts for RTL
            fabPlacement?.let { placement ->
                fabPlaceable.place(placement.left, layoutHeight - fabOffsetFromBottom!!)
            }
        }
    }
}
@Immutable internal class FabPlacement(val left: Int, val width: Int, val height: Int)

// FAB spacing above the bottom bar / bottom of the Scaffold
private val FabSpacing = 16.dp

private enum class SuScaffoldLayoutContent {
    MainContent,
    Snackbar,
    Fab,
    BottomBar,
}
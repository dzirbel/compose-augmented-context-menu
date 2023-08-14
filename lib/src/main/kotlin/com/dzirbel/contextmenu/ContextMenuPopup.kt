package com.dzirbel.contextmenu

import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.InputModeManager
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.type
import androidx.compose.ui.node.Ref
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import java.awt.event.KeyEvent

@ExperimentalComposeUiApi
@Composable
internal fun ContextMenuPopup(
    params: ContextMenuParams,
    popupPositionProvider: PopupPositionProvider,
    onDismissRequest: () -> Unit,
    items: () -> List<ContextMenuItem>,
) {
    val focusManagerRef = remember { Ref<FocusManager>() }
    val inputModeManagerRef = remember { Ref<InputModeManager>() }

    Popup(
        focusable = true,
        onDismissRequest = onDismissRequest,
        popupPositionProvider = popupPositionProvider,
        onKeyEvent = { keyEvent ->
            if (keyEvent.type == KeyEventType.KeyDown) {
                when (keyEvent.key.nativeKeyCode) {
                    KeyEvent.VK_ESCAPE -> {
                        onDismissRequest()
                        true
                    }

                    KeyEvent.VK_DOWN -> {
                        inputModeManagerRef.value?.requestInputMode(InputMode.Keyboard)
                        focusManagerRef.value?.moveFocus(FocusDirection.Next)
                        true
                    }

                    KeyEvent.VK_UP -> {
                        inputModeManagerRef.value?.requestInputMode(InputMode.Keyboard)
                        focusManagerRef.value?.moveFocus(FocusDirection.Previous)
                        true
                    }

                    else -> {
                        false
                    }
                }
            } else {
                false
            }
        },
    ) {
        focusManagerRef.value = LocalFocusManager.current
        inputModeManagerRef.value = LocalInputModeManager.current

        Surface(
            elevation = params.measurements.elevation,
            shape = params.measurements.popupShape,
            color = params.colors.surface,
        ) {
            val scrollState = rememberScrollState()
            OptionalVerticalScroll(
                scrollState = scrollState,
                includeScrollbarWhenUsed = params.showScrollbarOnOverFlow,
            ) {
                Column(
                    modifier = Modifier
                        .width(IntrinsicSize.Max)
                        .padding(
                            top = params.measurements.menuTopPadding,
                            bottom = params.measurements.menuBottomPadding,
                        )
                        .verticalScroll(scrollState),
                ) {
                    val resolvedItems = remember { items() }

                    val hoverInteractionSources = remember {
                        List(resolvedItems.size) { MutableInteractionSource() }
                    }
                    val hoveredItem = remember { hoverInteractionSources.hoveredIndex() }
                        .collectAsState(initial = -1)

                    resolvedItems.forEachIndexed { index, item ->
                        ContextMenuItemContent(
                            item = item,
                            params = params,
                            onDismissRequest = onDismissRequest,
                            menuOpen = item is ContextMenuGroup && hoveredItem.value == index,
                            modifier = Modifier.hoverable(hoverInteractionSources[index]),
                        )
                    }
                }
            }
        }
    }
}

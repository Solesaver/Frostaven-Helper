package com.example.frosthavenhelper

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.core.text.isDigitsOnly
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DropdownRow(
    label: String,
    entries: Collection<NamedObject>,
    start: Int,
    onSelection: (Int) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "${label}: ",
            style = MaterialTheme.typography.h6,
            modifier = Modifier
                .padding(vertical = Dp(10f))
                .align(alignment = Alignment.CenterVertically))
        Dropdown(entries, start, onSelection)
    }
}

@Composable
fun Dropdown(entries: Collection<NamedObject>, start: Int, onSelection: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableStateOf(start) }
    Box(modifier = Modifier) {
        Text(text = "  ${entries.elementAt(selectedIndex).name}  ",
            modifier = Modifier
                .clickable(onClick = { expanded = true })
                .background(Color.Gray)
                .padding(vertical = Dp(10f))
                .align(alignment = Alignment.CenterStart),
            style = MaterialTheme.typography.h6)
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(
                    Color.Gray
                )
        ) {
            entries.forEachIndexed { index, s ->
                DropdownMenuItem(onClick = {
                    selectedIndex = index
                    onSelection(selectedIndex)
                    expanded = false
                }) {
                    Text(text = s.name, style = MaterialTheme.typography.h6)
                }
            }
        }
    }
}

@Composable
fun NumberEntryRow(label: String, initialize: Int, digits: Int, onEntry: (Int) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth()) {
        var text by remember { mutableStateOf(TextFieldValue("$initialize")) }
        val scope = rememberCoroutineScope()
        Text(
            "${label}: ",
            style = MaterialTheme.typography.h6,
            modifier = Modifier
                .padding(vertical = Dp(10f))
                .align(alignment = Alignment.CenterVertically)
        )
        TextField(
            value = text,
            onValueChange = { newText ->
                var builder = ""
                var count = 0
                run loop@{
                    newText.text.forEach {
                        if (++count > digits)
                            return@loop
                        if (it.isDigit())
                            builder += it
                    }
                }
                text = TextFieldValue(text = builder, selection = TextRange(builder.length))
                if (text.text.isEmpty() || !text.text.isDigitsOnly())
                    onEntry(0)
                else
                    onEntry(text.text.toInt())
            },
            textStyle = MaterialTheme.typography.h6,
            modifier = Modifier
                .onFocusChanged { focusState ->
                    scope.launch {
                        delay(0)
                        if (focusState.isFocused) {
                            val len = text.text.length
                            text = if (len > 0) {
                                text.copy(selection = TextRange(0, len))
                            } else {
                                text.copy(selection = TextRange(0))
                            }
                        }
                    }
                },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            )
        )
    }
}

@Composable
fun NavigateButton (target: String, context: Context) {
    Button (onClick = {
        context.nav.navigate(target)
    }) {
        Text(text = target, style = MaterialTheme.typography.h6)
    }
}

@Composable
fun ToggleButton (add: Boolean, toggle: MutableState<Boolean>, onClick: () -> Unit) {
    Button(onClick = {
        onClick()
        toggle.value = !toggle.value
    }) {
        Text(text = if(add) "O" else "X", style = MaterialTheme.typography.h6)
    }

}
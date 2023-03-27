package com.example.frosthavenhelper

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp


@Composable
fun SetSession (context: Context) {
    Column {
        NumberEntryRow("Encounter Level", context.session.level, 1) { level ->
            context.session.level = level
        }
        val mutable = remember { mutableStateOf(false) }
        val toggle by mutable
        Row {
            var selection by remember { mutableStateOf(0) }
            Text(
                "Add: ",
                style = MaterialTheme.typography.h6,
                modifier = Modifier
                    .padding(vertical = Dp(10f))
                    .align(alignment = Alignment.CenterVertically)
            )
            Dropdown(context.mon_database.values, selection) { index ->
                selection = index
            }
            ToggleButton(true, mutable) {
                val id = context.mon_database.keys.elementAt(selection)
                if(!context.session.monsters.contains(id))
                    context.session.monsters.add(id)
            }
        }
        key(toggle) {
            context.session.monsters.forEach {id: MonsterId ->
                Row {
                    Text(
                        text = context.mon_database[id]!!.name,
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    ToggleButton(false, mutable) {
                        context.session.monsters.remove(id)
                    }
                }
            }
        }
        NavigateButton("Enter Attacks", context)
    }
}
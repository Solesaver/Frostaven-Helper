package com.example.frosthavenhelper

import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.runtime.*

@Composable
fun CheckData (context: Context) {
    Column {
        var selectedIndex by remember { mutableStateOf(0) }
        Dropdown(context.monsters.values, selectedIndex){ index: Int ->
            selectedIndex = index
        }
        val monster = context.monsters.values.elementAt(selectedIndex)
        val base = context.mon_database[translateMonsterId(monster.name)]!!
        val attack = context.att_database[monster.mon_class]!!.deck.elementAt(monster.selection)
        val level = context.session.level
        Text("Base Attack: ${base.attack[level]}", style = MaterialTheme.typography.h6)
        Text("Attack Modifier: ${attack.attack}", style = MaterialTheme.typography.h6)
        Text("Base Move: ${base.move[level]}", style = MaterialTheme.typography.h6)
        Text("Move Modifier: ${attack.move}", style = MaterialTheme.typography.h6)

        Button (onClick = {
            context.nav.navigate("Enter Attacks")
        }) {
            Text(text = "Enter Attacks", style = MaterialTheme.typography.h6)
        }
    }
}
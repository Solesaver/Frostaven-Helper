package com.example.frosthavenhelper

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier


@Composable
fun EnterAttacks(context: Context) {
    val activeClasses: MutableSet<MonsterClass> = mutableSetOf()
    Column(modifier = Modifier.fillMaxHeight()) {
        context.monsters.forEach { monster ->
            if (!monster.value.active || activeClasses.contains(monster.value.mon_class))
                return@forEach
            activeClasses += monster.value.mon_class
            val entry = context.att_database.getOrElse(key = monster.value.mon_class) {
                return@forEach
            }
            updateMonsters(context, entry, monster.value.selection)
            DropdownRow(entry.name, entry.deck, monster.value.selection) { index: Int ->
                updateMonsters(context, entry, index)
            }
        }
        context.players.forEach {
            if (!it.value.active)
                return@forEach
            NumberEntryRow(it.value.name, it.value.init, 2) {init: Int ->
                it.value.init = init
            }
        }
        NavigateButton("Round Info", context)
        NavigateButton("Check Data", context)
        NavigateButton("Set Session", context)
    }
}
package com.example.frosthavenhelper

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp


@Composable
fun RoundInfo (context: Context) {
    val order: MutableList<CombatantId> = mutableListOf()
    val gone: MutableSet<CombatantId> = mutableSetOf()
    // Split combatants between who is active vs not
    // While you're at it sort by initiative
    context.players.forEach {
        val newId = CombatantId(player = it.key)
        if (context.players[it.key]!!.active) {
            val sort = getSort(newId, context)
            order.forEachIndexed { index: Int, id: CombatantId ->
                if (sort < getSort(id, context)) {
                    order.add(index, newId)
                    return@forEach
                }
            }
            order.add(newId)
        }
        else
            gone.add(newId)
    }
    context.monsters.forEach {
        val newId = CombatantId(monster = it.key)
        if (context.monsters[it.key]!!.active) {
            val sort = getSort(newId, context)
            order.forEachIndexed { index: Int, id: CombatantId ->
                if (sort < getSort(id, context)) {
                    order.add(index, newId)
                    return@forEach
                }
            }
            order.add(newId)
        }
        else
            gone.add(newId)
    }
    // This is just a weird hack to get the list to redraw when I add/remove combatants
    val mutable = remember { mutableStateOf(false) }
    val toggle by mutable
    key(toggle) {
        Column(modifier = Modifier.fillMaxWidth()) {
            order.forEach {
                Row {
                    if (it.monster != null) {
                        val mon = context.monsters[it.monster]!!
                        Text(
                            "${mon.init} - ${mon.name}: Mov(${mon.move}) Att(${mon.attack})",
                            style = MaterialTheme.typography.h6
                        )
                        ToggleButton(false, mutable) {
                            mon.active = false
                        }
                    } else {
                        val player = context.players[it.player]!!
                        Text(
                            "${player.init} - ${player.name}",
                            style = MaterialTheme.typography.h6
                        )
                        ToggleButton(false, mutable) {
                            player.active = false
                        }
                    }
                }
            }
            NavigateButton("Enter Attacks", context)
            Spacer(modifier = Modifier.size(Dp(32f)))
            gone.forEach {
                Row {
                    if (it.monster != null) {
                        val mon = context.monsters[it.monster]!!
                        Text(
                            mon.name,
                            style = MaterialTheme.typography.h6
                        )
                        ToggleButton(true, mutable) {
                            mon.active = true
                        }
                    } else {
                        val player = context.players[it.player]!!
                        Text(
                            player.name,
                            style = MaterialTheme.typography.h6
                        )
                        ToggleButton(true, mutable) {
                            player.active = true
                        }
                    }
                }
            }
        }
    }
}

// This is all just to handle ties (players->elites->normal)
fun getSort (id: CombatantId, context: Context): Int {
    var sort = 0
    if (id.monster != null) {
        sort += 5
        val monster = context.monsters[id.monster]
        if (!monster!!.elite)
            sort += 1
        sort += monster.init * 10
    }
    else
        sort += context.players[id.player]!!.init * 10
    return sort
}
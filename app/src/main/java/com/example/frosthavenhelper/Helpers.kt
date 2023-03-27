package com.example.frosthavenhelper


fun translatePlayerId(id: String): PlayerId {
    val translate = id.filterNot { c -> c.isWhitespace() }
    return PlayerId.values().find { it.name == translate } ?: PlayerId.None
}

fun translateMonsterId(id: String): MonsterId {
    val translate = id.filterNot { c -> c.isWhitespace() }
    return MonsterId.values().find { it.name == translate } ?: MonsterId.None
}

fun translateMonsterClass(id: String): MonsterClass {
    val translate = id.filterNot { c -> c.isWhitespace() }
    return MonsterClass.values().find { it.name == translate } ?: MonsterClass.None
}

fun populateMonsters (session: SessionInfo, monDatabase: Map<MonsterId, MonsterBase>): Map<MonsterId, Monster> {
    val monsters = mutableMapOf<MonsterId, Monster>()
    session.monsters.forEach {
        val base = monDatabase[it]!!
        monsters[it] = Monster(
            name = base.name,
            attack = base.attack[session.level],
            move = base.move[session.level],
            mon_class = base.mon_class,
            elite = base.elite
        )
    }
    return monsters
}

fun updateMonsters (context: Context, entry: AttackEntry, index: Int) {
    val card = entry.deck.elementAt(index)
    context.monsters.forEach {
        if (it.value.mon_class == entry.mon_class) {
            val base = context.mon_database[it.key]!!
            it.value.selection = index
            it.value.init = card.init
            it.value.attack = card.attack + base.attack[context.session.level]
            it.value.move = card.move + base.move[context.session.level]
        }
    }
}
package com.example.frosthavenhelper

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import org.json.JSONObject
import java.io.InputStream


@Composable
fun jsonHelper(file: Int): String {
    val resources = LocalContext.current.resources
    val inputStream: InputStream = resources.openRawResource(file)
    val br = inputStream.bufferedReader()
    val sb = StringBuilder()
    var line = br.readLine()
    while(line != null) {
        sb.append(line)
        line = br.readLine()
    }
    return sb.toString()
}

@Composable
fun parsePlayers(): Map<PlayerId, Player> {
    val players = mutableMapOf<PlayerId, Player>()

    val fullString = jsonHelper(R.raw.players)
    val jsonPlayers = JSONObject(fullString).getJSONArray("players")
    for (i in 0 until jsonPlayers.length()) {
        val jsonPlayer = jsonPlayers.getJSONObject(i)
        val id = translatePlayerId(jsonPlayer.getString("id"))
        val name = jsonPlayer.getString("name")
        players[id] = Player(name)
    }

    return players
}

@Composable
fun parseMonsters (): Map<MonsterId, MonsterBase> {
    val monsters = mutableMapOf<MonsterId, MonsterBase>()

    val fullString = jsonHelper(R.raw.monsters)
    val jsonMonsters = JSONObject(fullString).getJSONArray("monsters")
    for (i in 0 until jsonMonsters.length()) {
        val jsonMonster = jsonMonsters.getJSONObject(i)
        val name = jsonMonster.getString("id")
        val id = translateMonsterId(name)
        val jsonAttack = jsonMonster.getJSONArray("attack")
        val attack = IntArray(jsonAttack.length())
        for (j in 0 until jsonAttack.length())
            attack[j] = jsonAttack.getInt(j)
        val jsonMove = jsonMonster.getJSONArray("move")
        val move = IntArray(jsonMove.length())
        for (j in 0 until jsonMove.length())
            move[j] = jsonMove.getInt(j)
        monsters[id] = MonsterBase(
            name = name,
            mon_class = translateMonsterClass(jsonMonster.getString("class")),
            elite = jsonMonster.getBoolean("elite"),
            attack = attack,
            move = move
        )
    }
    return monsters
}

@Composable
fun parseAttacks (): Map<MonsterClass, AttackEntry> {
    val classes = mutableMapOf<MonsterClass, AttackEntry>()

    val fullString = jsonHelper(R.raw.attacks)
    val jsonClasses = JSONObject(fullString).getJSONArray("classes")
    for (i in 0 until jsonClasses.length()) {
        val jsonClass = jsonClasses.getJSONObject(i)
        val name = jsonClass.getString("name")
        val id = translateMonsterClass(name)
        val jsonDeck = jsonClass.getJSONArray("deck")
        val deck = mutableSetOf<AttackCard>()
        for (j in 0 until jsonDeck.length()) {
            val jsonCard = jsonDeck.getJSONObject(j)
            deck.add(AttackCard(
                name = jsonCard.getString("name"),
                attack = jsonCard.getInt("attack"),
                move = jsonCard.getInt("move"),
                init = jsonCard.getInt("init"),
                shuffle = jsonCard.getBoolean("shuffle")
            ))
        }
        classes[id] = AttackEntry(
            name = name,
            mon_class = id,
            deck = deck
        )
    }
    return classes
}

@Composable
fun parseSession(): SessionInfo {
    val fullString = jsonHelper(R.raw.session)
    val session = JSONObject(fullString)

    val jsonMonsters = session.getJSONArray("monsters")
    val monsters = MutableList(jsonMonsters.length()) {MonsterId.None}
    for(i in 0 until jsonMonsters.length()) {
        monsters[i] = translateMonsterId(jsonMonsters.getString(i))
    }
    return SessionInfo(
        level = session.getInt("level"),
        monsters = monsters
    )
}
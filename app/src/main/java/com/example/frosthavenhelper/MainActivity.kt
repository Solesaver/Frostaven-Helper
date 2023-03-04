package com.example.frosthavenhelper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.core.text.isDigitsOnly
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.frosthavenhelper.ui.theme.FrosthavenHelperTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.InputStream


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FrosthavenHelperTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    Navigation()
                }
            }
        }
    }
}

data class Context (
    val nav: NavHostController,
    val players: Map<PlayerId, Player> = emptyMap(),
    val monsters: Map<MonsterId, Monster> = emptyMap(),
    val session: SessionInfo,
    val att_database: Map<MonsterClass, AttackEntry> = emptyMap(),
    val mon_database: Map<MonsterId, MonsterBase> = emptyMap()
)

data class AttackCard (
    val name: String = "",
    val move: Int = 0,
    val attack: Int = 0,
    val init: Int = 0,
    val shuffle: Boolean = false
)

data class AttackEntry (
    val name: String,
    val mon_class: MonsterClass,
    val deck: Set<AttackCard> = emptySet()
)

data class Player (
    val name: String = "",
    var init: Int = 0,
    var active: Boolean = true
)

data class Monster (
    val name: String = "",
    val mon_class: MonsterClass,
    val elite: Boolean = true,
    var selection: Int = 0,
    var init: Int = 0,
    var attack: Int = 0,
    var move: Int = 0,
    var active: Boolean = true
)

data class MonsterBase (
    val name: String = "",
    val mon_class: MonsterClass,
    val elite: Boolean = true,
    val attack: IntArray = intArrayOf(),
    val move: IntArray = intArrayOf()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MonsterBase

        if (mon_class != other.mon_class) return false
        if (elite != other.elite) return false

        return true
    }

    override fun hashCode(): Int {
        var result = mon_class.hashCode()
        result = 31 * result + elite.hashCode()
        return result
    }
}

data class SessionInfo (
    val level: Int = 0,
    val monsters: Array<MonsterId> = arrayOf()
)

data class CombatantId (
    val monster: MonsterId? = null,
    val player: PlayerId? = null
)

enum class MonsterClass {
    None,
    Icespeaker,
    Snowspeaker,
    Scout,
    FrostDemon,
    SnowImp
}

enum class PlayerId {
    None,
    Trevor,
    Janet,
    Nick,
    Bismark
}

enum class MonsterId {
    None,
    EAlgoxIcespeaker,
    AlgoxIcespeaker,
    EAlgoxSnowspeaker,
    AlgoxSnowspeaker,
    EAlgoxScout,
    AlgoxScout,
    EFrostDemon,
    FrostDemon,
    ESnowImp,
    SnowImp
}

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
    val monsters = Array(jsonMonsters.length()) {MonsterId.None}
    for(i in 0 until jsonMonsters.length()) {
        monsters[i] = translateMonsterId(jsonMonsters.getString(i))
    }
    return SessionInfo(
        level = session.getInt("level"),
        monsters = monsters
    )
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

@Composable
fun Navigation() {
    val players = parsePlayers()
    val session = parseSession()
    val monDatabase = parseMonsters()
    val monsters = populateMonsters(session, monDatabase)
    val context = Context(
        nav = rememberNavController(),
        players = players,
        monsters = monsters,
        session = session,
        att_database = parseAttacks(),
        mon_database = monDatabase
    )
    NavHost(navController = context.nav, startDestination = "EnterAttacks") {
        composable("EnterAttacks") { EnterAttacks(context) }
        composable("RoundInfo") { RoundInfo(context) }
        composable("CheckData") { CheckData(context) }
    }
    context.nav.navigate("EnterAttacks")
}

@Composable
fun EnterAttacks(context: Context) {
    val activeClasses: MutableSet<MonsterClass> = mutableSetOf()
    Column(modifier = Modifier.fillMaxHeight()) {
        context.monsters.forEach {
            if (!it.value.active || activeClasses.contains(it.value.mon_class))
                return@forEach
            activeClasses += it.value.mon_class
            val entry = context.att_database.getOrElse(key = it.value.mon_class) {
                return@forEach
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "${entry.name}: ",
                    style = MaterialTheme.typography.h5,
                    modifier = Modifier
                        .padding(vertical = Dp(10f))
                        .align(alignment = Alignment.CenterVertically))
                DeckDropdown(entry, context)
            }
        }
        context.players.forEach {
            if (!it.value.active)
                return@forEach
            Row(modifier = Modifier.fillMaxWidth()) {
                var text by remember { mutableStateOf(TextFieldValue("${it.value.init}")) }
                val scope = rememberCoroutineScope()
                Text(
                    "${it.value.name}: ",
                    style = MaterialTheme.typography.h5,
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
                                if (++count > 2)
                                    return@loop
                                if (it.isDigit())
                                    builder += it
                            }
                        }
                        text = TextFieldValue(text = builder, selection = TextRange(builder.length))
                        if (text.text.isEmpty() || !text.text.isDigitsOnly())
                            it.value.init = 0
                        else
                            it.value.init = text.text.toInt()
                    },
                    textStyle = MaterialTheme.typography.h5,
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
        Button (onClick = {
            context.nav.navigate("RoundInfo")
        }) {
            Text(text = "Round Info", style = MaterialTheme.typography.h5)
        }
        Button (onClick = {
            context.nav.navigate("CheckData")
        }) {
            Text(text = "CheckData", style = MaterialTheme.typography.h5)
        }
    }
}

@Composable
fun DeckDropdown(entry: AttackEntry, context: Context) {
    var expanded by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableStateOf(0) }
    Box(modifier = Modifier) {
        Text(text = "  " + entry.deck.elementAt(selectedIndex).name,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = { expanded = true })
                .background(Color.Gray)
                .padding(vertical = Dp(10f))
                .align(alignment = Alignment.CenterStart),
            style = MaterialTheme.typography.h5)
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color.Gray
                )
        ) {
            entry.deck.forEachIndexed { index, s ->
                DropdownMenuItem(onClick = {
                    selectedIndex = index
                    val card = entry.deck.elementAt(index)
                    context.monsters.forEach {
                        if (it.value.mon_class == entry.mon_class) {
                            val base = context.mon_database[it.key]!!
                            it.value.selection = selectedIndex
                            it.value.init = card.init
                            it.value.attack = card.attack + base.attack[0]
                            it.value.move = card.move + base.move[0]
                        }
                    }
                    expanded = false
                }) {
                    Text(text = s.name, style = MaterialTheme.typography.h5)
                }
            }
        }
    }
}

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

@Composable
fun RoundInfo (context: Context) {
    val order: MutableList<CombatantId> = mutableListOf()
    val gone: MutableSet<CombatantId> = mutableSetOf()
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
    var toggle by remember { mutableStateOf(false) }
    key(toggle) {
        Column(modifier = Modifier.fillMaxWidth()) {
            order.forEach {
                Row {
                    if (it.monster != null) {
                        val mon = context.monsters[it.monster]!!
                        Text(
                            "${mon.init} - ${mon.name}: Att(${mon.attack}) Mov(${mon.move})",
                            style = MaterialTheme.typography.h5
                        )
                        Button(onClick = {
                            mon.active = false
                            toggle = !toggle
                        }) {
                            Text("X", style = MaterialTheme.typography.h5)
                        }
                    } else {
                        val player = context.players[it.player]!!
                        Text(
                            "${player.init} - ${player.name}",
                            style = MaterialTheme.typography.h5
                        )
                        Button(onClick = {
                            player.active = false
                            toggle = !toggle
                        }) {
                            Text("X", style = MaterialTheme.typography.h5)
                        }
                    }
                }
            }
            Button(onClick = {
                context.nav.navigate("EnterAttacks")
            }) {
                Text(text = "Enter Attacks", style = MaterialTheme.typography.h5)
            }
            Spacer(modifier = Modifier.size(Dp(32f)))
            gone.forEach {
                Row {
                    if (it.monster != null) {
                        val mon = context.monsters[it.monster]!!
                        Text(
                            mon.name,
                            style = MaterialTheme.typography.h5
                        )
                        Button(onClick = {
                            mon.active = true
                            toggle = !toggle
                        }) {
                            Text("O", style = MaterialTheme.typography.h5)
                        }
                    } else {
                        val player = context.players[it.player]!!
                        Text(
                            player.name,
                            style = MaterialTheme.typography.h5
                        )
                        Button(onClick = {
                            player.active = true
                            toggle = !toggle
                        }) {
                            Text("O", style = MaterialTheme.typography.h5)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CheckData (context: Context) {
    val monsters = context.monsters.values.toTypedArray()
    Column {
        var expanded by remember { mutableStateOf(false) }
        var selectedIndex by remember { mutableStateOf(0) }
        Box(modifier = Modifier) {
            Text(text = monsters.elementAt(selectedIndex).name,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = { expanded = true })
                    .background(Color.Gray)
                    .padding(vertical = Dp(10f))
                    .align(alignment = Alignment.CenterStart),
                style = MaterialTheme.typography.h5)
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color.Gray
                    )
            ) {
                monsters.forEachIndexed { index, s ->
                    DropdownMenuItem(onClick = {
                        selectedIndex = index
                        expanded = false
                    }) {
                        Text(text = s.name, style = MaterialTheme.typography.h5)
                    }
                }
            }
        }
        val monster = monsters[selectedIndex]
        val base = context.mon_database[translateMonsterId(monster.name)]!!
        val attack = context.att_database[monster.mon_class]!!.deck.elementAt(monster.selection)
        val level = context.session.level
        Text("Base Attack: ${base.attack[level]}", style = MaterialTheme.typography.h5)
        Text("Attack Modifier: ${attack.attack}", style = MaterialTheme.typography.h5)
        Text("Base Move: ${base.move[level]}", style = MaterialTheme.typography.h5)
        Text("Move Modifier: ${attack.move}", style = MaterialTheme.typography.h5)

        Button (onClick = {
            context.nav.navigate("EnterAttacks")
        }) {
            Text(text = "Enter Attacks", style = MaterialTheme.typography.h5)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    FrosthavenHelperTheme {
        // A surface container using the 'background' color from the theme
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
            Navigation()
        }
    }
}
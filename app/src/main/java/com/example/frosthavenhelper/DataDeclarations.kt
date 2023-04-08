package com.example.frosthavenhelper

import androidx.navigation.NavHostController

// Enums
enum class MonsterClass {
    None,
    AlgoxIcespeaker,
    AlgoxSnowspeaker,
    AncientArtillery,
    Boss,
    EarthDemon,
    FlameDemon,
    FrostDemon,
    NightDemon,
    WindDemon,
    FlamingBladespinner,
    IceWraith,
    Imp,
    Scout,
    RuinedMachine,
    SteelAutomaton
}
enum class MonsterId {
    None,
    EAncientArtillery,
    AncientArtillery,
    EAlgoxIcespeaker,
    AlgoxIcespeaker,
    EAlgoxScout,
    AlgoxScout,
    EAlgoxSnowspeaker,
    AlgoxSnowspeaker,
    AlgoxStormcaller,
    EEarthDemon,
    EarthDemon,
    EFlameDemon,
    FlameDemon,
    EFrostDemon,
    FrostDemon,
    ENightDemon,
    NightDemon,
    EWindDemon,
    WindDemon,
    EFlamingBladespinner,
    FlamingBladespinner,
    EIceWraith,
    IceWraith,
    ESnowImp,
    SnowImp,
    ERuinedMachine,
    RuinedMachine,
    ESteelAutomaton,
    SteelAutomaton
}

enum class PlayerId {
    None,
    Trevor,
    Janet,
    Nick,
    Bismark
}

// State
data class Context (
    val nav: NavHostController,
    val players: Map<PlayerId, Player> = emptyMap(),
    var monsters: Map<MonsterId, Monster> = emptyMap(),
    val session: SessionInfo,
    val att_database: Map<MonsterClass, AttackEntry> = emptyMap(),
    val mon_database: Map<MonsterId, MonsterBase> = emptyMap()
)

interface NamedObject {
    val name: String
}

interface ActiveObject {
    var active: Boolean
}

data class Player (
    override val name: String = "",
    var init: Int = 0,
    override var active: Boolean = true
) : NamedObject, ActiveObject

data class Monster (
    override val name: String = "",
    val mon_class: MonsterClass,
    val elite: Boolean = true,
    var selection: Int = 0,
    var init: Int = 0,
    var attack: Int = 0,
    var move: Int = 0,
    override var active: Boolean = true
) : NamedObject, ActiveObject

data class SessionInfo (
    var level: Int = 0,
    val monsters: MutableList<MonsterId> = mutableListOf()
)

data class CombatantId (
    val monster: MonsterId? = null,
    val player: PlayerId? = null
)

// Database
data class AttackEntry (
    override val name: String,
    val mon_class: MonsterClass,
    val deck: Set<AttackCard> = emptySet()
) : NamedObject

data class AttackCard (
    override val name: String = "",
    val move: Int = 0,
    val attack: Int = 0,
    val init: Int = 0,
    val shuffle: Boolean = false
) : NamedObject

data class MonsterBase (
    override val name: String = "",
    val mon_class: MonsterClass,
    val elite: Boolean = true,
    val attack: IntArray = intArrayOf(),
    val move: IntArray = intArrayOf()
) : NamedObject
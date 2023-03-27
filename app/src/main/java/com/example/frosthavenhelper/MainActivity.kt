package com.example.frosthavenhelper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.frosthavenhelper.ui.theme.FrosthavenHelperTheme


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
    NavHost(navController = context.nav, startDestination = "Set Session") {
        composable("Set Session") { SetSession(context) }
        composable("Enter Attacks") { EnterAttacks(context) }
        composable("Round Info") { RoundInfo(context) }
        composable("Check Data") { CheckData(context) }
    }
    context.nav.navigate("Set Session")
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
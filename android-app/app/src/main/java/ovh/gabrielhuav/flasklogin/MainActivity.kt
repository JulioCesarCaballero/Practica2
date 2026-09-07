package ovh.gabrielhuav.flasklogin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ovh.gabrielhuav.flasklogin.ui.theme.FlaskLoginTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FlaskLoginTheme {
                AppRoot()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot() {
    val navController: NavHostController = rememberNavController()
    var isLoggedIn by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Practica 2 - CRUD App") },
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menú")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Inicio de Sesión") },
                            onClick = {
                                menuExpanded = false
                                navController.navigate(Screen.Login.route)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Registro de Usuario") },
                            onClick = {
                                menuExpanded = false
                                navController.navigate(Screen.Register.route)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Operaciones CRUD") },
                            enabled = isLoggedIn,
                            onClick = {
                                menuExpanded = false
                                navController.navigate(Screen.Crud.route)
                            }
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        isLoggedIn = true
                        navController.navigate(Screen.Crud.route)
                    },
                    onGoToRegister = { navController.navigate(Screen.Register.route) }
                )
            }
            composable(Screen.Register.route) {
                RegisterScreen(
                    onRegisterSuccess = { navController.navigate(Screen.Login.route) },
                    onGoToLogin = { navController.navigate(Screen.Login.route) }
                )
            }
            composable(Screen.Crud.route) {
                if (isLoggedIn) {
                    CrudScreen()
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Text("Debes iniciar sesión primero")
                    }
                }
            }
        }
    }
}
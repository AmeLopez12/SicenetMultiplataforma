package com.example.sicenetmultiplataforma

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.sicenetmultiplataforma.ui.SicenetViewModel
import com.example.sicenetmultiplataforma.ui.screens.*
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App() {
    // Provee el grafo de dependencias de Koin a toda la jerarquía de vistas en Compose.
    KoinContext {
        MaterialTheme {
            val navController = rememberNavController()
            // Inyecta el ViewModel unificado resolviendo su constructor de forma multiplataforma.
            val viewModel: SicenetViewModel = koinViewModel()
            val alumno by viewModel.alumnoLocal.collectAsState()

            // Escucha de manera reactiva la ruta actual en la que se encuentra navegando el usuario.
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                // Barra de navegación inferior global. Se oculta automáticamente si la ruta activa es "login".
                bottomBar = {
                    if (currentRoute != "login") {
                        NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                            NavigationBarItem(
                                selected = currentRoute == "profile",
                                onClick = {
                                    if(currentRoute != "profile") {
                                        navController.navigate("profile") {
                                            popUpTo("profile") { inclusive = false }
                                        }
                                    }
                                },
                                icon = { Icon(Icons.Default.Person, contentDescription = null) },
                                label = { Text("Perfil") }
                            )
                            NavigationBarItem(
                                selected = currentRoute == "carga",
                                onClick = {
                                    if(currentRoute != "carga") {
                                        navController.navigate("carga")
                                    }
                                },
                                icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                                label = { Text("Carga") }
                            )
                            NavigationBarItem(
                                selected = currentRoute == "kardex",
                                onClick = { if(currentRoute != "kardex") navController.navigate("kardex") },
                                icon = { Icon(Icons.Default.Info, contentDescription = null) },
                                label = { Text("Kardex") }
                            )
                            NavigationBarItem(
                                selected = currentRoute == "unidades",
                                onClick = { if(currentRoute != "unidades") navController.navigate("unidades") },
                                icon = { Icon(Icons.Default.Star, contentDescription = null) },
                                label = { Text("Unidades") }
                            )
                            NavigationBarItem(
                                selected = currentRoute == "finales",
                                onClick = { if(currentRoute != "finales") navController.navigate("finales") },
                                icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                                label = { Text("Final") }
                            )
                        }
                    }
                }
            ) { paddingValues ->
                // Delimita el lienzo de la UI aplicando el espaciado seguro reservado por la barra inferior.
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Enrutador principal. Evalúa la presencia del alumno en Room para decidir el destino inicial.
                    NavHost(
                        navController = navController,
                        startDestination = if (alumno == null) "login" else "profile"
                    ) {
                        composable("login") {
                            LoginScreen(
                                viewModel = viewModel,
                                onLoginSuccess = {
                                    // Limpia el historial de navegación para evitar regresar al login con el botón físico de atrás.
                                    navController.navigate("profile") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("profile") {
                            ProfileScreen(
                                viewModel = viewModel,
                                onLogout = {
                                    viewModel.logout()
                                    navController.navigate("login") {
                                        popUpTo("profile") { inclusive = true }
                                    }
                                }
                            )
                        }
                        // Declaración modular de los destinos secundarios pasando la referencia del ViewModel y la pila de retorno.
                        composable("kardex") { KardexScreen(viewModel = viewModel, onBack = { navController.popBackStack() }) }
                        composable("carga") { CargaScreen(viewModel = viewModel, onBack = { navController.popBackStack() }) }
                        composable("unidades") { CalifUnidadesScreen(viewModel = viewModel, onBack = { navController.popBackStack() }) }
                        composable("finales") { CalifFinalScreen(viewModel = viewModel, onBack = { navController.popBackStack() }) }
                    }
                }
            }
        }
    }
}
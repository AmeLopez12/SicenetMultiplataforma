package com.example.sicenetmultiplataforma.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sicenetmultiplataforma.ui.SicenetViewModel
import com.example.sicenetmultiplataforma.ui.theme.SicenetGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: SicenetViewModel,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Variables para guardar lo que el usuario escribe
    var matricula by remember { mutableStateOf("") }
    var contrasenia by remember { mutableStateOf("") }
    val loginState = viewModel.loginState

    // Configuracion de colores para los inputs
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = SicenetGreen,
        unfocusedBorderColor = Color.Gray,
        focusedLabelColor = SicenetGreen,
        unfocusedLabelColor = Color.Gray,
        cursorColor = SicenetGreen,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent
    )

    Scaffold(
        containerColor = Color.White,
        topBar = {
            // Barra superior verde con el titulo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SicenetGreen)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .height(60.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "SICENET",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier.padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Si hubo un error en el login, mostramos este aviso
                if (loginState is SicenetViewModel.LoginResult.Error) {
                    Surface(
                        color = Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = if (loginState.message.contains("incorrecta")) "Matrícula o contraseña incorrecta" else "Error de conexión",
                            color = Color(0xFFD32F2F),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Input para la matricula
                OutlinedTextField(
                    value = matricula,
                    onValueChange = { matricula = it },
                    label = { Text("Ingresa matrícula...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !viewModel.isLoading,
                    colors = textFieldColors
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Input para la contraseña
                OutlinedTextField(
                    value = contrasenia,
                    onValueChange = { contrasenia = it },
                    label = { Text("Contraseña...") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !viewModel.isLoading,
                    colors = textFieldColors
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Si esta cargando mostramos el circulo, si no, el boton
                if (viewModel.isLoading) {
                    CircularProgressIndicator(color = SicenetGreen)
                } else {
                    Button(
                        onClick = {
                            if (matricula.isNotBlank() && contrasenia.isNotBlank()) {
                                viewModel.login(matricula, contrasenia, onLoginSuccess)
                            }
                        },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(containerColor = SicenetGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Entrar", color = Color.White)
                    }
                }
            }
        }
    }
}

package com.example.sicenetmultiplataforma.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sicenetmultiplataforma.ui.SicenetViewModel
import com.example.sicenetmultiplataforma.ui.theme.SicenetGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalifFinalScreen(
    viewModel: SicenetViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Escuchamos de Room usando el StateFlow oficial
    val listaFinales by viewModel.califFinalLocal.collectAsState()
    val isLoading = viewModel.isLoading

    LaunchedEffect(Unit) {
        if (listaFinales.isEmpty()) {
            viewModel.syncCalifFinal()
        }
    }

    Scaffold(
        containerColor = Color(0xFFF5F5F5),
        topBar = {
            TopAppBar(
                title = { Text("Calificaciones Finales", color = Color.White, fontWeight = FontWeight.Bold) },

                actions = {
                    IconButton(onClick = { viewModel.syncCalifFinal() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sincronizar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SicenetGreen)
            )
        }
    ) { paddingValues ->
        Box(modifier = modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            if (listaFinales.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (isLoading) {
                        CircularProgressIndicator(color = SicenetGreen)
                    } else {
                        Text("No hay calificaciones finales disponibles.", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(listaFinales) { finalItem ->
                        val esAprobada = finalItem.calificacion >= 70

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    // Nombre de la Materia
                                    Text(
                                        text = finalItem.materia,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color.DarkGray
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    // Tipo de Acreditación (Ordinario, etc.)
                                    Text(
                                        text = "Acreditación: ${if(finalItem.acreditacion.isNotBlank()) finalItem.acreditacion else "N/A"}",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Chip de Calificación Numérica Final
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            color = if (esAprobada) SicenetGreen.copy(alpha = 0.1f) else Color.Red.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(8.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = finalItem.calificacion.toString(),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp,
                                        color = if (esAprobada) SicenetGreen else Color.Red
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
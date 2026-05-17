package com.example.sicenetmultiplataforma.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun KardexScreen(
    viewModel: SicenetViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listaKardex by viewModel.kardexLocal.collectAsState()
    val isLoading = viewModel.isLoading

    LaunchedEffect(Unit) {
        if (listaKardex.isEmpty()) {
            viewModel.syncKardex()
        }
    }

    // Agrupamos la lista plana de Room en un mapa ordenado por Periodo
    val kardexAgrupado = listaKardex.groupBy { it.periodo }

    Scaffold(
        containerColor = Color(0xFFF5F5F5),
        topBar = {
            TopAppBar(
                title = { Text("Kardex Histórico", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {

                },
                actions = {
                    IconButton(onClick = { viewModel.syncKardex() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sincronizar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SicenetGreen)
            )
        }
    ) { paddingValues ->
        Box(modifier = modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp)) {
            if (listaKardex.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (isLoading) {
                        CircularProgressIndicator(color = SicenetGreen)
                    } else {
                        Text("No hay datos de Kardex localmente.", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    // RECORREMOS EL MAPA: Iteramos periodo por periodo
                    kardexAgrupado.forEach { (periodo, materiasDelPeriodo) ->

                        stickyHeader {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF5F5F5))
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = if (periodo.isNotBlank()) periodo else "Otros Periodos",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = SicenetGreen,
                                    modifier = Modifier
                                        .background(SicenetGreen.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp))
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Pintamos las materias correspondientes únicamente a este bloque de semestre
                        items(materiasDelPeriodo) { materiaKardex ->
                            val califNum = materiaKardex.calificacion.toIntOrNull() ?: 0
                            val esAprobada = califNum >= 70

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = materiaKardex.materia,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color.DarkGray
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            Text("Clave: ${materiaKardex.clave}", fontSize = 12.sp, color = Color.Gray)
                                        }
                                    }

                                    // Calificación Vistoso
                                    Box(
                                        modifier = Modifier
                                            .size(45.dp)
                                            .background(
                                                color = if (esAprobada) SicenetGreen.copy(alpha = 0.1f) else Color.Red.copy(alpha = 0.1f),
                                                shape = RoundedCornerShape(8.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = materiaKardex.calificacion,
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
}
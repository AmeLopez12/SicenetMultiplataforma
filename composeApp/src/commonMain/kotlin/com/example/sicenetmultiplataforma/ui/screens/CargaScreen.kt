package com.example.sicenetmultiplataforma.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.sicenetmultiplataforma.data.model.Materia
import com.example.sicenetmultiplataforma.ui.SicenetViewModel
import com.example.sicenetmultiplataforma.ui.theme.SicenetGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CargaScreen(
    viewModel: SicenetViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listaMaterias by viewModel.cargaLocal.collectAsState()
    val isLoading = viewModel.isLoading

    LaunchedEffect(Unit) {
        if (listaMaterias.isEmpty()) {
            viewModel.syncCargaAcademica()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Carga Académica", color = Color.White, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { viewModel.syncCargaAcademica() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refrescar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SicenetGreen)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (listaMaterias.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (isLoading) {
                        CircularProgressIndicator(color = SicenetGreen)
                    } else {
                        Text("No hay datos disponibles.", color = Color.Gray, fontSize = 16.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp) // Un poco más de espacio por el tamaño de la tarjeta
                ) {
                    items(listaMaterias) { materia ->
                        MateriaCard(materia = materia)
                    }
                }
            }
        }
    }
}

@Composable
fun MateriaCard(materia: Materia, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Nombre de la Materia
            Text(
                text = materia.nombre,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = SicenetGreen
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Nombre del Maestro / Docente
            Text(
                text = if (materia.docente.isNotBlank()) materia.docente else "Docente no asignado",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Bloque de Horarios / Días (Fila de cajitas estilo Parciales)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                DiaHorarioBox(dia = "L", horario = materia.lunes, modifier = Modifier.weight(1f))
                DiaHorarioBox(dia = "M", horario = materia.martes, modifier = Modifier.weight(1f))
                DiaHorarioBox(dia = "M", horario = materia.miercoles, modifier = Modifier.weight(1f))
                DiaHorarioBox(dia = "J", horario = materia.jueves, modifier = Modifier.weight(1f))
                DiaHorarioBox(dia = "V", horario = materia.viernes, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Pie de Tarjeta: Grupo y Créditos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Grupo: ${materia.grupo}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.DarkGray
                )
                Text(
                    text = "${materia.creditos} Créditos",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SicenetGreen
                )
            }
        }
    }
}

@Composable
fun DiaHorarioBox(dia: String, horario: String, modifier: Modifier = Modifier) {
    // Si el horario está vacío o tiene solo guiones, significa que ese día no se imparte
    val tieneClase = horario.isNotBlank() && horario != "-" && horario != "null"

    Column(
        modifier = modifier
            .background(
                color = if (tieneClase) SicenetGreen.copy(alpha = 0.1f) else Color(0xFFF5F5F5),
                shape = RoundedCornerShape(6.dp)
            )
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Inicial del día
        Text(
            text = dia,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (tieneClase) SicenetGreen else Color.Gray
        )
        Spacer(modifier = Modifier.height(2.dp))
        // Hora (Ej. 08:00-09:00 -> Muestra un indicador o la hora recortada)
        Text(
            text = if (tieneClase) horario.substringBefore("-").trim() else "-",
            fontSize = 10.sp,
            fontWeight = FontWeight.Normal,
            color = if (tieneClase) Color.DarkGray else Color.LightGray
        )
    }
}
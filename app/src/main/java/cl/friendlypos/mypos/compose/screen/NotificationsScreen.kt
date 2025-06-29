package cl.friendlypos.mypos.compose.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

import java.time.format.DateTimeFormatter

import cl.friendlypos.mypos.compose.viewmodel.NotificationsViewModel
import cl.friendlypos.mypos.model.Notification
import cl.friendlypos.mypos.model.NotificationType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel = viewModel()
) {
    val notifications by viewModel.filteredNotifications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val fromDate by viewModel.fromDate.collectAsState()
    val toDate by viewModel.toDate.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    var datePickerType by remember { mutableStateOf("from") }
    var showTypeDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Notificaciones",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Filtros de fecha
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Fecha desde
            OutlinedTextField(
                value = fromDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "",
                onValueChange = { },
                modifier = Modifier.weight(1f),
                readOnly = true,
                placeholder = { Text("Desde") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Fecha desde"
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Fecha hasta
            OutlinedTextField(
                value = toDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "",
                onValueChange = { },
                modifier = Modifier.weight(1f),
                readOnly = true,
                placeholder = { Text("Hasta") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Fecha hasta"
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        }

        // Filtro por tipo/categoría
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = showTypeDropdown,
                onExpandedChange = { showTypeDropdown = !showTypeDropdown },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = selectedType?.name ?: "Todas las categorías",
                    onValueChange = { },
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTypeDropdown)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filtrar por tipo"
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = showTypeDropdown,
                    onDismissRequest = { showTypeDropdown = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Todas las categorías") },
                        onClick = {
                            viewModel.updateSelectedType(null)
                            showTypeDropdown = false
                        }
                    )
                    NotificationType.values().forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.name) },
                            onClick = {
                                viewModel.updateSelectedType(type)
                                showTypeDropdown = false
                            }
                        )
                    }
                }
            }

            // Botón limpiar filtros
            if (fromDate != null || toDate != null || selectedType != null) {
                IconButton(
                    onClick = { viewModel.clearAllFilters() },
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Limpiar filtros"
                    )
                }
            }
        }

        // Botones para seleccionar fechas
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    datePickerType = "from"
                    showDatePicker = true
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Seleccionar Desde")
            }

            Button(
                onClick = {
                    datePickerType = "to"
                    showDatePicker = true
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Seleccionar Hasta")
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(notifications) { notification ->
                    NotificationItemCard(
                        notification = notification,
                        onMarkAsRead = { viewModel.markAsRead(it) }
                    )
                }
            }
        }
    }

    // DatePicker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDateSelected = { selectedDate ->
                when (datePickerType) {
                    "from" -> viewModel.updateFromDate(selectedDate)
                    "to" -> viewModel.updateToDate(selectedDate)
                }
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@Composable
fun NotificationItemCard(
    notification: Notification,
    onMarkAsRead: (String) -> Unit
) {
    val (icon, iconColor) = when (notification.type) {
        NotificationType.INFO -> Icons.Default.Info to Color.Blue
        NotificationType.WARNING -> Icons.Default.Warning to Color(0xFFFF9800)
        NotificationType.ERROR -> Icons.Default.Error to Color.Red
        NotificationType.SUCCESS -> Icons.Default.CheckCircle to Color.Green
    }

    val backgroundColor = if (notification.isRead) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        onClick = {
            if (!notification.isRead) {
                onMarkAsRead(notification.id)
            }
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = notification.type.name,
                tint = iconColor,
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.Top)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.weight(1f)
                    )

                    if (!notification.isRead) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(8.dp)
                        ) {}
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = iconColor.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = notification.type.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = iconColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = notification.timestamp.format(
                            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
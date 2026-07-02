package cl.friendlypos.mypos.compose.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import cl.friendlypos.mypos.ui.sales.SaleItem

private val POS_BLUE = Color(0xFF2196F3)

/**
 * Fila de un ítem del carrito. Compartida por `CartScreen` y `ScanCartScreen`.
 * - Tap largo → [onLongPress] (eliminar).
 * - Botón lápiz → [onEdit].
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun SaleItemRow(
    item: SaleItem,
    onLongPress: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .combinedClickable(onLongClick = onLongPress, onClick = {}),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF212121)
                )
                Text(
                    text = "$${item.unitPrice}",
                    fontSize = 14.sp,
                    color = Color(0xFF2E2E33)
                )
                Text(
                    text = "Cantidad: ${item.quantity}",
                    fontSize = 14.sp,
                    color = Color(0xFF2E2E33)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = POS_BLUE,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "$${item.unitPrice * item.quantity}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF2E2E33)
                )
            }
        }
    }
}

/**
 * Diálogo para editar nombre / precio unitario / cantidad de un [SaleItem].
 * Compartido por `CartScreen` y `ScanCartScreen`.
 */
@Composable
internal fun EditItemDialog(
    item: SaleItem,
    onDismiss: () -> Unit,
    onConfirm: (SaleItem) -> Unit
) {
    var name by remember { mutableStateOf(item.name) }
    var unitPrice by remember { mutableStateOf(item.unitPrice.toString()) }
    var quantity by remember { mutableStateOf(item.quantity.toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    "Editar item",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = unitPrice,
                    onValueChange = { unitPrice = it.filter { c -> c.isDigit() } },
                    label = { Text("Precio unitario") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it.filter { c -> c.isDigit() } },
                    label = { Text("Cantidad") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "¿Guardar cambios?",
                    fontSize = 16.sp,
                    color = Color(0xFF2E2E33),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = POS_BLUE
                        )
                    ) { Text("No") }

                    Button(
                        onClick = {
                            val price = unitPrice.toIntOrNull() ?: item.unitPrice
                            val qty = quantity.toIntOrNull()?.coerceAtLeast(1) ?: item.quantity
                            onConfirm(
                                item.copy(
                                    name = name.ifBlank { item.name },
                                    unitPrice = price,
                                    quantity = qty
                                )
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = POS_BLUE)
                    ) { Text("Sí") }
                }
            }
        }
    }
}

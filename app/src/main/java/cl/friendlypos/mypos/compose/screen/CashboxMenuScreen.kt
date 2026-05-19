package cl.friendlypos.mypos.compose.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.friendlypos.mypos.R
import cl.friendlypos.mypos.compose.theme.AppColors

@Composable
fun CashboxMenuScreen(
    isSessionOpen: Boolean,
    onOpenCashbox: () -> Unit,
    onCloseCashbox: () -> Unit,
    onRegisterMovement: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_cash),
            contentDescription = "Caja",
            tint = Color(0xFF009688),
            modifier = Modifier.size(56.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Caja",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isSessionOpen) "Caja abierta" else "Sin sesión activa",
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSessionOpen) Color(0xFF2E7D32) else Color(0xFF666666)
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onOpenCashbox,
            enabled = !isSessionOpen,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.BrandPrimary,
                disabledContainerColor = AppColors.BrandPrimary.copy(alpha = 0.38f),
                disabledContentColor = Color.White.copy(alpha = 0.6f)
            )
        ) {
            Text("Abrir Caja", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onCloseCashbox,
            enabled = isSessionOpen,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE53935),
                disabledContainerColor = Color(0xFFE53935).copy(alpha = 0.38f),
                disabledContentColor = Color.White.copy(alpha = 0.6f)
            )
        ) {
            Text("Cerrar Caja", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onRegisterMovement,
            enabled = isSessionOpen,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF607D8B),
                disabledContainerColor = Color(0xFF607D8B).copy(alpha = 0.38f),
                disabledContentColor = Color.White.copy(alpha = 0.6f)
            )
        ) {
            Text("Registrar Movimiento", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }
    }
}

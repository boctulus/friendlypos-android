package cl.friendlypos.mypos

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import cl.friendlypos.mypos.api.ApiClient
import cl.friendlypos.mypos.compose.screen.LoginScreen
import cl.friendlypos.mypos.compose.viewmodel.LoginFlowViewModel
import cl.friendlypos.mypos.db.AppDatabase
import cl.friendlypos.mypos.utils.DeviceIdProvider
import cl.friendlypos.mypos.utils.FingerprintUtils
import cl.friendlypos.mypos.work.PendingClosureWorker
import java.util.concurrent.TimeUnit

class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ApiClient.init(this)
        FingerprintUtils.deviceFingerprintHash(this)

        if (SessionManager.isLoggedIn(this) && ApiClient.hasValidSession()) {
            startMainActivity()
            return
        }

        setContent {
            MaterialTheme {
                val loginVm: LoginFlowViewModel = viewModel()

                val state by loginVm.state.collectAsState()
                val isLoading by loginVm.isLoading.collectAsState()
                val errorMessage by loginVm.errorMessage.collectAsState()
                val savedEmail = remember { SessionManager.getLastEmail(this@LoginActivity) }

                var showPendingRecovery by remember { mutableStateOf(false) }
                var navigatingToMain by remember { mutableStateOf(false) }

                LaunchedEffect(state) {
                    when (val s = state) {
                        is LoginFlowViewModel.FlowState.Done -> {
                            if (!navigatingToMain) {
                                SessionManager.save(this@LoginActivity, s.session)
                                SessionManager.saveLastEmail(this@LoginActivity, s.session.email)
                                if (s.session.role == "cashier") {
                                    val deviceId = DeviceIdProvider.getDeviceId(this@LoginActivity)
                                    val db = AppDatabase.getInstance(this@LoginActivity)
                                    val pending = db.pendingCashboxOperationsDao()
                                        .getByDeviceAndCashier(deviceId, s.session.uid)
                                    if (pending != null && pending.status == "pending") {
                                        showPendingRecovery = true
                                    } else {
                                        navigatingToMain = true
                                        startMainActivity()
                                    }
                                } else {
                                    navigatingToMain = true
                                    startMainActivity()
                                }
                            }
                        }
                        else -> {}
                    }
                }

                if (showPendingRecovery) {
                    AlertDialog(
                        onDismissRequest = {
                            showPendingRecovery = false
                            navigatingToMain = true
                            startMainActivity()
                        },
                        title = { Text("Cierre pendiente") },
                        text = {
                            Text("Tienes un cierre de caja pendiente que no se pudo completar. ¿Deseas reintentarlo ahora?")
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                showPendingRecovery = false
                                WorkManager.getInstance(this@LoginActivity).enqueue(
                                    OneTimeWorkRequestBuilder<PendingClosureWorker>()
                                        .setConstraints(
                                            Constraints.Builder()
                                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                                .build()
                                        )
                                        .setBackoffCriteria(
                                            BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS
                                        )
                                        .build()
                                )
                                navigatingToMain = true
                                startMainActivity()
                            }) {
                                Text("Reintentar")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                showPendingRecovery = false
                                navigatingToMain = true
                                startMainActivity()
                            }) {
                                Text("Ignorar por ahora")
                            }
                        }
                    )
                }

                if (state is LoginFlowViewModel.FlowState.Login) {
                    LoginScreen(
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        initialEmail = savedEmail,
                        onLogin = { email, password -> loginVm.login(email, password) },
                        onClearError = { loginVm.clearError() }
                    )
                }
            }
        }
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

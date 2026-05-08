package cl.friendlypos.mypos

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.friendlypos.mypos.compose.screen.CashboxOpenScreen
import cl.friendlypos.mypos.compose.screen.LoginScreen
import cl.friendlypos.mypos.compose.viewmodel.CashboxViewModel
import cl.friendlypos.mypos.compose.viewmodel.LoginFlowViewModel

class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (SessionManager.isLoggedIn(this)) {
            startMainActivity()
            return
        }

        setContent {
            MaterialTheme {
                val loginVm: LoginFlowViewModel = viewModel()
                val cashboxVm: CashboxViewModel = viewModel()

                val state by loginVm.state.collectAsState()
                val isLoading by loginVm.isLoading.collectAsState()
                val errorMessage by loginVm.errorMessage.collectAsState()

                val stores by cashboxVm.stores.collectAsState()
                val cashboxes by cashboxVm.cashboxes.collectAsState()
                val cashboxLoading by cashboxVm.isLoading.collectAsState()
                val cashboxError by cashboxVm.errorMessage.collectAsState()
                val cashboxSuccess by cashboxVm.successMessage.collectAsState()

                LaunchedEffect(state) {
                    if (state is LoginFlowViewModel.FlowState.Done) {
                        val session = (state as LoginFlowViewModel.FlowState.Done).session
                        SessionManager.save(this@LoginActivity, session)
                        startMainActivity()
                    }
                }

                LaunchedEffect(cashboxSuccess) {
                    if (cashboxSuccess != null) {
                        cashboxVm.clearMessages()
                        loginVm.onCashboxOpened()
                    }
                }

                LaunchedEffect(state) {
                    if (state is LoginFlowViewModel.FlowState.CashboxOpen) {
                        cashboxVm.loadStores()
                    }
                }

                when (val s = state) {
                    is LoginFlowViewModel.FlowState.Login -> {
                        LoginScreen(
                            isLoading = isLoading,
                            errorMessage = errorMessage,
                            onLogin = { email, password -> loginVm.login(email, password) },
                            onClearError = { loginVm.clearError() }
                        )
                    }
                    is LoginFlowViewModel.FlowState.CashboxOpen -> {
                        CashboxOpenScreen(
                            storeId = s.storeId,
                            stores = stores,
                            cashboxes = cashboxes,
                            isLoading = cashboxLoading,
                            errorMessage = cashboxError,
                            onLoadCashboxes = { id -> cashboxVm.loadCashboxesForStore(id) },
                            onOpenSession = { sid, num, amt, notes ->
                                cashboxVm.openSession(sid, num, amt, notes)
                            },
                            onSessionOpened = {},
                            successMessage = null,
                            onClearMessages = {}
                        )
                    }
                    is LoginFlowViewModel.FlowState.Done -> Unit
                }
            }
        }
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

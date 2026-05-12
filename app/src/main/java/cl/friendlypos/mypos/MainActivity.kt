package cl.friendlypos.mypos

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.content.Intent
import android.util.Log

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.take
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.coordinatorlayout.widget.CoordinatorLayout

import com.google.android.material.bottomnavigation.BottomNavigationView

import cl.friendlypos.mypos.api.ApiClient
import cl.friendlypos.mypos.compose.viewmodel.CashboxViewModel
import cl.friendlypos.mypos.databinding.ActivityMainBinding
import cl.friendlypos.mypos.utils.FingerprintUtils
import cl.friendlypos.mypos.utils.SystemUtils

import kotlinx.coroutines.launch

//import com.zcs.sdk.DriverManager;
//import com.zcs.sdk.Sys;

class MainActivity : AppCompatActivity()
{
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var coordinatorLayout: CoordinatorLayout
    private lateinit var cashboxViewModel: CashboxViewModel

//    private lateinit var mDriverManager: DriverManager
//    private lateinit var mSys: Sys

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ApiClient.init(this)

        Log.d("DeviceCheck", if (SystemUtils.isEmulator()) "Emulador" else "Real Android")
        FingerprintUtils.deviceFingerprintHash(this)

//        mDriverManager = DriverManager.getInstance();
//        mSys = mDriverManager.getBaseSysDevice();
//        initSdk();

        cashboxViewModel = ViewModelProvider(this)[CashboxViewModel::class.java]

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                cashboxViewModel.currentSession.collect {
                    invalidateOptionsMenu()
                }
            }
        }

        lifecycleScope.launch {
            cashboxViewModel.hasInitialLoadCompleted
                .filter { it }
                .take(1)
                .collect {
                    val role = SessionManager.getRole(this@MainActivity)
                    val session = cashboxViewModel.currentSession.value
                    if (role == "cashier" && session == null) {
                        findNavController(R.id.nav_host_fragment_activity_main)
                            .navigate(R.id.navigation_cashbox)
                    }
                }
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        coordinatorLayout = findViewById(R.id.coordinator_layout)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.navigation_home)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    navController.navigate(R.id.navigation_home)
                    true
                }
                else -> {
                    navController.navigate(item.itemId)
                    true
                }
            }
        }

        sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
    }

    private fun initSdk() {
//        val status = mSys.sdkInit();
//        if(status != SdkResult.SDK_OK) { ... }
//        mSys.showDetailLog(true);
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.top_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val role = SessionManager.getRole(this)
        val sessionOpen = cashboxViewModel.currentSession.value?.status == "open"

        menu.findItem(R.id.action_scanner_testing)?.isVisible = role == "admin"
        menu.findItem(R.id.action_logout)?.apply {
            isVisible = role.isNotEmpty()
            title = "Cerrar sesión"
        }
        menu.findItem(R.id.action_open_cashbox)?.apply {
            isVisible = role == "cashier" && !sessionOpen
            title = "Abrir caja"
        }
        menu.findItem(R.id.action_close_cashbox)?.apply {
            isVisible = role == "cashier" && sessionOpen
            title = "Cerrar caja"
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_scanner_testing -> {
                val intent = Intent(this, ScannerActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_logout -> {
                SessionManager.clear(this)
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                true
            }
            R.id.action_open_cashbox, R.id.action_close_cashbox -> {
                findNavController(R.id.nav_host_fragment_activity_main)
                    .navigate(R.id.navigation_cashbox)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}

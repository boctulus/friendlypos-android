package cl.friendlypos.mypos

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import cl.friendlypos.mypos.databinding.ActivityMainBinding
import android.util.Log

class MainActivity : AppCompatActivity()
{
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Configurar los IDs de menú como destinos de nivel superior
        // Asegúrate de actualizar estos IDs para que coincidan con tu bottom_nav_menu.xml
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_products,
                R.id.navigation_payments,
                R.id.navigation_history
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        
        // Configurar acción especial para el botón Home (central)
        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    // Asegurarse de que se navega al fragmento Home incluso si ya está seleccionado
                    navController.navigate(R.id.navigation_home)
                    true
                }
                else -> {
                    // Comportamiento normal para otros elementos
                    navController.navigate(item.itemId)
                    true
                }
            }
        }

        // Configurar credenciales de acceso (SharedPreferences)
        sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflar el menú superior (tres puntos)
        menuInflater.inflate(R.menu.top_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                Log.d("POS", "Configuración seleccionada")
                // Implementar acción para abrir configuración
                true
            }
            R.id.action_reports -> {
                Log.d("POS", "Reportes seleccionados")
                // Implementar acción para mostrar reportes
                true
            }
            R.id.action_user -> {
                Log.d("POS", "Gestión de usuario seleccionada")
                // Implementar acción para gestión de usuario
                true
            }
            R.id.action_sync -> {
                Log.d("POS", "Sincronización seleccionada")
                // Implementar acción para sincronizar datos
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupNavigation() {
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                // R.id.navigation_dashboard,
                R.id.navigation_home,
                R.id.navigation_notifications
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }
}
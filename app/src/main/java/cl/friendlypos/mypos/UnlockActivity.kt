package cl.friendlypos.mypos

import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.util.Log

class UnlockActivity : AppCompatActivity() {

    private lateinit var tvTime: TextView
    private lateinit var tvDate: TextView
    private lateinit var sliderUnlock: SeekBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.screen_unlock)

        // Referencias
        tvTime = findViewById(R.id.tv_time)
        tvDate = findViewById(R.id.tv_date)
        sliderUnlock = findViewById(R.id.slider_unlock)

        // Actualizar hora y fecha
        val currentDateTime = Date()
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE dd MMMM", Locale.getDefault())
        tvTime.text = timeFormat.format(currentDateTime)
        tvDate.text = dateFormat.format(currentDateTime)

        // Configurar el slider
        sliderUnlock.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (progress == 100) {
                    openTill()
                    seekBar?.progress = 0 // Reiniciar el slider
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun openTill() {
        Toast.makeText(this, "Caja abierta", Toast.LENGTH_SHORT).show()
        // Lógica para abrir la caja
    }
}

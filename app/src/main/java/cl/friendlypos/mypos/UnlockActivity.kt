package cl.friendlypos.mypos

import android.os.Bundle
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class UnlockActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unlock)

        val sliderUnlock = findViewById<SeekBar>(R.id.slider_unlock)

        sliderUnlock.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (progress == 100) {
                    // Acción cuando el usuario desliza completamente
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
        // Aquí puedes agregar la lógica para abrir la caja
    }
}

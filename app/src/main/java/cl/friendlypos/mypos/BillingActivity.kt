package cl.friendlypos.mypos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import cl.friendlypos.mypos.databinding.ScreenCashfundBinding


class BillingActivity : AppCompatActivity() {

    private lateinit var rgDocumentType: RadioGroup
    private lateinit var rbDocumentoAfecto: RadioButton
    private lateinit var rbDocumentoExento: RadioButton
    private lateinit var rbFacturaAfecta: RadioButton
    private lateinit var rbFacturaExenta: RadioButton
    private lateinit var rbSinDocumento: RadioButton
    private lateinit var btnBack: ImageButton
    private lateinit var btnCancel: Button

    private var selectedDocumentType: String = "Documento afecto"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_billing)

        // Initialize views
        rgDocumentType = findViewById(R.id.rgDocumentType)
        rbDocumentoAfecto = findViewById(R.id.rbDocumentoAfecto)
        rbDocumentoExento = findViewById(R.id.rbDocumentoExento)
        rbFacturaAfecta = findViewById(R.id.rbFacturaAfecta)
        rbFacturaExenta = findViewById(R.id.rbFacturaExenta)
        rbSinDocumento = findViewById(R.id.rbSinDocumento)
        btnBack = findViewById(R.id.btnBack)
        btnCancel = findViewById(R.id.btnCancel)

        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        tvTitle.text = "Documento"

        // Get selected document type from intent
        selectedDocumentType = intent.getStringExtra("selectedDocumentType") ?: "Documento afecto"

        // Check the appropriate radio button based on the selected document type
        when (selectedDocumentType) {
            "Documento afecto" -> rbDocumentoAfecto.isChecked = true
            "Documento exento" -> rbDocumentoExento.isChecked = true
            "Factura afecta" -> rbFacturaAfecta.isChecked = true
            "Factura exenta" -> rbFacturaExenta.isChecked = true
            "Sin documento" -> rbSinDocumento.isChecked = true
        }

        // Setup radio group listener
        rgDocumentType.setOnCheckedChangeListener { _, checkedId ->
            selectedDocumentType = when (checkedId) {
                R.id.rbDocumentoAfecto -> "Documento afecto"
                R.id.rbDocumentoExento -> "Documento exento"
                R.id.rbFacturaAfecta -> "Factura afecta"
                R.id.rbFacturaExenta -> "Factura exenta"
                R.id.rbSinDocumento -> "Sin documento"
                else -> "Documento afecto"
            }
        }

        // Setup click listeners
        btnBack.setOnClickListener {
            onBackPressed()
        }

        btnCancel.setOnClickListener {
            returnSelectedDocumentType()
        }
    }

    private fun returnSelectedDocumentType() {
        val intent = Intent()
        intent.putExtra("selectedDocumentType", selectedDocumentType)
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        returnSelectedDocumentType()
    }
}
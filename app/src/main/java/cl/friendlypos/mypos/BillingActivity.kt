package cl.friendlypos.mypos

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import cl.friendlypos.mypos.compose.screen.BillingScreen

class BillingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val initialDocumentType = intent.getStringExtra("selectedDocumentType") ?: "Documento afecto"

        setContent {
            BillingScreen(
                initialDocumentType = initialDocumentType,
                onConfirm = { selectedType ->
                    returnSelectedDocumentType(selectedType)
                }
            )
        }
    }

    private fun returnSelectedDocumentType(documentType: String) {
        val intent = Intent()
        intent.putExtra("selectedDocumentType", documentType)
        setResult(RESULT_OK, intent)
        finish()
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        super.onBackPressed()
    }
}

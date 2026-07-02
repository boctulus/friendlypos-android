package cl.friendlypos.mypos

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import cl.friendlypos.mypos.checkout.DocumentType
import cl.friendlypos.mypos.compose.screen.BillingScreen

class BillingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val initialDocumentType = DocumentType.fromName(intent.getStringExtra(EXTRA_DOCUMENT_TYPE))

        setContent {
            BillingScreen(
                initialDocumentType = initialDocumentType,
                onConfirm = { selectedType ->
                    returnSelectedDocumentType(selectedType)
                }
            )
        }
    }

    private fun returnSelectedDocumentType(documentType: DocumentType) {
        val intent = Intent()
        intent.putExtra(EXTRA_DOCUMENT_TYPE, documentType.name)
        setResult(RESULT_OK, intent)
        finish()
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        super.onBackPressed()
    }

    companion object {
        const val EXTRA_DOCUMENT_TYPE = "documentTypeName"
    }
}

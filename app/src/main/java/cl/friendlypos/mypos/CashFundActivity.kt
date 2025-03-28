package cl.friendlypos.mypos

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import cl.friendlypos.mypos.databinding.ScreenCashfundBinding

class CashFundActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ScreenCashfundBinding =
            DataBindingUtil.setContentView(this, R.layout.screen_cashfund)
        // El título se maneja desde el XML
    }
}

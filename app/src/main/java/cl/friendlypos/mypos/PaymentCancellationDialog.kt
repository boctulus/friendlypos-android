package com.friendlypos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import cl.friendlypos.mypos.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PaymentCancellationDialog : DialogFragment() {

    interface OnCancelTransactionListener {
        fun onCancel()
    }

    private var cancelListener: OnCancelTransactionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, androidx.appcompat.R.style.Theme_AppCompat_Dialog_Alert)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_payment_cancellation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnNo: Button = view.findViewById(R.id.btnNo)
        val btnYes: Button = view.findViewById(R.id.btnYes)

        btnNo.setOnClickListener {
            dismiss()
        }

        btnYes.setOnClickListener {
            cancelListener?.onCancel()
            dismiss()
        }
    }

    fun setOnCancelTransactionListener(listener: OnCancelTransactionListener) {
        this.cancelListener = listener
    }
}
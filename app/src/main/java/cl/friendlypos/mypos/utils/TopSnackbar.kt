package cl.friendlypos.mypos.utils

import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import cl.friendlypos.mypos.R

class TopSnackbar {

    enum class Type {
        SUCCESS, ERROR, INFO, WARNING
    }

    companion object {
        fun make(view: View, message: String, duration: Int): Snackbar {
            // Crear Snackbar normal
            val snackbar = Snackbar.make(view, message, duration)

            // Obtener el layout del Snackbar
            val snackbarView = snackbar.view

            // Cambiar el layout para mostrarlo en la parte superior
            val params = snackbarView.layoutParams as CoordinatorLayout.LayoutParams
            params.gravity = Gravity.TOP
            snackbarView.layoutParams = params

            return snackbar
        }

        fun show(view: View, message: String, type: Type) {
            val snackbar = make(view, message, Snackbar.LENGTH_SHORT)

            // Personalizar la apariencia según el tipo
            val snackbarView = snackbar.view

            // Establecer el color de fondo según el tipo
            val backgroundColor = when (type) {
                Type.SUCCESS -> R.color.successGreen
                Type.ERROR -> R.color.errorRed
                Type.WARNING -> R.color.warningOrange
                Type.INFO -> R.color.infoBlue
            }

            snackbarView.setBackgroundColor(ContextCompat.getColor(view.context, backgroundColor))

            // Personalizar el color del texto
            val textView = snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
            textView.setTextColor(ContextCompat.getColor(view.context, R.color.white))

            snackbar.show()
        }

        // Métodos de conveniencia
        fun showSuccess(view: View, message: String) {
            show(view, message, Type.SUCCESS)
        }

        fun showError(view: View, message: String) {
            show(view, message, Type.ERROR)
        }

        fun showInfo(view: View, message: String) {
            show(view, message, Type.INFO)
        }

        fun showWarning(view: View, message: String) {
            show(view, message, Type.WARNING)
        }
    }
}
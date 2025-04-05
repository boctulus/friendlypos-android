package cl.friendlypos.mypos

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment

class SetupAccessFragment : Fragment(R.layout.fragment_setup_access) {

    private lateinit var editTextApiKey: EditText
    private lateinit var buttonSave: Button
    private lateinit var sharedPreferences: SharedPreferences

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        editTextApiKey = view.findViewById(R.id.editTextApiKey)
        buttonSave = view.findViewById(R.id.buttonSave)

        // Initialize SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

        // Retrieve the saved API key and set it to the EditText
        val apiKey = sharedPreferences.getString("API_KEY", "")
        editTextApiKey.setText(apiKey)

        // Save the API key when the button is clicked
        buttonSave.setOnClickListener {
            val newApiKey = editTextApiKey.text.toString()
            sharedPreferences.edit().putString("API_KEY", newApiKey).apply()
        }
    }
}

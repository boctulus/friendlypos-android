package cl.friendlypos.mypos

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText

class SetupAccessFragment : androidx.fragment.app.Fragment(R.layout.fragment_setup_access) {

    private lateinit var editTextApiKey: EditText
    private lateinit var buttonSave: Button
    private lateinit var buttonRetrieve: Button
    private lateinit var sharedPreferences: SharedPreferences

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        editTextApiKey = view.findViewById(R.id.editTextApiKey)
        buttonSave = view.findViewById(R.id.buttonSave)

        sharedPreferences = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

        buttonSave.setOnClickListener {
            val apiKey = editTextApiKey.text.toString()
            sharedPreferences.edit().putString("API_KEY", apiKey).apply()
        }

        buttonRetrieve.setOnClickListener {
            val apiKey = sharedPreferences.getString("API_KEY", "")
            editTextApiKey.setText(apiKey)
        }
    }
}

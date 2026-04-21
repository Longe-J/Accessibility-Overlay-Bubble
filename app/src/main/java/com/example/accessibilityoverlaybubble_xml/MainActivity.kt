package com.example.accessibilityoverlaybubble_xml


import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.accessibilityoverlaybubble_xml.data.PreferencesManager
import com.example.accessibilityoverlaybubble_xml.data.UserTone
import com.example.accessibilityoverlaybubble_xml.service.KeyboardAssistantAccessibilityService

class MainActivity : AppCompatActivity() {

    private lateinit var txtServiceStatus: TextView
    private lateinit var btnEnableAccessibility: Button
    private lateinit var radioToneGroup: RadioGroup
    private lateinit var radioCasual: RadioButton
    private lateinit var radioPolite: RadioButton
    private lateinit var radioProfessional: RadioButton
    private lateinit var editArtefactId: EditText
    private lateinit var btnSaveArtefactId: Button
    private lateinit var txtArtefactStatus: TextView

    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preferencesManager = PreferencesManager(this)

        txtServiceStatus = findViewById(R.id.txtServiceStatus)
        btnEnableAccessibility = findViewById(R.id.btnEnableAccessibility)
        radioToneGroup = findViewById(R.id.radioToneGroup)
        radioCasual = findViewById(R.id.radioCasual)
        radioPolite = findViewById(R.id.radioPolite)
        radioProfessional = findViewById(R.id.radioProfessional)
        editArtefactId = findViewById(R.id.editArtefactId)
        btnSaveArtefactId = findViewById(R.id.btnSaveArtefactId)
        txtArtefactStatus = findViewById(R.id.txtArtefactStatus)

        btnEnableAccessibility.setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        }

        btnSaveArtefactId.setOnClickListener {
            val artefactId = editArtefactId.text.toString().trim()
            preferencesManager.saveArtefactId(artefactId)
            updateArtefactStatus()
        }

        applySavedToneToUi()
        setupToneSelection()
        loadSavedArtefactId()
        updateArtefactStatus()
    }

    override fun onResume() {
        super.onResume()
        updateAccessibilityStatus()
    }

    private fun updateAccessibilityStatus() {
        val expectedComponent = ComponentName(
            this,
            KeyboardAssistantAccessibilityService::class.java
        ).flattenToString()

        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )

        val isEnabled = enabledServices
            ?.split(":")
            ?.any { it.equals(expectedComponent, ignoreCase = true) }
            ?: false

        txtServiceStatus.text = if (isEnabled) "Enabled" else "Disabled"
    }

    private fun applySavedToneToUi() {
        when (preferencesManager.getUserTone()) {
            UserTone.CASUAL -> radioCasual.isChecked = true
            UserTone.POLITE -> radioPolite.isChecked = true
            UserTone.PROFESSIONAL -> radioProfessional.isChecked = true
        }
    }

    private fun setupToneSelection() {
        radioToneGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedTone = when (checkedId) {
                R.id.radioPolite -> UserTone.POLITE
                R.id.radioProfessional -> UserTone.PROFESSIONAL
                else -> UserTone.CASUAL
            }

            preferencesManager.saveUserTone(selectedTone)
        }
    }

    private fun loadSavedArtefactId() {
        editArtefactId.setText(preferencesManager.getArtefactId())
    }

    private fun updateArtefactStatus() {
        val artefactId = preferencesManager.getArtefactId()
        txtArtefactStatus.text =
            if (artefactId.isBlank()) "No artefact ID saved"
            else "Saved artefact ID: $artefactId"
    }
}
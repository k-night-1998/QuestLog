package com.example.questlog

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

class PreferencesActivity : Activity() {

    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferences = getSharedPreferences(
            "QuestLogPreferences",
            MODE_PRIVATE
        )

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(32, 32, 32, 32)

        val title = TextView(this)
        title.text = "Quest Log Preferences"
        title.textSize = 24f

        val priorityLabel = TextView(this)
        priorityLabel.text = "Default Quest Priority"

        val defaultPriority = EditText(this)
        defaultPriority.hint = "Enter priority"
        defaultPriority.setText(
            preferences.getString("default_priority", "Medium")
        )

        val showCompleted = CheckBox(this)
        showCompleted.text = "Show completed quests"
        showCompleted.isChecked =
            preferences.getBoolean("show_completed", true)

        val saveButton = Button(this)
        saveButton.text = "Save Preferences"

        saveButton.setOnClickListener {
            preferences.edit()
                .putString(
                    "default_priority",
                    defaultPriority.text.toString()
                )
                .putBoolean(
                    "show_completed",
                    showCompleted.isChecked
                )
                .apply()

            finish()
        }

        val backButton = Button(this)
        backButton.text = "Back to Main"

        backButton.setOnClickListener {
            finish()
        }

        layout.addView(title)
        layout.addView(priorityLabel)
        layout.addView(defaultPriority)
        layout.addView(showCompleted)
        layout.addView(saveButton)
        layout.addView(backButton)

        setContentView(layout)
    }
}
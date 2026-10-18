package com.example.questlog

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class SecondaryActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val questName = intent.getStringExtra("QUEST_NAME") ?: "No quest entered"

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(32, 32, 32, 32)

        val title = TextView(this)
        title.text = "Quest Details"
        title.textSize = 24f

        val questText = TextView(this)
        questText.text = "Selected Quest: $questName"
        questText.textSize = 20f

        val backButton = Button(this)
        backButton.text = "Back to Main"

        backButton.setOnClickListener {
            finish()
        }

        layout.addView(title)
        layout.addView(questText)
        layout.addView(backButton)

        setContentView(layout)
    }
}
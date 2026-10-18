package com.example.questlog

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class HelpActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(32, 32, 32, 32)

        val title = TextView(this)
        title.text = "Quest Log Help"
        title.textSize = 24f

        val helpText = TextView(this)
        helpText.text = """
            
            About Quest Log
            
            Quest Log is an app designed to help users keep track of video game quests and games they want to play. The Main Activity allows users to enter a game or quest and view the information on a separate screen.
            
            Preferences
            
            The Preferences screen allows users to set their default quest priority and choose whether completed quests should be displayed. These settings are saved and remain available when the user returns to the app.
            
            Main Activity
            
            The Main Activity is the starting screen of the app. Users can enter a game or quest and use the buttons to view the quest, open Preferences, or view this Help screen.
            
        """.trimIndent()

        val backButton = Button(this)
        backButton.text = "Back to Main"

        backButton.setOnClickListener {
            finish()
        }

        layout.addView(title)
        layout.addView(helpText)
        layout.addView(backButton)

        setContentView(layout)
    }
}
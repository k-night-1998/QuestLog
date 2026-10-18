
package com.example.questlog

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : Activity() {

    private val preferences by lazy {
        getSharedPreferences("QuestLogGames", MODE_PRIVATE)
    }

    private val settings by lazy {
        getSharedPreferences("QuestLogPreferences", MODE_PRIVATE)
    }

    private lateinit var gameInput: EditText
    private lateinit var gameListLayout: LinearLayout

    private val priorities = listOf("Low", "Medium", "High")

    private data class SavedGame(
        val name: String,
        var priority: String = "Medium",
        var completed: Boolean = false
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        layout.addView(TextView(this).apply {
            text = "Quest Log"
            textSize = 28f
        })

        layout.addView(TextView(this).apply {
            text = "Add and manage your games or quests:"
            textSize = 16f
        })

        gameInput = EditText(this).apply {
            hint = "Game or Quest Name"
            setSingleLine(true)
        }
        layout.addView(gameInput)

        layout.addView(Button(this).apply {
            text = "Add Game / Quest"
            setOnClickListener { addGame() }
        })

        layout.addView(TextView(this).apply {
            text = "Your Saved Games and Quests"
            textSize = 20f
            setPadding(0, 20, 0, 8)
        })

        gameListLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val gameListScroll = ScrollView(this).apply {
            addView(gameListLayout)
        }

        layout.addView(
            gameListScroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        layout.addView(Button(this).apply {
            text = "Preferences"
            setOnClickListener {
                startActivity(
                    Intent(
                        this@MainActivity,
                        PreferencesActivity::class.java
                    )
                )
            }
        })

        layout.addView(Button(this).apply {
            text = "Help"
            setOnClickListener {
                startActivity(
                    Intent(this@MainActivity, HelpActivity::class.java)
                )
            }
        })

        setContentView(layout)
        refreshGameList()
    }

    // Refresh the list when returning from Preferences or another screen.
    override fun onResume() {
        super.onResume()

        if (::gameListLayout.isInitialized) {
            refreshGameList()
        }
    }

    private fun addGame() {
        val name = gameInput.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(
                this,
                "Enter a game or quest name first.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val games = loadGames()

        if (games.any { it.name.equals(name, ignoreCase = true) }) {
            Toast.makeText(
                this,
                "That game or quest is already saved.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val defaultPriority = settings
            .getString("default_priority", "Medium")
            ?.trim()
            ?.replaceFirstChar { it.uppercase() }
            ?.takeIf { it in priorities } ?: "Medium"

        games.add(
            SavedGame(
                name = name,
                priority = defaultPriority
            )
        )

        saveGames(games)
        gameInput.text.clear()
        refreshGameList()

        Toast.makeText(
            this,
            "$name saved.",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun refreshGameList() {
        gameListLayout.removeAllViews()

        val allGames = loadGames()

        // Read the setting saved by PreferencesActivity.
        val showCompleted = settings.getBoolean("show_completed", true)

        // Hide completed games without deleting their saved data.
        val visibleGames = if (showCompleted) {
            allGames
        } else {
            allGames.filterNot { it.completed }
        }

        if (visibleGames.isEmpty()) {
            gameListLayout.addView(TextView(this).apply {
                text = if (allGames.isEmpty()) {
                    "No games saved yet. Add one above to get started."
                } else {
                    "No unfinished games to display. Enable 'Show completed quests' in Preferences to see completed games."
                }

                textSize = 16f
                setPadding(0, 12, 0, 12)
            })
            return
        }

        visibleGames.forEach { game ->
            val itemLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, 8, 0, 12)
            }

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            val viewButton = Button(this).apply {
                text = game.name
                isAllCaps = false

                setOnClickListener {
                    val intent = Intent(
                        this@MainActivity,
                        SecondaryActivity::class.java
                    )

                    intent.putExtra("QUEST_NAME", game.name)
                    intent.putExtra("QUEST_PRIORITY", game.priority)
                    intent.putExtra("QUEST_COMPLETED", game.completed)

                    startActivity(intent)
                }
            }

            val removeButton = Button(this).apply {
                text = "Remove"

                setOnClickListener {
                    saveGames(
                        loadGames().filterNot {
                            it.name == game.name
                        }
                    )

                    refreshGameList()

                    Toast.makeText(
                        this@MainActivity,
                        "${game.name} removed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            row.addView(
                viewButton,
                LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )
            row.addView(removeButton)

            val priorityRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            priorityRow.addView(TextView(this).apply {
                text = "Play priority: "
                textSize = 15f
            })

            val prioritySpinner = Spinner(this).apply {
                adapter = ArrayAdapter(
                    this@MainActivity,
                    android.R.layout.simple_spinner_dropdown_item,
                    priorities
                )

                setSelection(
                    priorities.indexOf(game.priority).coerceAtLeast(0)
                )
            }

            priorityRow.addView(
                prioritySpinner,
                LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            prioritySpinner.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {

                    override fun onNothingSelected(
                        parent: AdapterView<*>?
                    ) = Unit

                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        val selectedPriority = priorities[position]

                        if (selectedPriority != game.priority) {
                            game.priority = selectedPriority
                            updateGame(game)
                        }
                    }
                }

            val completedCheckBox = CheckBox(this).apply {
                text = "Finished playing this game?"
                isChecked = game.completed

                setOnCheckedChangeListener { _, isChecked ->
                    if (game.completed != isChecked) {
                        game.completed = isChecked
                        updateGame(game)

                        // Rebuild the list immediately. If completed games
                        // are hidden, this game disappears as soon as it
                        // is marked finished.
                        refreshGameList()
                    }
                }
            }

            itemLayout.addView(row)
            itemLayout.addView(priorityRow)
            itemLayout.addView(completedCheckBox)

            gameListLayout.addView(itemLayout)
        }
    }

    private fun updateGame(updatedGame: SavedGame) {
        val games = loadGames()
        val index = games.indexOfFirst {
            it.name == updatedGame.name
        }

        if (index >= 0) {
            games[index] = updatedGame.copy()
            saveGames(games)
        }
    }

    private fun loadGames(): MutableList<SavedGame> {
        val saved = preferences.getString("saved_games", "[]") ?: "[]"

        return try {
            val json = JSONArray(saved)

            MutableList(json.length()) { index ->
                val item = json.get(index)

                if (item is JSONObject) {
                    SavedGame(
                        name = item.optString("name", "Untitled"),
                        priority = item.optString(
                            "priority",
                            "Medium"
                        ).let {
                            if (it in priorities) it else "Medium"
                        },
                        completed = item.optBoolean("completed", false)
                    )
                } else {
                    // Preserve compatibility with older saved games.
                    SavedGame(name = item.toString())
                }
            }
        } catch (_: Exception) {
            mutableListOf()
        }
    }

    private fun saveGames(games: List<SavedGame>) {
        val json = JSONArray()

        games.forEach { game ->
            json.put(JSONObject().apply {
                put("name", game.name)
                put("priority", game.priority)
                put("completed", game.completed)
            })
        }

        preferences.edit()
            .putString("saved_games", json.toString())
            .apply()
    }
}

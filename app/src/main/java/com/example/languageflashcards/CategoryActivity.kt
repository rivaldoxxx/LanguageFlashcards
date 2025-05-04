package com.example.languageflashcards

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class CategoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        val buttonAnimals = findViewById<Button>(R.id.buttonAnimals)
        val buttonFood = findViewById<Button>(R.id.buttonFood)
        val buttonObjects = findViewById<Button>(R.id.buttonObjects)

        buttonAnimals.setOnClickListener {
            openFlashcardScreen("Zwierzęta")
        }

        buttonFood.setOnClickListener {
            openFlashcardScreen("Jedzenie")
        }

        buttonObjects.setOnClickListener {
            openFlashcardScreen("Przedmioty")
        }
    }

    private fun openFlashcardScreen(category: String) {
        val intent = Intent(this, FlashcardActivity::class.java)
        intent.putExtra("CATEGORY_NAME", category)
        startActivity(intent)
    }
}

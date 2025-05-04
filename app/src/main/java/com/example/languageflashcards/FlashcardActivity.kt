package com.example.languageflashcards

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class FlashcardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flashcard)

        val category = intent.getStringExtra("CATEGORY_NAME")
        val categoryTitle = findViewById<TextView>(R.id.flashcardCategory)
        categoryTitle.text = "Kategoria: $category"
        val backButton = findViewById<Button>(R.id.backButton)
        backButton.setOnClickListener {
            finish() // zamyka tę aktywność i wraca do CategoryActivity
        }
    }
}

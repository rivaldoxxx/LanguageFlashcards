package com.example.languageflashcards

import android.net.Uri

object FlashcardRepository {

    fun getFlashcardsForCategory(category: String): List<Flashcard> {
        return when (category) {
            "Zwierzęta" -> listOf(
                Flashcard("cat"),
                Flashcard("dog"),
                Flashcard("snake"),
                Flashcard("bird")
            )
            "Jedzenie" -> listOf(
                Flashcard("apple"),
                Flashcard("cake"),
                Flashcard("bacon"),
                Flashcard("grapes")
            )
            "Przedmioty" -> listOf(
                Flashcard("pencil", isVideo = true),
                Flashcard("glasses", isVideo = true),
                Flashcard("playing_cards", isVideo = true),
                Flashcard("chair")
            )
            else -> listOf(
                Flashcard("placeholder")
            )
        }
    }

    data class Flashcard(
        val nameWithoutExtension: String,
        val isVideo: Boolean = false,
        val uri: Uri? = null
    )
}

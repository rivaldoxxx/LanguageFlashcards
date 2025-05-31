package com.example.languageflashcards

import org.junit.Assert.*
import org.junit.Test

class FlashcardRepositoryTest {

    @Test
    fun `Zwierzęta - powinny zawierać 4 fiszki`() {
        val flashcards = FlashcardRepository.getFlashcardsForCategory("Zwierzęta")
        assertEquals(4, flashcards.size)
    }

    @Test
    fun `Jedzenie - wszystkie nazwy powinny być unikalne`() {
        val flashcards = FlashcardRepository.getFlashcardsForCategory("Jedzenie")
        val names = flashcards.map { it.nameWithoutExtension }
        assertEquals(names.size, names.toSet().size)
    }

    @Test
    fun `Przedmioty - przynajmniej jedna fiszka to wideo`() {
        val flashcards = FlashcardRepository.getFlashcardsForCategory("Przedmioty")
        assertTrue(flashcards.any { it.isVideo })
    }

    @Test
    fun `Nieznana kategoria - powinna zwrócić placeholder`() {
        val flashcards = FlashcardRepository.getFlashcardsForCategory("Inna")
        assertEquals(1, flashcards.size)
        assertEquals("placeholder", flashcards.first().nameWithoutExtension)
    }

    @Test
    fun `Fiszki nie zawierają duplikatów nazw w obrębie kategorii`() {
        val categories = listOf("Zwierzęta", "Jedzenie", "Przedmioty")
        for (category in categories) {
            val flashcards = FlashcardRepository.getFlashcardsForCategory(category)
            val names = flashcards.map { it.nameWithoutExtension }
            assertEquals(names.size, names.toSet().size)
        }
    }

    @Test
    fun `Każda fiszka ma nazwę niepustą`() {
        val categories = listOf("Zwierzęta", "Jedzenie", "Przedmioty")
        for (category in categories) {
            val flashcards = FlashcardRepository.getFlashcardsForCategory(category)
            assertTrue(flashcards.all { it.nameWithoutExtension.isNotBlank() })
        }
    }
}

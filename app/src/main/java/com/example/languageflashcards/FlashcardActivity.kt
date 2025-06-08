package com.example.languageflashcards

import android.animation.ValueAnimator
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.TranslateAnimation
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class FlashcardActivity : AppCompatActivity() {

    private val numberOfFlashcards = 4
    private val videoViews = mutableListOf<VideoView>()

    private lateinit var flashcards: List<Flashcard>
    private var currentFlashcardIndex = 0
    private var correctAnswers = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flashcard)

        val category = intent.getStringExtra("CATEGORY_NAME") ?: "Zwierzęta"
        findViewById<TextView>(R.id.flashcardCategory).text = "Kategoria: $category"
        findViewById<Button>(R.id.backButton).setOnClickListener { finish() }

        flashcards = getFlashcardsForCategory(category).shuffled().take(numberOfFlashcards)
        loadFlashcard(currentFlashcardIndex)
    }

    private fun loadFlashcard(index: Int) {
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val imageSize = if (isLandscape) 250.dpToPx() else 400.dpToPx()
        val maxContainerWidth = if (isLandscape) 400.dpToPx() else 500.dpToPx()

        val container = findViewById<LinearLayout>(R.id.flashcardContainer)
        container.removeAllViews()
        container.layoutParams = FrameLayout.LayoutParams(
            maxContainerWidth,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
        }
        if (index >= flashcards.size) {
            Toast.makeText(this, "Gratulacje! Ukończyłeś wszystkie fiszki!", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val flashcard = flashcards[index]
        val correctAnswer = flashcard.nameWithoutExtension

        // Obrazek lub wideo
        if (flashcard.isVideo) {
            val videoView = VideoView(this).apply {
                layoutParams = LinearLayout.LayoutParams(imageSize, imageSize)
                setVideoURI(flashcard.uri)
                setOnPreparedListener {
                    it.isLooping = false
                }

            }

            val replayButton = Button(this).apply {
                text = "▶️ Odtwórz"
                setButtonStyle()
                setOnClickListener {
                    videoView.seekTo(0)
                    videoView.start()
                }
            }

            container.addView(videoView)
            container.addView(replayButton)
        } else {
            val imageView = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(imageSize, imageSize)
                setImageResource(flashcard.drawableResId!!)
                scaleType = ImageView.ScaleType.CENTER_CROP
            }

            container.addView(imageView)
        }

        // Odpowiedzi
        val allNames = flashcards.map { it.nameWithoutExtension }.shuffled()
        val wrongAnswers = allNames.filter { it != correctAnswer }.take(2).toMutableList()
        wrongAnswers.add(Random.nextInt(0, 3), correctAnswer)

        for (answer in wrongAnswers) {
            val textSize1 = if (isLandscape) 16f else 20f

            val button = Button(this).apply {
                text = answer
                textSize = textSize1
                setButtonStyle()

                setOnClickListener {
                    if (answer == correctAnswer) {
                        correctAnswers++
                        highlightButton(this, true) {
                            currentFlashcardIndex++
                            loadFlashcard(currentFlashcardIndex)
                        }
                    } else {
                        highlightButton(this, false)
                        shakeView(this)
                    }
                }
            }
            container.addView(button)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        loadFlashcard(currentFlashcardIndex)
    }

    // Rest of the code remains unchanged
    private fun Button.setButtonStyle() {
        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 10f // Set corner radius to 10
            setStroke(2.dpToPx(), Color.parseColor("#2E7D32")) // Set outline color to #2E7D32
            setColor(Color.parseColor("#FFFFFF"))
        }
        background = drawable
    }

    private fun scrollOrFinish(index: Int, scrollView: LockableHorizontalScrollView, container: LinearLayout) {
        if (index + 1 < container.childCount) {
            val nextCard = container.getChildAt(index + 1)
            val startX = scrollView.scrollX
            val endX = nextCard.left - 20.dpToPx()

            val animator = ValueAnimator.ofInt(startX, endX)
            animator.duration = 300
            animator.addUpdateListener { valueAnimator ->
                val scrollPos = valueAnimator.animatedValue as Int
                scrollView.scrollTo(scrollPos, 0)
            }
            animator.start()

            // Pause all videos
            videoViews.forEach { it.pause() }
        } else {
            Toast.makeText(this, "Gratulacje! Koniec fiszek.", Toast.LENGTH_SHORT).show()
            finish()
        }
        if (index + 1 >= container.childCount) {
            // If last card is reached but less than 4 correct answers, show message
            Toast.makeText(this, "Odpowiedz poprawnie na wszystkie fiszki ($correctAnswers/4).", Toast.LENGTH_SHORT).show()
        }
    }

    private fun highlightButton(button: Button, isCorrect: Boolean, onEnd: (() -> Unit)? = null) {
        val color = if (isCorrect) Color.parseColor("#A5D6A7") else Color.parseColor("#EF9A9A")
        val originalDrawable = button.background

        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 10f // Maintain corner radius
            setColor(color)
            setStroke(2.dpToPx(), Color.parseColor("#2E7D32")) // Maintain outline
        }

        button.background = drawable
        button.isEnabled = false

        button.postDelayed({
            button.setButtonStyle() // Restore styled background
            button.isEnabled = true
            onEnd?.invoke()
        }, 400)
    }

    private fun shakeView(view: View) {
        val shake = TranslateAnimation(0f, 10f, 0f, 0f).apply {
            duration = 60
            repeatCount = 3
            repeatMode = TranslateAnimation.REVERSE
        }
        view.startAnimation(shake)
    }

    data class Flashcard(
        val nameWithoutExtension: String,
        val drawableResId: Int? = null,
        val uri: Uri? = null,
        val isVideo: Boolean = false
    )

    private fun getFlashcardsForCategory(category: String): List<Flashcard> {
        return when (category) {
            "Zwierzęta" -> listOf(
                Flashcard("cat", drawableResId = R.drawable.cat),
                Flashcard("dog", drawableResId = R.drawable.dog),
                Flashcard("snake", drawableResId = R.drawable.snake),
                Flashcard("bird", drawableResId = R.drawable.bird)
            )
            "Jedzenie" -> listOf(
                Flashcard("apple", drawableResId = R.drawable.apple),
                Flashcard("cake", drawableResId = R.drawable.cake),
                Flashcard("bacon", drawableResId = R.drawable.bacon),
                Flashcard("grapes", drawableResId = R.drawable.grapes)
            )
            "Przedmioty" -> listOf(
                Flashcard(
                    nameWithoutExtension = "pencil",
                    uri = Uri.parse("android.resource://$packageName/${R.raw.pencil}"),
                    isVideo = true
                ),
                Flashcard(
                    nameWithoutExtension = "glasses",
                    uri = Uri.parse("android.resource://$packageName/${R.raw.glasses}"),
                    isVideo = true
                ),
                Flashcard(
                    nameWithoutExtension = "playing_cards",
                    uri = Uri.parse("android.resource://$packageName/${R.raw.playing_cards}"),
                    isVideo = true
                ),
                Flashcard("chair", drawableResId = R.drawable.chair)
            )
            else -> listOf(
                Flashcard("placeholder", drawableResId = R.drawable.wieloryb)
            )
        }
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density + 0.5f).toInt()
    }
}
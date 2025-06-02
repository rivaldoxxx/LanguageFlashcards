package com.example.languageflashcards

import android.animation.ValueAnimator
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flashcard)

        val category = intent.getStringExtra("CATEGORY_NAME") ?: "Zwierzęta"
        findViewById<TextView>(R.id.flashcardCategory).text = "Kategoria: $category"
        findViewById<Button>(R.id.backButton).setOnClickListener { finish() }

        val scrollView = findViewById<LockableHorizontalScrollView>(R.id.scrollView)
        val container = findViewById<LinearLayout>(R.id.carouselContainer)
        scrollView.isScrollingEnabled = false
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val cardWidth = 300.dpToPx()
        val sidePadding = (screenWidth - cardWidth) / 2

        container.setPadding(sidePadding, 0, sidePadding, 0)

        val flashcards = getFlashcardsForCategory(category).shuffled()

        for (i in 0 until numberOfFlashcards) {
            val flashcard = flashcards[i % flashcards.size]
            val correctAnswer = flashcard.nameWithoutExtension
            val allNames = flashcards.map { it.nameWithoutExtension }.shuffled()
            val wrongAnswers = allNames.filter { it != correctAnswer }.take(2).toMutableList()
            wrongAnswers.add(Random.nextInt(0, 3), correctAnswer)

            val card = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(300.dpToPx(), ViewGroup.LayoutParams.MATCH_PARENT).apply {
                    setMargins(32, 0, 32, 0)
                }
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
            }

            if (flashcard.isVideo) {
                val videoView = VideoView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(200.dpToPx(), 200.dpToPx())
                    setZOrderOnTop(true)

                    setOnPreparedListener { mp ->
                        mp.isLooping = false
                        mp.setVolume(1f, 1f)
                        start()
                        pause()
                    }

                    setOnErrorListener { _, _, _ ->
                        Toast.makeText(this@FlashcardActivity, "Nie można odtworzyć wideo", Toast.LENGTH_SHORT).show()
                        true
                    }

                    flashcard.uri?.let { setVideoURI(it) }
                }

                videoViews.add(videoView)
                card.addView(videoView)

                val replayButton = Button(this).apply {
                    text = "▶️ Odtwórz"
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply { setMargins(0, 8.dpToPx(), 0, 8.dpToPx()) }

                    setButtonStyle() // Apply custom button style

                    setOnClickListener {
                        videoView.seekTo(0)
                        videoView.start()
                    }
                }

                card.addView(replayButton)
            } else {
                val imageView = ImageView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(200.dpToPx(), 200.dpToPx())
                    setImageResource(flashcard.drawableResId!!)
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    setPadding(0, 0, 0, 16.dpToPx())
                }
                card.addView(imageView)
            }

            for (answer in wrongAnswers) {
                val button = Button(this).apply {
                    text = answer
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply { setMargins(0, 0, 0, 8.dpToPx()) }

                    setButtonStyle() // Apply custom button style

                    setOnClickListener {
                        if (text == correctAnswer) {
                            correctAnswers++ // Increment correct answers
                            highlightButton(this, true) {
                                scrollOrFinish(i, scrollView, container)
                            }
                        } else {
                            highlightButton(this, false)
                            shakeView(this)
                        }
                    }
                }
                card.addView(button)
            }

            container.addView(card)
        }

        scrollView.post {
            scrollView.scrollTo(0, 0)
        }
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
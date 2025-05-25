package com.example.languageflashcards

import android.animation.ValueAnimator
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.CycleInterpolator
import android.view.animation.TranslateAnimation
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class FlashcardActivity : AppCompatActivity() {

    private val numberOfFlashcards = 4
    private val videoViews = mutableListOf<VideoView>()
    private var currentPlayingIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flashcard)

        val category = intent.getStringExtra("CATEGORY_NAME") ?: "Zwierzęta"
        findViewById<TextView>(R.id.flashcardCategory).text = "Kategoria: $category"
        findViewById<Button>(R.id.backButton).setOnClickListener { finish() }

        val scrollView = findViewById<LockableHorizontalScrollView>(R.id.scrollView)
        val container = findViewById<LinearLayout>(R.id.carouselContainer)
        scrollView.isScrollingEnabled = false

        val flashcards = getFlashcardsForCategory(category).shuffled()

        for (i in 0 until numberOfFlashcards) {
            val flashcard = flashcards[i % flashcards.size]
            val correctAnswer = flashcard.nameWithoutExtension
            val allNames = flashcards.map { it.nameWithoutExtension }.shuffled()
            val wrongAnswers = allNames.filter { it != correctAnswer }.take(2).toMutableList()
            wrongAnswers.add(Random.nextInt(0, 3), correctAnswer)

            val card = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(300.dpToPx(), ViewGroup.LayoutParams.MATCH_PARENT).apply {
                    setMargins(16, 0, 16, 0)
                }
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
            }

            if (flashcard.isVideo) {
                val videoView = VideoView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(200.dpToPx(), 200.dpToPx())
                    visibility = View.INVISIBLE
                    setVideoURI(flashcard.uri)

                    setOnPreparedListener { mp ->
                        mp.isLooping = true
                        mp.setVolume(1f, 1f)
                        visibility = View.VISIBLE
                    }

                    setOnErrorListener { _, _, _ ->
                        Toast.makeText(context, "Nie można odtworzyć wideo", Toast.LENGTH_SHORT).show()
                        true
                    }
                }

                videoViews.add(videoView)
                card.addView(videoView)

                val replayButton = Button(this).apply {
                    text = "▶️ Odtwórz ponownie"
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply { setMargins(0, 8.dpToPx(), 0, 8.dpToPx()) }

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

                    setOnClickListener {
                        val parent = this@apply.parent as ViewGroup
                        for (j in 0 until parent.childCount) {
                            val sibling = parent.getChildAt(j)
                            if (sibling is Button) {
                                sibling.isEnabled = false
                            }
                        }

                        if (text == correctAnswer) {
                            setBackgroundColor(0xFF4CAF50.toInt()) // zielony
                            postDelayed({ scrollOrFinish(i, scrollView, container) }, 500)
                        } else {
                            setBackgroundColor(0xFFF44336.toInt()) // czerwony
                            startShakeAnimation(this)
                            postDelayed({
                                for (j in 0 until parent.childCount) {
                                    val sibling = parent.getChildAt(j)
                                    if (sibling is Button) {
                                        sibling.isEnabled = true
                                        sibling.setBackgroundResource(android.R.drawable.btn_default)
                                    }
                                }
                            }, 600)
                        }
                    }
                }
                card.addView(button)
            }

            container.addView(card)
        }

        scrollView.post {
            scrollView.scrollTo(0, 0)
            if (videoViews.isNotEmpty()) {
                videoViews[0].start()
            }
        }
    }

    private fun scrollOrFinish(index: Int, scrollView: LockableHorizontalScrollView, container: LinearLayout) {
        if (index + 1 < container.childCount) {
            val nextCard = container.getChildAt(index + 1)
            val startX = scrollView.scrollX
            val endX = nextCard.left

            val animator = ValueAnimator.ofInt(startX, endX).apply {
                duration = 400
                addUpdateListener { valueAnimator ->
                    scrollView.scrollTo(valueAnimator.animatedValue as Int, 0)
                }
            }
            animator.start()

            videoViews.getOrNull(index)?.pause()
            videoViews.getOrNull(index + 1)?.seekTo(0)
            videoViews.getOrNull(index + 1)?.start()
        } else {
            Toast.makeText(this, "Gratulacje! Koniec fiszek.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun startShakeAnimation(view: View) {
        val shake = TranslateAnimation(0f, 10f, 0f, 0f).apply {
            duration = 60
            interpolator = CycleInterpolator(5f)
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
                Flashcard("pencil", uri = Uri.parse("android.resource://com.example.languageflashcards/${R.raw.pencil}"), isVideo = true),
                Flashcard("glasses", uri = Uri.parse("android.resource://com.example.languageflashcards/${R.raw.glasses}"), isVideo = true),
                Flashcard("playing_cards", uri = Uri.parse("android.resource://com.example.languageflashcards/${R.raw.playing_cards}"), isVideo = true),
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

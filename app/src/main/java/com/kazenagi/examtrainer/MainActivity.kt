package com.kazenagi.examtrainer

import android.content.res.AssetManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var questionDatabase: QuestionDatabase
    private lateinit var questionTextView: TextView
    private lateinit var optionButtons: List<Button>
    private lateinit var imageView: ImageView
    private lateinit var classificationStatsTextView: TextView
    private lateinit var recognitionRateTextView: TextView
    private lateinit var estimatedScoreTextView: TextView
    private lateinit var questionIdTextView: TextView
    private lateinit var correctAnswerTextView: TextView
    private var currentQuestion: Question? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_main)
            Log.d("MainActivity", "Set content view successfully")
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to set content view: ${e.message}", e)
            return
        }

        try {
            questionDatabase = QuestionDatabase(this)
            Log.d("MainActivity", "QuestionDatabase initialized successfully")
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to initialize QuestionDatabase: ${e.message}", e)
            return
        }

        try {
            questionIdTextView = findViewById(R.id.question_id_text_view)
            questionTextView = findViewById(R.id.question_text_view)
            optionButtons = listOf(
                findViewById(R.id.option_a_button),
                findViewById(R.id.option_b_button),
                findViewById(R.id.option_c_button),
                findViewById(R.id.option_d_button),
                findViewById(R.id.dont_know_button)
            )
            imageView = findViewById(R.id.image_view)
            classificationStatsTextView = findViewById(R.id.classification_stats_text_view)
            recognitionRateTextView = findViewById(R.id.recognition_rate_text_view)
            estimatedScoreTextView = findViewById(R.id.estimated_score_text_view)
            correctAnswerTextView = findViewById(R.id.correct_answer_text_view)
            Log.d("MainActivity", "Views initialized successfully")
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to initialize views: ${e.message}", e)
            return
        }

        try {
            updateCategoryCounts()
            updateRecognitionRateAndScore()
            showNextQuestion()
            Log.d("MainActivity", "Initial data loaded successfully")
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to load initial data: ${e.message}", e)
            return
        }

        for (button in optionButtons) {
            button.setOnClickListener { onOptionSelected(button) }
        }
    }

    private fun showNextQuestion() {
        try {
            currentQuestion = questionDatabase.getNextQuestion()
            if (currentQuestion != null) {
                questionIdTextView.text = "题号: ${currentQuestion!!.id}"
                questionTextView.text = currentQuestion!!.questionContent
                val shuffledOptions = currentQuestion!!.options.shuffled()
                for (i in optionButtons.indices) {
                    if (i < shuffledOptions.size) {
                        optionButtons[i].text = "${'A' + i}. ${shuffledOptions[i]}"
                    } else {
                        optionButtons[i].text = "不会"
                    }
                    optionButtons[i].setBackgroundColor(Color.parseColor("#FF808080"))
                    optionButtons[i].isEnabled = true
                }
                correctAnswerTextView.text = "正确答案: ${currentQuestion!!.correctAnswer}"
                showImage()
                Log.d("MainActivity", "Next question loaded successfully")
            } else {
                questionIdTextView.text = ""
                questionTextView.text = "没有更多题目了"
                correctAnswerTextView.text = ""
                for (button in optionButtons) {
                    button.isEnabled = false
                }
                Log.d("MainActivity", "No more questions")
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to load next question: ${e.message}", e)
        }
    }

    private fun showImage() {
        try {
            val imageName = currentQuestion?.imageName
            if (imageName != null) {
                val assetManager: AssetManager = assets
                val inputStream = assetManager.open("Pic/$imageName.jpg")
                imageView.setImageBitmap(android.graphics.BitmapFactory.decodeStream(inputStream))
                imageView.visibility = View.VISIBLE
                Log.d("MainActivity", "Image loaded successfully")
            } else {
                imageView.visibility = View.GONE
                Log.d("MainActivity", "No image for this question")
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to load image: ${e.message}", e)
            imageView.visibility = View.GONE
        }
    }

    private fun onOptionSelected(selectedButton: Button) {
        try {
            val selectedAnswerIndex = optionButtons.indexOf(selectedButton)
            val correctAnswerIndex = currentQuestion!!.options.indexOf(currentQuestion!!.options[currentQuestion!!.correctAnswer[0] - 'A'])

            if (selectedAnswerIndex == correctAnswerIndex) {
                selectedButton.setBackgroundColor(Color.GREEN)
                questionDatabase.updateQuestionCategory(currentQuestion!!, getNextCategory(currentQuestion!!.category))
            } else {
                selectedButton.setBackgroundColor(Color.RED)
                optionButtons[correctAnswerIndex].setBackgroundColor(Color.GREEN)
                questionDatabase.updateQuestionCategory(currentQuestion!!, QuestionCategory.WRONG)
            }

            for (button in optionButtons) {
                button.isEnabled = false
            }

            updateCategoryCounts()
            updateRecognitionRateAndScore()

            Handler(Looper.getMainLooper()).postDelayed({
                showNextQuestion()
            }, 2000)
            Log.d("MainActivity", "Option selected and processed successfully")
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to process option selection: ${e.message}", e)
        }
    }

    private fun getNextCategory(currentCategory: QuestionCategory): QuestionCategory {
        return when (currentCategory) {
            QuestionCategory.INITIAL -> QuestionCategory.FAMILIAR
            QuestionCategory.FAMILIAR -> QuestionCategory.KNOWLEDGEABLE
            QuestionCategory.KNOWLEDGEABLE -> QuestionCategory.MASTERED
            else -> currentCategory
        }
    }

    private fun updateCategoryCounts() {
        try {
            val counts = StringBuilder()
            for (category in QuestionCategory.values()) {
                when (category) {
                    QuestionCategory.WRONG -> counts.append("错题: ${questionDatabase.getCategoryCount(category)} ")
                    QuestionCategory.INITIAL -> counts.append("初识: ${questionDatabase.getCategoryCount(category)} ")
                    QuestionCategory.FAMILIAR -> counts.append("熟悉: ${questionDatabase.getCategoryCount(category)} ")
                    QuestionCategory.KNOWLEDGEABLE -> counts.append("了解: ${questionDatabase.getCategoryCount(category)} ")
                    QuestionCategory.MASTERED -> counts.append("精通: ${questionDatabase.getCategoryCount(category)} ")
                }
            }
            classificationStatsTextView.text = counts.toString()
            Log.d("MainActivity", "Category counts updated successfully")
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to update category counts: ${e.message}", e)
        }
    }

    private fun updateRecognitionRateAndScore() {
        try {
            val totalQuestions = questionDatabase.getTotalQuestionCount()
            val knownQuestions = questionDatabase.getKnownQuestionCount()
            val recognitionRate = if (totalQuestions > 0) knownQuestions.toFloat() / totalQuestions * 100 else 0f
            val estimatedScore = recognitionRate
            recognitionRateTextView.text = "认知率: %.2f%%".format(Locale.getDefault(), recognitionRate)
            estimatedScoreTextView.text = "预估分: %.2f".format(Locale.getDefault(), estimatedScore)
            Log.d("MainActivity", "Recognition rate and score updated successfully")
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to update recognition rate and score: ${e.message}", e)
        }
    }
}
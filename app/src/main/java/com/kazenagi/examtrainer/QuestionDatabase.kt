package com.kazenagi.examtrainer

import android.content.Context
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import kotlin.random.Random

class QuestionDatabase(private val context: Context) {
    private val questions = mutableListOf<Question>()
    private val categoryMap = mutableMapOf<QuestionCategory, MutableList<Question>>()

    init {
        for (category in QuestionCategory.values()) {
            categoryMap[category] = mutableListOf()
        }
        loadQuestions()
    }

    private fun loadQuestions() {
        try {
            val inputStream = context.assets.open("questions.txt")
            val reader = BufferedReader(InputStreamReader(inputStream))
            var line: String?
            var id: String? = null
            var questionContent: String? = null
            val options = mutableListOf<String>()
            var correctAnswer: String? = null
            var imageName: String? = null

            while (reader.readLine().also { line = it } != null) {
                when {
                    line!!.startsWith("[I]") -> id = line!!.substring(3)
                    line!!.startsWith("[Q]") -> questionContent = line!!.substring(3)
                    line!!.startsWith("[A]") -> {
                        correctAnswer = "A"
                        options.add(line!!.substring(3))
                    }
                    line!!.startsWith("[B]") -> options.add(line!!.substring(3))
                    line!!.startsWith("[C]") -> options.add(line!!.substring(3))
                    line!!.startsWith("[D]") -> options.add(line!!.substring(3))
                    line!!.startsWith("[P]") -> imageName = line!!.substring(3)
                    line!!.isEmpty() -> {
                        if (id != null && questionContent != null && correctAnswer != null) {
                            val question = Question(id, questionContent, options.toList(), correctAnswer, imageName)
                            questions.add(question)
                            categoryMap[QuestionCategory.INITIAL]?.add(question)
                        }
                        options.clear()
                        id = null
                        questionContent = null
                        correctAnswer = null
                        imageName = null
                    }
                }
            }
            if (id != null && questionContent != null && correctAnswer != null) {
                val question = Question(id, questionContent, options.toList(), correctAnswer, imageName)
                questions.add(question)
                categoryMap[QuestionCategory.INITIAL]?.add(question)
            }
            reader.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun getNextQuestion(): Question? {
        for (category in listOf(QuestionCategory.WRONG, QuestionCategory.INITIAL, QuestionCategory.FAMILIAR, QuestionCategory.KNOWLEDGEABLE, QuestionCategory.MASTERED)) {
            val categoryQuestions = categoryMap[category]
            if (categoryQuestions?.isNotEmpty() == true) {
                return categoryQuestions.removeAt(Random.nextInt(categoryQuestions.size))
            }
        }
        return null
    }

    fun updateQuestionCategory(question: Question, newCategory: QuestionCategory) {
        categoryMap[question.category]?.remove(question)
        question.category = newCategory
        categoryMap[newCategory]?.add(question)
    }

    fun getCategoryCount(category: QuestionCategory): Int {
        return categoryMap[category]?.size ?: 0
    }

    fun getTotalQuestionCount(): Int {
        return questions.size
    }

    fun getKnownQuestionCount(): Int {
        return getCategoryCount(QuestionCategory.KNOWLEDGEABLE) + getCategoryCount(QuestionCategory.FAMILIAR) + getCategoryCount(QuestionCategory.MASTERED)
    }
}    
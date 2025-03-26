package com.kazenagi.examtrainer

data class Question(
    val id: String,
    val questionContent: String,
    val options: List<String>,
    val correctAnswer: String,
    val imageName: String? = null,
    var category: QuestionCategory = QuestionCategory.INITIAL
)

enum class QuestionCategory {
    WRONG, INITIAL, FAMILIAR, KNOWLEDGEABLE, MASTERED
}    
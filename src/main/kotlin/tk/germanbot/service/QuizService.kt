package tk.germanbot.service

import tk.germanbot.model.Quiz
import java.util.Optional


interface QuizService {
    fun saveQuiz(userId: String, userQuestionInput: String, userAnswersInput: String): Quiz

    fun saveQuiz(quiz: Quiz): Quiz

    fun checkAnswer(userId: String, quizId: String, answer: String): AnswerValidationResult

    fun getDefaultAnswer(quizId: String): String

    fun getQuiz(quizId: String): Optional<Quiz>

    fun getQuizzesByTopics(userId: String, topics: Set<String>, myOnly: Boolean = false): List<Quiz>

    fun selectQuizzesForUser(userId: String, topics: Set<String>, totalQuestions: Int): List<Quiz>

    fun getTopicsToQuizCount(userId: String) :Map<String, Int>
}
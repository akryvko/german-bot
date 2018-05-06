package tk.germanbot.service

import tk.germanbot.data.QuizEntity
import java.util.Random

interface QuizService {
    fun saveQuiz(userId: String, quiz: QuizEntity): QuizEntity

    fun saveQuiz(userId: String, quiz: String, answer: String): QuizEntity

    fun checkAnswer(userId: String, quizId: String, answer: String): AnswerValidationResult

    fun getAnswer(quizId: String): String

    fun getQuiz(questionId: String): QuizEntity

    fun getQuizzesByTopics(userId: String, topics: Set<String>, myOnly: Boolean = false): List<QuizEntity>

    fun selectQuizzesForUser(userId: String, topics: Set<String>, totalQuestions: Int): List<String>

    fun getTopicsToQuizCount(userId: String) :Map<String, Int>
}

enum class Correctness(private val answers: Array<String>) {
    CORRECT(arrayOf(
            "Correct! \uD83D\uDC4D",
            "Yes! Well done! \uD83D\uDC4D",
            "Absolutely right! \uD83D\uDC4D",
            "Cool! Have a cookie! \uD83C\uDF6A"
    )),
    PARTIALLY_CORRECT(arrayOf(
            "Not quite. Expected answer:",
            "Almost there. Expected answer:",
            "Getting there. Expected answer:",
            "You were so close! Expected answer:"
    )),
    INCORRECT(arrayOf(
            "Better luck next time! Expected answer:",
            "Missed it! Expected answer:",
            ";-( Not quite. Expected answer:"
    ));

    private val r = Random(0)

    fun getAnswer(correctAnswer: String): String {
        val bound = this.answers.size
        val index = r.nextInt(bound)
        return this.answers[index] + if (correctAnswer.isNotBlank()) " *${correctAnswer}*" else ""
    }

}

class AnswerValidationResult(
        val result: Correctness,
        val correctAnswer: String = "")

package tk.germanbot.service

import org.springframework.beans.factory.annotation.Autowired
import tk.germanbot.data.QuizEntity
import tk.germanbot.data.QuizTopic
import tk.germanbot.data.es.EsQuizRepository
import tk.germanbot.model.Quiz
import tk.germanbot.model.QuizAnswer
import java.util.Optional


class QuizService2Impl(
        @Autowired val quizRepository: EsQuizRepository,
        @Autowired private val quizValidator: QuizValidator,
        @Autowired private val statService: UserStatService
) : QuizService2 {

    override fun saveQuiz(userId: String, userQuestionInput: String, userAnswersInput: String): Quiz {
        val (question, topics) = QuestionString.parse(userQuestionInput);

        val answers = userAnswersInput.split("+")
                .map(::removeFormatting)
                .map(String::trim)
                .filter(String::isNotBlank)
                .mapIndexed { idx, answer -> QuizAnswer(answer, idx == 0) }
                .toSet()

        val quiz = Quiz(
                createdBy = userId,
                question = removeFormatting(question),
                answers = answers,
                topics = topics
        )

        quiz.validate()
        quizRepository.saveQuiz(quiz)
        return quiz
    }

    override fun getQuiz(quizId: String): Optional<Quiz> {
        return quizRepository.getQuiz(quizId)
    }

    override fun getDefaultAnswer(quizId: String): String {
        return quizRepository.getQuiz(quizId)
                .map { quiz ->
                    quiz.answers.first { a -> a.isDefault }
                }
                .map(QuizAnswer::content)
                .orElseThrow { EntityNotFoundException(Quiz::class, quizId) }
    }

    override fun checkAnswer(userId: String, quizId: String, answer: String): AnswerValidationResult {
        val quiz = quizRepository.getQuiz(quizId)
                .orElseThrow { EntityNotFoundException(QuizEntity::class, quizId) }
        val validationResult = quizValidator.validate(answer, quiz.answers.map(QuizAnswer::content).toSet())

        statService.updateQuizStat(userId, quizId, quiz.topics, validationResult.result != Correctness.INCORRECT)

        return validationResult

    }

    override fun getQuizzesByTopics(userId: String, topics: Set<String>, myOnly: Boolean): List<Quiz> {
        return if (myOnly)
            quizRepository.findByUserAndTopics(userId, topics)
        else
            quizRepository.findByTopics(topics)
    }

    override fun selectQuizzesForUser(userId: String, topics: Set<String>, totalQuestions: Int): List<String> {
        TODO("not implemented")
    }

    override fun getTopicsToQuizCount(userId: String): Map<String, Int> {
        TODO("not implemented")
    }

    private fun removeFormatting(str: String): String {
        return str.replace(Regex("\\*|_|`"), "")
    }

    private data class QuestionString(
            val question: String,
            val topics: Set<String>) {

        companion object {
            fun parse(questionStr: String): QuestionString {
                val topicRegex = Regex("#([\\w-\\.]+)")

                val topics = topicRegex.findAll(questionStr)
                        .map { it.groupValues[1] }
                        .filter(String::isNotBlank)
                        .toSet()
                        .let { if (it.isEmpty()) setOf(QuizTopic.UNDEFINED) else it }

                val q = topicRegex.replace(questionStr, "").trim()

                return QuestionString(q, topics)
            }
        }
    }

}
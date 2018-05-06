package tk.germanbot.activity.lesson

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import tk.germanbot.MessengerConfig
import tk.germanbot.activity.Activity
import tk.germanbot.activity.ActivityData
import tk.germanbot.activity.ActivityManager
import tk.germanbot.activity.Event
import tk.germanbot.activity.UserCommand
import tk.germanbot.activity.UserTextMessageEvent
import tk.germanbot.service.Correctness
import tk.germanbot.service.HintService
import tk.germanbot.service.MessageGateway
import tk.germanbot.service.QuizService
import java.util.UUID

data class QuizActivityData(
        override var userId: String = "",
        var quizId: String = "") : ActivityData {
    override var id: String = UUID.randomUUID().toString()
    var result: Correctness = Correctness.INCORRECT
    var isCancelled = false
    var correctAnswers: Set<String> = emptySet()
    var example: String? = null
    var lastHint: String? = null
    var hintCount: Int = 0
}

@Component
class QuizActivity(
        @Autowired val activityManager: ActivityManager,
        @Autowired override val messageGateway: MessageGateway,
        @Autowired val quizService: QuizService,
        @Autowired val hintService: HintService
) : Activity<QuizActivityData>() {

    private val logger = LoggerFactory.getLogger(MessengerConfig::class.java)

    override val helpText = "Answer the question above or:\n" +
            "#h - get hint, #end - end lesson"

    override fun onStart(data: QuizActivityData) {
        val quizOpt = quizService.getQuiz(data.quizId)

        if (!quizOpt.isPresent) {
            logger.error("Quiz with id {} not found!", data.quizId)
            return
        }

        val quiz = quizOpt.get()

        data.correctAnswers = quiz.answersContent.toSet()
        data.example = quiz.example
        messageGateway.textMessage(data.userId, "❓ ${quiz.question!!}")
    }

    override fun onEvent(event: Event, data: QuizActivityData): Boolean {
        if (event !is UserTextMessageEvent) {
            return false
        }

        if (UserCommand.END.eq(event.message)) {
            data.isCancelled = true
            activityManager.endActivity(this, data)
            return true
        }

        if (UserCommand.HINT.eq(event.message)) {
            val correctAnswer = data.correctAnswers.iterator().next()
            data.hintCount++
            data.lastHint = hintService.hint(correctAnswer, data.hintCount)
            if (correctAnswer == data.lastHint) {
                messageGateway.textMessage(data.userId, "Here's the answer: *$correctAnswer*")
                data.result = Correctness.INCORRECT
                activityManager.endActivity(this, data)
            } else {
                messageGateway.textMessage(data.userId, "\uD83D\uDCA1 ${data.lastHint.toString()}")
            }
            return true
        }

        val valuation = quizService.checkAnswer(data.userId, data.quizId, event.message)
        messageGateway.textMessage(data.userId, valuation.result.getAnswer(valuation.correctAnswer))

        if (data.example != null) {
            val highlightedExample =
                    if (!data.example!!.contains("*"))
                        data.example!!.replace(data.correctAnswers.first(), "*${data.correctAnswers.first()}*", true)
                    else
                        data.example!!
            messageGateway.textMessage(data.userId, "For example: ${highlightedExample}")
        }

        data.result = valuation.result
        activityManager.endActivity(this, data)
        return true
    }
}
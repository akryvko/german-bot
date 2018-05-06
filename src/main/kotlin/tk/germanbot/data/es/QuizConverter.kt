package tk.germanbot.data.es

import com.google.common.base.Converter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import tk.germanbot.model.Quiz
import tk.germanbot.model.QuizAnswer

@Component
class QuizConverter(
        @Autowired val answerConverter: QuizAnswerConverter
) : Converter<Quiz, QuizDoc>() {

    private val answerDocConverter = answerConverter.reverse()

    override fun doForward(q: Quiz): QuizDoc = QuizDoc(
            id = q.id,
            createdDate = q.createdDate,
            createdBy = q.createdBy,
            organizationId = q.organizationId,

            question = q.question,
            answers = q.answers.map(answerConverter::convert).toSet(),
            example = q.example,

            topics = q.topics,
            isPublished = q.isPublished,
            gender = q.gender,
            level = q.level
    )

    override fun doBackward(qd: QuizDoc): Quiz = Quiz(
            id = qd.id,
            createdDate = qd.createdDate,
            createdBy = qd.createdBy,
            organizationId = qd.organizationId,

            question = qd.question,
            answers = qd.answers.map(answerDocConverter::convert).toSet(),
            example = qd.example,

            topics = qd.topics,
            isPublished = qd.isPublished,
            gender = qd.gender,
            level = qd.level
    )
}

@Component
class QuizAnswerConverter : Converter<QuizAnswer, QuizAnswerNestedDoc>() {
    override fun doForward(a: QuizAnswer): QuizAnswerNestedDoc = QuizAnswerNestedDoc(
            content = a.content,
            isDefault = a.isDefault
    )

    override fun doBackward(ad: QuizAnswerNestedDoc): QuizAnswer = QuizAnswer(
            content = ad.content,
            isDefault = ad.isDefault
    )
}

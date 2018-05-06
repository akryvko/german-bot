package tk.germanbot.model

import tk.germanbot.data.es.QuizDoc
import tk.germanbot.service.EntityValidationException
import java.time.Instant
import java.util.UUID

enum class Gender {
    M, F, N, None
}

enum class QuizLevel {
    A1, A2, B1, B2, C1, C2
}

data class Quiz(
        val id: String = UUID.randomUUID().toString(),

        val createdDate: Instant = Instant.now(),

        val createdBy: String,

        val organizationId: String? = null,

        val question: String,

        val answers: Set<QuizAnswer>,

        val example: String? = null,

        val topics: Set<String>,

        val level: QuizLevel = QuizLevel.A1,

        val gender: Gender = Gender.None,

        val isPublished: Boolean = false

) {

    val answersContent: List<String>
        get() = answers
                .sortedByDescending(QuizAnswer::isDefault)
                .map(QuizAnswer::content)

    fun validate() {
        if (question.isBlank()) throw EntityValidationException(QuizDoc::class, "Blank question for quiz $id")
        if (createdBy.isBlank()) throw EntityValidationException(QuizDoc::class, "CreatedBy is null in $id")
        if (answers.isEmpty()) throw EntityValidationException(QuizDoc::class, "No answers for quiz $id")
        if (topics.isEmpty()) throw EntityValidationException(QuizDoc::class, "No topics for quiz $id")
    }
}

data class QuizAnswer(
        val content: String,
        val isDefault: Boolean = false
) {

    companion object {
        fun fromStrings(answers: Iterable<String>): Set<QuizAnswer> =
                answers.mapIndexed { idx, a -> QuizAnswer(a, idx == 0) }.toSet()
    }

}

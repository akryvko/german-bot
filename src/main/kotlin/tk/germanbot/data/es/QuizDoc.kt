package tk.germanbot.data.es

import com.google.gson.annotations.JsonAdapter
import tk.germanbot.model.Gender
import tk.germanbot.model.QuizLevel
import java.time.Instant
import java.util.UUID

data class QuizDoc(
        val id: String = UUID.randomUUID().toString(),

        @JsonAdapter(InstantConverter::class)
        val createdDate: Instant = Instant.now(),

        val createdBy: String,

        val organizationId: String? = null,

        val question: String,

        val answers: Set<QuizAnswerNestedDoc>,

        val example: String? = null,

        val topics: Set<String> = setOf(),

        val level: QuizLevel = QuizLevel.A1,

        val gender: Gender = Gender.None,

        val isPublished: Boolean = false

)

data class QuizAnswerNestedDoc(
        val content: String,
        val isDefault: Boolean = false
)

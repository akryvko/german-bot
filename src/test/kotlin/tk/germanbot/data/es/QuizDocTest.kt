package tk.germanbot.data.es

import com.google.gson.Gson
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import tk.germanbot.model.Gender
import tk.germanbot.model.QuizLevel
import java.time.Instant

class QuizDocTest{

    companion object {
        val aDate = Instant.parse("2011-01-01T23:33:44Z")!!
    }

    @Test
    fun `can serialize QuizDoc`(){
        val qDoc = QuizDoc(
                id="id",
                createdDate = aDate,
                createdBy = "createdBy",
                question = "question",
                answers = setOf(
                        QuizAnswerNestedDoc("answer1", true), QuizAnswerNestedDoc("answer2")),
                organizationId = "organizationId",
                example = "example",
                topics = setOf("topic1", "topic2"),
                level = QuizLevel.A2,
                gender = Gender.F,
                isPublished = true
        )

        val json = Gson().toJson(qDoc)
        print(json)
        assertThat(json).isNotBlank()
    }

    @Test
    fun `can deserialize QuizDoc`(){
        val json = "{\"id\":\"id\",\"createdDate\":\"2011-01-01T23:33:44Z\",\"createdBy\":\"createdBy\"," +
                "\"organizationId\":\"organizationId\",\"question\":\"question\"," +
                "\"answers\":[{\"content\":\"answer1\",\"isDefault\":true},{\"content\":\"answer2\",\"isDefault\":false}]," +
                "\"example\":\"example\",\"topics\":[\"topic1\",\"topic2\"],\"level\":\"A2\"," +
                "\"gender\":\"F\",\"isPublished\":true}"

        val qDoc = Gson().fromJson(json, QuizDoc::class.java)
        assertThat(qDoc).isNotNull()
        assertThat(qDoc.id).isEqualTo("id")
        assertThat(qDoc.createdDate).isEqualTo(aDate)
        assertThat(qDoc.createdBy).isEqualTo("createdBy")
        assertThat(qDoc.answers).containsExactly(QuizAnswerNestedDoc("answer1", true), QuizAnswerNestedDoc("answer2"))
        assertThat(qDoc.topics).containsExactly("topic1", "topic2")
    }

}
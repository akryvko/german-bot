package tk.germanbot.data.es

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.web.WebAppConfiguration
import pl.allegro.tech.embeddedelasticsearch.EmbeddedElastic
import tk.germanbot.Application
import tk.germanbot.IntegrationTestsConfig
import tk.germanbot.model.Quiz
import tk.germanbot.model.QuizAnswer

@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest(classes = arrayOf(Application::class))
@WebAppConfiguration
@Import(IntegrationTestsConfig::class)
@ActiveProfiles("test")
class EsQuizRepositoryTest {

    @Autowired
    var embeddedElastic: EmbeddedElastic? = null

    @Autowired
    var repo: EsQuizRepository? = null

    @Test
    fun saveGetQuiz() {
        repo!!.saveQuiz(Quiz(
                id = "q1",
                createdBy = "me",
                question = "What?",
                topics = setOf("top1", "top2"),
                answers = setOf(QuizAnswer("!", true))
        ))

        embeddedElastic!!.refreshIndices()

        val quiz = repo!!.getQuiz("q1")

        assertThat(quiz.isPresent).isTrue()
        quiz.get().let { q ->
            assertThat(q.id).isEqualTo("q1")
            assertThat(q.createdBy).isEqualTo("me")
            assertThat(q.question).isEqualTo("What?")
            assertThat(q.topics).containsExactly("top1", "top2")
            assertThat(q.answers).containsExactly(QuizAnswer("!", true))
        }
    }

    @Test
    fun findByTopics() {
        repo!!.saveQuiz(Quiz(
                id = "q1",
                createdBy = "me",
                question = "What?",
                topics = setOf("top1", "top2"),
                answers = setOf(QuizAnswer("!", true))
        ))

        repo!!.saveQuiz(Quiz(
                id = "q2",
                createdBy = "them",
                question = "What?",
                topics = setOf("top1", "top3"),
                answers = setOf(QuizAnswer("!", true))
        ))

        repo!!.saveQuiz(Quiz(
                id = "q3",
                createdBy = "them",
                question = "What?",
                topics = setOf("top3"),
                answers = setOf(QuizAnswer("!", true))
        ))

        embeddedElastic!!.refreshIndices()

        val quizTop1 = repo!!.findByTopics(setOf("top1"))
        assertThat(quizTop1).isNotEmpty()
        assertThat(quizTop1.map(Quiz::id)).containsExactlyInAnyOrder("q1", "q2")

        val quizTop13 = repo!!.findByTopics(setOf("top1", "top3"))
        assertThat(quizTop13).isNotEmpty()
        assertThat(quizTop13.map(Quiz::id)).containsExactlyInAnyOrder("q2")

        val quizTop4 = repo!!.findByTopics(setOf("top4"))
        assertThat(quizTop4).isEmpty()

        val quizTop1My = repo!!.findByUserAndTopics("them", setOf("top1"))
        assertThat(quizTop1My).isNotEmpty()
        assertThat(quizTop1My.map(Quiz::id)).containsExactlyInAnyOrder("q2")
    }

}
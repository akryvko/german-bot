package tk.germanbot.service

import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.mock
import tk.germanbot.data.es.EsQuizRepository
import tk.germanbot.model.Quiz
import tk.germanbot.model.QuizAnswer

class QuizService2ImplTest {

    private val repo = mock(EsQuizRepository::class.java)
    private val quizValidator = mock(QuizValidator::class.java)
    private val statService = mock(UserStatService::class.java)

    private val service = QuizService2Impl(repo, quizValidator, statService)

    @Test
    fun saveQuiz() {
        // when
        service.saveQuiz("user1", " Question *one* #topic1 #A2.1-12 ", "Answer `one` +   Answer *two* ")

        // then
        val quizCaptor = argumentCaptor<Quiz>()

        verify(repo).saveQuiz(quizCaptor.capture())

        assertThat(quizCaptor.firstValue).isNotNull();
        quizCaptor.firstValue.let { quiz ->
            assertThat(quiz.question).isEqualTo("Question one")
            assertThat(quiz.topics).containsExactly("topic1", "A2.1-12")
            assertThat(quiz.answers).containsExactly(QuizAnswer("Answer one", true), QuizAnswer("Answer two"))
        }

        assertThat(quizValidator).isNotNull();
        assertThat(statService).isNotNull();
    }
}
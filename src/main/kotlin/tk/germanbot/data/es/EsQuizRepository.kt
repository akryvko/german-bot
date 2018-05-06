package tk.germanbot.data.es

import com.google.gson.Gson
import org.elasticsearch.ElasticsearchException
import org.elasticsearch.action.get.GetRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders
import org.elasticsearch.rest.RestStatus
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import tk.germanbot.EsProperties
import tk.germanbot.model.Quiz
import java.io.InputStreamReader
import java.util.Optional
import java.util.Random


@Component
class EsQuizRepository(
        @Autowired val elasticProps: EsProperties,
        @Autowired val client: RestHighLevelClient,
        @Autowired val quizConverter: QuizConverter,
        @Autowired val gson: Gson) {

    companion object {
        val MAX_QUIZZES = 1000
    }

    private val quizDocConverter = quizConverter.reverse()

    fun saveQuiz(quiz: Quiz) {
        val quizDoc = quizConverter.convert(quiz)
        val quizDocJson = gson.toJson(quizDoc)
        client.index(
                IndexRequest(elasticProps.quizIndexName, "_doc", quiz.id)
                        .source(quizDocJson, XContentType.JSON))
    }

    fun getQuiz(quizId: String): Optional<Quiz> {
        val response = client.get(
                GetRequest(elasticProps.quizIndexName).id(quizId))

        return if (response.isExists && !response.isSourceEmpty)
            Optional.of(
                    quizDocConverter.convert(
                            InputStreamReader(response.sourceAsBytesRef.streamInput()).use { reader ->
                                gson.fromJson(reader, QuizDoc::class.java)
                            }))
        else
            Optional.empty()

    }

    fun findByUserAndTopics(userId: String, topics: Set<String>): List<Quiz> {
        val searchSourceBuilder = SearchSourceBuilder()
        searchSourceBuilder.from(0)
        searchSourceBuilder.size(MAX_QUIZZES)
        searchSourceBuilder.sort("createdDate")
        searchSourceBuilder.query(
                addTermsFilters(QueryBuilders.boolQuery(), topics)
                        .filter(
                                QueryBuilders.termQuery("createdBy", userId))
        )

        val searchRequest = SearchRequest(elasticProps.quizIndexName)
        searchRequest.source(searchSourceBuilder)

        val response = client.search(searchRequest)
        return extractQuizzHits(response)
    }

    fun findByTopics(topics: Set<String>): List<Quiz> {
        val searchSourceBuilder = SearchSourceBuilder()
        searchSourceBuilder.from(0)
        searchSourceBuilder.size(MAX_QUIZZES)
        searchSourceBuilder.sort("createdDate")
        searchSourceBuilder.query(
                addTermsFilters(QueryBuilders.boolQuery(), topics)
        )

        val searchRequest = SearchRequest(elasticProps.quizIndexName)
        searchRequest.source(searchSourceBuilder)
        val response = client.search(searchRequest)

        return extractQuizzHits(response)
    }

    private fun addTermsFilters(boolQuery: BoolQueryBuilder, topics: Set<String>): BoolQueryBuilder {
        for (topic in topics) {
            boolQuery.filter(
                    QueryBuilders.termQuery("topics", topic)
            )
        }
        return boolQuery;
    }

    private fun addTermsQuery(boolQuery: BoolQueryBuilder, topics: Set<String>): BoolQueryBuilder {
        for (topic in topics) {
            boolQuery.must(
                    QueryBuilders.termQuery("topics", topic)
            )
        }
        return boolQuery;
    }

    private fun extractQuizzHits(response: SearchResponse): List<Quiz> {
        if (response.status() != RestStatus.OK) {
            throw ElasticsearchException("Got error status from Elastic: ", response.status())
        }

        return response.hits
                .map { hit ->
                    InputStreamReader(hit.sourceRef.streamInput()).use { reader ->
                        gson.fromJson(reader, QuizDoc::class.java)
                    }
                }
                .map(quizDocConverter::convert)
    }

    fun findRandomByUserAndTopics(userId: String, topics: Set<String>, totalQuestions: Int): List<Quiz> {
        val searchSourceBuilder = SearchSourceBuilder()
        searchSourceBuilder.from(0)
        searchSourceBuilder.size(totalQuestions)
        searchSourceBuilder.query(
                QueryBuilders.functionScoreQuery(
                        addTermsQuery(QueryBuilders.boolQuery(), topics)
                                .must(
                                        QueryBuilders.termQuery("createdBy", userId)),
                        ScoreFunctionBuilders.randomFunction().seed(Random().nextLong()))
        )

        val searchRequest = SearchRequest(elasticProps.quizIndexName)
        searchRequest.source(searchSourceBuilder)

        val response = client.search(searchRequest)
        return extractQuizzHits(response)
    }

}

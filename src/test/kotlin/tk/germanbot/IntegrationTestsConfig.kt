package tk.germanbot

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.local.embedded.DynamoDBEmbedded
import com.google.gson.Gson
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile
import pl.allegro.tech.embeddedelasticsearch.EmbeddedElastic
import pl.allegro.tech.embeddedelasticsearch.PopularProperties
import java.lang.ClassLoader.getSystemResourceAsStream

@Configuration
@Profile("test")
@EnableConfigurationProperties(EsProperties::class)
@Import(ConsoleConfig::class)
class IntegrationTestsConfig(
        @Autowired val elasticProps: EsProperties) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    @Bean
    fun amazonDynamoDB(): AmazonDynamoDB = DynamoDBEmbedded.create().amazonDynamoDB()

    @Bean
    fun mapper(@Autowired db: AmazonDynamoDB): DynamoDBMapper = DynamoDBMapper(db)

    @Bean
    fun awsProperties(): AwsProperties = AwsProperties()


    @Bean
    fun gson(): Gson {
        return Gson()
    }

    @Bean
    fun elasticsearchClient(): RestHighLevelClient {
        return RestHighLevelClient(
                RestClient.builder(HttpHost("localhost", elasticProps.port, "http")))
    }

    @Bean
    fun embeddedElastic(): EmbeddedElastic {
        val quizScript = getSystemResourceAsStream("scripts/es_quiz_index.json")

        logger.info("Starting embedded Elasticsearch port: {}, quizIndex: {}", elasticProps.port, elasticProps.quizIndexName)

        return EmbeddedElastic.builder()
                .withElasticVersion("6.2.4")
                .withSetting(PopularProperties.HTTP_PORT, elasticProps.port)
                .withSetting(PopularProperties.CLUSTER_NAME, "bot_test_cluster")
                .withTemplate("quiz_*", quizScript)
                .withIndex(elasticProps.quizIndexName)
                .build()
                .start()
    }

}

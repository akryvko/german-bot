package tk.germanbot

import com.google.gson.Gson
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile


@ConfigurationProperties(prefix = "elasticsearch")
class EsProperties {
    var host: String = "localhost"
    var port: Int = 9200
    var quizIndexName: String = "quiz"
}

@Configuration
@EnableConfigurationProperties(EsProperties::class)
@Profile("!test")
class EsConfig(@Autowired val props: EsProperties) {

    @Bean
    fun gson() : Gson {
        return Gson()
    }

    @Bean
    fun elasticsearchClient() : RestHighLevelClient {
        return RestHighLevelClient(
                RestClient.builder(HttpHost(props.host, props.port, "http")))
    }

}
package net.xeraa.integration_test_demo;

import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.main.MainResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import pl.allegro.tech.embeddedelasticsearch.EmbeddedElastic;
import pl.allegro.tech.embeddedelasticsearch.PopularProperties;

import java.io.IOException;
import java.util.Properties;

import static java.util.concurrent.TimeUnit.SECONDS;

public class EmbeddedTest extends ParentTest {

    private static final Logger logger = LogManager.getLogger(EmbeddedTest.class.getName());
    private static EmbeddedElastic embeddedElastic;

    @BeforeClass
    public static void startElasticsearchRestClient() throws IOException, InterruptedException {
        final int testClusterPort = 9200;
        final String testClusterHost = "localhost";
        final String testClusterScheme = "http";

        // Get the Elasticsearch version from the POM
        Properties properties = new Properties();
        properties.load(EmbeddedTest.class.getClassLoader()
                .getResourceAsStream("elasticsearch.version.properties"));
        String elasticsearchVersion = properties.getProperty("version");

        // Start the Elasticsearch process
        logger.info("Start an embedded instance with version {}", elasticsearchVersion);
        embeddedElastic = EmbeddedElastic.builder()
                .withElasticVersion(elasticsearchVersion)
                .withSetting(PopularProperties.HTTP_PORT, testClusterPort)
                .withSetting(PopularProperties.CLUSTER_NAME, "elasticsearch")
                .withEsJavaOpts("-Xms512m -Xmx512m")
                .withStartTimeout(60, SECONDS)
                .build();
        embeddedElastic.start();
        logger.info("Embedded Elasticsearch instance started");

        // Start a client
        logger.info("Starting a client on {}://{}:{}",testClusterScheme, testClusterHost, testClusterPort);
        RestClientBuilder builder = getClientBuilder(new HttpHost(testClusterHost, testClusterPort, testClusterScheme));
        client = new RestHighLevelClient(builder);
        MainResponse info = client.info(RequestOptions.DEFAULT.toBuilder().build());
        logger.info("Client is running against an Elasticsearch cluster {}", info.getVersion().toString());
    }

    @AfterClass
    public static void stopElasticsearchRestClient() throws IOException {
        if(client != null) {
            logger.info("Closing Elasticsearch client");
            client.close();
        }
        if(embeddedElastic != null){
            logger.info("Shutting down embedded Elasticsearch");
            embeddedElastic.stop();
        }
    }
}

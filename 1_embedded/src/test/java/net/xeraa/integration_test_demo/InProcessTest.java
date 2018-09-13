package net.xeraa.integration_test_demo;

import org.apache.http.HttpHost;
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
import java.util.logging.Logger;

import static java.util.concurrent.TimeUnit.SECONDS;

public class InProcessTest  extends ParentTest {

    private static final Logger logger = Logger.getLogger(InProcessTest.class.getName());
    private static EmbeddedElastic embeddedElastic;

    @BeforeClass
    public static void startElasticsearchRestClient() throws IOException, InterruptedException {
        int testClusterPort = Integer.parseInt(System.getProperty("tests.cluster.port", "9200"));
        String testClusterHost = System.getProperty("tests.cluster.host", "localhost");
        String testClusterScheme = System.getProperty("tests.cluster.scheme", "http");

        logger.info("Starting a client on " + testClusterScheme + "://" + testClusterHost + ":" + testClusterPort);

        // Start the Elasticsearch process
        Properties properties = new Properties();
        properties.load(InProcessTest.class.getClassLoader().getResourceAsStream("elasticsearch.version.properties"));
        String elasticsearchVersion = properties.getProperty("version");
        logger.info("No node running — we need to start an embedded instance with version " + elasticsearchVersion);
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
        RestClientBuilder builder = getClientBuilder(new HttpHost(testClusterHost, testClusterPort, testClusterScheme));
        client = new RestHighLevelClient(builder);

        // Make sure the cluster is running
        MainResponse info = client.info(RequestOptions.DEFAULT.toBuilder().build());
        logger.info("Client is running against an Elasticsearch cluster " + info.getVersion().toString());
    }

    @AfterClass
    public static void stopElasticsearchRestClient() throws IOException {
        if (client != null) {
            logger.info("Closing Elasticsearch client");
            client.close();
            logger.info("Shutting down embedded Elasticsearch");
            embeddedElastic.stop();
        }
    }
}

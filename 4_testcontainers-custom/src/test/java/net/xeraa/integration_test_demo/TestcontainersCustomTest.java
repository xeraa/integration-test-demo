package net.xeraa.integration_test_demo;

import fr.pilato.elasticsearch.containers.ElasticsearchContainer;
import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.main.MainResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.testcontainers.containers.wait.HttpWaitStrategy;

import java.io.IOException;
import java.time.Duration;
import java.util.Properties;

public class TestcontainersCustomTest extends ParentTest {

    private static final Logger logger = LogManager.getLogger(TestcontainersCustomTest.class.getName());
    private static ElasticsearchContainer container;

    @BeforeClass
    public static void startElasticsearchRestClient() throws IOException {

        // Get the Elasticsearch version from the POM
        Properties properties = new Properties();
        properties.load(TestcontainersCustomTest.class.getClassLoader()
                .getResourceAsStream("elasticsearch.version.properties"));
        String elasticsearchVersion = properties.getProperty("version");

        // Start the Elasticsearch process
        logger.info("Start an Elasticsearch Testcontainer with version {}", elasticsearchVersion);
        container = new ElasticsearchContainer().withVersion(elasticsearchVersion);
        container.setWaitStrategy(
                new HttpWaitStrategy()
                        .forStatusCode(200)
                        .withStartupTimeout(Duration.ofSeconds(60)));
        container.start();
        logger.info("Docker instance started");
        final String testClusterHost = container.getHost().getHostName();
        final int testClusterPort = container.getFirstMappedPort();
        final String testClusterScheme = System.getProperty("tests.cluster.scheme", "http");

        // Build the Elasticsearch High Level Client based on the parameters
        logger.info("Starting a client on {}://{}:{}",testClusterScheme, testClusterHost, testClusterPort);
        RestClientBuilder builder = RestClient.builder(new HttpHost(testClusterHost, testClusterPort, testClusterScheme));
        client = new RestHighLevelClient(builder);
        MainResponse info = client.info(RequestOptions.DEFAULT.toBuilder().build());
        logger.info("Client is running against an Elasticsearch cluster " + info.getVersion().toString());
    }

    @AfterClass
    public static void stopElasticsearchRestClient() throws IOException {
        if (client != null) {
            logger.info("Closing Elasticsearch client");
            client.close();
        }
        if (container != null) {
            logger.info("Stopping Docker instance");
            container.close();
        }
    }
}

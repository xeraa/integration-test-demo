package net.xeraa.integration_test_demo;

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
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.io.IOException;
import java.time.Duration;

public class TestcontainersGeneralTest  extends ParentTest {

    private static final Logger logger = LogManager.getLogger(TestcontainersGeneralTest.class.getName());
    private static DockerComposeContainer container;

    @BeforeClass
    public static void startElasticsearchRestClient() throws IOException {
        final int testClusterPort = Integer.parseInt(System.getProperty("tests.cluster.port", "9200"));
        final String testClusterHost = System.getProperty("tests.cluster.host", "localhost");
        final String testClusterScheme = System.getProperty("tests.cluster.scheme", "http");

        // Start the Elasticsearch process
        logger.info("Start Elasticsearch with Docker Compose");
        container = new DockerComposeContainer(new File("src/test/resources/docker-compose.yml"))
                .withExposedService("elasticsearch_1", testClusterPort,
                        Wait.forHttp("/").forStatusCode(200).withStartupTimeout(Duration.ofSeconds(60)));
        container.start();
        logger.info("Docker Compose instance started");

        // Start a client
        logger.info("Starting a client on {}://{}:{}",testClusterScheme, testClusterHost, testClusterPort);
        RestClientBuilder builder = getClientBuilder(new HttpHost(testClusterHost, testClusterPort, testClusterScheme));
        client = new RestHighLevelClient(builder);
        MainResponse info = client.info(RequestOptions.DEFAULT.toBuilder().build());
        logger.info("Client is running against an Elasticsearch cluster {}", info.getVersion().toString());
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

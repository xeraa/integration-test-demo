package net.xeraa.integration_test_demo;

import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.MainResponse;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;

public class TestcontainersGeneralTest extends ParentTest {

    private static final Logger logger = LogManager.getLogger(TestcontainersGeneralTest.class.getName());
    private static DockerComposeContainer container;

    @BeforeClass
    public static void startElasticsearchRestClient() throws IOException {
        final int TEST_CLUSTER_PORT = 9200;
        final String TEST_CLUSTER_HOST = "localhost";
        final String TEST_CLUSTER_SCHEME = "http";

        // Get the Elasticsearch version from the POM
        Properties properties = new Properties();
        properties.load(TestcontainersGeneralTest.class.getClassLoader()
                .getResourceAsStream("elasticsearch.configuration.properties"));
        String elasticsearchVersion = properties.getProperty("version");

        // Start the Elasticsearch process
        logger.info("Start Elasticsearch with Docker Compose");
        container = new DockerComposeContainer(new File("src/test/resources/docker-compose.yml"))
                .withEnv("ELASTIC_VERSION", elasticsearchVersion)
                .withExposedService("elasticsearch_1", TEST_CLUSTER_PORT,
                        Wait.forHttp("/").forStatusCode(200).withStartupTimeout(Duration.ofSeconds(90)));
        container.start();
        logger.info("Docker Compose instance started");

        // Start a client
        logger.info("Starting a client on {}://{}:{}",TEST_CLUSTER_SCHEME, TEST_CLUSTER_HOST, TEST_CLUSTER_PORT);
        RestClientBuilder builder =
                getClientBuilder(new HttpHost(TEST_CLUSTER_HOST, TEST_CLUSTER_PORT, TEST_CLUSTER_SCHEME));
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

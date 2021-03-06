package net.xeraa.integration_test_demo;

import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.MainResponse;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Properties;

import static java.util.Collections.singletonMap;

public class TestcontainersCustomTest extends ParentTest {

    private static final Logger logger = LogManager.getLogger(TestcontainersCustomTest.class.getName());
    private static ElasticsearchContainer container;
    private static final Instant startTime = Instant.now();

    @BeforeClass
    public static void startElasticsearchRestClient() throws IOException {

        // Get the Elasticsearch version from the POM
        Properties properties = new Properties();
        properties.load(TestcontainersCustomTest.class.getClassLoader()
                .getResourceAsStream("elasticsearch.version.properties"));
        String elasticsearchVersion = properties.getProperty("version");

        // Start the Elasticsearch process
        logger.info("Start an Elasticsearch Testcontainer with version {}", elasticsearchVersion);
        container = new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:"
                + elasticsearchVersion)
                .withStartupTimeout(Duration.ofSeconds(90))
                .withEnv("ES_JAVA_OPTS", "-Xms256m -Xmx256m")
                .withEnv("bootstrap.memory_lock", "true")
                .withTmpFs(singletonMap("/usr/share/elasticsearch/data", "rw")); //No change anything with a single doc
        container.start();
        logger.info("Docker instance started");
        final String TEST_CLUSTER_URL = container.getHttpHostAddress();

        // Build the Elasticsearch High Level Client based on the parameters
        logger.info("Starting a client on {}", TEST_CLUSTER_URL);
        RestClientBuilder builder = RestClient.builder(HttpHost.create(TEST_CLUSTER_URL));
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

        // Calculate the time of the full run to see if heap size or TempFS change anything
        logger.info("Time of the full test: " + Duration.between(startTime, Instant.now()));
    }
}

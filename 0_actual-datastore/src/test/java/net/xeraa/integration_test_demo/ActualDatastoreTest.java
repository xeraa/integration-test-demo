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

import java.io.IOException;

import static junit.framework.TestCase.fail;

public class ActualDatastoreTest extends ParentTest {

    private static final Logger logger = LogManager.getLogger(ParentTest.class);

    @BeforeClass
    public static void startElasticsearchRestClient(){
        final int TEST_CLUSTER_PORT =
                Integer.parseInt(System.getProperty("tests.elasticsearch.port", "9200"));
        final String TEST_CLUSTER_HOST = System.getProperty("tests.elasticsearch.host", "localhost");
        final String TEST_CLUSTER_SCHEME = System.getProperty("tests.elasticsearch.scheme", "http");

        // Start a client
        logger.info("Starting a client on {}://{}:{}",TEST_CLUSTER_SCHEME, TEST_CLUSTER_HOST, TEST_CLUSTER_PORT);
        RestClientBuilder builder =
                getClientBuilder(new HttpHost(TEST_CLUSTER_HOST, TEST_CLUSTER_PORT, TEST_CLUSTER_SCHEME));
        try {
            client = new RestHighLevelClient(builder);
            MainResponse info = client.info(RequestOptions.DEFAULT.toBuilder().build());
            logger.info("Client is running against an Elasticsearch cluster {}", info.getVersion().toString());
        } catch(IOException e){
            logger.error("No node running — you need to manage this manually", e);
            fail();
        }
    }

    @AfterClass
    public static void stopElasticsearchRestClient() throws IOException {
        if (client != null) {
            logger.info("Closing Elasticsearch client");
            client.close();
        }
    }
}

package net.xeraa.integration_test_demo;

import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.main.MainResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.logging.Logger;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class MavenTest {

    private static final Logger logger = Logger.getLogger(MavenTest.class.getName());
    private static RestHighLevelClient client;
    private static final String INDEX = "maven";

    @BeforeClass
    public static void startElasticsearchRestClient() throws IOException {
        int testClusterPort = Integer.parseInt(System.getProperty("tests.cluster.port", "9200"));
        String testClusterHost = System.getProperty("tests.cluster.host", "localhost");
        String testClusterScheme = System.getProperty("tests.cluster.scheme", "http");

        logger.info("Starting a client on " + testClusterScheme + "://" + testClusterHost + ":" + testClusterPort);

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
        }
    }

    private static RestClientBuilder getClientBuilder(HttpHost host) {
        return RestClient.builder(host);
    }

    @Test
    public void test() throws IOException {
        RequestOptions requestOptions = RequestOptions.DEFAULT.toBuilder().build();

        // Remove any existing index
        try {
            logger.info("-> Removing index " + INDEX);
            client.indices().delete(new DeleteIndexRequest(INDEX), requestOptions);
        } catch (ElasticsearchStatusException e) {
            assertThat(e.status().getStatus(), is(404));
        }

        // Create a new index
        logger.info("-> Creating index " + INDEX);
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(INDEX);
        createIndexRequest.settings(Settings.builder()
                .put("index.number_of_shards", 1)
                .put("index.number_of_replicas", 0)
        );
        client.indices().create(createIndexRequest, requestOptions);

        // Index some documents
        logger.info("-> Indexing one document in " + INDEX);
        IndexResponse indexResponse = client.index(new IndexRequest(INDEX, "_doc").source(
                jsonBuilder()
                        .startObject()
                        .field("name", "Philipp")
                        .endObject()
        ).setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE), requestOptions);
        logger.info("-> Document indexed with _id " + indexResponse.getId());

        // Search
        SearchResponse searchResponse = client.search(new SearchRequest(INDEX), requestOptions);
        logger.info(searchResponse.toString());
        assertThat(searchResponse.getHits().totalHits, is(1L));
    }
}

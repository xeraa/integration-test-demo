package net.xeraa.integration_test_demo;

import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.fail;

public abstract class ParentTest {

    private static final Logger logger = LogManager.getLogger(ParentTest.class.getName());
    protected static RestHighLevelClient client;
    private static final String ELASTICSEARCH_INDEX = "test-index";

    protected static RestClientBuilder getClientBuilder(HttpHost host) {
        return RestClient.builder(host);
    }

    @Test
    public void test() throws IOException {
        RequestOptions requestOptions = RequestOptions.DEFAULT.toBuilder().build();

        // Remove any existing index
        try {
            logger.info("-> Removing index {}", ELASTICSEARCH_INDEX);
            client.indices().delete(new DeleteIndexRequest(ELASTICSEARCH_INDEX), requestOptions);
            fail("There should be no index " + ELASTICSEARCH_INDEX + " yet");
        } catch (ElasticsearchStatusException e) {
            assertThat(e.status().getStatus(), equalTo(404));
        }

        // Create a new index
        logger.info("-> Creating index {}", ELASTICSEARCH_INDEX);
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(ELASTICSEARCH_INDEX);
        createIndexRequest.settings(Settings.builder()
                .put("index.number_of_shards", 1)
                .put("index.number_of_replicas", 0)
        );
        client.indices().create(createIndexRequest, requestOptions);

        // Index a document
        logger.info("-> Indexing one document in {}", ELASTICSEARCH_INDEX);
        IndexResponse indexResponse = client.index(new IndexRequest(ELASTICSEARCH_INDEX).source(
                jsonBuilder()
                        .startObject()
                        .field("name", "Philipp")
                        .endObject()
        ).setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE), requestOptions);
        logger.info("-> Document indexed with _id {}", indexResponse.getId());

        // Check if the index exists
        GetIndexRequest indexRequest = new GetIndexRequest("*");
        GetIndexResponse fetchedIndices = client.indices().get(indexRequest, RequestOptions.DEFAULT);
        assertThat(Arrays.toString(fetchedIndices.getIndices()), equalTo("[" + ELASTICSEARCH_INDEX + "]"));

        // Check if the document is really there
        SearchResponse searchResponse = client.search(new SearchRequest(ELASTICSEARCH_INDEX), requestOptions);
        logger.info(searchResponse.toString());
        assertThat(searchResponse.getHits().iterator().next().getId(), equalTo(indexResponse.getId()));

        // Clean up at the end
        client.indices().delete(new DeleteIndexRequest(ELASTICSEARCH_INDEX), requestOptions);
    }
}

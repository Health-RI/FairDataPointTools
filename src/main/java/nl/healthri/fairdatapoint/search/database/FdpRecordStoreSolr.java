package nl.healthri.fairdatapoint.search.database;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.UpdateResponse;

import java.io.IOException;
import java.util.List;

public class FdpRecordStoreSolr extends FdpRecordStoreBatches {

    private static final int BatchSize = 1000;
    private final String SOLR_URL = "http://localhost:8983/solr/mycore";
    private final SolrClient solrClient;

    public FdpRecordStoreSolr() throws SolrServerException, IOException {
        super(1000);
        solrClient = new HttpSolrClient.Builder(SOLR_URL).build();
        UpdateResponse response = solrClient.deleteByQuery("*:*");
        solrClient.commit();
    }

    @Override
    public void close() throws Exception {
        if (solrClient != null) {
            solrClient.close();
        }
    }

    @Override
    void store(List<FdpRecord> batch) {

    }
}

package com.redhat.acs.exporter;

import java.util.List;

import com.redhat.acs.exporter.client.AcsApiClient;
import com.redhat.acs.exporter.parser.NetGraphParser;
import com.redhat.acs.exporter.parser.NetGraphParser.NetGraphData;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

public class AcsApiClientTest {
    final static Logger log = LogManager.getLogger(AcsApiClientTest.class);

    final private static String BASE_URL = "changeme";
    final private static String CREDENTIALS = "changeme";
    final private static String CLUSTER_ID = "changeme";
    final private static String NAMESPACE = "changeme";
    
    @Test
    public void getNetworkGraphTest() {
        try {
            AcsApiClient client = new AcsApiClient(BASE_URL, CREDENTIALS);
            String json = client.getNetworkGraph(CLUSTER_ID, NAMESPACE);
        
            List<NetGraphData> netGraphs = new NetGraphParser().parse(json);

            netGraphs.stream().forEach(h -> System.out.println(h.toCsv()));

            Assert.assertNotNull(netGraphs);
        } catch(Exception e) {
            log.error("error on getNetworkGraphTest: ", e);
        }
    }
}

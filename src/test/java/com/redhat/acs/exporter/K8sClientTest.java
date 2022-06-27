package com.redhat.acs.exporter;

import java.util.Arrays;
import java.util.List;

import com.redhat.acs.exporter.client.K8sClient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.junit.Assert;
import org.junit.Test;

public class K8sClientTest {
    final static Logger log = LogManager.getLogger(K8sClientTest.class);

    String url = "https://<hostname>:<port>";
    String user = "changeme";
    String pass = "changeme";

    K8sClient client = new K8sClient(url, user, pass);
    
    @Test
    public void getIdTest() {
        try {
            String id = client.getId();

            System.out.println("clusterID: " + id);
            
            Assert.assertEquals(id, "6ee0d2be-750b-420b-8c08-9ee01c40c133");
        } catch(Exception e) {
            log.error("error on getIdTest: ", e);
        }
    }

    @Test
    public void getNamespacesTest() {
        try {
            String nsPattern = "das.*";
            List<String> nss = client.getNamespaces(nsPattern);
            
            nss.stream()
                .forEach(ns -> System.out.println("found namespace: "+ns));

            Assert.assertEquals(nss, Arrays.asList("das","das-corporate", "dashboard-gp-affluent"));
        } catch(Exception e) {
            log.error("error on getNamespacesTest: ", e);
        }
    }
}

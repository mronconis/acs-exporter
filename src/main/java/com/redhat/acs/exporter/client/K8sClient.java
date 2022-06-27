package com.redhat.acs.exporter.client;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.redhat.acs.exporter.ApplicationProperties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.fabric8.kubernetes.api.model.NamespaceList;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;

import io.fabric8.openshift.api.model.ClusterVersion;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;

public class K8sClient implements AutoCloseable {

    private final static Logger log = LogManager.getLogger(K8sClient.class);

    final private OpenShiftClient client;

    final private static int REQUEST_TIMEOUT = 30000;

    public K8sClient(String url, String authToken) {
        Config config = new ConfigBuilder()
                .withMasterUrl(url)
                .withOauthToken(authToken)
                .withRequestTimeout(REQUEST_TIMEOUT)
                .build();
        this.client = new DefaultOpenShiftClient(config);
    }

    public K8sClient(String url, String username, String password) {
        Config config = new ConfigBuilder()
                .withMasterUrl(url)
                .withUsername(username)
                .withPassword(password)
                .withRequestTimeout(REQUEST_TIMEOUT)
                .build();
        this.client = new DefaultOpenShiftClient(config);
    }

    public List<String> getNamespaces() {
        return getNamespaces(ns -> {return true;});
    }
    
    public List<String> getNamespaces(String nsPattern) {
        if (nsPattern == null)
            return getNamespaces();
            
        return getNamespaces(ns -> {
            return Pattern.compile(nsPattern).matcher(ns).matches();
        });
    }

    public List<String> getNamespaces(Predicate<String> nsPredicate) {
        log.debug("GET: namespaces");

        NamespaceList nss = client.namespaces().list();
        Pattern defaultNsFilterPattern = Pattern.compile(ApplicationProperties.getInstance().get("default.namespace.filter"));

        return nss.getItems().stream()
            .map(ns -> ns.getMetadata().getName())
            .filter(ns -> !defaultNsFilterPattern.matcher(ns).matches())
            .filter(nsPredicate)
            .collect(Collectors.toList());
    }

    public String getId() {
        log.debug("GET: clusterId");

        ClusterVersion clusterVersion = client.config().clusterVersions().list().getItems().get(0);
        return clusterVersion.getSpec().getClusterID();
    }

    public void close() {
        client.close();
    }
}

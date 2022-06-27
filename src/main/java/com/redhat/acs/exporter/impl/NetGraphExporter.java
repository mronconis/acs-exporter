package com.redhat.acs.exporter.impl;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;

import com.redhat.acs.exporter.Exporter;
import com.redhat.acs.exporter.client.AcsApiClient;
import com.redhat.acs.exporter.client.K8sClient;
import com.redhat.acs.exporter.parser.NetGraphParser;
import com.redhat.acs.exporter.parser.NetGraphParser.NetGraphData;

import org.apache.commons.cli.CommandLine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

public class NetGraphExporter implements Exporter {

    final static Logger log = LogManager.getLogger(NetGraphExporter.class);

    private NetGraphParser parser = new NetGraphParser();
    
    @Override
    public Optional<Set<NetGraphData>> export(CommandLine cmd, JSONObject acsCfg, JSONObject clusterCfg) throws Exception {

        log.info("Analize cluster {} [env={}]", clusterCfg.getString("name"), clusterCfg.getString("env"));
        
        Set<NetGraphData> netGraphs = new TreeSet<>();
        
        String url = clusterCfg.getString("url");
        String user = cmd.getOptionValue("user");
        String passwd = cmd.getOptionValue("passwd");
        String ns = cmd.getOptionValue("namespace");
        
        try (K8sClient k8sClient = new K8sClient(url, user, passwd)) {
            k8sClient.getNamespaces(ns)
                .forEach(exportNS(acsCfg, clusterCfg, netGraphs));   
        }

        return Optional.of(netGraphs);
    }

    public Consumer<String> exportNS(JSONObject acsCfg, JSONObject clusterCfg, Set<NetGraphData> netGraphs) {
        
        return namespace -> {   
            log.info("Analize namespace {}", namespace);

            String acsUrl = acsCfg.getString("url");
            String acsCredentials = acsCfg.getString("credentials");
            String clusterId = clusterCfg.getString("id");

            try (AcsApiClient acsClient = new AcsApiClient(acsUrl, acsCredentials)) {
                String response = acsClient.getNetworkGraph(clusterId, namespace);
                List<NetGraphData> tmp = parser.parse(response);
                netGraphs.addAll(tmp);
            } catch (Exception e) {
                throw new RuntimeException("Error on get network graph: ", e);
            }
        };
    }
}

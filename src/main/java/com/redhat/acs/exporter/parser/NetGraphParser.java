package com.redhat.acs.exporter.parser;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import lombok.Data;

public class NetGraphParser {

    public List<NetGraphData> parse(String data) {
        JSONObject document = new JSONObject(data);
        JSONArray nodes = document.getJSONArray("nodes");

        List<NetGraphData> netGraphs = new ArrayList<NetGraphData>();

        for (int index = 0; index < nodes.length(); index++) {
            JSONObject node = (JSONObject) nodes.get(index);
            JSONObject entity = (JSONObject) node.get("entity");

            if (entity.has("deployment"))
                parseDeployment(entity, index, node, nodes, netGraphs);
        }
        
        return netGraphs;
    }

    private void parseDeployment(JSONObject entity, int index, JSONObject node, JSONArray nodes, List<NetGraphData> netGraphs) {
        boolean queryMatch = (boolean) node.getBoolean("queryMatch");
        JSONObject deployment = (JSONObject) entity.get("deployment");
        
        // out edges
        JSONObject outEdges = (JSONObject) node.get("outEdges");
        if (outEdges != null) {
            for (String key : outEdges.keySet()) {
                JSONObject outEdge = (JSONObject) outEdges.get(key);
                JSONArray properties = outEdge.getJSONArray("properties");

                JSONObject toNode = (JSONObject) nodes.get(Integer.valueOf(key));
                JSONObject toEntity = (JSONObject) toNode.get("entity");

                String toName = null, toNamespace = null;
                if (toEntity.has("deployment")) {
                    JSONObject toDeployment = (JSONObject) toEntity.get("deployment");
                    toName = toDeployment.getString("name");
                    toNamespace = toDeployment.getString("namespace");
                } else {
                    toName = toEntity.getString("type");
                }

                for (int k = 0; k < properties.length(); k++) {
                    JSONObject prop = (JSONObject) properties.get(k);
                    
                    NetGraphData netGraph = new NetGraphData();

                    netGraph.setKind("DEPLOYMENT");
                    netGraph.setPort(String .valueOf(prop.get("port")) + 
                        (prop.getString("protocol").endsWith("TCP") ? "/TCP" : "/UDP"));

                    if (queryMatch) {
                        netGraph.setName(deployment.getString("name"));
                        netGraph.setNamespace(deployment.getString("namespace"));
                        netGraph.setFlowDirection("OUTBOUND");
                        netGraph.setEName(toName);
                        netGraph.setENamespace(toNamespace);
                    } else {
                        netGraph.setName(toName);
                        netGraph.setNamespace(toNamespace);
                        netGraph.setFlowDirection("INBOUND");
                        netGraph.setEName(deployment.getString("name"));
                        netGraph.setENamespace(deployment.getString("namespace"));
                    }

                    netGraphs.add(netGraph);
                }
            }
        }

        // in edges
        JSONObject inEdges = findInternetEdges(nodes);
        if (inEdges != null) {
            for (String key : inEdges.keySet()) {
                if (key.equals(String.valueOf(index))) {
                    
                    JSONObject inEdge = (JSONObject) inEdges.get(key);
                    JSONArray properties = inEdge.getJSONArray("properties");

                    for (int k = 0; k < properties.length(); k++) {
                        JSONObject prop = (JSONObject) properties.get(k);

                        NetGraphData netGraph = new NetGraphData();

                        netGraph.setKind(entity.getString("type"));
                        netGraph.setName(deployment.getString("name"));
                        netGraph.setNamespace(deployment.getString("namespace"));
                        netGraph.setFlowDirection("INBOUND");
                        netGraph.setEName("INTERNET");
                        netGraph.setPort(String .valueOf(prop.get("port")) + 
                            (prop.getString("protocol").endsWith("TCP") ? "/TCP" : "/UDP"));
                    
                        netGraphs.add(netGraph);
                    }
                }
            }
        }
    }

    private JSONObject findInternetEdges(JSONArray nodes) {
        for (int i = 0; i < nodes.length(); i++) {
            JSONObject node = (JSONObject) nodes.get(i);
            JSONObject entity = (JSONObject) node.get("entity");

            String type = entity.getString("type");

            if (type.endsWith("INTERNET"))
                return (JSONObject) node.get("outEdges");
        }
        return null;
    }

    @Data
    public class NetGraphData implements Comparable {
        private String kind;
        private String namespace;
        private String name;
        private String flowDirection;
        private String eNamespace;
        private String eName;
        private String port;

        public String toCsv() {
            return namespace + ", " + kind + ", " + name + ",   "  + flowDirection + ", " + eNamespace + ", " + eName + ", " + port;
        }

        @Override
        public int compareTo(Object o) {
            NetGraphData data = (NetGraphData) o;
            return Comparator
                .comparing(NetGraphData::getKind)
                .thenComparing(NetGraphData::getNamespace)
                .thenComparing(NetGraphData::getName)
                .thenComparing(NetGraphData::getFlowDirection)
                .thenComparing(NetGraphData::getENamespace, Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparing(NetGraphData::getEName)
                .thenComparing(NetGraphData::getPort)
                .compare(this, data);
        }
    }
}

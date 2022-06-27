package com.redhat.acs.exporter.decorator;

import java.util.Optional;
import java.util.Set;

import com.redhat.acs.exporter.Exporter;
import com.redhat.acs.exporter.parser.NetGraphParser.NetGraphData;

import org.apache.commons.cli.CommandLine;
import org.json.JSONObject;

public class NetGraphCsvDecorator extends ExporterDecorator {

    public NetGraphCsvDecorator(Exporter exporter) {
        super(exporter);
    }
    
    public Optional<String> export(CommandLine cmd, JSONObject acsCfg, JSONObject clusterCfg) throws Exception {
        Optional<Set<NetGraphData>> netGraphDatas = super.export(cmd, acsCfg, clusterCfg);
        String csv = exportAsCsv(netGraphDatas.get());
        System.out.println(csv);
        return Optional.of(csv);
    }
    
    private String exportAsCsv(Set<NetGraphData> netGraphDatas) {
       StringBuilder sb = new StringBuilder();
        for (NetGraphData netGraphData : netGraphDatas) {
            sb.append(netGraphData.toCsv()).append("\n");
        }
        return sb.toString();
    } 
}

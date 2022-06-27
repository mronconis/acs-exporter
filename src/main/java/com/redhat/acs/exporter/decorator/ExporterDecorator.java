package com.redhat.acs.exporter.decorator;

import java.util.Optional;

import com.redhat.acs.exporter.Exporter;

import org.apache.commons.cli.CommandLine;
import org.json.JSONObject;

public abstract class ExporterDecorator implements Exporter {
    private Exporter exporter;
    
    public ExporterDecorator(Exporter exporter) {
        this.exporter = exporter;
    }

    @Override
    public <T> Optional<T> export(CommandLine cmd, JSONObject acsCfg, JSONObject clusterCfg) throws Exception {
        return exporter.export(cmd, acsCfg, clusterCfg);
    }
}

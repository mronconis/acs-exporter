package com.redhat.acs.exporter;

import java.util.Optional;

import org.apache.commons.cli.CommandLine;
import org.json.JSONObject;

public interface Exporter {
    public <T> Optional<T> export(CommandLine cmd, JSONObject acsCfg, JSONObject clusterCfg) throws Exception;
}

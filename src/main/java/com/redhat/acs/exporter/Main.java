package com.redhat.acs.exporter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.redhat.acs.exporter.decorator.NetGraphExcelDecorator;
import com.redhat.acs.exporter.impl.NetGraphExporter;

import org.apache.commons.cli.*;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class Main {
    final static Logger log = LogManager.getLogger(Main.class);

    public static void main(String[] args) throws ParseException {

        Options options = new Options();

        Option exporterConfigOption = Option.builder("c").longOpt("config")
                .argName("config")
                .hasArg()
                .required(true)
                .desc("Set exporter config file").build();
        options.addOption(exporterConfigOption);

        Option exporterOutputOption = Option.builder("o").longOpt("output")
                .argName("output")
                .hasArg()
                .required(true)
                .desc("Set exporter output dir").build();
        options.addOption(exporterOutputOption);

        Option ocpUsernameOption = Option.builder("u").longOpt("user")
                .argName("user")
                .hasArg()
                .required(true)
                .desc("Set OCP username").build();
        options.addOption(ocpUsernameOption);

        Option ocpPasswordOption = Option.builder("p").longOpt("passwd")
                .argName("passwd")
                .hasArg()
                .required(true)
                .desc("Set OCP password").build();
        options.addOption(ocpPasswordOption);

        Option ocpClusterEnvOption = Option.builder("e").longOpt("env")
                .argName("env")
                .hasArg()
                .required(false)
                .desc("Set OCP cluster environment").build();
        options.addOption(ocpClusterEnvOption);

        Option ocpNamespaceOption = Option.builder("n").longOpt("namespace")
                .argName("n")
                .hasArg()
                .required(false)
                .desc("Set OCP namespace").build();
        options.addOption(ocpNamespaceOption);

        Option verboseOption = Option.builder("v").longOpt("verbose")
                .argName("v")
                .required(false)
                .desc("Set verbose mode").build();
        options.addOption(verboseOption);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter helper = new HelpFormatter();

        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("verbose"))
                Configurator.setAllLevels("com.redhat.acs.exporter", Level.DEBUG);

            run(cmd);

            System.exit(1);

        } catch (ParseException e) {
            log.warn(e.getMessage());
            helper.printHelp("Usage:", options);
            System.exit(0);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            System.exit(0);
        }
    }

    private static void run(CommandLine cmd) throws IOException {

        JSONObject config = readConfig(cmd.getOptionValue("config"));
        JSONObject acsCfg = config.getJSONObject("acs");
        JSONArray clusters = config.getJSONArray("clusters");

        clusters.forEach(c -> {
            JSONObject clusterCfg = (JSONObject) c;
            
            String env = cmd.getOptionValue("env");
            
            if (null == env || clusterCfg.getString("env").equalsIgnoreCase(env)) {
                Exporter exporter = new NetGraphExcelDecorator(new NetGraphExporter());
                
                try {
                    exporter.export(cmd, acsCfg, clusterCfg);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private static JSONObject readConfig(String config) throws IOException {
        InputStream is = new FileInputStream(config);
        String json = IOUtils.toString(is, "UTF-8");
        return new JSONObject(json);
    }
}
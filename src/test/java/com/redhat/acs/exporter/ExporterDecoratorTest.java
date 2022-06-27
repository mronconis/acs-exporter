package com.redhat.acs.exporter;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.redhat.acs.exporter.decorator.NetGraphCsvDecorator;
import com.redhat.acs.exporter.decorator.NetGraphExcelDecorator;
import com.redhat.acs.exporter.parser.NetGraphParser;
import com.redhat.acs.exporter.parser.NetGraphParser.NetGraphData;

import org.apache.commons.cli.CommandLine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

public class ExporterDecoratorTest {

    final static Logger log = LogManager.getLogger(ExporterDecoratorTest.class);

    Map<String, String> args = new HashMap<>(); 
    {
        args.put("config", "");
        args.put("output", "/Users/mronconi");

        args.put("user", "changeme");
        args.put("passwd", "changeme");
        
        args.put("namespace", "das.*");
        args.put("env", "svil");
    }

    CommandLine cmd = new CommandLine() {
        @Override
        public String getOptionValue(String opt) {
            return args.get(opt);
        }
    };

    /*
    @Test
    public void exportCsvTest() {
        try {
            String json = new String(
                Files.readAllBytes(
                    Paths.get(NetGraphTest.class.getResource("/response.json").toURI())
                ), "UTF-8");
    
            Exporter exporter = new NetGraphCsvDecorator(new Exporter() {
                @Override 
                public Optional<Set<NetGraphData>> export(CommandLine cmd) throws Exception {
                    List<NetGraphData> datas = new NetGraphParser().parse(json);
                    return Optional.of(new HashSet<>(datas));
                }
            });

            exporter.export(cmd);
           
        } catch(Exception e) {
            log.error("error on exportCsvTest: ", e);
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void exportExcelTest() {
        try {
            String json = new String(
                Files.readAllBytes(
                    Paths.get(NetGraphTest.class.getResource("/response.json").toURI())
                ), "UTF-8");
    
            Exporter exporter = new NetGraphExcelDecorator(new Exporter() {
                @Override
                public Optional<Set<NetGraphData>> export(CommandLine cmd) throws Exception {
                    List<NetGraphData> datas = new NetGraphParser().parse(json);
                    return Optional.of(new HashSet<>(datas));
                }
            });
            
            exporter.export(cmd);
           
        } catch(Exception e) {
            log.error("error on exportExcelTest: ", e);
            Assert.fail(e.getMessage());
        }
    }
    */
}

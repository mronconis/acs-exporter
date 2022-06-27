package com.redhat.acs.exporter;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.redhat.acs.exporter.parser.NetGraphParser;
import com.redhat.acs.exporter.parser.NetGraphParser.NetGraphData;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

public class NetGraphTest {
    final static Logger log = LogManager.getLogger(NetGraphTest.class);

    @Test
    public void parserNetGraphTest() {
        try {
            String json = new String(
                Files.readAllBytes(
                    Paths.get(NetGraphTest.class.getResource("/response2.json").toURI())
                ), "UTF-8");

            List<NetGraphData> datas = new NetGraphParser().parse(json);

            datas.stream().forEach(h -> System.out.println(h.toCsv()));

            Assert.assertNotNull(datas);
        } catch(Exception e) {
            log.error("error on parserNetGraphTest: ", e);
            Assert.fail(e.getMessage());
        }
    }
}

package org.pizazz2.kafka.monitor;


import org.pizazz2.common.YAMLUtils;
import org.pizazz2.data.TupleObject;
import org.pizazz2.exception.BaseException;
import org.pizazz2.helper.TupleObjectHelper;

import java.time.Duration;

public class SimpleMonitorTest {

    public void monitorExample() throws BaseException, InterruptedException {
        TupleObject config = YAMLUtils.fromYAML("kafka_config_template.yml");
        SimpleMonitor<String, String> monitor = new SimpleMonitor<>(
                TupleObjectHelper.getTupleObject(config, "subscription"));
        monitor.activate(Duration.ofSeconds(1), true);

        for (int i = 0; i < 10; i ++) {
            monitor.nodeCache();
            monitor.consumerCache();
//
//            for (NodeEntity item : nodes) {
//                System.out.println(item);
//            }
            Thread.sleep(1000);
        }
    }
}
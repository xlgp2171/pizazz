package org.pizazz2.kafka;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.pizazz2.common.JSONUtils;
import org.pizazz2.common.SystemUtils;
import org.pizazz2.common.YAMLUtils;
import org.pizazz2.data.TupleObject;
import org.pizazz2.exception.BaseException;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.helper.TupleObjectHelper;
import org.pizazz2.kafka.exception.KafkaException;

import java.time.Duration;

/**
 * 发送消息测试
 *
 * @author xlgp2171
 * @version 1.0.220626
 */
public class ProductionTest {
    static final String RESOURCE = "kafka_config_template.yml";
    static Production<String, String> PRODUCTION;
    static final String TOPIC = "H_D_TRACE";

    @org.junit.BeforeClass
    public static void setUp() throws ValidateException, BaseException {
        TupleObject _config = YAMLUtils.fromYAML(RESOURCE);
        PRODUCTION = new Production<>(TupleObjectHelper.getTupleObject(_config, KafkaConstant.KEY_PRODUCTION));
    }

    @org.junit.Test
    public void sentExample() throws KafkaException {
        TupleObject data = TupleObjectHelper.newObject(4).append("start", "2019-08-01 19:45:00")
                .append("end", "2019-08-30 05:15:00").append("sessionID", SystemUtils.newUUIDSimple())
                .append("T", TOPIC);
        String value = JSONUtils.toJSON(data);

        for (int i = 0; i < 7; i ++) {
            PRODUCTION.sent(new ProducerRecord<>(TOPIC, value), new Callback() {
                @Override
                public void onCompletion(RecordMetadata metadata, Exception exception) {
                    System.out.println("F: " + metadata);
                }
            });
        }
        System.out.println("SEND FINISHED");
    }

    @org.junit.AfterClass
    public static void tearDown() {
        SystemUtils.destroy(PRODUCTION, Duration.ZERO);
    }
}

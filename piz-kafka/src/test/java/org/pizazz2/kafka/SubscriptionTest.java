package org.pizazz2.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.pizazz2.common.DateUtils;
import org.pizazz2.common.SystemUtils;
import org.pizazz2.common.YAMLUtils;
import org.pizazz2.data.TupleObject;
import org.pizazz2.exception.BaseException;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.helper.TupleObjectHelper;
import org.pizazz2.kafka.consumer.IMultiDataExecutor;
import org.pizazz2.kafka.consumer.IOffsetProcessor;
import org.pizazz2.kafka.consumer.ISingleDataExecutor;
import org.pizazz2.kafka.exception.KafkaException;

import java.time.Duration;
import java.util.Collection;

/**
 * 接收消息测试
 *
 * @author xlgp2171
 * @version 1.0.220626
 */
public class SubscriptionTest {
    static final String RESOURCE = "kafka_config_template.yml";
    static Subscription<String, String> SUBSCRIPTION;
    static final String[] TOPICS = new String[] {"H_G_TRACE", "H_D_TRACE"};

    @org.junit.BeforeClass
    public static void setUp() throws ValidateException, BaseException {
        TupleObject _config = YAMLUtils.fromYAML(RESOURCE);
        SUBSCRIPTION = new Subscription<>(TupleObjectHelper.getTupleObject(_config, KafkaConstant.KEY_SUBSCRIPTION));
    }

    @org.junit.Test
    public void testSubscribeSingleData() throws KafkaException {
        SUBSCRIPTION.subscribe(new ISingleDataExecutor<String, String>() {
            @Override
            public void execute(ConsumerRecord<String, String> record) throws Exception {
                System.out.println(record.value());
                //				TupleObject object = JSONUtils.fromJSON(record.value(), TupleObject.class);
                //				System.out.println(object);
            }

            @Override
            public void end(IOffsetProcessor offset) {
                //				SUBSCRIPTION.unsubscribe();
            }
        }, TOPICS);
    }

    @org.junit.Test
    public void testSubscribeMultiData() throws KafkaException {
        SUBSCRIPTION.subscribe(new IMultiDataExecutor<String, String>() {

            @Override
            public void execute(Collection<ConsumerRecord<String, String>> consumerRecords) throws Exception {
                for (ConsumerRecord<String, String> item : consumerRecords) {
                    System.out.println(item.value());
                }
            }

            @Override
            public void begin(int count) {
                System.out.println(DateUtils.now() + "/" + count);
            }
        }, TOPICS);
    }

    @org.junit.AfterClass
    public static void tearDown() {
        SystemUtils.destroy(SUBSCRIPTION, Duration.ZERO);
    }
}

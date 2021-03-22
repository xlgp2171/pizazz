package org.pizazz2.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.pizazz2.common.SystemUtils;
import org.pizazz2.common.YAMLUtils;
import org.pizazz2.data.TupleObject;
import org.pizazz2.exception.BaseException;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.helper.TupleObjectHelper;
import org.pizazz2.kafka.consumer.IDataExecutor;
import org.pizazz2.kafka.consumer.IOffsetProcessor;
import org.pizazz2.kafka.exception.KafkaException;

import java.time.Duration;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class KafkaTest {
	static final String RESOURCE = "kafka_config_template.yml";
	static final String TOPIC = "TOPIC_DEMO_DEF";

	static Production<String, String> PRODUCTION;
	static Subscription<String, String> SUBSCRIPTION;

//	@org.junit.BeforeClass
	public static void setUp() throws ValidateException, BaseException {
		TupleObject _config = YAMLUtils.fromYAML(RESOURCE);
		PRODUCTION = new Production<>(TupleObjectHelper.getTupleObject(_config, KafkaConstant.KEY_PRODUCTION));
		SUBSCRIPTION = new Subscription<>(TupleObjectHelper.getTupleObject(_config, KafkaConstant.KEY_SUBSCRIPTION));
	}

//	@org.junit.Test
	public void sentExample() throws KafkaException {
		for (int i = 0; i < 100; i ++) {
			PRODUCTION.sent(new ProducerRecord<>(TOPIC, "MSG:" + i));
		}
		System.out.println("SEND FINISHED");
	}

//	@org.junit.Test
	public void subscribeExample() throws KafkaException {
		SUBSCRIPTION.subscribe(new IDataExecutor<String, String>() {
			@Override
			public void execute(ConsumerRecord<String, String> record) throws Exception {
				System.out.println(record.value());
			}

			@Override
			public void end(IOffsetProcessor offset) {
				SUBSCRIPTION.unsubscribe();
			}
		}, TOPIC);
	}

//	@org.junit.AfterClass
	public static void tearDown() {
		SystemUtils.destroy(PRODUCTION, Duration.ZERO);
		SystemUtils.destroy(PRODUCTION, Duration.ZERO);
	}
}

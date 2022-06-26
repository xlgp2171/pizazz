package org.pizazz2.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.pizazz2.common.JSONUtils;
import org.pizazz2.common.SystemUtils;
import org.pizazz2.common.YAMLUtils;
import org.pizazz2.data.TupleObject;
import org.pizazz2.exception.BaseException;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.helper.TupleObjectHelper;
import org.pizazz2.kafka.consumer.ISingleDataExecutor;
import org.pizazz2.kafka.consumer.IOffsetProcessor;
import org.pizazz2.kafka.exception.KafkaException;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class KafkaTest {
	static final String RESOURCE = "kafka_config_template.yml";
//	static final String TOPIC = "TOPIC_WLSJFX";
	static final String TOPIC = "H_D_TRACE";

	static Production<String, String> PRODUCTION;
	static Subscription<String, String> SUBSCRIPTION;

	@org.junit.BeforeClass
	public static void setUp() throws ValidateException, BaseException {
		TupleObject _config = YAMLUtils.fromYAML(RESOURCE);
		PRODUCTION = new Production<>(TupleObjectHelper.getTupleObject(_config, KafkaConstant.KEY_PRODUCTION));
		SUBSCRIPTION = new Subscription<>(TupleObjectHelper.getTupleObject(_config, KafkaConstant.KEY_SUBSCRIPTION));
	}

	@org.junit.Test
	public void sentExample() throws KafkaException {
		TupleObject data = TupleObjectHelper.newObject(4).append("start", "2019-07-01 19:45:00")
				.append("end", "2019-07-30 05:15:00").append("sessionID", SystemUtils.newUUIDSimple())
				.append("MBMC", "0000000034453465013445e58dc90529");
		String value = JSONUtils.toJSON(data);

		for (int i = 0; i < 1; i ++) {
			PRODUCTION.sent(new ProducerRecord<>(TOPIC, value));
		}
		System.out.println("SEND FINISHED");
	}

//	@org.junit.Test
	public void sentForFile() throws KafkaException, IOException {
		try (BufferedReader reader = Files.newBufferedReader(Paths.get("D:/data.dat"))) {
			String line;

			while ((line = reader.readLine()) != null) {
				line = "[" + line + "]";
				PRODUCTION.sent(new ProducerRecord<>(TOPIC, line));
			}
		}
		System.out.println("SEND FINISHED");
	}

	@org.junit.Test
	public void subscribeExample() throws KafkaException {
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
		}, TOPIC);
	}

	@org.junit.AfterClass
	public static void tearDown() {
		SystemUtils.destroy(PRODUCTION, Duration.ZERO);
		SystemUtils.destroy(PRODUCTION, Duration.ZERO);
	}
}

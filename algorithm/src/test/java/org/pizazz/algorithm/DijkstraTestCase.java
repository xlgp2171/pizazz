package org.pizazz.algorithm;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.pizazz.IMessageOutput;
import org.pizazz.algorithm.Dijkstra;

public class DijkstraTestCase {

	@Test
	public void testMain() {
		Dijkstra _process = new Dijkstra(new IMessageOutput<String>() {
			@Override
			public void write(String message) {
				System.err.println("\t" + message);
			}

			@Override
			public boolean isEnable() {
				return true;
			}
		});
		String[] _tmp = _process.setData(build(null)).execute("A", "F");
		System.out.println(Arrays.toString(_tmp));

		for (String _item : _process.getDistance().keySet()) {
			System.out.println(_item + "=" + _process.getDistance().get(_item));
		}
		for (String _item : _process.getRoute().keySet()) {
			System.out.println(_item + "=" + _process.getRoute().get(_item));
		}
	}

	private static Map<String, Map<String, Double>> build(Map<String, Double> tmp) {
		Map<String, Map<String, Double>> _data;
		_data = new HashMap<String, Map<String, Double>>();
		tmp = new HashMap<String, Double>();
		tmp.put("B", 6.0);
		tmp.put("C", 3.0);
		_data.put("A", tmp);
		tmp = new HashMap<String, Double>();
		tmp.put("A", 6.0);
		tmp.put("C", 2.0);
		tmp.put("D", 5.0);
		_data.put("B", tmp);
		tmp = new HashMap<String, Double>();
		tmp.put("A", 3.0);
		tmp.put("B", 2.0);
		tmp.put("D", 3.0);
		tmp.put("E", 4.0);
		_data.put("C", tmp);
		tmp = new HashMap<String, Double>();
		tmp.put("B", 5.0);
		tmp.put("C", 3.0);
		tmp.put("E", 2.0);
		tmp.put("F", 3.0);
		_data.put("D", tmp);
		tmp = new HashMap<String, Double>();
		tmp.put("C", 4.0);
		tmp.put("D", 2.0);
		tmp.put("F", 5.0);
		_data.put("E", tmp);
		tmp = new HashMap<String, Double>();
		tmp.put("D", 3.0);
		tmp.put("E", 5.0);
		_data.put("F", tmp);
		return _data;
	}
}

package org.pizazz.algorithm;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.pizazz.algorithm.exception.AlgorithmException;

public class CombinationTestCase {

	// 测试5选3
	@Test
	public void testMain() throws AlgorithmException {
		String[] _source = new String[]{"a", "b", "c", "d", "e"};
		long _begin = System.currentTimeMillis();
		List<String[]> _result = new Combination<String>(_source).execute(3);
		long _end = System.currentTimeMillis();
		System.out.println(_result.size() + " COST:" + (_end - _begin));
		_result.stream().forEach(_item -> System.out.println(Arrays.toString(_item)));
	}
}

package org.pizazz.algorithm;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.pizazz.algorithm.exception.AlgorithmCodeEnum;
import org.pizazz.algorithm.exception.AlgorithmException;
import org.pizazz.common.CollectionUtils;

/**
 * 组合算法(参考)<br>
 * 从任意长度数组获取其中几个元素的所有组合 :<br>
 * 本程序的思路是开一个数组，其下标表示1到m个数，数组元素的值为1表示其下标<br>
 * 代表的数被选中，为0则没选中<br>
 * 首先初始化，将数组前n个元素置1，表示第一个组合为前n个数<br>
 * 然后从左到右扫描数组元素值的"10"组合，找到第一个"10"组合后将其变为<br>
 * "01"组合，同时将其(在本算法中具体指i的位置)左边的所有"1"全部移动到数组的最左端<br>
 * 当第一个"1"移动到数组的m-n的位置，即n个"1"全部移动到最右端时，就得到了最后一个组合
 * 
 * <pre>
 * 例如求5中选3的组合：   
 * 1 1 1 0 0 //1,2,3   
 * 1 1 0 1 0 //1,2,4   
 * 1 0 1 1 0 //1,3,4   
 * 0 1 1 1 0 //2,3,4   
 * 1 1 0 0 1 //1,2,5   
 * 1 0 1 0 1 //1,3,5   
 * 0 1 1 0 1 //2,3,5   
 * 1 0 0 1 1 //1,4,5   
 * 0 1 0 1 1 //2,4,5   
 * 0 0 1 1 1 //3,4,5
 * </pre>
 * 
 * @param <T>
 * 
 * @author xlgp2171
 * @version 1.0.181223
 */
public class Combination<T> {
	private T[] source;

	public Combination(T[] source) throws AlgorithmException {
		if (source == null) {
			throw new AlgorithmException(AlgorithmCodeEnum.ALG_0002, "source null");
		}
		this.source = source;
	}

	private List<T[]> check(int num) throws AlgorithmException {
		List<T[]> _result = new LinkedList<T[]>();

		if (num == 0) {
			return null;
		} else if (source.length < num) {
			throw new AlgorithmException(AlgorithmCodeEnum.ALG_0001, "num>array.length");
		} else if (source.length == num) {
			_result.add(source);
		} else if (num == 1) {
			for (int _i = 0; _i < source.length; _i ++) {
				_result.add(Arrays.copyOfRange(source, _i, _i + 1));
			}
		}
		return _result;
	}

	private int[] newTemplete(int num) {
		int[] _templete = new int[source.length];
		// 初始化，将数组前n个元素置1，表示第一个组合为前n个数。
		for (int i = 0; i < source.length; i++) {
			_templete[i] = i < num ? 1 : 0;
		}
		return _templete;
	}
	/**
	 * @param num 获取个数
	 * @return
	 * @throws BaseException
	 */
	public List<T[]> execute(int num) throws AlgorithmException {
		List<T[]> _result = check(num);

		if (_result == null) {
			return CollectionUtils.emptyList();
		} else if (!_result.isEmpty()) {
			return _result;
		}
		int _length = source.length;
		// 判断是否最后一种组合
		boolean _loop = true;
		// 生成辅助数组
		int[] _templete = newTemplete(num);
		// 标记位
		int _index = 0;

		while (_loop) {
			// 是否找到10的标记
			boolean _find10 = false;
			boolean _swap = false;
			// 从左到右扫描数组元素值的"10"组合，找到第一个"10"组合后将其变为"01"
			for (int _i = 0; _i < _length; _i++) {
				int _j = 0;

				if (!_find10 && _templete[_i] == 1) {
					_index = _i;
					_find10 = true;
				}
				if (_templete[_i] == 1 && _templete[_i + 1] == 0) {
					_templete[_i] = 0;
					_templete[_i + 1] = 1;
					_swap = true;
					// 将其左边的所有"1"全部移动到数组的最左端。
					for (_j = 0; _j < _i - _index; _j++) {
						_templete[_j] = _templete[_index + _j];
					}
					for (_j = _i - _index; _j < _i; _j++) {
						_templete[_j] = 0;
					}
					// 若第一个"1"刚刚移动到第n-m+1个位置,则终止整个寻找
					if (_index == _i && _i + 1 == _length - num) {
						_loop = false;
					}
				}
				if (_swap) {
					break;
				}
			}
			// 添加下一种默认组合
			_result.add(fromTemplete(_templete, num));
		}
		return _result;
	}

	/**
	 * 从模版数组构建新数组<br>
	 * 模版数组中元素1为交换位置
	 * 
	 * @param templete 模版数组
	 * @return
	 * @throws BaseException 源数组小于模版数组时
	 */
	protected T[] fromTemplete(int[] templete, int num) throws AlgorithmException {
		T[] _tmp = Arrays.copyOf(source, num);
		int _index = 0;
		
		for (int _i = 0; _i < source.length; _i++) {
			if (templete[_i] == 1) {
				_tmp[_index++] = source[_i];
			}
		}
		return _tmp;
	}
}

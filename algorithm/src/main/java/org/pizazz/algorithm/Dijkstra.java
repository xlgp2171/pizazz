package org.pizazz.algorithm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.pizazz.IMessageOutput;
import org.pizazz.common.MessageOutputHelper;

import java.util.Map.Entry;

/**
 * 最短路径算法(参考)<br>
 * Dijkstra(迪杰斯特拉)算法<br>
 * 优化设计：现在方式为全遍历，优化为方向性遍历（需要配合经纬度）
 * 
 * @author xlgp2171
 * @version 1.0.181223
 */
public class Dijkstra {
	private final IMessageOutput<String> output;
	private String separator = "-";
	// 所有节点去向
	private Map<String, Map<String, Double>> data;
	// 最短路径距离
	private final Map<String, Double> distance = new HashMap<String, Double>();
	/**
	 * 参考数据<br>
	 * 数据格式{key:节点ID,value={key:目的节点ID,value:距离}}
	 */
	private final Map<String, String> route = new HashMap<String, String>();

	public Dijkstra() {
		this(null);
	}

	public Dijkstra(IMessageOutput<String> output) {
		this.output = output == null ? MessageOutputHelper.EMPTY_STRING : output;
	}

	/**
	 * 执行最短距离算法
	 * 
	 * @param startId 起始ID
	 * @param endId 终止ID
	 */
	public String[] execute(String startId, String endId) {
		// 每次清洗数据
		distance.clear();
		route.clear();
		// 已处理过的节点
		Set<String> _out = new HashSet<String>();
		// 初始节点
		Set<String> _nextIds = new HashSet<String>(1);
		_nextIds.add(startId);
		loop(startId, endId, _nextIds, null, _out);
		_out.clear();
		return new String[] { String.valueOf(distance.get(format(startId, endId))), route.get(format(startId, endId)) };
	}

	private void loop(String startId, String endId, Set<String> nextIds, String fromId, Set<String> out) {
		boolean _loaded = false;
		// 每个节点的下一个节点集合
		Map<String, Set<String>> _nextIdMap = null;
		// 遍历就近节点
		for (String _item : nextIds) {
			// 根据来源获取最短路径
			if (output.isEnable()) {
				output.write("SELECT:" + fromId + "->" + _item);
			}
			Set<String> _nextIds = each(startId, _item, out);
			// FIXME 用于终止endId继续向下遍历问题,是否会导致遍历不全?
			if (_item.equals(endId)) {
				return;
			}
			// 添加当前节点的就近节点
			if (_nextIds != null) {
				if (!_loaded) {
					_nextIdMap = new HashMap<String, Set<String>>();
					_loaded = true;
				}
				_nextIdMap.put(_item, _nextIds);
			}
			// 处理过的节点不再追踪
			out.add(_item);
		}
		// 处理就近节点
		if (_nextIdMap != null) {
			for (Entry<String, Set<String>> _item : _nextIdMap.entrySet()) {
				loop(startId, endId, _item.getValue(), _item.getKey(), out);
			}
		}
	}

	/**
	 * 
	 * @param beginId 起始节点ID
	 * @param nowId 当前节点ID
	 * @return 当前节点就近节点集合
	 */
	private Set<String> each(String startId, String nowId, Set<String> out) {
		boolean _loaded = false;
		// 当前节点下一个节点集合
		Set<String> _nextIds = null;
		// 获取当前节点去向集合
		Map<String, Double> _nodes = getData().get(nowId);
		// 获取起始点到当前点的路径
		double _distance = 0;
		String _path = "";
		// 遍历当前节点所有路径
		// for (Node _item : _nodes) {
		for (Entry<String, Double> _item : _nodes.entrySet()) {
			// 排除已经处理过的节点
			if (out.contains(_item.getKey())) {
				continue;
			}
			// 第一次加载数据
			if (!_loaded) {
				// 当前节点下一个节点集合
				_nextIds = new HashSet<String>();
				String _key = format(startId, nowId);
				// 获取起始点到当前点的路径距离
				_distance = distance.containsKey(_key) ? distance.get(_key) : 0;
				// 获取起始点到当前点的路径ID
				_path = route.containsKey(_key) ? route.get(_key) : "";
				_loaded = true;
			}
			// 若没有最短路径，则是下一个节点
			_nextIds.add(_item.getKey());
			// FIXME 缓存节点之间的路径,是否可以删除用以提高速度?
			if (output.isEnable()) {
				output.write("CREATE:" + format(nowId, _item.getKey()) + "=" + _item.getValue());
			}
			String _id = format(nowId, _item.getKey());
			route.put(_id, _id);
			distance.put(_id, _item.getValue());
			// 获取起点到当前节点的标记
			String _fromStart = format(startId, _item.getKey());
			// 获取最短路径累加值
			double _tmpDistance = _item.getValue() + _distance;
			String _tmpPath = format(_path, _item.getKey());
			// 搜索是否有最短路径，若没有则增加
			if (distance.containsKey(_fromStart)) {
				// 根据来源获取最短路径
				if (output.isEnable()) {
					output.write("??????:" + _fromStart + " " + _item.getValue() + "+" + _distance + "<"
							+ distance.get(_fromStart) + "?");
				}
				// 若最短路径小于累加路径，则更新最短路径
				if (_tmpDistance < distance.get(_fromStart)) {
					if (output.isEnable()) {
						output.write("UPDATE:" + _fromStart + "=" + _tmpDistance);
					}
					route.put(_fromStart, _tmpPath);
					distance.put(_fromStart, _tmpDistance);
				}
			} else {
				// 缓存起始节点到当前节点的路径
				if (output.isEnable()) {
					output.write("CREATE:" + _fromStart + "=" + _item.getValue() + "+" + _distance);
				}
				route.put(_fromStart, _tmpPath);
				distance.put(_fromStart, _tmpDistance);
			}
		}
		return _nextIds;
	}

	/**
	 * 最短路径格式组合
	 * 
	 * @param prefix
	 * @param suffix
	 * @return
	 */
	protected String format(String prefix, String suffix) {
		return prefix + separator + suffix;
	}

	public Map<String, Map<String, Double>> getData() {
		return data == null ? data = new HashMap<String, Map<String, Double>>() : data;
	}

	/**
	 * 初始化数据<br>
	 * 数据格式{key:节点ID,value={key:目的节点ID,value:距离}}
	 * 
	 * @param
	 */
	public Dijkstra setData(Map<String, Map<String, Double>> data) {
		this.data = data;
		return this;
	}

	public Map<String, Double> getDistance() {
		return distance;
	}

	public Map<String, String> getRoute() {
		return route;
	}
}

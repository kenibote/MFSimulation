package mainfunction;

import java.util.*;

import sql.*;
import tool.GenerateCreaterUser;

public class MixCo {

	// 每小时内，平均每分钟到达率
	static double[] SamePressure = { 27.43, 6.99, 1.15, 0.22, 0.11, 1.00, 5.28, 20.81, 60.80, 132.65, 209.12, 244.16,
			209.60, 131.40, 62.76, 31.38, 107.87, 290.16, 250.67, 281.85, 324.13, 278.37, 175.60, 81.02 };

	public static HashMap<String, double[]> HourPressure = new HashMap<>();
	public static HashMap<String, Integer> ZoneMap = new HashMap<>();
	static {
		for (int i = 1; i <= GenerateCreaterUser.zoneNumber; i++) {
			HourPressure.put("Zone_" + i, SamePressure);
			ZoneMap.put("Zone_" + i, i);
		}
	}

	public static int hour = 0;
	// 用于记录每个区域总期望点击数
	public static double[] totalZoneExp = new double[GenerateCreaterUser.zoneNumber + 1];

	// 克隆的缓存信息
	static ArrayList<Content> cloneContentAll = null;
	static HashMap<String, ArrayList<Content>> cloneContentRank = new HashMap<>();
	// TODO 10个往上增长
	static int increaseBatch = 10;

	@SuppressWarnings({ "deprecation", "unchecked" })
	public static void PartOne(Task task) {
		hour = task.getDate().getHours();

		// 重新初始化计数器
		for (int i = 0; i <= GenerateCreaterUser.zoneNumber; i++) {
			totalZoneExp[i] = 0;
		}

		// 清理低于0的数据
		for (Content c : StartHereV2.ContentAll) {
			c.checkIfZero();
			for (String s : c.ValueZone.keySet()) {
				totalZoneExp[ZoneMap.get(s)] += c.ValueZone.get(s);
			}
		}

		// 准备排名顺序
		cloneContentAll = (ArrayList<Content>) StartHereV2.ContentAll.clone();
		Collections.sort(cloneContentAll, Content.zoneComparetor.get("MixCo"));
		for (int i = 1; i <= GenerateCreaterUser.zoneNumber; i++) {
			cloneContentRank.put("Zone_" + i, (ArrayList<Content>) StartHereV2.ContentAll.clone());
			Collections.sort(cloneContentRank.get("Zone_" + i), Content.zoneComparetor.get("Zone_" + i));
		}

		// 初始化第一部分的空间
		HashMap<String, Integer> firstPartSet = new HashMap<>();
		for (int i = 1; i <= GenerateCreaterUser.zoneNumber; i++) {
			firstPartSet.put("Zone_" + i, 0);
		}

		double minLatency = Double.MAX_VALUE;
		double newLatenct;
		// 如果小于已知最小latency
		while ((newLatenct = PartTwo(firstPartSet)) < minLatency) {
			minLatency = newLatenct;
			BackupCache = TempCache;
			for (int i = 1; i <= GenerateCreaterUser.zoneNumber; i++) {
				int val = firstPartSet.get("Zone_" + i) + increaseBatch;
				firstPartSet.put("Zone_" + i, val);
			}
			// 不可以超出容量限制
			if (firstPartSet.get("Zone_1") > StartHereV2.MEC_Max_Cache) {
				break;
			}
		}

		// 将结果写回去
		for (int i = 1; i <= GenerateCreaterUser.zoneNumber; i++) {
			MEC mec = StartHereV2.MEC_Info.get("Zone_" + i);
			mec.CacheSet.clear();
			for (Content c : BackupCache.get("Zone_" + i)) {
				mec.CacheSet.add(c);
			}
		}
	}

	// 用于记录MEC缓存信息
	static HashMap<String, HashSet<Content>> BackupCache = null;
	static HashMap<String, HashSet<Content>> TempCache = null;
	static double[] EstimatePressure = new double[GenerateCreaterUser.zoneNumber + 1];
	// TODO 这里的阈值需要调整可能
	static double threadhold = 200.0;
	static double L1 = 1.0, L2 = 2.0, L3 = 6.0;

	static double PartTwo(HashMap<String, Integer> firstPartSet) {
		// 初始化缓存
		TempCache = new HashMap<>();
		for (int i = 1; i <= GenerateCreaterUser.zoneNumber; i++) {
			TempCache.put("Zone_" + i, new HashSet<>());
			EstimatePressure[i] = 0;
		}

		// 用于记录哪些内容已经被存过
		HashSet<Content> saved = new HashSet<>();

		// 缓存第一部分
		for (int i = 1; i <= GenerateCreaterUser.zoneNumber; i++) {
			HashSet<Content> zoneCache = TempCache.get("Zone_" + i);
			int pox = 0;
			while (zoneCache.size() < firstPartSet.get("Zone_" + i)) {
				// 确定要缓存的内容
				Content content = cloneContentRank.get("Zone_" + i).get(pox);

				// 更新预估压力值 点击期望*到达率/总期望
				EstimatePressure[i] += (content.ValueZone.get("Zone_" + i) * HourPressure.get("Zone_" + i)[hour]
						/ totalZoneExp[i]);

				// 将内容放入缓存
				zoneCache.add(content);
				saved.add(content);
				pox++;
			}
		}

		// 保存第二部分
		ArrayList<String> still_have_space = null;
		int index = 0;
		// 如果还有存储空间
		while ((still_have_space = stillHaveSpace()).size() > 0) {
			// 直到找到一个没有存过的
			while (saved.contains(cloneContentAll.get(index))) {
				index++;
			}
			Content content = cloneContentAll.get(index);
			saved.add(content);

			// 对区域进行排序
			ArrayList<ZoneRank> part2rank = new ArrayList<>();
			for (String s : still_have_space) {
				double value = content.ValueZone.get(s) * HourPressure.get(s)[hour] * 10000
						/ EstimatePressure[ZoneMap.get(s)];
				part2rank.add(new ZoneRank(s, value));
			}
			Collections.sort(part2rank);

			// 按顺序进行尝试
			for (ZoneRank z : part2rank) {
				if (EstimatePressure[ZoneMap.get(z.name)] < threadhold || z == part2rank.get(part2rank.size() - 1)) {
					TempCache.get(z.name).add(content);

					// 更新阈值
					double contentPressure = 0;
					for (String s : content.ValueZone.keySet()) {
						contentPressure += (content.ValueZone.get(s) * HourPressure.get(s)[hour]
								/ totalZoneExp[ZoneMap.get(s)]);
					}

					EstimatePressure[ZoneMap.get(z.name)] += contentPressure;
					break;
				}
			}

		} // end while

		// 计算预估时延
		double estimateLatency = 0;
		double totalArrival = 0;
		for (String zone : ZoneMap.keySet()) {
			for (Content c : StartHereV2.ContentAll) {
				double val = c.ValueZone.get(zone) * HourPressure.get(zone)[hour] * 10000
						/ totalZoneExp[ZoneMap.get(zone)];
				if (TempCache.get(zone).contains(c)) {
					estimateLatency += (L1 * val);
				} else {
					if (saved.contains(c)) {
						estimateLatency += (L2 * val);
					} else {
						estimateLatency += (L3 * val);
					}
				}

			} // end content for
			totalArrival += HourPressure.get(zone)[hour];
		} // end zone for

		return estimateLatency / totalArrival;
	}

	static ArrayList<String> stillHaveSpace() {
		ArrayList<String> res = new ArrayList<>();

		for (String s : TempCache.keySet()) {
			if (TempCache.get(s).size() < StartHereV2.MEC_Max_Cache) {
				res.add(s);
			}
		}

		return res;
	}

	static class ZoneRank implements Comparable<ZoneRank> {
		double value;
		String name;

		public ZoneRank(String name, double value) {
			this.name = name;
			this.value = value;
		}

		@Override
		public int compareTo(ZoneRank o) {
			return (int) (-this.value + o.value);
		}
	}

}

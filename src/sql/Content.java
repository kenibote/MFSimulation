package sql;

import java.util.*;

import mainfunction.MixCo;
import tool.GenerateCreaterUser;

public class Content {

	public String ContentName;

	public int ValueGlobal = 0;
	public HashMap<String, Integer> ValueZone = new HashMap<>();
	public HashMap<String, HashSet<User>> ContentCopy = new HashMap<>();
	public int watchCount = 0;

	public static HashMap<String, ContentCompareterZone> zoneComparetor = new HashMap<>();
	static {
		for (int i = 1; i <= GenerateCreaterUser.zoneNumber; i++) {
			zoneComparetor.put("Zone_" + i, new ContentCompareterZone("Zone_" + i));
		}

		zoneComparetor.put("Global", new ContentCompareterZone("Global"));
		zoneComparetor.put("MixCo", new ContentCompareterZone("MixCo"));
		zoneComparetor.put("WatchCount", new ContentCompareterZone("WatchCount"));
	}

	public Content() {
	}

	public Content(String name) {
		this.ContentName = name;
	}

	public void InitValue(Creater creater) {
		this.ValueGlobal = creater.getTotalSubscribeNmuber();

		for (String z : creater.getZoneSubscribeNumber().keySet()) {
			ValueZone.put(z, creater.getZoneSubscribeNumber().get(z));
			ContentCopy.put(z, new HashSet<>());
		}

	}

	public void decreaseZoneValue(String zone) {
		int val = ValueZone.get(zone) - 1;
		ValueZone.put(zone, val);
	}

	public ArrayList<String> getMaxOrderValueZone() {
		ArrayList<String> result = new ArrayList<>();
		TreeMap<Double, String> rank = new TreeMap<>(Collections.reverseOrder());

		double delta = 0.02;
		for (String s : ValueZone.keySet()) {
			rank.put(ValueZone.get(s) + delta, s);
			delta = delta + 0.02;
		}

		for (double d : rank.keySet()) {
			result.add(rank.get(d));
		}

		return result;
	}

	public void checkIfZero() {
		if (ValueGlobal < 0)
			ValueGlobal = 0;

		for (String s : ValueZone.keySet()) {
			int val = ValueZone.get(s);
			if (val < 0)
				ValueZone.put(s, 0);
		}
	}

	private int MixCoValue() {
		int value = 0;

		for (String s : ValueZone.keySet()) {
			// TODO 此处值得思考
			value = value + (int) (ValueZone.get(s) * MixCo.HourPressure.get(s)[MixCo.hour] * 10000
					/ MixCo.totalZoneExp[MixCo.ZoneMap.get(s)]);
		}

		return value;
	}

	static class ContentCompareterZone implements Comparator<Content> {

		String zone;

		public ContentCompareterZone(String zone) {
			this.zone = zone;
		}

		@Override
		public int compare(Content o1, Content o2) {
			if ("Global".equals(zone)) {
				return -o1.ValueGlobal + o2.ValueGlobal;
			}

			if ("MixCo".equals(zone)) {
				return -o1.MixCoValue() + o2.MixCoValue();
			}

			if ("WatchCount".equals(zone)) {
				return -o1.watchCount + o2.watchCount;
			}

			return -o1.ValueZone.get(zone) + o2.ValueZone.get(zone);
		}

	}

	@Override
	public String toString() {
		return "Content [ContentName=" + ContentName + ", ValueGlobal=" + ValueGlobal + ", ValueZone=" + ValueZone
				+ "]";
	}

}

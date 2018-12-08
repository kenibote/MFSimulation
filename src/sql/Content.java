package sql;

import java.util.*;

import tool.GenerateCreaterUser;

public class Content {

	public String ContentName;

	public int ValueGlobal = 0;
	public HashMap<String, Integer> ValueZone = new HashMap<>();
	public HashMap<String, HashSet<User>> ContentCopy = new HashMap<>();

	public static HashMap<String, ContentCompareterZone> zoneComparetor = new HashMap<>();
	static {
		for (int i = 1; i <= GenerateCreaterUser.zoneNumber; i++) {
			zoneComparetor.put("Zone_" + i, new ContentCompareterZone("Zone_" + i));
		}
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

	static class ContentCompareterZone implements Comparator<Content> {

		String zone;

		public ContentCompareterZone(String zone) {
			this.zone = zone;
		}

		@Override
		public int compare(Content o1, Content o2) {
			return o1.ValueZone.get(zone) - o2.ValueZone.get(zone);
		}

	}

	@Override
	public String toString() {
		return "Content [ContentName=" + ContentName + ", ValueGlobal=" + ValueGlobal + ", ValueZone=" + ValueZone
				+ "]";
	}

}

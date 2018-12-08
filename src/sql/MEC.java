package sql;

import java.util.*;

import mainfunction.StartHereV2;

public class MEC implements Comparable<MEC> {

	public String BelongToZone = null;
	public int MEC_Available_State = 0;

	public LinkedList<Content> CacheLRU = new LinkedList<>();
	public HashSet<Content> CacheSet = new HashSet<>();

	public MEC() {
	}

	public MEC(String zoneName) {
		this.BelongToZone = zoneName;
	}

	public boolean isAvailable() {
		if (MEC_Available_State < StartHereV2.MEC_MAX_Capacity) {
			return true;
		}
		return false;
	}

	// 用于非LRU模式
	public void deleteOneCache() {
		// 找到值最低的 o(n)复杂度
		Content content = null;
		int min = Integer.MAX_VALUE;

		for (Content c : CacheSet) {
			if (c.ValueZone.get(BelongToZone) < min) {
				content = c;
				min = c.ValueZone.get(BelongToZone);
			}
		}

		// 移除该内容
		CacheSet.remove(content);
	}

	public void addOneContent(Content content) {
		CacheSet.add(content);
	}

	private void deleteOneCacheLRU() {
		Content content = CacheLRU.pollLast();
		CacheSet.remove(content);
	}

	public void addOneContentLRU(Content content) {
		if (CacheSet.size() >= StartHereV2.MEC_Max_Cache) {
			deleteOneCacheLRU();
		}

		CacheSet.add(content);
		CacheLRU.offerFirst(content);
	}

	public void updataLRUorder(Content content) {
		CacheLRU.remove(content);
		CacheLRU.offerFirst(content);
	}

	@Override
	public int compareTo(MEC o) {
		return this.MEC_Available_State - o.MEC_Available_State;
	}
}

package sql;

import java.util.*;
import javax.persistence.*;

import org.hibernate.annotations.GenericGenerator;

import mainfunction.DelMode;
import mainfunction.StartHereV2;

@Entity
@Table(name = "user_info")
public class User {

	@Id
	@GenericGenerator(name = "user_id_generator", strategy = "assigned")
	@GeneratedValue(generator = "user_id_generator")
	private Integer userId;

	private String userName;

	private String belongZoneName;

	private Integer totalSubscribeNumber;

	private Integer watchTimeWeek;

	@Enumerated(EnumType.STRING)
	private CacheEnable cacheEnable;

	private String cacheAddress;

	private Integer TimePatternId;

	private Double watchDayProbability;

	@Transient
	public HashSet<Content> WatchListSub = new HashSet<>();

	@Transient
	public int UserAvailableState = 0;

	@Transient
	public LinkedList<Content> CacheLRU = new LinkedList<>();

	@Transient
	public HashSet<Content> CacheSet = new HashSet<>();

	public boolean isAvailable() {
		if (UserAvailableState < StartHereV2.User_MAX_Capacity) {
			return true;
		}
		return false;
	}

	private void deleteOneCache() {
		Content del_content = null;
		TreeMap<Integer, HashSet<Content>> linshi = new TreeMap<>();

		if (StartHereV2.Delmode == DelMode.MaxCopy) {
			for (Content c : CacheSet) {
				int copy = c.ContentCopy.get(belongZoneName).size();
				if (!linshi.containsKey(copy)) {
					linshi.put(copy, new HashSet<>());
				}
				linshi.get(copy).add(c);
			}

			// 以后还要在多判断
			for (Content c : linshi.lastEntry().getValue()) {
				del_content = c;
			}
		}

		if (StartHereV2.Delmode == DelMode.MinExp) {
			for (Content c : CacheSet) {
				int value = c.ValueZone.get(belongZoneName);
				if (!linshi.containsKey(value)) {
					linshi.put(value, new HashSet<>());
				}
				linshi.get(value).add(c);
			}

			// 以后还要在多判断
			for (Content c : linshi.firstEntry().getValue()) {
				del_content = c;
			}
		}

		if (StartHereV2.Delmode == DelMode.MixMuilti) {
			for (Content c : CacheSet) {
				int value = c.ValueZone.get(belongZoneName);
				int copy = c.ContentCopy.get(belongZoneName).size();
				int mult = value * copy;

				if (!linshi.containsKey(mult)) {
					linshi.put(mult, new HashSet<>());
				}
				linshi.get(mult).add(c);
			}

			// 以后还要在多判断
			for (Content c : linshi.firstEntry().getValue()) {
				del_content = c;
			}
		}

		CacheSet.remove(del_content);
		// 向Content中删除信息
		del_content.ContentCopy.get(belongZoneName).remove(this);
	}

	public void addOneContent(Content content) {
		if (CacheSet.size() >= StartHereV2.User_Max_Cache) {
			deleteOneCache();
		}

		CacheSet.add(content);
		// 向Content中注册信息
		content.ContentCopy.get(belongZoneName).add(this);
	}

	private void deleteOneCacheLRU() {
		Content content = CacheLRU.pollLast();
		CacheSet.remove(content);
		// 向Content中删除信息
		content.ContentCopy.get(belongZoneName).remove(this);
	}

	public void addOneContentLRU(Content content) {
		if (CacheSet.size() >= StartHereV2.User_Max_Cache) {
			deleteOneCacheLRU();
		}

		CacheSet.add(content);
		CacheLRU.offerFirst(content);
		// 向Content中注册信息
		content.ContentCopy.get(belongZoneName).add(this);
	}

	public void updataLRUorder(Content content) {
		CacheLRU.remove(content);
		CacheLRU.offerFirst(content);
	}

	public User() {
	}

	public User(Integer userId, String userName) {
		super();
		this.userId = userId;
		this.userName = userName;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getBelongZoneName() {
		return belongZoneName;
	}

	public void setBelongZoneName(String belongZoneName) {
		this.belongZoneName = belongZoneName;
	}

	public Integer getTotalSubscribeNumber() {
		return totalSubscribeNumber;
	}

	public void setTotalSubscribeNumber(Integer totalSubscribeNumber) {
		this.totalSubscribeNumber = totalSubscribeNumber;
	}

	public Integer getWatchTimeWeek() {
		return watchTimeWeek;
	}

	public void setWatchTimeWeek(Integer watchTimeWeek) {
		this.watchTimeWeek = watchTimeWeek;
	}

	public CacheEnable getCacheEnable() {
		return cacheEnable;
	}

	public void setCacheEnable(CacheEnable cacheEnable) {
		this.cacheEnable = cacheEnable;
	}

	public String getCacheAddress() {
		return cacheAddress;
	}

	public void setCacheAddress(String cacheAddress) {
		this.cacheAddress = cacheAddress;
	}

	public Integer getTimePatternId() {
		return TimePatternId;
	}

	public void setTimePatternId(Integer timePatternId) {
		TimePatternId = timePatternId;
	}

	public Double getWatchDayProbability() {
		return watchDayProbability;
	}

	public void setWatchDayProbability(Double watchDayProbability) {
		this.watchDayProbability = watchDayProbability;
	}

	@Override
	public String toString() {
		return "User [userId=" + userId + ", userName=" + userName + ", belongZoneName=" + belongZoneName
				+ ", totalSubscribeNumber=" + totalSubscribeNumber + ", watchTimeWeek=" + watchTimeWeek
				+ ", cacheEnable=" + cacheEnable + ", cacheAddress=" + cacheAddress + ", TimePatternName="
				+ TimePatternId + ", watchDayProbability=" + watchDayProbability + "]";
	}

}

package sql;

import javax.persistence.*;

import org.hibernate.annotations.GenericGenerator;

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

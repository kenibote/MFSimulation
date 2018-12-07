package sql;

import java.util.*;
import javax.persistence.*;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "creater_info")
public class Creater {

	@Id
	@GenericGenerator(name = "creator_id_generator", strategy = "assigned")
	@GeneratedValue(generator = "creator_id_generator")
	private Integer createrId;

	private String createrName;

	private Double zipfLike;

	private Double uploadArrivalRate;

	private int TimePatternId;

	private Integer totalSubscribeNmuber;

	@Enumerated(EnumType.STRING)
	private Popular popular;

	@ElementCollection(targetClass = Integer.class, fetch = FetchType.EAGER)
	@CollectionTable(name = "creater_zoneNumber_info", joinColumns = @JoinColumn(name = "createrId", nullable = false))
	@MapKeyColumn(name = "zoneName")
	@MapKeyClass(String.class)
	@Column(name = "subscribeNumber", nullable = false)
	private Map<String, Integer> zoneSubscribeNumber = new HashMap<>();

	@ElementCollection(targetClass = Integer.class, fetch = FetchType.EAGER)
	@CollectionTable(name = "creater_subscriber_info", joinColumns = @JoinColumn(name = "createrId", nullable = false))
	@Column(name = "subscriberId", nullable = false)
	private Set<Integer> subscribers = new HashSet<>();

	public Creater() {
	}

	public Creater(Integer createrId, String createrName) {
		super();
		this.createrId = createrId;
		this.createrName = createrName;
	}

	public Integer getCreaterId() {
		return createrId;
	}

	public void setCreaterId(Integer createrId) {
		this.createrId = createrId;
	}

	public String getCreaterName() {
		return createrName;
	}

	public void setCreaterName(String createrName) {
		this.createrName = createrName;
	}

	public Double getZipfLike() {
		return zipfLike;
	}

	public void setZipfLike(Double zipfLike) {
		this.zipfLike = zipfLike;
	}

	public Double getUploadArrivalRate() {
		return uploadArrivalRate;
	}

	public void setUploadArrivalRate(Double uploadArrivalRate) {
		this.uploadArrivalRate = uploadArrivalRate;
	}

	public int getTimePatternId() {
		return TimePatternId;
	}

	public void setTimePatternId(int timePatternId) {
		TimePatternId = timePatternId;
	}

	public Integer getTotalSubscribeNmuber() {
		return totalSubscribeNmuber;
	}

	public void setTotalSubscribeNmuber(Integer totalSubscribeNmuber) {
		this.totalSubscribeNmuber = totalSubscribeNmuber;
	}

	public Popular getPopular() {
		return popular;
	}

	public void setPopular(Popular popular) {
		this.popular = popular;
	}

	public Map<String, Integer> getZoneSubscribeNumber() {
		return zoneSubscribeNumber;
	}

	public void setZoneSubscribeNumber(Map<String, Integer> zoneSubscribeNumber) {
		this.zoneSubscribeNumber = zoneSubscribeNumber;
	}

	public Set<Integer> getSubscribers() {
		return subscribers;
	}

	public void setSubscribers(Set<Integer> subscribers) {
		this.subscribers = subscribers;
	}

	@Override
	public String toString() {
		return "Creater [createrId=" + createrId + ", createrName=" + createrName + ", zipfLike=" + zipfLike
				+ ", uploadArrivalRate=" + uploadArrivalRate + ", TimePatternId=" + TimePatternId
				+ ", totalSubscribeNmuber=" + totalSubscribeNmuber + ", popular=" + popular + "]";
	}

}

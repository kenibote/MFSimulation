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

	private String uploadTimePatternName;

	private Integer totalSubscribeNmuber;

	@Enumerated(EnumType.STRING)
	private Popular popular;

	@ElementCollection(targetClass = Integer.class)
	@CollectionTable(name = "creater_zoneNumber_info", joinColumns = @JoinColumn(name = "createrId", nullable = false))
	@MapKeyColumn(name = "zoneId")
	@MapKeyClass(Integer.class)
	@Column(name = "subscribeNumber", nullable = false)
	private Map<Integer, Integer> zoneSubscribeNumber = new HashMap<>();

	@ElementCollection(targetClass = Integer.class)
	@CollectionTable(name = "creater_subscriber_info", joinColumns = @JoinColumn(name = "createrId", nullable = false))
	@Column(name = "subscriberId", nullable = false)
	private Set<Integer> subscribers = new HashSet<>();

	public Creater() {
		// TODO 自动生成的构造函数存根
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

	public String getUploadTimePatternName() {
		return uploadTimePatternName;
	}

	public void setUploadTimePatternName(String uploadTimePatternName) {
		this.uploadTimePatternName = uploadTimePatternName;
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

	public Map<Integer, Integer> getZoneSubscribeNumber() {
		return zoneSubscribeNumber;
	}

	public void setZoneSubscribeNumber(Map<Integer, Integer> zoneSubscribeNumber) {
		this.zoneSubscribeNumber = zoneSubscribeNumber;
	}

	public Set<Integer> getSubscribers() {
		return subscribers;
	}

	public void setSubscribers(Set<Integer> subscribers) {
		this.subscribers = subscribers;
	}

}

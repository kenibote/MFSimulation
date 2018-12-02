package sql;

import javax.persistence.*;
import java.util.*;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "time_pattern")
public class TimePattern {

	@Id
	@GenericGenerator(name = "time_pattern_generator", strategy = "assigned")
	@GeneratedValue(generator = "time_pattern_generator")
	private Integer TimePatternId;

	private String TimePatternName;

	@ElementCollection(targetClass = Double.class)
	@CollectionTable(name = "time_pattern_probability", joinColumns = @JoinColumn(name = "TimePatternId", nullable = false))
	@MapKeyColumn(name = "time_slot")
	@MapKeyClass(Integer.class)
	@Column(name = "probability", nullable = false)
	private Map<Integer, Double> pattern = new TreeMap<>();

	@Transient
	private TreeMap<Double, Integer> rank = new TreeMap<>();

	public TimePattern() {
	}

	public TimePattern(Integer timePatternId, String timePatternName) {
		super();
		TimePatternId = timePatternId;
		TimePatternName = timePatternName;
	}

	public Integer getTimePatternId() {
		return TimePatternId;
	}

	public void setTimePatternId(Integer timePatternId) {
		TimePatternId = timePatternId;
	}

	public String getTimePatternName() {
		return TimePatternName;
	}

	public void setTimePatternName(String timePatternName) {
		TimePatternName = timePatternName;
	}

	public Map<Integer, Double> getPattern() {
		return pattern;
	}

	public void setPattern(Map<Integer, Double> pattern) {
		this.pattern = pattern;
	}

	private void initRank() {
		// 生成rank序列
		double sum = 0.0;
		for (int slot = 0; slot < 24; slot++) {
			rank.put(sum, slot);
			sum = sum + pattern.get(slot);
		}
	}

	@SuppressWarnings("deprecation")
	// 生成时间值
	public Date getRandomTime(int month, int day) {
		// 如果没有初始化，则先初始化
		if (rank.isEmpty()) {
			initRank();
		}

		Random random = new Random();

		double random_value = random.nextDouble();
		int hour = rank.get(rank.floorKey(random_value));
		int min = random.nextInt(60);
		int sec = random.nextInt(60);

		Date date = new Date(2018 - 1900, month - 1, day, hour, min, sec);

		return date;
	}

	@Override
	public String toString() {
		return "TimePattern [TimePatternId=" + TimePatternId + ", TimePatternName=" + TimePatternName + "]";
	}

}

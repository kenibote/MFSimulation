package sql;

import javax.persistence.*;
import java.util.*;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "upload_time_pattern")
public class UploadTimePattern {

	@Id
	@GenericGenerator(name = "upload_time_generator", strategy = "assigned")
	@GeneratedValue(generator = "upload_time_generator")
	private Integer uploadTimePatternId;

	private String uploadTimePatternName;

	@ElementCollection(targetClass = Double.class)
	@CollectionTable(name = "upload_time_probability", joinColumns = @JoinColumn(name = "uploadTimePatternId", nullable = false))
	@MapKeyColumn(name = "time_slot")
	@MapKeyClass(Integer.class)
	@Column(name = "probability", nullable = false)
	private Map<Integer, Double> pattern = new TreeMap<>();
	
	@Transient
	private TreeMap<Double, Integer> rank = new TreeMap<>();

	public UploadTimePattern() {
	}

	public UploadTimePattern(Integer uploadTimePatternId, String uploadTimePatternName) {
		super();
		this.uploadTimePatternId = uploadTimePatternId;
		this.uploadTimePatternName = uploadTimePatternName;
	}

	public Integer getUploadTimePatternId() {
		return uploadTimePatternId;
	}

	public void setUploadTimePatternId(Integer uploadTimePatternId) {
		this.uploadTimePatternId = uploadTimePatternId;
	}

	public String getUploadTimePatternName() {
		return uploadTimePatternName;
	}

	public void setUploadTimePatternName(String uploadTimePatternName) {
		this.uploadTimePatternName = uploadTimePatternName;
	}

	public Map<Integer, Double> getPattern() {
		return pattern;
	}

	public void setPattern(Map<Integer, Double> pattern) {
		this.pattern = pattern;
	}
	
	public void initRank(){
		// TODO
	}
	
	public Date getRandomUploadTime(){
		// TODO
		return null;
	}

}

package sql;

import java.util.Date;

import javax.persistence.*;

import com.alibaba.fastjson.JSON;

@Entity
@Table(name = "time_line_info")
public class Task {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long taskid;

	@Temporal(TemporalType.DATE)
	private Date date;

	private int priority;

	private long time;

	@Enumerated(EnumType.STRING)
	private TaskType taskType;

	// 资源释放任务, 该任务在仿真过程中自动建立
	private String source_address;
	private String source_id;

	// MEC资源整理任务

	// 上传任务
	private int upload_id; // 上传者id
	private String upload_content; // 上传内容的name

	// TODO 请求任务
	private int user_id;

	// 监测任务

	public Task() {
	}

	public long getTaskid() {
		return taskid;
	}

	public void setTaskid(long taskid) {
		this.taskid = taskid;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public TaskType getTaskType() {
		return taskType;
	}

	public void setTaskType(TaskType taskType) {
		this.taskType = taskType;
	}

	public String getSource_address() {
		return source_address;
	}

	public void setSource_address(String source_address) {
		this.source_address = source_address;
	}

	public String getSource_id() {
		return source_id;
	}

	public void setSource_id(String source_id) {
		this.source_id = source_id;
	}

	public int getUpload_id() {
		return upload_id;
	}

	public void setUpload_id(int upload_id) {
		this.upload_id = upload_id;
	}

	public String getUpload_content() {
		return upload_content;
	}

	public void setUpload_content(String upload_content) {
		this.upload_content = upload_content;
	}

	public int getUser_id() {
		return user_id;
	}

	public void setUser_id(int user_id) {
		this.user_id = user_id;
	}

	@Override
	public String toString() {
		return "Task [taskid=" + taskid + ", date=" + date + ", priority=" + priority + ", taskType=" + taskType + "]";
	}

	public String toJSONString(){
		return JSON.toJSONString(this);
	}
}
package sql;

import java.util.*;

import com.alibaba.fastjson.JSON;

public class CheckInfo {

	private long taskid;

	private Date date;

	private long time;

	private HashMap<String, Integer> MEC_Pressure = new HashMap<>();

	public CheckInfo() {
	}

	public long getTaskid() {
		return taskid;
	}

	public void setTaskid(long taskid) {
		this.taskid = taskid;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public HashMap<String, Integer> getMEC_Pressure() {
		return MEC_Pressure;
	}

	public void setMEC_Pressure(HashMap<String, Integer> mEC_Pressure) {
		MEC_Pressure = mEC_Pressure;
	}

	public String toJSON() {
		return JSON.toJSONString(this);
	}

	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
		CheckInfo c = new CheckInfo();
		c.setDate(new Date(2018 - 1900, 0, 1, 12, 34, 56));
		c.setTaskid(c.getDate().getTime());
		c.getMEC_Pressure().put("Zone_1", 5);
		c.getMEC_Pressure().put("Zone_2", 20);

		System.out.println(c.toJSON());
	}

}

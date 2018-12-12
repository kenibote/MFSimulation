package mainfunction;

import java.io.FileWriter;
import java.util.*;

import org.junit.Test;

import com.alibaba.fastjson.JSON;

import redis.clients.jedis.Jedis;
import sql.CheckInfo;
import sql.Content;
import sql.Task;
import sql.TaskResult;
import tool.DataBaseTool;
import tool.GenerateCreaterUser;

public class Analysis {
	static Jedis redis = DataBaseTool.getJedis();

	@SuppressWarnings("deprecation")
	@Test
	public void analysisHit() throws Exception {

		long start_time = new Date(2018 - 1900, 0, 1, 0, 0, 0).getTime();
		long end_time = new Date(2018 - 1900, 0, 4, 23, 59, 59).getTime();
		long batch = 60 * 60 * 1000; // 1 hour
		ArrayList<TimeSlot> Result = new ArrayList<>();
		FileWriter file = getFileForHit();

		// 按照分钟取任务
		while (start_time < end_time) {

			String Line = new Date(start_time).toString();
			TimeSlot timeslot = new TimeSlot(Line);
			Result.add(timeslot);

			// 获取内容并更新时间
			System.out.println("------:" + start_time);
			Set<String> tasklist = redis.zrangeByScore("A_Time_Line_Result", start_time, start_time + batch);
			start_time += batch;

			for (String s : tasklist) {
				// 监测使用
				Task task = JSON.parseObject(s, Task.class);

				timeslot.increase(task.getZoneName(), task.getTaskResult().toString());
			} // end for

			// 输出结果
			file.write(timeslot.name + ",");
			for (int i = 1; i <= GenerateCreaterUser.zoneNumber; i++) {
				file.write(",");
				for (TaskResult t : TaskResult.values()) {
					file.write(timeslot.time_container.get("Zone_" + i).get(t.toString()) + ",");
				}
			}
			file.write("\n");
			file.flush();

		} // end while

		file.close();
	}

	static FileWriter getFileForHit() throws Exception {
		FileWriter file = new FileWriter("D:\\WangNing\\MFSimulation.csv");

		file.write(",");
		for (int i = 1; i <= GenerateCreaterUser.zoneNumber; i++) {
			file.write(",");
			for (TaskResult t : TaskResult.values()) {
				file.write(t.toString() + ",");
			}
		}
		file.write("\n");

		file.write(",");
		for (int i = 1; i <= GenerateCreaterUser.zoneNumber; i++) {
			file.write(",");
			for (int j = 1; j <= TaskResult.values().length; j++) {
				file.write("Zone" + i + ",");
			}
		}
		file.write("\n");

		return file;
	}

	static class TimeSlot {
		String name;

		HashMap<String, HashMap<String, Integer>> time_container = new HashMap<>();

		public TimeSlot(String name) {
			this.name = name;

			for (int i = 1; i <= GenerateCreaterUser.zoneNumber; i++) {
				HashMap<String, Integer> zone_container = new HashMap<>();
				for (TaskResult t : TaskResult.values()) {
					zone_container.put(t.toString(), 0);
				}

				time_container.put("Zone_" + i, zone_container);
			}
		}

		public void increase(String zone, String type) {
			int value = time_container.get(zone).get(type) + 1;
			time_container.get(zone).put(type, value);
		}

	}

	static FileWriter getFileForPressure() throws Exception {
		FileWriter file = new FileWriter("D:\\WangNing\\MFSimulationPressure.csv");

		file.write(",");
		for (int i = 1; i <= GenerateCreaterUser.zoneNumber; i++) {
			file.write("Zone_" + i + ",");
		}
		file.write("\n");

		return file;
	}

	@SuppressWarnings("deprecation")
	@Test
	public void analysisPressure() throws Exception {

		long start_time = new Date(2018 - 1900, 0, 3, 0, 0, 0).getTime();
		long end_time = new Date(2018 - 1900, 0, 4, 23, 59, 59).getTime();
		long batch = 60 * 60 * 1000; // 1 hour
		FileWriter file = getFileForPressure();

		// 按照分钟取任务
		while (start_time < end_time) {

			String Line = new Date(start_time).toString();

			// 获取内容并更新时间
			System.out.println("------:" + start_time);
			Set<String> infolist = redis.zrangeByScore("Check_Info", start_time, start_time + batch);
			start_time += batch;

			for (String s : infolist) {
				// 监测使用
				CheckInfo info = JSON.parseObject(s, CheckInfo.class);

				file.write(Line + ",");
				for (int i = 1; i <= GenerateCreaterUser.zoneNumber; i++) {
					file.write(info.getMEC_Pressure().get("Zone_" + i) + ",");
				}
				file.write("\n");
			} // end for

			file.flush();
		} // end while

		file.close();
	}

	@SuppressWarnings("deprecation")
	public static void ContentWatchCount(Date date) {
		String fileName = StartHereV2.mecmode + "_" + StartHereV2.fogmode + "_TotalWC_" + date.getDate() + ".csv";
		String fileName2 = StartHereV2.mecmode + "_" + StartHereV2.fogmode + "_TopWatch_" + date.getDate() + ".csv";
		FileWriter file, file2;
		try {
			file = new FileWriter("D:\\WangNing\\" + fileName);
			file2 = new FileWriter("D:\\WangNing\\" + fileName2);
			for (Content c : StartHereV2.ContentAll) {
				file.write(c.ContentName + "," + c.totalWatchCount + ",\n");
				file2.write(c.ContentName + "," + c.watchCount + ",\n");
			}
			file.close();
			file2.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

package mainfunction;

import java.util.*;

import org.junit.Test;

import com.alibaba.fastjson.JSON;

import redis.clients.jedis.Jedis;
import sql.Task;
import sql.TaskResult;
import tool.DataBaseTool;

public class Analysis {
	static Jedis redis = DataBaseTool.getJedis();

	@SuppressWarnings("deprecation")
	@Test
	public void analysisHit() {

		long start_time = new Date(2018 - 1900, 0, 1, 0, 0, 0).getTime();
		long end_time = new Date(2018 - 1900, 0, 31, 23, 59, 59).getTime();
		long batch = 60 * 1000; // 60s

		// 按照分钟取任务
		while (start_time < end_time) {
			// 监测使用
			System.out.println("------:" + start_time);

			Set<String> tasklist = redis.zrangeByScore("A_Time_Line_Result", start_time, start_time + batch);
			// 更新时间
			start_time += batch;

			for (String s : tasklist) {
				// 监测使用
				System.out.println(s);
				Task task = JSON.parseObject(s, Task.class);

				String target = task.getZoneName();
				// Total_Zone_1_16
				redis.hincrBy("AnalysisHit", "Total_" + target + "_" + task.getDate().getDate(), 1);

				if (task.getTaskResult() == TaskResult.Self_MEC) {
					redis.hincrBy("AnalysisHit", "Self_" + target + "_" + task.getDate().getDate(), 1);
				}

				if (task.getTaskResult() == TaskResult.Other_MEC) {
					redis.hincrBy("AnalysisHit", "OtherMec_" + target + "_" + task.getDate().getDate(), 1);
				}

				if (task.getTaskResult() == TaskResult.Self_Zone_Users) {
					redis.hincrBy("AnalysisHit", "Fog_" + target + "_" + task.getDate().getDate(), 1);
				}

				if (task.getTaskResult() == TaskResult.Original) {
					redis.hincrBy("AnalysisHit", "Original_" + target + "_" + task.getDate().getDate(), 1);
				}

			} // end for

		} // end while
	}

	@Test
	public void analysisPressure() {

	}
}

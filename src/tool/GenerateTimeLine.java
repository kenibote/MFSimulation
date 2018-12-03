package tool;

import java.util.*;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Test;

import redis.clients.jedis.Jedis;
import sql.*;

public class GenerateTimeLine {
	// 内容存在Redis sort set中， 值为时间。
	// task 具有优先级。

	// 2018-1-1 00:00:00
	static long start_time = 1514736000000L;

	@Test
	public void generateMEC_Arrange_Task() {
		Session session = DataBaseTool.getSession();
		Transaction tx = session.beginTransaction();
		// ------------------------------------------

		// MEC arrange 任务从第3天开始，前2天要保证有充足的内容被上传
		long time = start_time + 2 * 24 * 60 * 60 * 1000;

		for (int day = 3; day <= 31; day++) {
			for (int hour = 0; hour < 24; hour++) {
				Task task = new Task();
				task.setDate(new Date(time));
				task.setPriority(TaskType.MEC_Cache_Arrange.ordinal());
				task.setTime(task.getDate().getTime() + task.getPriority());
				task.setTaskType(TaskType.MEC_Cache_Arrange);

				session.save(task);
				// 时间往后移动1小时
				time = time + 60 * 60 * 1000;
			}
		}

		// ------------------------------------------
		tx.commit();
		session.close();
		DataBaseTool.closeSessionFactory();

	}

	@Test
	public void generateMEC_Check_Task() {
		Session session = DataBaseTool.getSession();
		Transaction tx = session.beginTransaction();
		// ------------------------------------------

		// MEC check 任务从第3天开始，前2天要保证有充足的内容被上传; 每隔5分钟检查一次
		long time = start_time + 2 * 24 * 60 * 60 * 1000;

		for (int day = 3; day <= 31; day++) {
			for (int hour = 0; hour < 24; hour++) {
				for (int min = 0; min < 60; min += 5) {
					Task task = new Task();
					task.setDate(new Date(time));
					task.setPriority(TaskType.MEC_Pressure_Check.ordinal());
					task.setTime(task.getDate().getTime() + task.getPriority());
					task.setTaskType(TaskType.MEC_Pressure_Check);

					session.save(task);
					// 时间往后移动5分钟
					time = time + 5 * 60 * 1000;
				}
			}
			System.out.println(day);
		}

		// ------------------------------------------
		tx.commit();
		session.close();
		DataBaseTool.closeSessionFactory();

	}

	@SuppressWarnings({ "deprecation", "unchecked" })
	@Test
	public void transferTasktoRedis() {
		Session session = DataBaseTool.getSession();
		Transaction tx = session.beginTransaction();
		// ------------------------------------------

		Jedis jedis = DataBaseTool.getJedis();

		List<Task> taskList = session.createCriteria(Task.class).list();
		for (Task task : taskList) {
			jedis.zadd("A_Time_Line", task.getTime(), task.toJSONString());
		}

		// ------------------------------------------
		tx.commit();
		session.close();
		DataBaseTool.closeSessionFactory();
	}

}

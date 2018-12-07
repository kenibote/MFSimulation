package tool;

import java.math.BigInteger;
import java.util.*;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.junit.Test;

import redis.clients.jedis.Jedis;
import sql.*;

public class GenerateTimeLine {
	// 内容存在Redis sort set中， 值为时间。
	// task 具有优先级。

	// 2018-1-1 00:00:00
	static long start_time = 1514736000000L;
	static int EndDay = 31;

	@Test
	public void generateMEC_Arrange_Task() {
		Session session = DataBaseTool.getSession();
		Transaction tx = session.beginTransaction();
		// ------------------------------------------

		// MEC arrange 任务从第3天开始，前2天要保证有充足的内容被上传
		long time = start_time + 2 * 24 * 60 * 60 * 1000;

		for (int day = 3; day <= EndDay; day++) {
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

		for (int day = 3; day <= EndDay; day++) {
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

	@Test
	public void generateCreaterUploadTask() {
		Session session = DataBaseTool.getSession();
		Transaction tx = session.beginTransaction();
		// ------------------------------------------
		// 生成上传时间缓存
		HashMap<Integer, TimePattern> timeMap = new HashMap<>();
		for (int id = 1; id <= 34; id++) {
			timeMap.put(id, session.get(TimePattern.class, id));
		}

		// 为每个creater生成上传task
		for (int id = 1; id <= GenerateCreaterUser.TotalCreaterNumber; id++) {
			Creater creater = session.get(Creater.class, id);
			// 打印监测信息
			System.out.println(creater.getCreaterId());

			// 先产生每天上传多少个视频， 服从泊松分布
			int[] uploadNumber = new int[EndDay + 1];
			for (int day = 1; day <= EndDay; day++) {
				uploadNumber[day] = getPossionVariable(creater.getUploadArrivalRate());
			}

			int contentcount = 1;
			// 为每一天创建task
			for (int day = 1; day <= EndDay; day++) {
				String pix = "C_2018_01_" + getFormateNumber(day, 2) + "_" + creater.getCreaterName() + "_";

				// 如果该天有多个上传任务
				while (uploadNumber[day]-- > 0) {
					String contentName = pix + getFormateNumber(contentcount++, 3);

					Task task = new Task();
					task.setPriority(TaskType.Upload.ordinal());
					task.setTaskType(TaskType.Upload);
					task.setDate(timeMap.get(creater.getTimePatternId()).getRandomTime(1, day));
					task.setTime(task.getDate().getTime() + task.getPriority());
					// 设置上传者信息
					task.setUpload_id(creater.getCreaterId());
					task.setUpload_content(contentName);
					// 保存任务
					session.save(task);
				} // end while
			} // end for

			session.flush();
		}

		// ------------------------------------------
		tx.commit();
		session.close();
		DataBaseTool.closeSessionFactory();
	}

	@Test
	public void generateUserRequestTast_1() {
		Session session = DataBaseTool.getSession();
		Transaction tx = session.beginTransaction();
		// ------------------------------------------

		Random random = new Random();
		// 目前没有用user中存的信息
		double watchpro = 50.0 / 7.0;
		TimePattern tp = session.get(TimePattern.class, 34);

		// 用户请求任务从第3天开始
		for (int day = 3; day <= EndDay; day++) {
			// 监测使用
			System.out.println(day);

			// 为每一位用户设置请求任务
			for (int u_id = 1; u_id <= GenerateCreaterUser.TotalUserNumber; u_id++) {
				double whether_wahct = random.nextDouble() * 10;

				// 代表今天会观看
				if (whether_wahct < watchpro) {
					// 获取用户的信息
					User user = session.get(User.class, u_id);

					// 得到会看多少个视频的信息
					// 目前没有用user中存的信息
					double ratio = random.nextDouble() * 0.4 + 0.8;
					int watch_time = (int) (user.getTotalSubscribeNumber() * ratio);

					// 产生时间点
					HashSet<Long> time_point = new HashSet<>();
					while (time_point.size() < watch_time) {
						time_point.add(tp.getRandomTime(1, day).getTime());
					}

					// 根据获取的时间，创建任务
					for (long task_time : time_point) {
						Task task = new Task();
						task.setTaskType(TaskType.Request);
						task.setPriority(TaskType.Request.ordinal());
						task.setDate(new Date(task_time));
						task.setTime(task.getDate().getTime() + task.getPriority());

						task.setUser_id(u_id);
						task.setZoneName(user.getBelongZoneName());
						session.save(task);
					}

				} // end if

				if (u_id % 1000 == 0)
					System.out.print("#");
			} // end for user

			// 重新开启任务
			tx.commit();
			session.clear();
			tx = session.beginTransaction();

			System.out.println("Done");
		} // end for day

		// ------------------------------------------
		tx.commit();
		session.close();
		DataBaseTool.closeSessionFactory();
	}

	@Test
	public void generateUserRequestTast_2() {
		Session session = DataBaseTool.getSession();
		Transaction tx = session.beginTransaction();
		// ------------------------------------------

		Random random = new Random();
		// 目前没有用user中存的信息
		double watchpro = 50.0 / 7.0;
		TimePattern tp = session.get(TimePattern.class, 34);

		// 为每一位用户设置请求任务
		for (int u_id = 1; u_id <= GenerateCreaterUser.TotalUserNumber; u_id++) {
			// 获取用户的信息
			User user = session.get(User.class, u_id);
			// 监测使用
			System.out.println(u_id);

			// 用户请求任务从第3天开始
			for (int day = 3; day <= EndDay; day++) {
				double whether_watch = random.nextDouble() * 10;

				// 代表今天会观看
				if (whether_watch < watchpro) {
					// 得到会看多少个视频的信息
					// 目前没有用user中存的信息
					double ratio = random.nextDouble() * 0.4 + 0.8;
					int watch_time = (int) (user.getTotalSubscribeNumber() * ratio);

					// 产生时间点
					HashSet<Long> time_point = new HashSet<>();
					while (time_point.size() < watch_time) {
						time_point.add(tp.getRandomTime(1, day).getTime());
					}

					// 根据获取的时间，创建任务
					for (long task_time : time_point) {
						Task task = new Task();
						task.setTaskType(TaskType.Request);
						task.setPriority(TaskType.Request.ordinal());
						task.setDate(new Date(task_time));
						task.setTime(task.getDate().getTime() + task.getPriority());

						task.setUser_id(u_id);
						task.setZoneName(user.getBelongZoneName());
						session.save(task);
					}

				} // end if

				if (u_id % 1000 == 0)
					System.out.print("#");
			} // end for user

			// 重新开启任务
			if (u_id % 10 == 0) {
				tx.commit();
				session.clear();
				tx = session.beginTransaction();
			}

			System.out.println("Done");
		} // end for day

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
		int start = 0;
		int batch = 10_0000;

		while (start < 2040_0000) {
			Criteria criteria = session.createCriteria(Task.class);
			criteria.setFirstResult(start);
			criteria.setMaxResults(batch);

			List<Task> taskList = criteria.list();
			for (Task task : taskList) {
				jedis.zadd("A_Time_Line", task.getTime(), task.toJSONString());
			}

			session.clear();
			start += batch;
			System.out.print(start);
		}

		// ------------------------------------------
		tx.commit();
		session.close();
		DataBaseTool.closeSessionFactory();
	}

	@SuppressWarnings({ "deprecation", "unchecked" })
	@Test
	public void transferUploadTasktoRedis() {
		Session session = DataBaseTool.getSession();
		Transaction tx = session.beginTransaction();
		// ------------------------------------------

		Jedis jedis = DataBaseTool.getJedis();

		Criteria criteria = session.createCriteria(Task.class);
		criteria.add(Restrictions.eq("priority", TaskType.Upload.ordinal()));

		List<Task> taskList = criteria.list();
		for (Task task : taskList) {
			jedis.zadd("A_Time_Line", task.getTime(), task.toJSONString());
		}

		session.clear();

		// ------------------------------------------
		tx.commit();
		session.close();
		DataBaseTool.closeSessionFactory();
	}

	@SuppressWarnings({ "deprecation", "unchecked" })
	@Test
	public void TestZoneRequestPatternInOneDay() {
		Session session = DataBaseTool.getSession();
		Transaction tx = session.beginTransaction();
		// ------------------------------------------

		int day = 3;
		String zoneName = "Zone_1";
		long time_start = new Date(2018 - 1900, 0, day, 0, 0, 0).getTime();
		long time_end = new Date(2018 - 1900, 0, day + 1, 0, 0, 0).getTime();

		Criteria criteria = session.createCriteria(Task.class);
		criteria.add(Restrictions.eq("priority", TaskType.Request.ordinal()));
		criteria.add(Restrictions.eq("zoneName", zoneName));
		criteria.add(Restrictions.gt("time", time_start));
		criteria.add(Restrictions.lt("time", time_end));

		int[] count = new int[24];
		List<Task> tasklist = criteria.list();
		for (Task task : tasklist) {
			count[task.getDate().getHours()]++;
		}

		// 画图
		TimeSeries timeseries = new TimeSeries("Request Count");
		for (int i = 0; i < 24; i++) {
			timeseries.add(new Hour(i, day, 1, 2018), count[i]);
		}
		TimeSeriesCollection timeseriescollection = new TimeSeriesCollection();
		timeseriescollection.addSeries(timeseries);

		DrawPicture.DrawTimeLine(timeseriescollection, "CheckTimePattern", "Hour", "Request");
		DrawPicture.waitExit();

		// ------------------------------------------
		tx.commit();
		session.close();
		DataBaseTool.closeSessionFactory();
	}

	@SuppressWarnings({ "deprecation", "rawtypes" })
	@Test
	public void TestZoneTotalRequest() {
		Session session = DataBaseTool.getSession();
		Transaction tx = session.beginTransaction();
		// ------------------------------------------

		int start_day = 3;
		String zoneName = "Zone_1";
		int[] count = new int[EndDay + 1];

		for (int day = start_day; day <= EndDay; day++) {
			System.out.print(day + "......");
			long time_start = new Date(2018 - 1900, 0, day, 0, 0, 0).getTime();
			long time_end = new Date(2018 - 1900, 0, day + 1, 0, 0, 0).getTime();

			String sql = " SELECT count(*) FROM mfsimulation.time_line_info "
					+ "where time > :time_start and time < :time_end and zoneName = :zonename";

			Query query = session.createSQLQuery(sql);
			query.setParameter("time_start", time_start);
			query.setParameter("time_end", time_end);
			query.setParameter("zonename", zoneName);

			count[day] = ((BigInteger) query.uniqueResult()).intValue();
			System.out.println(count[day]);

			// 清理垃圾
			session.clear();
		}

		// 画图
		TimeSeries timeseries = new TimeSeries("Request Count per Day " + zoneName);
		for (int day = 1; day <= EndDay; day++) {
			timeseries.add(new Day(day, 1, 2018), count[day]);
		}
		TimeSeriesCollection timeseriescollection = new TimeSeriesCollection();
		timeseriescollection.addSeries(timeseries);

		DrawPicture.DrawTimeLine(timeseriescollection, "CheckTimePattern", "Day", "Request");
		DrawPicture.waitExit();

		// ------------------------------------------
		tx.commit();
		session.close();
		DataBaseTool.closeSessionFactory();
	}

	@SuppressWarnings({ "deprecation", "unchecked" })
	@Test
	public void TestUserMonthPattern() {
		Session session = DataBaseTool.getSession();
		Transaction tx = session.beginTransaction();
		// ------------------------------------------

		int user_id = 20;
		int[] count = new int[EndDay + 1];

		Criteria criteria = session.createCriteria(Task.class);
		criteria.add(Restrictions.eq("priority", TaskType.Request.ordinal()));
		criteria.add(Restrictions.eq("user_id", user_id));

		List<Task> tasklist = criteria.list();
		for (Task task : tasklist) {
			count[task.getDate().getDate()]++;
		}

		// 画图
		TimeSeries timeseries = new TimeSeries("Request Count for User " + user_id);
		for (int day = 1; day <= EndDay; day++) {
			timeseries.add(new Day(day, 1, 2018), count[day]);
		}
		TimeSeriesCollection timeseriescollection = new TimeSeriesCollection();
		timeseriescollection.addSeries(timeseries);

		DrawPicture.DrawTimeLine(timeseriescollection, "CheckTimePattern", "Day", "Request");
		DrawPicture.waitExit();

		// ------------------------------------------
		tx.commit();
		session.close();
		DataBaseTool.closeSessionFactory();
	}

	public static String getFormateNumber(int data, int len) {
		int length = ("" + data).length();
		String res = "";

		len = len - length;
		while (len-- > 0) {
			res = res + "0";
		}
		res = res + data;

		return res;
	}

	@Test
	public void TestPosion() {
		double p = 2.0;

		int total = 10000;
		HashMap<Integer, Integer> count = new HashMap<>();
		for (int i = 1; i <= total; i++) {
			int pos = getPossionVariable(p);

			if (!count.containsKey(pos)) {
				count.put(pos, 0);
			}

			int val = count.get(pos) + 1;
			count.put(pos, val);
		}

		// Prepare data
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for (int i : count.keySet()) {
			dataset.addValue(count.get(i) / (double) total, "Value", "" + i);
		}

		// 画图
		DrawPicture.DrawChart(dataset, "Propability Count", "Time", "Count");
		DrawPicture.waitExit();
	}

	// 产生泊松分布随机数
	private static int getPossionVariable(double lamda) {
		int x = 0;
		double y = Math.random(), cdf = getPossionProbability(x, lamda);
		while (cdf < y) {
			x++;
			cdf += getPossionProbability(x, lamda);
		}
		return x;
	}

	private static double getPossionProbability(int k, double lamda) {
		double c = Math.exp(-lamda), sum = 1;
		for (int i = 1; i <= k; i++) {
			sum *= lamda / i;
		}
		return sum * c;
	}

}

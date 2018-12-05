package tool;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jfree.data.category.DefaultCategoryDataset;
import org.junit.Test;

import java.io.*;
import java.util.*;

import sql.CacheEnable;
import sql.Creater;
import sql.Popular;
import sql.TimePattern;
import sql.User;

public class GenerateCreaterUser {

	// Configuration
	public static int TotalCreaterNumber = 1000;
	public static double zipfAlpha = 1.0;
	public static double popular_ratio = 0.2;
	public static int zoneNumber = 4;
	public static int[] userNumber = { 0, 2500, 2500, 2500, 2500 };
	public static int TotalUserNumber = 10000;

	// TODO Questions:
	/*
	 * 0.2的比例是否合适？ alpha = 0.88 ==> 0.35 alpha = 1.0 ==> 0.23
	 */

	@Test
	// 第一步：生成基本creater的信息； id, name, zipfLike, popular
	public void generateBasicCreaterInfo() {
		Session session = DataBaseTool.getSession();
		Transaction tx = session.beginTransaction();
		// ------------------------------------------
		HashMap<Integer, Double> map = new HashMap<>();
		double sum = 0.0;
		for (int i = 1; i <= TotalCreaterNumber; i++) {
			double value = 1 / Math.pow(i, zipfAlpha);
			sum = sum + value;
			map.put(i, value);
		}

		for (int i = 1; i <= TotalCreaterNumber; i++) {
			Creater c = new Creater(i, "Creater_" + GenerateTimeLine.getFormateNumber(i, 4));
			double value = map.get(i) / sum;
			c.setZipfLike(value);
			if (i <= TotalCreaterNumber * popular_ratio) {
				c.setPopular(Popular.YES);
			} else {
				c.setPopular(Popular.NO);
			}
			session.save(c);
		}

		// ------------------------------------------
		tx.commit();
		session.close();
		DataBaseTool.closeSessionFactory();
	}

	@Test
	public void generateCreaterArrivalRate() {
		Session session = DataBaseTool.getSession();
		Transaction tx = session.beginTransaction();
		// ------------------------------------------
		Random random = new Random();

		for (int id = 1; id <= TotalCreaterNumber; id++) {
			Creater c = session.get(Creater.class, id);

			// 高活跃度创作者的上传频率更高。 这样平均每日的到达率在1000附近
			if (id <= TotalCreaterNumber * popular_ratio) {
				c.setUploadArrivalRate(random.nextDouble() + 1.3);
			} else {
				c.setUploadArrivalRate(random.nextDouble() + 0.3);
			}
		}

		// ------------------------------------------
		tx.commit();
		session.close();
		DataBaseTool.closeSessionFactory();
	}

	@Test
	// 平坦时间分布 id = 1
	public void TimePattern_1_flat() {
		Session session = DataBaseTool.getSession();
		Transaction tx = session.beginTransaction();
		// ------------------------------------------

		TimePattern up = new TimePattern(1, "flat");
		for (int i = 0; i < 24; i++) {
			up.getPattern().put(i, 1.0 / 24);
		}

		session.save(up);

		// ------------------------------------------
		tx.commit();
		session.close();
		DataBaseTool.closeSessionFactory();
	}

	@Test
	public void LoadRealTimePattern() throws Exception {
		Session session = DataBaseTool.getSession();
		Transaction tx = session.beginTransaction();
		// ------------------------------------------

		int id = 2;
		Scanner file = new Scanner(
				new FileReader(new File("/Users/kenibote/Documents/workspace/MFSimulation/CreaterTime.csv")));
		System.out.println(file.nextLine());

		while (file.hasNextLine()) {
			String[] comp = file.nextLine().split(",");
			double sum = 0;
			TreeMap<Integer, Double> temp = new TreeMap<>();
			for (int slot = 0; slot < 24; slot++) {
				temp.put(slot, Double.parseDouble(comp[slot + 1]));
				sum = sum + temp.get(slot);
			}

			TimePattern up = new TimePattern(id++, comp[0]);
			for (int slot = 0; slot < 24; slot++) {
				up.getPattern().put(slot, temp.get(slot) / sum);
			}
			session.save(up);

		}

		file.close();

		// ------------------------------------------
		tx.commit();
		session.close();
		DataBaseTool.closeSessionFactory();
	}

	static class OneND {
		int id;
		String name;
		double u;
		double o;

		public OneND(int id, String name, double u, double o) {
			super();
			this.id = id;
			this.name = name;
			this.u = u;
			this.o = o;
		}
	}

	// 生成正态分布函数 24小时循环
	public static TreeMap<Integer, Double> NormalDistribution(double u, double o) {
		TreeMap<Integer, Double> treeMap = new TreeMap<>();
		LinkedList<Double> list = new LinkedList<>();
		double sum = 0.0;

		double v1 = 1.0 / (Math.sqrt(2 * Math.PI) * o);
		for (int x = -12; x < 12; x++) {
			double v2 = Math.pow(x, 2) / (2 * o * o);
			double value = v1 * Math.exp(-v2);

			list.offerFirst(value);
			sum = sum + value;
		}

		// 位移
		boolean direction = (u - 12) < 0 ? true : false;
		int U = (int) Math.abs(u - 12);
		while (U-- > 0) {
			if (direction) {
				double value = list.pollLast();
				list.offerFirst(value);
			} else {
				double value = list.pollFirst();
				list.offerLast(value);
			}
		}

		// 归一化
		for (int i = 0; i < 24; i++) {
			double value = list.pollLast() / sum;
			treeMap.put(i, value);
		}

		return treeMap;
	}

	public static HashSet<OneND> getOneND() {
		HashSet<OneND> data = new HashSet<>();

		data.add(new OneND(22, "OneND_dinner_17_15", 17.0, 1.5));
		data.add(new OneND(23, "OneND_dinner_17_20", 17.0, 2.0));
		data.add(new OneND(24, "OneND_dinner_17_25", 17.0, 2.5));
		data.add(new OneND(25, "OneND_dinner_18_15", 18.0, 1.5));
		data.add(new OneND(26, "OneND_dinner_18_20", 18.0, 2.0));
		data.add(new OneND(27, "OneND_dinner_18_25", 18.0, 2.5));

		data.add(new OneND(28, "OneND_noon_10_15", 10.0, 1.5));
		data.add(new OneND(29, "OneND_noon_10_20", 10.0, 2.0));
		data.add(new OneND(30, "OneND_noon_10_25", 10.0, 2.5));
		data.add(new OneND(31, "OneND_noon_11_15", 11.0, 1.5));
		data.add(new OneND(32, "OneND_noon_11_20", 11.0, 2.0));
		data.add(new OneND(33, "OneND_noon_11_25", 11.0, 2.5));

		return data;
	}

	@Test
	// 生成单一正态时间分布
	public void TimePattern_One_ND() {
		Session session = DataBaseTool.getSession();
		Transaction tx = session.beginTransaction();
		// ------------------------------------------

		for (OneND ond : getOneND()) {

			TimePattern up = new TimePattern(ond.id, ond.name);
			TreeMap<Integer, Double> nd = NormalDistribution(ond.u, ond.o);
			for (int i = 0; i < 24; i++) {
				up.getPattern().put(i, nd.get(i));
			}

			session.save(up);
		}

		// ------------------------------------------
		tx.commit();
		session.close();
		DataBaseTool.closeSessionFactory();
	}

	public static HashMap<Integer, HashMap<TreeMap<Integer, Double>, Double>> MoreND_data() {
		HashMap<Integer, HashMap<TreeMap<Integer, Double>, Double>> data = new HashMap<>();

		HashMap<TreeMap<Integer, Double>, Double> comb1 = new HashMap<>();
		comb1.put(NormalDistribution(11, 1.8), 3.0);
		comb1.put(NormalDistribution(17, 0.7), 1.0);
		comb1.put(NormalDistribution(20, 1.8), 4.0);

		data.put(34, comb1);

		return data;
	}

	@Test
	// 多个正态分布叠加，构成复杂曲线图
	public void TimePattern_More_ND() {
		Session session = DataBaseTool.getSession();
		Transaction tx = session.beginTransaction();
		// ------------------------------------------

		HashMap<Integer, HashMap<TreeMap<Integer, Double>, Double>> data = MoreND_data();

		for (int id : data.keySet()) {
			HashMap<TreeMap<Integer, Double>, Double> comb = data.get(id);
			TimePattern up = new TimePattern(id, "MoreND_" + id);

			TreeMap<Integer, Double> temp = new TreeMap<>();
			double sum = 0;

			for (int slot = 0; slot < 24; slot++) {
				double value = 0;
				for (TreeMap<Integer, Double> nd : comb.keySet()) {
					value = value + comb.get(nd) * nd.get(slot);
				}
				temp.put(slot, value);
				sum = sum + value;
			}

			for (int slot = 0; slot < 24; slot++) {
				up.getPattern().put(slot, temp.get(slot) / sum);
			}

			session.save(up);
		}

		// ------------------------------------------
		tx.commit();
		session.close();
		DataBaseTool.closeSessionFactory();
	}

	@Test
	public void DeleteTimePatternBatch() {
		Session session = DataBaseTool.getSession();
		Transaction tx = session.beginTransaction();
		// ------------------------------------------

		int[] delList = { 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34 };
		// int[] delList = { 14 };
		for (int id : delList) {
			TimePattern up = session.get(TimePattern.class, id);
			session.delete(up);
		}

		// ------------------------------------------
		tx.commit();
		session.close();
		DataBaseTool.closeSessionFactory();
	}

	@SuppressWarnings("deprecation")
	@Test
	// 测试时间产生函数
	public void TestTimePatternRank() {
		Session session = DataBaseTool.getSession();
		Transaction tx = session.beginTransaction();
		// ------------------------------------------
		int[] count = new int[24];
		// 测试次数
		int sum = 100000;

		// 此处设置需要测试的id
		TimePattern up = session.get(TimePattern.class, 17);
		for (int i = 1; i <= sum; i++) {
			Date date = up.getRandomTime(1, 1);

			count[date.getHours()]++;
		}

		// Prepare data
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for (int i = 0; i < 24; i++) {
			dataset.addValue(count[i] / (double) sum, "Value", "" + i);
		}

		// 画图
		DrawPicture.DrawChart(dataset, "Propability Count", "Time", "Count");
		DrawPicture.waitExit();

		// ------------------------------------------
		tx.commit();
		session.close();
		DataBaseTool.closeSessionFactory();
	}

	@Test
	public void assignCreaterTimePattern() {
		Session session = DataBaseTool.getSession();
		Transaction tx = session.beginTransaction();
		// ------------------------------------------
		Random random = new Random();

		for (int id = 1; id <= TotalCreaterNumber; id++) {
			Creater c = session.get(Creater.class, id);

			// 高活跃度的创作者服从真实分布，地活跃度的创作者时间更均摊
			if (id <= TotalCreaterNumber * popular_ratio * 2) {
				c.setTimePatternId(random.nextInt(32) + 2);
			} else {
				c.setTimePatternId(1);
			}
		}

		// ------------------------------------------
		tx.commit();
		session.close();
		DataBaseTool.closeSessionFactory();
	}

	@Test
	// 产生基本的User信息 id, name, zone, cache_address
	public void generateUserBasicInfo() {
		Session session = DataBaseTool.getSession();
		Transaction tx = session.beginTransaction();
		// ------------------------------------------

		int id = 1;
		for (int z = 1; z <= zoneNumber; z++) {
			for (int i = 1; i <= userNumber[z]; i++) {
				User user = new User(id++, "User_" + z + "_" + GenerateTimeLine.getFormateNumber(i, 4));
				user.setBelongZoneName("Zone_" + z);
				user.setCacheAddress("ContentCache_" + user.getUserName());
				session.save(user);
			}
			session.flush();
			session.clear();
		}

		// ------------------------------------------
		tx.commit();
		session.close();
		DataBaseTool.closeSessionFactory();
	}

	@Test
	public void gengerateSubscribeInfoForUser() {
		Session session = DataBaseTool.getSession();
		Transaction tx = session.beginTransaction();
		// ------------------------------------------
		HashMap<Integer, Double> zipfMap = new HashMap<>();
		double sum = 0.0;
		for (int i = 1; i <= TotalCreaterNumber; i++) {
			double value = 1 / Math.pow(i, zipfAlpha);
			sum = sum + value;
			zipfMap.put(i, value);
		}

		double bond = 0;
		TreeMap<Double, Integer> rank = new TreeMap<>();
		for (int id = 1; id <= TotalCreaterNumber; id++) {
			rank.put(bond, id);
			bond = bond + zipfMap.get(id) / sum;
		}

		// 准备随机数
		Random random = new Random();

		for (int user_id = 1; user_id <= TotalUserNumber; user_id++) {
			User user = session.get(User.class, user_id);
			int sub_number = (int) (random.nextGaussian() * 15 + 100);
			// 监测使用
			System.out.println("ID:" + user.getUserId() + " sub_number:" + sub_number);

			// 设置用户总订阅数
			user.setTotalSubscribeNumber(sub_number);

			// 根据订阅数，按照zipf分布生成订阅id
			HashSet<Integer> user_sub_id = new HashSet<>();
			while (user_sub_id.size() < sub_number) {
				int cr_id = rank.floorEntry(random.nextDouble()).getValue();
				user_sub_id.add(cr_id);
			}

			// 更新创作者的信息
			for (int cr_id : user_sub_id) {
				session.get(Creater.class, cr_id).getSubscribers().add(user.getUserId());
			}
		}

		// ------------------------------------------
		tx.commit();
		session.close();
		DataBaseTool.closeSessionFactory();
	}

	@Test
	public void deleteSubscribeInfoForUser() {
		Session session = DataBaseTool.getSession();
		Transaction tx = session.beginTransaction();
		// ------------------------------------------

		for (int user_id = 1; user_id <= TotalUserNumber; user_id++) {
			User user = session.get(User.class, user_id);
			user.setTotalSubscribeNumber(0);
			user.setTimePatternId(0);
			user.setWatchTimeWeek(0);
		}

		for (int cr_id = 1; cr_id <= TotalCreaterNumber; cr_id++) {
			Creater creater = session.get(Creater.class, cr_id);
			creater.setTotalSubscribeNmuber(0);
			creater.getSubscribers().clear();
			creater.getZoneSubscribeNumber().clear();
		}

		// ------------------------------------------
		tx.commit();
		session.close();
		DataBaseTool.closeSessionFactory();
	}

	@Test
	public void TestGussDistribution() {

		Random random = new Random();
		int sum = 10000;

		TreeMap<Integer, Integer> count = new TreeMap<>();

		for (int i = 1; i <= sum; i++) {
			int target = (int) (random.nextGaussian() * 15 + 100);

			if (!count.containsKey(target)) {
				count.put(target, 0);
			}

			int val = count.get(target) + 1;
			count.put(target, val);
		}

		// Prepare data
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for (int i : count.keySet()) {
			System.out.println(i);
			dataset.addValue(count.get(i) / (double) sum, "Value", "" + i);
		}

		// 画图
		DrawPicture.DrawChart(dataset, "Propability Count", "Time", "Count");
		DrawPicture.waitExit();

	}

	@Test
	public void generateTotalWatchTimeforUser() {
		Session session = DataBaseTool.getSession();
		Transaction tx = session.beginTransaction();
		// ------------------------------------------
		Random random = new Random();

		for (int id = 1; id <= TotalUserNumber; id++) {
			User user = session.get(User.class, id);

			// 范围是 0.8 ~ 1.2 之间
			double ratio = random.nextDouble() * 0.4 + 0.8;
			int watchTime = (int) (user.getTotalSubscribeNumber() * 5 * ratio);
			user.setWatchTimeWeek(watchTime);
		}

		// ------------------------------------------
		tx.commit();
		session.close();
		DataBaseTool.closeSessionFactory();
	}

	@Test
	public void setUserWatchPattern() {
		Session session = DataBaseTool.getSession();
		Transaction tx = session.beginTransaction();
		// ------------------------------------------

		for (int id = 1; id <= TotalUserNumber; id++) {
			User user = session.get(User.class, id);

			// 目前是固定的用户请求模式
			user.setTimePatternId(34);
		}

		// ------------------------------------------
		tx.commit();
		session.close();
		DataBaseTool.closeSessionFactory();
	}

	@Test
	public void setUserWatchProbability() {
		Session session = DataBaseTool.getSession();
		Transaction tx = session.beginTransaction();
		// ------------------------------------------

		for (int id = 1; id <= TotalUserNumber; id++) {
			User user = session.get(User.class, id);
			user.setWatchDayProbability(5.0 / 7.0);
		}

		// ------------------------------------------
		tx.commit();
		session.close();
		DataBaseTool.closeSessionFactory();
	}

	@Test
	public void setCacheEnable() {
		Session session = DataBaseTool.getSession();
		Transaction tx = session.beginTransaction();
		// ------------------------------------------
		Random random = new Random();

		for (int id = 1; id <= TotalUserNumber; id++) {
			User user = session.get(User.class, id);
			int value = (int) (random.nextDouble() * 100);

			// 约60%的人会进行缓存
			if (value <= 60) {
				user.setCacheEnable(CacheEnable.YES);
			} else {
				user.setCacheEnable(CacheEnable.NO);
			}

		}

		// ------------------------------------------
		tx.commit();
		session.close();
		DataBaseTool.closeSessionFactory();
	}

	@Test
	public void analysisCreaterSubInfo() {
		Session session = DataBaseTool.getSession();
		Transaction tx = session.beginTransaction();
		// ------------------------------------------

		for (int c_id = 1; c_id <= TotalCreaterNumber; c_id++) {
			Creater creater = session.get(Creater.class, c_id);

			// 设置总订阅数
			creater.setTotalSubscribeNmuber(creater.getSubscribers().size());

			HashMap<String, Integer> zone_info = new HashMap<>();
			for (int i = 1; i <= 4; i++) {
				zone_info.put("Zone_" + i, 0);
			}

			// 统计每个zone的订阅数
			for (int u_id : creater.getSubscribers()) {
				String zone_name = session.get(User.class, u_id).getBelongZoneName();
				int value = zone_info.get(zone_name) + 1;
				zone_info.put(zone_name, value);
			}

			// 同步信息
			for (String zone_name : zone_info.keySet()) {
				creater.getZoneSubscribeNumber().put(zone_name, zone_info.get(zone_name));
			}

		}

		// ------------------------------------------
		tx.commit();
		session.close();
		DataBaseTool.closeSessionFactory();
	}

}

package tool;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jfree.data.category.DefaultCategoryDataset;
import org.junit.Test;
import java.util.*;

import sql.Creater;
import sql.Popular;
import sql.TimePattern;
import sql.User;

public class GenerateCreaterUser {

	// Configuration
	static int TotalCreaterNumber = 1000;
	static double zipfAlpha = 1.0;
	static double popular_ratio = 0.2;
	static int zoneNumber = 4;
	static int[] userNumber = { 0, 2500, 2500, 2500, 2500 };

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
			String pix = "";
			int len = ("" + i).length();

			switch (len) {
			case 1:
				pix = "000" + i;
				break;
			case 2:
				pix = "00" + i;
				break;
			case 3:
				pix = "0" + i;
				break;
			case 4:
				pix = "" + i;
				break;
			}

			Creater c = new Creater(i, "Creater_" + pix);
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

		data.add(new OneND(2, "OneND_dinner_18_15", 18.0, 1.5));
		data.add(new OneND(3, "OneND_dinner_18_20", 18.0, 2.0));
		data.add(new OneND(4, "OneND_dinner_18_25", 18.0, 2.5));
		data.add(new OneND(5, "OneND_dinner_19_15", 19.0, 1.5));
		data.add(new OneND(6, "OneND_dinner_19_20", 19.0, 2.0));
		data.add(new OneND(7, "OneND_dinner_19_25", 19.0, 2.5));

		data.add(new OneND(8, "OneND_noon_11_15", 11.0, 1.5));
		data.add(new OneND(9, "OneND_noon_11_20", 11.0, 2.0));
		data.add(new OneND(10, "OneND_noon_11_25", 11.0, 2.5));
		data.add(new OneND(11, "OneND_noon_12_15", 12.0, 1.5));
		data.add(new OneND(12, "OneND_noon_12_20", 12.0, 2.0));
		data.add(new OneND(13, "OneND_noon_12_25", 12.0, 2.5));

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

		data.put(14, comb1);

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

		// int[] delList = { 2,3,4,5,6,7,8,9,10,11,12,13 };
		int[] delList = { 14 };
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

		TimePattern up = session.get(TimePattern.class, 14);
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
	// 产生基本的User信息 id, name, zone, cache_address
	public void generateUserBasicInfo() {
		Session session = DataBaseTool.getSession();
		Transaction tx = session.beginTransaction();
		// ------------------------------------------

		int id = 1;
		for (int z = 1; z <= zoneNumber; z++) {
			for (int i = 1; i <= userNumber[z]; i++) {
				String pix = "";
				int len = ("" + i).length();

				switch (len) {
				case 1:
					pix = "000" + i;
					break;
				case 2:
					pix = "00" + i;
					break;
				case 3:
					pix = "0" + i;
					break;
				case 4:
					pix = "" + i;
					break;
				}

				User user = new User(id++, "User_" + z + "_" + pix);
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

}

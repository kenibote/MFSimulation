package tool;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Test;
import java.util.*;

import redis.clients.jedis.Jedis;
import sql.User;

public class RedisTool {

	@SuppressWarnings({ "deprecation", "unchecked" })
	@Test
	// windows本机器上仅需几秒
	public void initRedis() {
		Session session = DataBaseTool.getSession();
		Transaction tx = session.beginTransaction();
		// ------------------------------------------

		Jedis redis = DataBaseTool.getJedis();
		redis.select(0);

		// 用于存储每个内容的 global value (type = sorted set)
		// redis.zadd("A_Content_ValueGlobal", -1, "init");

		for (int zone = 1; zone <= GenerateCreaterUser.zoneNumber; zone++) {
			// 用于存储每个内容在每个区域内的 value (type = sorted set)
			// redis.zadd("A_Content_ValueZone_" + zone, -1, "init");

			// 用于记录每个MEC内存了哪些内容 (type = sorted set)
			// redis.zadd("A_Content_CacheMEC_" + zone, 100000, "init");

			// 用于存放每个区域内，每个内容有多少份拷贝 (type = hash map)
			// redis.hset("A_Content_CopyNumberZone_" + zone, "init", "-1");
		}

		List<User> userList = session.createCriteria(User.class).list();
		for (User user : userList) {
			// 用于存储每个Fog用户的缓存内容 (type = set)
			// redis.sadd(user.getCacheAddress(), "init");

			// stopWatch
			System.out.println(user.getUserId());
		}

		// 每个区域内，每个内容在哪些Fog上有存
		// 这个还是放在Redis里面， 用每个内容的名字做地址。set数据类型，操作起来速度很快。

		// 每个MEC 服务器的目前负载 (type = hash map)
		for (int zone = 1; zone <= GenerateCreaterUser.zoneNumber; zone++) {
			redis.hset("A_MEC_AvailableState", "Zone_" + zone, "0");
		}

		for (User user : userList) {
			// 每个user 的目前负载 (type = hash map)
			redis.hset("A_User_AvailableState", "" + user.getUserId(), "0");

			// 初始化用户观看列表 (type = set)
			// redis.sadd("WatchList_Sub_" + user.getUserName(), "init");
			// redis.sadd("WatchList_Unsub_" + user.getUserName(), "init");

			// stopWatch
			System.out.println(user.getUserId());
		}

		DataBaseTool.clossJedis();

		// ------------------------------------------
		tx.commit();
		session.close();
		DataBaseTool.closeSessionFactory();
	}

	@Test
	public void deleteRedis() {
		Jedis redis = DataBaseTool.getJedis();
		redis.select(0);

		redis.flushAll();

		DataBaseTool.clossJedis();
	}

	@Test
	public void deeleteSomethingBeforeRunCode() {
		Jedis redis = DataBaseTool.getJedis();

		redis.del("A_ContentName");
		redis.del("A_Content_ValueGlobal");
		redis.del("B_linshi_candidate");
		redis.del("B_linshi_del");
		redis.del("A_MEC_AvailableState");
		redis.del("A_User_AvailableState");

		for (int i = 1; i <= GenerateCreaterUser.zoneNumber; i++) {
			redis.del("A_Content_ValueZone_" + i);
			redis.del("A_Content_CopyNumberZone_" + i);
			redis.del("A_Content_CacheMEC_" + i);
			redis.del("A_Content_CacheMEC_LRU_Zone_" + i);
			redis.del("A_Content_CacheMEC_SET_Zone_1" + i);
		}

		Set<String> keys = redis.keys("ContentCache*");
		for (String s : keys) {
			redis.del(s);
		}

		keys = redis.keys("C_2018_01_*");
		for (String s : keys) {
			redis.del(s);
		}

	}

}

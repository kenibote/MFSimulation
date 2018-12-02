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
	// 初始化需要大约3分钟
	public void initRedis() {
		Session session = DataBaseTool.getSession();
		Transaction tx = session.beginTransaction();
		// ------------------------------------------

		Jedis redis = DataBaseTool.getJedis();
		redis.select(0);

		// 用于存储每个内容的 global value (type = sorted set)
		redis.zadd("A_content_value_global", -1, "init");

		// 用于存储每个内容在每个区域内的 value (type = sorted set)
		redis.zadd("A_content_value_zone_1", -1, "init");
		redis.zadd("A_content_value_zone_2", -1, "init");
		redis.zadd("A_content_value_zone_3", -1, "init");
		redis.zadd("A_content_value_zone_4", -1, "init");

		// 用于记录每个MEC内存了哪些内容 (type = sorted set)
		redis.zadd("A_content_cache_MEC_1", 100000, "init");
		redis.zadd("A_content_cache_MEC_2", 100000, "init");
		redis.zadd("A_content_cache_MEC_3", 100000, "init");
		redis.zadd("A_content_cache_MEC_4", 100000, "init");

		// 用于存放每个区域内，每个内容有多少份拷贝 (type = hash map)
		redis.hset("A_content_copyNumber_zone_1", "init", "-1");
		redis.hset("A_content_copyNumber_zone_2", "init", "-1");
		redis.hset("A_content_copyNumber_zone_3", "init", "-1");
		redis.hset("A_content_copyNumber_zone_4", "init", "-1");

		List<User> userList = session.createCriteria(User.class).list();
		for (User user : userList) {
			// 用于存储每个Fog用户的缓存内容 (type = set)
			redis.sadd(user.getCacheAddress(), "init");

			// stopWatch
			System.out.println(user.getUserId());
		}

		// TODO 每个区域内，每个内容在哪些Fog上有存

		// 每个MEC 服务器的目前负载 (type = hash map)
		for (int i = 1; i <= 4; i++) {
			redis.hset("A_MEC_available_state", "MEC_" + i, "0");
		}

		for (User user : userList) {
			// 每个user 的目前负载 (type = hash map)
			redis.hset("A_User_available_state", user.getUserName(), "0");

			// 初始化用户观看列表 (type = set)
			redis.sadd("watchlist_sub_" + user.getUserName(), "init");
			redis.sadd("watchlist_unsub_" + user.getUserName(), "init");

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

}

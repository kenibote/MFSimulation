package mainfunction;

import java.util.*;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.alibaba.fastjson.JSON;

import redis.clients.jedis.Jedis;
import sql.*;
import tool.*;

public class StartHere {

	static Jedis redis = DataBaseTool.getJedis();

	// 仿真程序从这里开始
	@SuppressWarnings("deprecation")
	public static void main(String[] args) {

		long start_time = new Date(2018 - 1900, 0, 1, 0, 0, 0).getTime();
		long end_time = new Date(2018 - 1900, 0, 31, 23, 59, 59).getTime();
		long batch = 60 * 1000; // 60s

		// 按照分钟取任务
		while (start_time < end_time) {
			// 监测使用
			System.out.println(start_time);

			Set<String> tasklist = redis.zrangeByScore("A_Time_Line", start_time, start_time + batch);
			// 更新时间
			start_time += batch;

			for (String s : tasklist) {
				// 监测使用
				System.out.println(s);
				Task task = JSON.parseObject(s, Task.class);

				switch (task.getTaskType()) {
				case Source_Release:
					break;
				case Upload:
					UploadTask(task);
					break;
				case MEC_Cache_Arrange:
					break;
				case Request:
					break;
				case MEC_Pressure_Check:
					break;
				default:
					System.out.println("Unknow Task.");
				}
			} // end for

		} // end while

	}

	public static void UploadTask(Task task) {
		Session session = DataBaseTool.getSession();
		Transaction tx = session.beginTransaction();
		// ------------------------------------------
		Creater creater = session.get(Creater.class, task.getUpload_id());
		String contentName = task.getUpload_content();

		// 1. 在全局以及每个zone中设置期望点击数的数据；
		redis.hset("A_Content_ValueGlobal", contentName, "" + creater.getTotalSubscribeNmuber());
		for (int zone = 1; zone <= 4; zone++) {
			redis.hset("A_Content_ValueZone_" + zone, contentName,
					"" + creater.getZoneSubscribeNumber().get("Zone_" + zone));
		}

		// 2. 在Redis中创建地址，用于记录哪些用户缓存了该内容；
		// 感觉应该还是由实际的业务产生时生成
		// jedis.sadd(task.getUpload_content(), "init");

		// 3. 在Redis中每个zone中添加记录，用于记录该区域有多少份copy；
		for (int zone = 1; zone <= 4; zone++) {
			redis.hset("A_Content_CopyNumberZone_" + zone, contentName, "0");
		}

		// 4. 将该内容推送给用户的观看列表；
		for (int u_id = 1; u_id <= GenerateCreaterUser.TotalUserNumber; u_id++) {
			if (creater.getSubscribers().contains(u_id)) {
				redis.sadd("WatchList_Sub_" + u_id, contentName);
			} else {
				redis.sadd("WatchList_Unsub_" + u_id, contentName);
			}
		}

		// 5. 根据该用户是否是热门用户，决定是否推送到MEC中；
		if (creater.getPopular() == Popular.YES) {
			for (int zone = 1; zone <= 4; zone++) {
				// 先清除一个位子出来
				DeleteOneContentInMEC("A_Content_CacheMEC_" + zone);
				// 将内容放进去
				redis.zadd("A_Content_CacheMEC_" + zone, creater.getZoneSubscribeNumber().get("Zone_" + zone),
						contentName);
			}
		}

		// ------------------------------------------
		tx.commit();
		session.close();
		DataBaseTool.closeSessionFactory();
	}

	public static void DeleteOneContentInMEC(String MEC_Name) {
		Set<String> content = redis.zrangeByScore(MEC_Name, 0, 10000, 0, 1);
		for (String s : content) {
			redis.srem(MEC_Name, s);
		}
	}
}

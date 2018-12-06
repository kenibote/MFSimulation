package mainfunction;

import java.util.*;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.alibaba.fastjson.JSON;

import redis.clients.jedis.Jedis;
import sql.*;
import tool.*;

public class StartHere {

	// 设置工作模式
	static WorkMode Workmode = WorkMode.LRU_NULL;

	static Jedis redis = DataBaseTool.getJedis();
	static int MEC_MAX_Capacity = 100;
	static int User_MAX_Capacity = 2;
	static int Server_Time = 20; // 20s
	static int User_Max_Cache = 25;
	static int MEC_Max_Cache = 500;
	static Random random = new Random();

	// 仿真程序从这里开始
	@SuppressWarnings("deprecation")
	public static void main(String[] args) {

		long start_time = new Date(2018 - 1900, 0, 1, 0, 0, 0).getTime();
		long end_time = new Date(2018 - 1900, 0, 31, 23, 59, 59).getTime();
		long batch = 60 * 1000; // 60s

		// 初始化Redis部分空间
		InitRedis();

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
					ReleaseTask(task);
					break;
				case Upload:
					UploadTask(task);
					break;
				case MEC_Cache_Arrange:
					MECArrangeTask();
					break;
				case Request:
					RequestTaskV2(task);
					break;
				case MEC_Pressure_Check:
					CheckTask(task);
					break;
				default:
					System.out.println("Unknow Task.");
				}
			} // end for

		} // end while

	}

	public static void InitRedis() {
		
		for (int zone = 1; zone <= GenerateCreaterUser.zoneNumber; zone++) {
			redis.hset("A_MEC_AvailableState", "Zone_" + zone, "0");
		}

		for (int u_id = 1; u_id <= GenerateCreaterUser.TotalUserNumber; u_id++) {
			// 每个user 的目前负载 (type = hash map)
			redis.hset("A_User_AvailableState", "" + u_id, "0");
		}
	}

	public static void UploadTask(Task task) {
		Session session = DataBaseTool.getSession();
		Transaction tx = session.beginTransaction();
		// ------------------------------------------
		// 内存中会读取所有creater的信息
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

		// 5. 根据该用户是否是热门用户，决定是否推送到MEC中； 且只有在MIXCO模式中才进行推送
		if (Workmode == WorkMode.MIXCO_EXPECT || Workmode == WorkMode.MIXCO_LRU || Workmode == WorkMode.MIXCO_NULL) {
			if (creater.getPopular() == Popular.YES) {
				for (int zone = 1; zone <= 4; zone++) {
					// 先清除一个位子出来
					DeleteOneContentInMEC("A_Content_CacheMEC_" + zone, "A_Content_ValueZone_" + zone);
					// 将内容放进去
					redis.zadd("A_Content_CacheMEC_" + zone, creater.getZoneSubscribeNumber().get("Zone_" + zone),
							contentName);
				}
			}
		}

		// ------------------------------------------
		tx.commit();
		session.close();
		// DataBaseTool.closeSessionFactory();
	}

	public static void DeleteOneContentInMEC(String MEC_Name, String zongValueAddress) {
		// 先更新排名数据
		Set<String> member = redis.zrange(MEC_Name, 0, -1);
		for (String s : member) {
			redis.zadd(MEC_Name, Integer.parseInt(redis.hget(zongValueAddress, s)), s);
		}

		// 删除排名最低的那一个数据
		Set<String> content = redis.zrangeByScore(MEC_Name, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 1);
		for (String s : content) {
			redis.zrem(MEC_Name, s);
		}
	}

	public static void DeleteOneContentInUserMix(String UserAddress) {
		long number = redis.scard(UserAddress);
		if (number < User_Max_Cache) {
			return;
		}

		// TODO 目前存在4中初步想法
		// 2. 弹出区域内缓存数目最多的内容；
		// 3. 弹出区域内期望观看次数最低的内容；
		// 4. 弹出 缓存数目*期望值 最低/高的内容？？？
	}

	public static void DeleteOneContentInUserLRU(String UserAddress, Task task) {
		if (redis.llen(UserAddress) >= MEC_Max_Cache) {
			String del = redis.lpop(UserAddress);
			redis.srem(del, "" + task.getUser_id());
			redis.hincrBy("A_Content_CopyNumber" + task.getZoneName(), del, -1l);
		}

	}

	static HashMap<String, String> MEC_Name_Map = new HashMap<>();
	static {
		for (int i = 1; i <= 4; i++) {
			MEC_Name_Map.put("Zone_" + i, "A_Content_CacheMEC_" + i);
		}
	}

	static HashMap<Integer, User> User_Info = new HashMap<>();
	static {
		Session session = DataBaseTool.getSession();
		Transaction tx = session.beginTransaction();
		// ------------------------------------------
		for (int u_id = 1; u_id <= GenerateCreaterUser.TotalUserNumber; u_id++) {
			User_Info.put(u_id, session.get(User.class, u_id));
			System.out.println("Loading User:" + u_id);
		}
		// ------------------------------------------
		tx.commit();
		session.close();
	}

	public static String RandomPickUpContent(int u_id) {
		double ratio = random.nextDouble() * 100;
		String content = null;
		if (ratio <= 90.0) {
			content = redis.srandmember("WatchList_Sub_" + u_id);
			redis.srem("WatchList_Sub_" + u_id, content);
		} else {
			// TODO 今后可能需要增加时间限制
			content = redis.srandmember("WatchList_Unsub_" + u_id);
			redis.srem("WatchList_Unsub_" + u_id, content);
		}

		return content;
	}

	public static void RequestTaskV2(Task task) {
		User user = User_Info.get(task.getUser_id());

		// 1. 先生成要观看的内容 （热门？非热门？）， 并将该内容从列表中移除
		String watchContentName = RandomPickUpContent(user.getUserId());

		// 创建资源释放任务
		Task release = new Task();
		release.setTaskType(TaskType.Source_Release);
		release.setPriority(TaskType.Source_Release.ordinal());

		// 2. 查找本地MEC服务器； 查找相邻MEC服务器； 查找本zone内的用户；
		boolean flag = false;

		// 先在MEC中查找
		if (Workmode == WorkMode.MIXCO_EXPECT || Workmode == WorkMode.MIXCO_LRU || Workmode == WorkMode.MIXCO_NULL) {
			flag = Find_MEC_MIX_MODE(watchContentName, task, release);
		} else {
			flag = Find_MEC_LRU_MODE(watchContentName, task, release);
		}

		// 如果MEC不能处理
		if (!flag) {
			// 即允许有FOG缓存的情况
			if (Workmode == WorkMode.LRU_LRU || Workmode == WorkMode.MIXCO_EXPECT || Workmode == WorkMode.MIXCO_LRU) {
				flag = Find_Fog(watchContentName, task, release, user);
			}
		}

		// 如果Fog不能处理
		if (!flag) {
			flag = true;
			task.setTaskResult(TaskResult.Original);

			// 如果工作在LRU模式
			if (Workmode == WorkMode.LRU_LRU || Workmode == WorkMode.LRU_NULL) {
				// 如果空间已满
				if (redis.scard("A_Content_CacheMEC_SET_" + task.getZoneName()) >= MEC_Max_Cache) {
					String del = redis.lpop("A_Content_CacheMEC_LRU_" + task.getZoneName());
					redis.srem("A_Content_CacheMEC_SET_" + task.getZoneName(), del);
				}

				redis.rpush("A_Content_CacheMEC_LRU_" + task.getZoneName(), watchContentName);
				redis.sadd("A_Content_CacheMEC_SET_" + task.getZoneName(), watchContentName);
			}

		}

		// 3. 生成任务结果，写入Redis； 并创建资源释放任务，写入Redis；
		release.setDate(new Date(task.getDate().getTime() + Server_Time * 1000));
		release.setTime(release.getDate().getTime() + release.getPriority());
		if (task.getTaskResult() != TaskResult.Original) {
			redis.zadd("A_Time_Line", release.getTime(), release.toJSONString());
		}
		redis.zadd("A_Time_Line_Result", task.getTime(), task.toJSONString());

		// 4. 更新本地缓存内容列表，以及相关记录数据；
		if (user.getCacheEnable() == CacheEnable.YES) {
			if (Workmode == WorkMode.MIXCO_EXPECT) {
				// 先清除一个空位
				DeleteOneContentInUserMix(user.getCacheAddress());
				// 缓存该内容
				redis.sadd(user.getCacheAddress(), watchContentName);
			}

			if (Workmode == WorkMode.LRU_LRU || Workmode == WorkMode.MIXCO_LRU) {
				// LRU模式
				DeleteOneContentInUserLRU(user.getCacheAddress(), task);
				// 缓存该内容
				redis.rpush(user.getCacheAddress(), watchContentName);
			}

			// 记录该内容有哪些用户缓存
			redis.sadd(watchContentName, "" + task.getUser_id());
			redis.hincrBy("A_Content_CopyNumber" + task.getZoneName(), watchContentName, 1l);
		}

		// 5. 更新本地以及全局，缓存中的期待观看数据；
		redis.hincrBy("A_Content_ValueGlobal", watchContentName, -1l);
		redis.hincrBy("A_Content_Value" + task.getZoneName(), watchContentName, -1l);
	}

	public static boolean Find_MEC_MIX_MODE(String watchContentName, Task task, Task release) {
		redis.del("B_linshi_candidate");
		HashSet<String> candid_set = new HashSet<>();

		boolean flag = false;
		for (int zone = 1; zone <= 4; zone++) {
			if (redis.zscore("A_Content_CacheMEC_" + zone, watchContentName) != null
					&& Integer.parseInt(redis.hget("A_MEC_AvailableState", "Zone_" + zone)) < MEC_MAX_Capacity) {
				// 如果该服务器有该资源并且有服务能力，则加入到候选人列表
				redis.zadd("B_linshi_candidate", Integer.parseInt(redis.hget("A_MEC_AvailableState", "Zone_" + zone)),
						"Zone_" + zone);
				candid_set.add("Zone_" + zone);
			}
		}

		String target_server = null;
		if (candid_set.contains(task.getZoneName())) {
			// 如果可以由本地服务器服务
			flag = true;
			// 记录任务结果
			task.setTaskResult(TaskResult.Self_MEC);
			target_server = task.getZoneName();
		}

		if (!flag && !candid_set.isEmpty()) {
			// 如果可以由别的MEC服务器服务
			flag = true;
			// 记录任务结果
			task.setTaskResult(TaskResult.Other_MEC);
			for (String s : redis.zrange("B_linshi_candidate", 0, 0)) {
				target_server = s;
			}
		}

		// 如果以上有一个服务成功
		if (flag) {
			task.setServer_MEC(target_server);

			// 更新服务器负载 +1
			redis.hincrBy("A_MEC_AvailableState", target_server, 1l);
			// 更新资源释放任务信息
			release.setSource_address("A_MEC_AvailableState");
			release.setSource_id(target_server);
		}

		return flag;
	}

	public static boolean Find_MEC_LRU_MODE(String watchContentName, Task task, Task release) {
		boolean flag = false;

		if (redis.sismember("A_Content_CacheMEC_SET_" + task.getZoneName(), watchContentName)
				&& Integer.parseInt(redis.hget("A_MEC_AvailableState", task.getZoneName())) < MEC_MAX_Capacity) {
			// 如果可以由本服务器服务
			flag = true;

			// 更新LRU顺序
			redis.lrem("A_Content_CacheMEC_LRU_" + task.getZoneName(), 1, watchContentName);
			redis.rpush("A_Content_CacheMEC_LRU_" + task.getZoneName(), watchContentName);

			// 记录任务结果
			task.setTaskResult(TaskResult.Self_MEC);
			String target_server = task.getZoneName();

			task.setServer_MEC(target_server);

			// 更新服务器负载 +1
			redis.hincrBy("A_MEC_AvailableState", target_server, 1l);
			// 更新资源释放任务信息
			release.setSource_address("A_MEC_AvailableState");
			release.setSource_id(target_server);
		}

		return flag;
	}

	public static boolean Find_Fog(String watchContentName, Task task, Task release, User user) {
		redis.del("B_linshi_candidate");
		HashSet<String> candid_set = new HashSet<>();
		boolean flag = false;

		// 尝试在本zone内查找资源
		int cache_number = Integer.parseInt(redis.hget("A_Content_CopyNumber" + task.getZoneName(), watchContentName));
		if (cache_number > 0) {
			Set<String> user_id_list = redis.smembers(watchContentName);
			for (String inuser_id : user_id_list) {
				User inner_user = User_Info.get(Integer.parseInt(inuser_id));
				int user_state = Integer.parseInt(redis.hget("A_User_AvailableState", inuser_id));
				if (inner_user.getBelongZoneName().equals(user.getBelongZoneName()) && user_state < User_MAX_Capacity) {
					redis.zadd("B_linshi_candidate", user_state, inuser_id);
					candid_set.add(inuser_id);
				}
			}

			if (!candid_set.isEmpty()) {
				flag = true;

				String target_user = "";
				for (String s : redis.zrange("B_linshi_candidate", 0, 0)) {
					target_user = s;
				}

				task.setTaskResult(TaskResult.Self_Zone_Users);
				task.setServer_user_id(Integer.parseInt(target_user));

				// 更新服务器负载 +1
				redis.hincrBy("A_User_AvailableState", target_user, 1l);
				// 更新资源释放任务信息
				release.setSource_address("A_User_AvailableState");
				release.setSource_id(target_user);

				if (Workmode == WorkMode.LRU_LRU || Workmode == WorkMode.MIXCO_LRU) {
					// 如果工作在LRU模式,使对方的地址列表更新
					redis.lrem(User_Info.get(target_user).getCacheAddress(), 1, watchContentName);
					redis.rpush(User_Info.get(target_user).getCacheAddress(), watchContentName);
				}
			}

		}

		return flag;
	}

	public static void ReleaseTask(Task task) {
		redis.hincrBy(task.getSource_address(), task.getSource_id(), -1l);
	}

	public static void CheckTask(Task task) {
		CheckInfo check = new CheckInfo();
		check.setTaskid(task.getTaskid());
		check.setDate(task.getDate());
		check.setTime(task.getTime());

		for (int i = 1; i <= 4; i++) {
			check.getMEC_Pressure().put("Zone_" + 1, Integer.parseInt(redis.hget("A_MEC_AvailableState", "Zone_" + i)));
		}

		// TODO 今后可能需要添加数据完整性检查过程

		redis.zadd("Check_Info", check.getTime(), check.toJSON());
	}

	public static void MECArrangeTask() {
		// TODO 只有工作在MEC模式才进行整理
		if (Workmode == WorkMode.MIXCO_EXPECT || Workmode == WorkMode.MIXCO_LRU || Workmode == WorkMode.MIXCO_NULL) {

			
		}
	}

}

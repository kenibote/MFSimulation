package mainfunction;

import java.util.*;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.alibaba.fastjson.JSON;

import redis.clients.jedis.Jedis;
import sql.*;
import tool.*;

public class StartHereV2 {

	// 设置工作模式
	static MECMode mecmode = MECMode.TOP;
	static FOGMode fogmode = FOGMode.EXP;
	public static DelMode Delmode = DelMode.MinExp;

	public static int MEC_MAX_Capacity = 100;
	public static int User_MAX_Capacity = 2;
	static int Server_Time = 20; // 20s
	public static int User_Max_Cache = 20;
	public static int MEC_Max_Cache = 500;
	static double Ratio = 85.0;

	static Jedis redis = DataBaseTool.getJedis();
	static Random random = new Random();

	// 存在JAVA缓存中，加速读取
	static HashMap<Integer, Creater> Creater_Info = new HashMap<>();
	static HashMap<Integer, User> User_Info = new HashMap<>();
	static HashMap<String, MEC> MEC_Info = new HashMap<>();
	static ArrayList<Content> ContentAll = new ArrayList<>();
	static Queue<Task> ReleaseTask = new LinkedList<>();
	static ArrayList<Content> TopHit = null;

	// 仿真程序从这里开始
	@SuppressWarnings("deprecation")
	public static void main(String[] args) {

		long start_time = new Date(2018 - 1900, 0, 1, 0, 0, 0).getTime();
		long end_time = new Date(2018 - 1900, 0, 31, 23, 59, 59).getTime();
		long batch = 60 * 60 * 1000; // 1 hour

		// 按照分钟取任务
		while (start_time < end_time) {
			// 监测使用
			System.out.println("------:" + start_time);

			Set<String> tasklist = redis.zrangeByScore("A_Time_Line", start_time, start_time + batch);
			// 更新时间
			start_time += batch;

			for (String s : tasklist) {
				// 监测使用
				System.out.println(s);
				Task task = JSON.parseObject(s, Task.class);

				while ((!ReleaseTask.isEmpty()) && ReleaseTask.peek().getTime() < task.getTime()) {
					ReleaseTask(ReleaseTask.poll());
				}

				switch (task.getTaskType()) {
				case Source_Release:
					// ReleaseTask(task);
					break;
				case Upload:
					UploadTask(task);
					break;
				case MEC_Cache_Arrange:
					MECArrangeTask(task);
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

	static {
		Session session = DataBaseTool.getSession();
		Transaction tx = session.beginTransaction();
		// ------------------------------------------

		System.out.println(">>>>>>正在载入Creater_Info...");
		for (int id = 1; id <= GenerateCreaterUser.TotalCreaterNumber; id++) {
			System.out.println("Loading Creater:" + id);
			Creater_Info.put(id, session.get(Creater.class, id));
		}

		System.out.println(">>>>>>正在载入User_Info......");
		for (int u_id = 1; u_id <= GenerateCreaterUser.TotalUserNumber; u_id++) {
			System.out.println("Loading User:" + u_id);
			User_Info.put(u_id, session.get(User.class, u_id));
		}

		System.out.println(">>>>>>正在载入MEC_Info......");
		for (int i = 1; i <= GenerateCreaterUser.zoneNumber; i++) {
			System.out.println("Loading MEC: Zone_" + i);
			MEC_Info.put("Zone_" + i, new MEC("Zone_" + i));
		}

		// ------------------------------------------
		tx.commit();
		session.close();
	}

	public static void UploadTask(Task task) {

		Creater creater = Creater_Info.get(task.getUpload_id());
		String contentName = task.getUpload_content();

		// 创建新的内容
		Content new_content = new Content(contentName);
		// 1. 在全局以及每个zone中设置期望点击数的数据；
		new_content.InitValue(creater);

		// 4. 将该内容推送给用户的观看列表；
		for (int u_id : creater.getSubscribers()) {
			User_Info.get(u_id).WatchListSub.add(new_content);
		}
		ContentAll.add(new_content);

		// 5. 根据该用户是否是热门用户，决定是否推送到MEC中； 且只有在MIXCO模式中才进行推送
		if (mecmode == MECMode.MIXCO) {
			if (creater.getPopular() == Popular.YES) {
				for (int zone = 1; zone <= GenerateCreaterUser.zoneNumber; zone++) {
					// 先清除一个位子出来
					MEC_Info.get("Zone_" + zone).deleteOneCache();
					// 将内容放进去
					MEC_Info.get("Zone_" + zone).addOneContent(new_content);
				}
			}
		}

	}

	public static Content RandomPickUpContent(User user) {
		HashSet<Content> watchlist = user.WatchListSub;

		double ratio = random.nextDouble() * 100;
		Content content = null;

		if (ratio <= Ratio && watchlist.size() > 1) {
			content = (Content) watchlist.toArray()[random.nextInt(watchlist.size())];
			watchlist.remove(content);
		} else {
			if (ratio <= 95) {
				content = TopHit.get(random.nextInt(1000));
			} else {
				content = ContentAll.get(random.nextInt(ContentAll.size()));
			}
		}

		return content;
	}

	public static boolean Find_MEC_MODE(Content watchContent, Task task, Task release) {
		ArrayList<MEC> candid_rank = new ArrayList<>();
		HashSet<String> candid_set = new HashSet<>();

		boolean flag = false;
		for (int zone = 1; zone <= GenerateCreaterUser.zoneNumber; zone++) {
			if (MEC_Info.get("Zone_" + zone).CacheSet.contains(watchContent)
					&& MEC_Info.get("Zone_" + zone).isAvailable()) {
				// 如果该服务器有该资源并且有服务能力，则加入到候选人列表
				candid_rank.add(MEC_Info.get("Zone_" + zone));
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
			// TODO 此处的顺序需要检查
			Collections.sort(candid_rank);
			target_server = candid_rank.get(0).BelongToZone;
		}

		// 如果以上有一个服务成功
		if (flag) {
			task.setServer_MEC(target_server);

			// 更新服务器负载 +1
			MEC_Info.get(target_server).MEC_Available_State++;
			// 更新资源释放任务信息
			release.setSource_address("A_MEC_AvailableState");
			release.setSource_id(target_server);

			if (mecmode == MECMode.LRU) {
				// 更新LRU顺序
				MEC_Info.get(target_server).updataLRUorder(watchContent);
			}
		}

		return flag;
	}

	public static boolean Find_MEC_LRU_MODE(Content watchContent, Task task, Task release) {
		boolean flag = false;

		if (MEC_Info.get(task.getZoneName()).CacheSet.contains(watchContent)
				&& MEC_Info.get(task.getZoneName()).isAvailable()) {
			// 如果可以由本服务器服务
			flag = true;

			// 更新LRU顺序
			MEC_Info.get(task.getZoneName()).updataLRUorder(watchContent);

			// 记录任务结果
			task.setTaskResult(TaskResult.Self_MEC);
			String target_server = task.getZoneName();

			task.setServer_MEC(target_server);

			// 更新服务器负载 +1
			MEC_Info.get(target_server).MEC_Available_State++;
			// 更新资源释放任务信息
			release.setSource_address("A_MEC_AvailableState");
			release.setSource_id(target_server);
		}

		return flag;
	}

	public static boolean Find_Fog(Content watchContent, Task task, Task release) {

		boolean flag = false;
		String targetZone = task.getZoneName();
		User target_user = null;

		// 尝试在本zone内查找资源
		if (watchContent.ContentCopy.get(targetZone).size() > 0) {
			for (User user : watchContent.ContentCopy.get(targetZone)) {
				if (user.isAvailable()) {
					flag = true;
					target_user = user;
					break;
				}
			} // end for

			if (flag) {

				task.setTaskResult(TaskResult.Self_Zone_Users);
				task.setServer_user_id(target_user.getUserId());

				// 更新用户负载 +1
				target_user.UserAvailableState++;
				// 更新资源释放任务信息
				release.setSource_address("A_User_AvailableState");
				release.setSource_id("" + target_user.getUserId());

				if (fogmode == FOGMode.LRU) {
					// 如果工作在LRU模式,使对方的地址列表更新
					target_user.updataLRUorder(watchContent);
				}
			} // end if

		} // end if

		return flag;
	}

	public static void RequestTaskV2(Task task) {
		User user = User_Info.get(task.getUser_id());

		// 1. 先生成要观看的内容 （热门？非热门？）， 并将该内容从列表中移除
		Content watchContent = RandomPickUpContent(user);

		// 创建资源释放任务
		Task release = new Task();
		release.setTaskType(TaskType.Source_Release);
		release.setPriority(TaskType.Source_Release.ordinal());

		// 2. 查找本地MEC服务器； 查找相邻MEC服务器； 查找本zone内的用户；
		boolean flag = false;

		// 先在MEC中查找
		flag = Find_MEC_MODE(watchContent, task, release);

		// 如果MEC不能处理
		if (!flag) {
			// 即允许有FOG缓存的情况
			if (fogmode != FOGMode.NULL) {
				flag = Find_Fog(watchContent, task, release);
			}
		}

		// 如果Fog不能处理
		if (!flag) {
			flag = true;
			task.setTaskResult(TaskResult.Original);

			// 如果工作在LRU模式
			if (mecmode == MECMode.LRU) {
				MEC_Info.get(task.getZoneName()).addOneContentLRU(watchContent);
			}

		}

		// 3. 生成任务结果，写入Redis； 并创建资源释放任务，写入Redis；
		release.setDate(new Date(task.getDate().getTime() + Server_Time * 1000));
		release.setTime(release.getDate().getTime() + release.getPriority());
		if (task.getTaskResult() != TaskResult.Original) {
			ReleaseTask.offer(release);
		}
		redis.zadd("A_Time_Line_Result", task.getTime(), task.toJSONString());

		// 4. 更新本地缓存内容列表，以及相关记录数据；
		if (user.getCacheEnable() == CacheEnable.YES) {
			if (fogmode == FOGMode.EXP) {
				user.addOneContent(watchContent);
			}

			if (fogmode == FOGMode.LRU) {
				// LRU模式
				user.addOneContentLRU(watchContent);
			}
		}

		// 5. 更新本地以及全局，缓存中的期待观看数据；
		watchContent.ValueGlobal--;
		watchContent.decreaseZoneValue(task.getZoneName());
		watchContent.watchCount++;
		watchContent.totalWatchCount++;
	}

	public static void ReleaseTask(Task task) {
		if (task.getSource_address().equals("A_MEC_AvailableState")) {
			MEC_Info.get(task.getSource_id()).MEC_Available_State--;
		}

		if (task.getSource_address().equals("A_User_AvailableState")) {
			User_Info.get(Integer.parseInt(task.getSource_id())).UserAvailableState--;
		}

	}

	public static void CheckTask(Task task) {
		CheckInfo check = new CheckInfo();
		check.setTaskid(task.getTaskid());
		check.setDate(task.getDate());
		check.setTime(task.getTime());

		for (int i = 1; i <= GenerateCreaterUser.zoneNumber; i++) {
			check.getMEC_Pressure().put("Zone_" + i, MEC_Info.get("Zone_" + i).MEC_Available_State);
		}

		// TODO 今后可能需要添加数据完整性检查过程
		redis.zadd("Check_Info", check.getTime(), check.toJSON());
	}

	@SuppressWarnings({ "deprecation", "unchecked" })
	public static void MECArrangeTask(Task task) {
		// 更新TOP榜
		if (task.getDate().getHours() == 0) {
			TopHit = (ArrayList<Content>) ContentAll.clone();
			Collections.sort(TopHit, Content.zoneComparetor.get("WatchCount"));
			// 重置TOP榜单
			for (Content c : ContentAll) {
				c.watchCount = 0;
			}

			// 观测使用
			Analysis.ContentWatchCount(task.getDate());
		}

		if (mecmode == MECMode.MIXCO) {
			MixCo.PartOne(task);
		}

		if (mecmode == MECMode.TOP) {
			// TODO 这里可能需要性能优化
			for (int i = 1; i <= GenerateCreaterUser.zoneNumber; i++) {
				Collections.sort(ContentAll, Content.zoneComparetor.get("Zone_" + i));
				MEC mec = MEC_Info.get("Zone_" + i);
				mec.CacheSet.clear();

				for (int c = 0; c < MEC_Max_Cache; c++) {
					mec.addOneContent(ContentAll.get(c));
				}
			}
		}

		if (mecmode == MECMode.DIS) {
			// 先清空
			for (int i = 1; i <= GenerateCreaterUser.zoneNumber; i++) {
				MEC_Info.get("Zone_" + i).CacheSet.clear();
			}

			// 排序
			Collections.sort(ContentAll, Content.zoneComparetor.get("Global"));

			// TODO 这里存在一个问题，初次重点内容都会被分配到mec1中，需要注意之后是否会动态调整。
			for (int point = 0; point < GenerateCreaterUser.zoneNumber * MEC_Max_Cache
					&& point < ContentAll.size(); point++) {
				Content c = ContentAll.get(point);
				ArrayList<String> order = c.getMaxOrderValueZone();
				for (String zone : order) {
					if (MEC_Info.get(zone).CacheSet.size() < MEC_Max_Cache) {
						MEC_Info.get(zone).addOneContent(c);
						break;
					}
				}
			} // end for

		} // end DIS
	}

}

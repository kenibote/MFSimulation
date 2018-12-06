如何启动项目？
	Author: Wang Ning
	Date:   2018-12

0, 检查数据库地址；

1， 初始化创作者信息； tool.GenerateCreaterUser.generateBasicCreaterInfo()
2， 为创作者设置到达率信息： tool.GenerateCreaterUser.generateCreaterArrivalRate()

3， 初始化一系列的时间描述信息：
	 平坦时间分布： tool.GenerateCreaterUser.TimePattern_1_flat() 
	 真实时间分布： tool.GenerateCreaterUser.LoadRealTimePattern()
	 高斯时间分布： tool.GenerateCreaterUser.TimePattern_One_ND()
	 多重高斯时间分布：  tool.GenerateCreaterUser.TimePattern_More_ND()
	 
	 其中测试工具：
	 	快速删除时间分布：  tool.GenerateCreaterUser.DeleteTimePatternBatch()
	 	图形化测试时间分布： tool.GenerateCreaterUser.TestTimePatternRank()

4， 为创作者设置创作时间分布： tool.GenerateCreaterUser.assignCreaterTimePattern()

5, 生成用户基本信息： tool.GenerateCreaterUser.generateUserBasicInfo()
6, 为用户生成订阅者信息： tool.GenerateCreaterUser.gengerateSubscribeInfoForUser()
	测试工具：
		删除用户订阅信息： tool.GenerateCreaterUser.deleteSubscribeInfoForUser()
		用户订阅数高斯分布图形化工具： tool.GenerateCreaterUser.TestGussDistribution()

7, 为用户生成观看总时间信息：tool.GenerateCreaterUser.generateTotalWatchTimeforUser()
8, 为用户指定观看时间分布信息：tool.GenerateCreaterUser.setUserWatchPattern()
9, 为用户设定观看概率信息； tool.GenerateCreaterUser.setUserWatchProbability()
10, 为用户设定是否缓存信息；tool.GenerateCreaterUser.setCacheEnable()

11, 为创作者统计订阅信息： tool.GenerateCreaterUser.analysisCreaterSubInfo()

-----------------------------------------------------------------------------------
12， 准备任务数据 存放在MySQL time_line_info中：
	1）准备MEC重整理任务： tool.GenerateTimeLine.generateMEC_Arrange_Task()
	2）准备负载检查任务：tool.GenerateTimeLine.generateMEC_Check_Task()
	3）准备创作者上传任务： tool.GenerateTimeLine.generateCreaterUploadTask()
	4）准备用户请求任务：  tool.GenerateTimeLine.generateUserRequestTast_1()
					   tool.GenerateTimeLine.generateUserRequestTast_2()
	5）资源释放任务会在程序仿真中动态创建
	
	6）将数据转移到Redis: tool.GenerateTimeLine.transferTasktoRedis()
	
		一些测试工具：
			测试泊松分布： tool.GenerateTimeLine.TestPosion()
			区域单日每小时点击分布可视化： tool.GenerateTimeLine.TestZoneRequestPatternInOneDay()
			区域每天点击总量分析（观察每个区域每天总量是否平稳）：tool.GenerateTimeLine.TestZoneTotalRequest()
			用户每天播放量分析：tool.GenerateTimeLine.TestUserMonthPattern()

-----------------------------------------------------------------------------------
13, 准备Redis缓存数据：
		tool.RedisTool.initRedis()

-----------------------------------------------------------------------------------
14, 主函数： 
	mainfunction.StartHere.main(String[])
	
		业务逻辑函数：
			mainfunction.StartHere.UploadTask(Task)
				1. 在全局以及每个zone中设置期望点击数的数据； 
				2. 在Redis中创建地址，用于记录哪些用户缓存了该内容； 
				3. 在Redis中每个zone中添加记录，用于记录该区域有多少份copy； 
				4. 将该内容推送给用户的观看列表； 
				5. 根据该用户是否是热门用户，决定是否推送到MEC中；
		
		Redis缓存信息：
		（x）	A_Time_Line / Time : (sort set) 记录时间轴
		（I）	A_MEC_AvailableState / Zone_1 : (hash) 服务器状态表
		（I）	A_User_AvailableState / u_id : (hash) 用户状态表
		（D）	A_Content_ValueGlobal / contentName : (hash) 全局点击数期望
		（D）	A_Content_ValueZone_1~4 / contentName : (hash) Zone点击数期望
		（D）	A_Content_CopyNumberZone_1~4 / contentName : (hash) zone中内容copy数目
		(D)		WatchList_Sub_1~10000 ：(set) 
		(D)		WatchList_Unsub_1~10000 : (set)
				
		（D）	B_linshi_candidate / String (sort set)

		（D）	A_Content_CacheMEC_1~4 / contentName : (sort set)			

				A_Content_CacheMEC_LRU_Zone_1~4 (list)
				A_Content_CacheMEC_SET_Zone_1~4 (set)
				
		（D）	user.getCacheAddress() 在LRU和MIX下有不同的数据结构
				
-----------------------------------------------------------------------------------
Task的逻辑处理函数

	一些工具：
		随机选择函数：
		
	数据分析工具：
		仿真进行过程中的数据完整性检查工具：
		负载分析：
		QoS分析：

	
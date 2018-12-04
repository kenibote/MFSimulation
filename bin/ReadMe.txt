如何启动项目？


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
准备任务数据：
	1）准备MEC冲整理任务： tool.GenerateTimeLine.generateMEC_Arrange_Task()
	2）准备负载检查任务：tool.GenerateTimeLine.generateMEC_Check_Task()
	3）准备创作者上传任务： tool.GenerateTimeLine.generateCreaterUploadTask()
	4）准备用户请求任务： 
	5）资源释放任务会在程序仿真中动态创建
	6）将数据转移到Redis: tool.GenerateTimeLine.transferTasktoRedis()
	
		一些测试工具：
			测试泊松分布： tool.GenerateTimeLine.TestPosion()

			
-----------------------------------------------------------------------------------
准备Redis缓存数据：
	tool.RedisTool.initRedis()



-----------------------------------------------------------------------------------
Task的逻辑处理函数

	一些工具：
		随机选择函数：


-----------------------------------------------------------------------------------
主函数
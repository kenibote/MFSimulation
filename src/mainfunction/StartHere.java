package mainfunction;

import org.hibernate.Session;
import org.hibernate.Transaction;

import redis.clients.jedis.Jedis;
import tool.DataBaseTool;

public class StartHere {

	// 仿真程序从这里开始
	public static void main(String[] args) {
		Session session = DataBaseTool.getSession();
		Transaction tx = session.beginTransaction();
		// ------------------------------------------

		Jedis jedis = DataBaseTool.getJedis();
		
		
		

		// ------------------------------------------
		tx.commit();
		session.close();
		DataBaseTool.closeSessionFactory();
	}
}

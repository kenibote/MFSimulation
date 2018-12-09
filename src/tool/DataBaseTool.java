package tool;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import redis.clients.jedis.Jedis;

public class DataBaseTool {

	private static Configuration conf = new Configuration().configure();
	private static SessionFactory sessionFactory = conf.buildSessionFactory();

	public static Session getSession() {
		Session session = sessionFactory.openSession();
		return session;
	}

	public static void closeSessionFactory() {
		sessionFactory.close();
	}

	// ---------------------------------------------------------

	private static Jedis jedis = new Jedis("10.10.12.115", 6379);
	static {
		jedis.auth("404wang");
	}

	public static Jedis getJedis() {
		return jedis;
	}

	public static void clossJedis() {
		jedis.close();
	}

	private DataBaseTool() {

	}

}

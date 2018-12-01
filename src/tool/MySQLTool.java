package tool;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class MySQLTool {

	private static Configuration conf = new Configuration().configure();
	private static SessionFactory sessionFactory = conf.buildSessionFactory();

	public static Session getSession() {
		Session session = sessionFactory.openSession();
		return session;
	}

	public static void closeSessionFactory() {
		sessionFactory.close();
	}

	private MySQLTool() {

	}

}

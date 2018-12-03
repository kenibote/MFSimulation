package demo;

import java.util.Date;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.junit.Test;

//测试Hibernate框架
public class Demo {

	@Test
	public void fun1() {
		Configuration conf = new Configuration().configure();

		SessionFactory sessionFactory = conf.buildSessionFactory();

		Session session = sessionFactory.openSession();

		Transaction tx = session.beginTransaction();
		// ----------------------------------------------

		TestSpeed ts = new TestSpeed();
		ts.setContent("This is a test message.");
		ts.setName("Test");

		session.save(ts);

		// ----------------------------------------------
		tx.commit();
		session.close();
		sessionFactory.close();
	}

	@Test
	public void DateTest() {
		Date d = new Date(2018 - 1900, 1, 1, 23, 26, 0);

		long t1 = d.getTime();

		Date d2 = new Date(t1 + 34 * 60 * 1000);
		System.out.println(d2);

	}
}

package tool;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Test;

import sql.Creater;

public class GenerateCreaterUser {

	// Configuration
	static int TotalCreaterNumber = 1000;
	static double zipfAlpha = 0.88;

	@Test
	// 第一步：生成基本creater的信息；
	public void generateBasicCreaterInfo() {
		Session session = MySQLTool.getSession();
		Transaction tx = session.beginTransaction();
		// ------------------------------------------
		for (int i = 1; i <= TotalCreaterNumber; i++) {
			String pix = "";
			int len = ("" + i).length();

			switch (len) {
			case 1:
				pix = "000" + i;
				break;
			case 2:
				pix = "00" + i;
				break;
			case 3:
				pix = "0" + i;
				break;
			case 4:
				pix = "" + i;
				break;
			}

			Creater c = new Creater(i, "Creater_" + pix);
			session.save(c);
		}

		// ------------------------------------------
		tx.commit();
		session.close();
		MySQLTool.closeSessionFactory();
	}
}

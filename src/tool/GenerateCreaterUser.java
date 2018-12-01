package tool;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Test;
import java.util.*;

import sql.Creater;
import sql.Popular;

public class GenerateCreaterUser {

	// Configuration
	static int TotalCreaterNumber = 1000;
	static double zipfAlpha = 0.88;

	@Test
	// 第一步：生成基本creater的信息；
	public void generateBasicCreaterInfo() {
		Session session = DataBaseTool.getSession();
		Transaction tx = session.beginTransaction();
		// ------------------------------------------
		HashMap<Integer, Double> map = new HashMap<>();
		double sum = 0.0;
		for (int i = 1; i <= TotalCreaterNumber; i++) {
			double value = 1 / Math.pow(i, zipfAlpha);
			sum = sum + value;
			map.put(i, value);
		}

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
			double value = map.get(i) / sum;
			c.setZipfLike(value);
			if (i <= TotalCreaterNumber * 0.2) {
				c.setPopular(Popular.YES);
			} else {
				c.setPopular(Popular.NO);
			}
			session.save(c);
		}

		// ------------------------------------------
		tx.commit();
		session.close();
		DataBaseTool.closeSessionFactory();
	}

}

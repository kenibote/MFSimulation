package demo;

import java.util.*;

import org.junit.Test;

public class TestSpeed {

	HashMap<String, HashSet<String>> WatchList = new HashMap<>();
	
	@Test
	public void testWatchList(){
		for(int  i=1;i<=10000;i++){
			WatchList.put(""+i, new HashSet<>());
		}
		
		for(int i=1;i<=10000;i++){
			for(int j=1;j<=3000;j++){
				WatchList.get(""+i).add("Content_1000_29_002_"+j);
			}
			
			System.out.println(i);
		}
		
		System.out.println("Done");
	}
	
}

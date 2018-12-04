package demo;

import redis.clients.jedis.Jedis;

public class JTest {
	static Jedis jedis = new Jedis("10.10.12.120", 6379);
	static {
		jedis.auth("404wang");
	}

	static void ChangeDataBase() {
		jedis.select(1);
		jedis.set("wang", "ning1");
	}

	static void StringGet() {
		// if the key not exist, return null
		String ans = jedis.get("hello");
		System.out.println(ans == null);
	}

	static void StringIncrFloat() {
		jedis.set("num", "5.3");
		System.out.println(jedis.incrByFloat("num", 1.2));
	}

	static void HashSomething() {
		// Map<String, String> ans = jedis.hgetAll("myhash");
		// jedis.hincrByFloat("myhash", "age", 1.2);
		// double age = Double.parseDouble(jedis.hget("myhash", "age"));
		jedis.hset("myhash", "age", "28");
		jedis.hset("myhash", "old", "28");

		System.out.println(jedis.hvals("myhash"));
	}

	public static void main(String[] args) {
		jedis.select(1);

		//jedis.sadd("Content_001", "user2");
		
		System.out.print(jedis.srem("Content_001", "user3"));

		//System.out.println(jedis.zrevrank("sort", "v2"));
	}
}

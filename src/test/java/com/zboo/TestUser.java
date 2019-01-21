package com.zboo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.zboo.leaderboard.LeaderboardService;
import com.zboo.leaderboard.LeaderboardServiceUserPointHandler;
import org.junit.*;
import redis.clients.jedis.Jedis;

import javax.net.ssl.SSLException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.util.LinkedHashMap;
import java.util.Set;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;

public class TestUser {
    static final String apiHost = "127.0.0.1";
    static final int apiPort = 8082;
    static LeaderboardService service;
    static final String TEST_LEADERBOARD_KEY = "test_leaderboard_user";
    static final String TEST_LEADERBOARD_COUNTER_KEY = "test_leaderboard_counter_user";
    static Gson gson = new GsonBuilder().create();
    static final String USERNAME_TOBE_DELETED = "zeroboo3";
    static final String USERNAME_VALID = "zeroboo1";
    static final String USERNAME_INVALID= "zerobooxxx";
    @BeforeClass
    public static void beforeClass()
    {
        System.out.println("TestUser.beforeClass");
        service = new LeaderboardService();
        service.getConfig().setApiUserHost(apiHost);
        service.getConfig().setApiUserPort(apiPort);
        service.getConfig().setRedisLeaderboardKey(TEST_LEADERBOARD_KEY);
        service.getConfig().setRedisLeaderboardUpdateCounterKey(TEST_LEADERBOARD_COUNTER_KEY);
        try {
            service.start();

            Thread.sleep(3000);

            ///Delete old leaderboard
            Jedis jedis = new Jedis(service.getConfig().getRedisHost());
            while(jedis.zcard(TEST_LEADERBOARD_KEY) > 0)
            {
                jedis.zremrangeByRank(TEST_LEADERBOARD_KEY, 0, 99);
            }
            Set<String> subKeys = jedis.hkeys(TEST_LEADERBOARD_COUNTER_KEY);
            for(String subKey: subKeys)
            {
                jedis.hdel(TEST_LEADERBOARD_COUNTER_KEY, subKey);
            }

            ///Create fake data
            jedis.zadd(TEST_LEADERBOARD_KEY, 1000, USERNAME_VALID);
            jedis.hincrBy(TEST_LEADERBOARD_COUNTER_KEY, USERNAME_VALID, 1);

            jedis.zadd(TEST_LEADERBOARD_KEY, 2000, "zeroboo2");
            jedis.hincrBy(TEST_LEADERBOARD_COUNTER_KEY, "zeroboo2", 2);

            jedis.zadd(TEST_LEADERBOARD_KEY, 3000, "zeroboo3");
            jedis.hincrBy(TEST_LEADERBOARD_COUNTER_KEY, "zeroboo3", 3);

            jedis.disconnect();
            jedis.close();


        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (SSLException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void afterClass()
    {
        try {
            System.out.println("TestUser.afterClass");
            service.stop();
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Before
    public void beforeTest()
    {

    }

    @After
    public void afterTest()
    {

    }


    @Test
    public void TestLoginValid_ResponseSuccess()
    {
        try {
            String targetURL = String.format("http://%s:%d/login", apiHost, apiPort);
            HttpURLConnection connection = null;

            URL url = new URL(targetURL);
            String content = "{\"username\":\"zeroboo3\"}";
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            connection.setRequestProperty("Content-Length",
                    Integer.toString(content.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches(false);
            connection.setDoOutput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream (
                    connection.getOutputStream());
            wr.writeBytes(content);
            wr.close();

            assertEquals(200, connection.getResponseCode());

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            String result=response.toString();
            System.out.println(result);
            LinkedHashMap<String, Object> resp = gson.fromJson(result, new TypeToken<LinkedHashMap<String, Object>>(){}.getType());
            assertEquals("zeroboo3", resp.get(LeaderboardServiceUserPointHandler.RESPONSE_KEY_USERNAME));
            assertEquals(true, resp.get(LeaderboardServiceUserPointHandler.RESPONSE_KEY_SUCCESS));

            ///Connection
            assertEquals("keep-alive", connection.getHeaderField("Connection"));

        } catch (IOException e) {
            e.printStackTrace();
            assertTrue("Has exception!", false);
        }
    }

    @Test
    public void TestLoginInvalidToken_ResponseError()
    {
        fail("not implemented");
    }

    @Test
    public void TestUpdatePointOnExistedUser_ResponseSuccess()
    {
        try {
            String targetURL = String.format("http://%s:%d/point", apiHost, apiPort);
            HttpURLConnection connection = null;

            URL url = new URL(targetURL);
            String content = "{\"username\":\"zeroboo3\",\"newPoint\":1000}";
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            connection.setRequestProperty("Content-Length",
                    Integer.toString(content.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches(false);
            connection.setDoOutput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream (
                    connection.getOutputStream());
            wr.writeBytes(content);
            wr.close();

            assertEquals(200, connection.getResponseCode());

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            String result=response.toString();
            System.out.println(result);
            LinkedHashMap<String, Object> resp = gson.fromJson(result, new TypeToken<LinkedHashMap<String, Object>>(){}.getType());
            assertEquals("zeroboo3", resp.get(LeaderboardServiceUserPointHandler.RESPONSE_KEY_USERNAME));
            assertEquals("1000.0", resp.get("currentPoint").toString());

            ///Connection
            assertEquals("keep-alive", connection.getHeaderField("Connection"));

        } catch (IOException e) {
            e.printStackTrace();
            assertTrue("Has exception!", false);
        }
    }

    @Test
    public void TestGetPointOnNotExistedUser_ResponseError() {
        fail("not implemented");
    }
    @Test
    public void TestGetPointOnExistedUser_ResponseSuccess()
    {
        try {
            String targetURL = String.format("http://%s:%d/point?username=%s", apiHost, apiPort, "zeroboo1");
            HttpURLConnection connection = null;

            URL url = new URL(targetURL);
            String content = "{\"username\":\"zeroboo3\",\"newPoint\":1000}";
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            connection.setRequestProperty("Content-Length",
                    Integer.toString(content.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches(false);

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            String result=response.toString();
            System.out.println(result);
            LinkedHashMap<String, Object> resp = gson.fromJson(result, new TypeToken<LinkedHashMap<String, Object>>(){}.getType());
            assertEquals(USERNAME_VALID, resp.get(LeaderboardServiceUserPointHandler.RESPONSE_KEY_USERNAME));
            assertEquals("1000.0", resp.get("currentPoint").toString());

            ///Connection
            assertEquals(200, connection.getResponseCode());
            assertEquals("keep-alive", connection.getHeaderField("Connection"));

        } catch (IOException e) {
            e.printStackTrace();
            assertTrue("Has exception!", false);
        }
    }
}

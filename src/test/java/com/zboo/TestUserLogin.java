package com.zboo;

import com.zboo.leaderboard.LeaderboardService;
import org.junit.*;

import javax.net.ssl.SSLException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;

public class TestUserLogin {
    static final String apiHost = "127.0.0.1";
    static final int apiPort = 8081;
    static LeaderboardService service;
    @BeforeClass
    public static void beforeClass()
    {
        service = new LeaderboardService();
        service.getConfig().setApiHost(apiHost);
        service.getConfig().setApiPort(apiPort);
        try {
            service.start();
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
            service.stop();
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

    public static String executePost(String targetURL, String urlParameters) {
        HttpURLConnection connection = null;
        String result=null;
        try {
            //Create connection
            URL url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            connection.setRequestProperty("Content-Length",
                    Integer.toString(urlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches(false);
            connection.setDoOutput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream (
                    connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.close();

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
            result=response.toString();
        } catch (Exception e) {
            e.printStackTrace();

        }
        return result;
    }
    public static String executeGET(String targetURL, String urlParameters) {
        HttpURLConnection connection = null;
        String result=null;
        try {
            //Create connection
            URL url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            connection.setRequestProperty("Content-Length",
                    Integer.toString(urlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches(false);
            connection.setDoOutput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream (
                    connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.close();

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
            result=response.toString();
        } catch (Exception e) {
            e.printStackTrace();

        }
        return result;
    }

    @Test
    public void TestLoginValid_Success()
    {
        try {
            String targetURL = String.format("http://%s:%d/login", apiHost, apiPort);
            HttpURLConnection connection = null;

            URL url = new URL(targetURL);
            String content = "{}";
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
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
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}

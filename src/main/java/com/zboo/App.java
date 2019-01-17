package com.zboo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zboo.leaderboard.LeaderboardServiceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.zboo.leaderboard.LeaderboardService;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.CertificateException;


/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        final Logger logger = LoggerFactory.getLogger(App.class);

        logger.info( "Starting!" );
        LeaderboardServiceConfig config = null;
        logger.info( "Loading config: ");
        if(args.length > 0)
        {
            Gson gson = new GsonBuilder().create();
            try {
                String content = new String(Files.readAllBytes(Paths.get(args[0])));
                config = gson.fromJson(content, LeaderboardServiceConfig.class);
            } catch (IOException e) {
                logger.error("Load config from {} failed");
            }
        }
        if(config == null)
        {
            logger.info("No config file loaded, use default config");
            config = LeaderboardServiceConfig.createDefaultConfig();
        }
        final LeaderboardService service = new LeaderboardService();
        service.setConfig(config);

        try {
            service.start();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (SSLException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            public void run()
            {
                logger.info("Application Terminating ...");
                try {
                    service.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                logger.info("Application Terminated ...");
            }
        });
    }
}

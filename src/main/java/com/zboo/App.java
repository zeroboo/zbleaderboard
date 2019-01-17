package com.zboo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.zboo.leaderboard.LeaderboardService;

import javax.net.ssl.SSLException;
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
        final LeaderboardService service = new LeaderboardService();
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

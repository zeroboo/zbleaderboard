package com.zboo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.zboo.leaderboard.LeaderboardService;


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
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            public void run()
            {
                logger.info("Application Terminating ...");
                service.stop();
                logger.info("Application Terminated ...");
            }
        });
    }
}

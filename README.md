# zbleaderboard
REST service for game leaderboard, based on Netty and Redis

## Build and run
1. Install maven: [https://maven.apache.org/install.html]

2. On the project folder, run:
```maven
mvn clean package
```
Jar file will be place on target folder:
  - zbleaderboard-<version>.jar 
  - zbleaderboard-<version>-full.jar: jar with dependencies 

3. Install & start redis: [https://redis.io]

4. Run service:
```
java -jar zbleaderboard-<version>.jar config.json
```


## Configuration
Edit config.json file for service endpoint and redis connection.
Below is default config:
```json
{
  "apiHost": "127.0.0.1",
  "apiPort": 8080,
  "nettyWorkerThread": 8,
  "redisHost": "127.0.0.1",
  "redisPort": 6379,
  "redisPassword": "",
  "redisTimeoutSecond": 30,
  "redisLeaderboardKey": "zbleaderboard",
  "redisLeaderboardUpdateCounterKey": "zbleaderboard_update_counter",
 ...
  }
}
```
## Features
1. Add/update a username and a score: 
2. Get notified when other users update their scores 
3. Admin able to see how many times a user updated their score. 
4. Admin can delete a username and score 

## Assumtions

- Authentication was not implemented 
- Clients are trusted every points they send, no verification performed 

## TODO


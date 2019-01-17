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
  User update point by sending a PUT request to endpoint host:port/point, with content as json
    eg: {"username":"zeroboo3", "newPoint":1000}
2. Get notified when other users update their scores:
  User login by a sending a POST request to endpoint host:port/login, with content as json: 
    eg: {"username":"zeroboo3"}
  After that user will be record by server and the connection will be kept for notification when a user update his/her scores.

3. Admin able to see how many times a user updated their score. 
  Admin can get user score and stats of this user by sending GET request to endpoint host:port/adminUserPoint?username=<targetUsername>
4. Admin can delete a username and score 
  Admin delete a user point by sending a DELETE request to endpoint host:port/adminUserPoint with body as json:
    eg: {"username":"zeroboo1", "admin":"admin"}

## Assumtions
- Authentication was not implemented 
- Clients are trusted every points they send, no verification performed 
- Statistics like "how many users updated their score in a time window" should be obtaining by processing server log (say, through elasticstack).




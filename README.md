Solution to the Asset Management Digital Challenge
==================================================

This is a solution for the "Asset Management Digital Challenge" defined in file README.pdf. 
The purpose of the assignment is to emulate the money transfers between two bank accounts with a RESTful application.

Main technologies used:
* Java 1.8
* Spring Boot 
* JUnit
* Mockito

List of added files
-------------------
* src/main/java/com/db/awmd/challenge/domain/Transfer.java
* src/main/java/com/db/awmd/challenge/exception/AccountNotFoundException.java
* src/main/java/com/db/awmd/challenge/exception/InsufficientBalanceException.java
* src/main/java/com/db/awmd/challenge/exception/InvalidTransferException.java
* src/main/java/com/db/awmd/challenge/service/TransfersService.java
* src/main/java/com/db/awmd/challenge/service/TransfersValidations.java
* src/main/java/com/db/awmd/challenge/web/TransfersController.java
* src/test/java/com/db/awmd/challenge/TransfersControllerTest.java
* src/test/java/com/db/awmd/challenge/TransfersServiceTest.java
* src/test/java/com/db/awmd/challenge/TransfersValidationsTest.java

Instructions to run the application
-----------------------------------
From the root folder of the project, build the application with:
```
./gradlew clean build
```

Run the application with:
```
java -jar build/libs/challenge-1.0-SNAPSHOT.jar
```

The requests can be sent by 'curl' command or other graphical applications like 'Postman'

Examples of queries with 'curl' command:
1. Create first account: 
```
curl -i -X POST \
-H "Content-Type:application/json" \
-d '{ "accountId": "Id-101", "balance": 1000 }' \
'http://localhost:18080/v1/accounts/'
```

2. Create second account:
```
curl -i -X POST \
-H "Content-Type:application/json" \
-d '{ "accountId": "Id-102", "balance": 500 }' \
'http://localhost:18080/v1/accounts/'
```

3. Make transfer:
```
curl -i -X POST \
-H "Content-Type:application/json" \
-d '{ "accountFromId": "Id-101", "accountToId": "Id-102", "amount": 2 }' \
'http://localhost:18080/v1/transfers/'
```

4. Check first account:
```
curl -i -X GET http://localhost:18080/v1/accounts/Id-101
```

5. Check second account:
```
curl -i -X GET http://localhost:18080/v1/accounts/Id-102
```

Pending points before going to production
-----------------------------------------
* The solution has been done by using Java Threads to allow multiple transfers at the same time. This is the simplest solution but in a real scenario it would be preferable to use a framework implementing the actor model, e.g. Akka Actors.

* It is needed to replace the in-memory repository by a transactional database. That provides us with persistence and transactions.

* Money transfers can happen between different banks, so an external integration system is needed. A possible solution is to use a messaging system i.e. RabbitMQ.

* Depending on the production platform, it can be necessary to create a Docker Image of the application. Once we are using a database and the application does not keep any state internally, it can be scaled by deploying many instances of the Docker container. Besides, it allows the use of other platforms like Kubernetes.

* To facilitate the maintenance and deployment of the application, a CI/CD system can be used, e.g. Jenkins. Jenkins can be watching the code repository and every time there is a commit in some specific branches, it can run a pipeline to build the application and deploy it. In our project, a Jenkinsfile can be added with the instructions of the build and deployment.

* In a production environment, it is needed to provide some monitoring capabilities. For example, some health checks can be necessary for platforms like Kubernetes. Also certain environments require some specific log format.

* Another 'nice to have' point is to update to the latest versions of Java, Spring Boot, and Gradle. The application will be easier to maintain in the future.



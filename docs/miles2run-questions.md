Some questions ...

1. What is the advantage of Thymeleaf over JSF ?

	Thymeleaf is an HTML 5 template engine. All the documents are valid HTMl 5 documents that you can use for static prototyping. It works great with AngularJS that we have used extensive for building this application. I don't think JSF is suitable for building Single Page web applications.

2. Why Thymeleaf was chosen instead of JSF that is part of Java EE ?
	
	1. I choose Thymeleaf because I wanted to use JAX-RS both as an MVC framework to render server side HTML pages and for exposing REST services. 
	2. This application is an Single Page application built using AngularJS. So, we needed a lightweight approach to send server side pages. JAX-RS along with Thyemleaf render the main HTML 5 page and then we use AngularJS to render different partials/views on that page. For example, when you log in to the application, the home page is rendered by JAX-RS and Thymeleaf. When you work with different sections of this page all of them are part of a SPA managed by AngualarJS.
	3. Thymeleaf documents are valid HTML 5 documents so you can work them offline for static prototyping.

3. What is the advantage of using SLF4J instead of JDK logging ?

	1. SLF4J is an abstraction or facade on top of different logging API. You can use any of the logging libraries with SLF4J including JDK logging. It allows you to change the underlying logging implementation without changing the source code. Logback is the successor of log4j and created by the same guy who created log4j. It is recommended people use logback instead of log4j because it is more performant and has better API and features.
	
	2. SLF4J API has great pattern substitution support. For example,
	<code>
	logger.debug("Calculating Goal-{} progress between {} and {}", goalId, startDate, 	endDate);
	</code>
	
		The above has two advantages: 
			1) There are no string concatenations. So it is more efficient. 
			2) As SLF4J avoids string concatenation and does not call toString(), so there is no need for isDebugEnabled(). String concatenation is only done if logging level is enabled. It leads to clean logging code.
	
4. What CDI services are used ? Are these reusable ? Using native NoSQL 
drivers ?
	
	We are mainly using CDI for dependency injection and for exposing application scoped beans. CDI Producers are used for creating EntityManagers and other configuration objects like Redis connection pool objects or MongoDB configuration object. By service, I meant service layer in a n-tiered application. 
	
	I don't understand what do you mean by reusable here.
	
	Yes, we are using native drivers for Redis and MongoDB. For Redis, we are using Jedis and for MongoDB we are using official MongoDB java driver provided by MongoDB itself. We have services that wrap these driver API and expose business functionalities that other layers could use.
	

5. Why jadira.usertype required ? Can't we convert this to UTC using Java 
and store in database ?

	You could do manual conversion of date to UTC before writing date object to database but that is only one side of the coin. When you read data back from the database, Hibernate(or JVM) convert the date back to the local timezone like in our case it was EDT. So, you have to do conversion on both the sides. I found this approach error prone. Using jadira is very easy, you just have to define an annotation in your entity class and it takes care of the rest as shown below.Also, jadira works with JodaTime date API as well as Java 8 date API. 

	```
	@Type(type = "org.jadira.usertype.dateandtime.legacyjdk.PersistentDate")
    private Date activityDate;
    ```

6. Is our database generated using SQL scripts or using new JPA functionality ?
	In the development environment database scripts are generated using new JPA properties as shown below. Then these scripts are manually applied to database. These scripts are committed to version control system. I don't use <code>javax.persistence.schema-generation.database.action</code> option as that is very risky.
	
	```
	<property name="javax.persistence.schema-generation.scripts.action" value="drop-	and-create"/>                                                                                    
	<property name="javax.persistence.schema-generation.scripts.create-target" value="file:~/tmp/create.sql"/>
	<property name="javax.persistence.schema-generation.scripts.drop-target" 	value="file:~/tmp/drop.sql"/>
	```

7. What is bean-discovery-mode for beans.xml ?

	I am using <code>bean-discovery-mode="all"</code>

8. Source for architecture and technology diagram images ?

	Available in Github repository under docs folder.

9. Lets plan to make the github workspace public before JavaOne and share 
the code, OK ?

	Sorry, this can't be done before JavaOne. There is lot of cleanup required before I would like it to be open source. My availability in September for miles2run is very limited so I can't cleanup all the source code. I also want to open source it but not now.

10. Slide 12 says JDK 7, should be JDK 8, right ?
	
	Yes it should be JDK 8 but we are not using any JDK 8 features.
	

11. Why Redis is optimal for storing/rendering counters ?

	In miles2run application, there are various application counters like counter for number of runners, cities, counters specific to goal like total distance covered in a goal etc. So, when you have lot of counters, it would be hell lot of write operations that you have to make to your database. Redis is an in-memory database so all the write and read operations are very performant. Redis counters are atomic, which means there are no concurrency issues associated with it as well. You can use Redis, INCR, INCRBY operations to add counters to your application.

12. How many instances of WildFly ? Load balancer ? Which one ?
	
	We are using two instances of WildFly running behind HAProxy load balancer. Application is autoscalable so it should scale as number of concurrent users go past the threshold.

13. Talk about dev/test and production environment easily facilitated by 
OpenShift ?

	For this app, I am not using OpenShift for my development environment. The advantage of working with OpenShift is that there is no OpenShift specific code in your application. So, its the same application that work locally that is deployed to test and production environment. The deployment currently is git based and we deploy the WAR to test and production environment. In future, I plan to use Jenkins to build the application and manage deployments.
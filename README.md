About
=====

This webapplication uses Play framework, Akka and Redis pubsub feature.

RedisController creates RedisSupervisorActor instance (that in turn creates 10 RedisPublisherActors and 10 RedisSubscriberActors) and on each GET request to /publish endpoint it asks RedisSupervisorActor to publish the value of 'message' URL parameter to Redis channel (configured in application.conf). RedisSupervisorActor delegates this request to RedisPublisherActor instance chosen via round robin (it uses RoundRobin logic in Akka router). Also on each GET request to /display endpoint it asks RedisSupervisorActor to display all messages published to Redis channel so far. RedisSupervisorActor delegates this request to RedisSubscriberActor instance (round robin is used here as well).

Branch 'master' contains implementation where each instance of RedisSubscriberActor holds  
RedisListener (i.e. Redis subscriber) instance internally (note that it blocks additional 
thread due to the fact that Jedis uses blocking IO), whereas branch 'onesubscriber' contains 
implementation where there is only one RedisListener instance that forwards each message received 
from Redis channel to all RedisSubscriberActor instances created. So branch 'onesubscriber' is 
more scalable whereas branch 'master' is designed to be used only with limited number of 
RedisSubscriberActors (in particlular, 10 actor instances are being created). 

Branch 'lettuce' contains implementation that uses Lettuce java library (instead of Jedis). 
Because Lettuce uses Netty (nonblocking IO) underneath, RedisListener instance doesn't block 
additional thread, so we can make each RedisSubscriberActor to hold RedisListener instance 
internally and avoid limitation on number of RedisSubscriberActor created (500 actor instances 
are created on this branch, but it can be modifier to whatever number you want).

=========================================================================================================

Usage
=====

1. Install and run Redis (redis-servier).
2. add Redis host, port, database, timeout and channel configurations into conf/application.conf
3. run ./bin/activator
4. Open the browser and navigate to [http://localhost:9000/publish?message=hello](http://localhost:9000/publish?message=hello)
5. run "PUBLISH [channel name from conf/application.conf] world" through redis-cli (in another terminal)
6. Open the browser and navigate to [http://localhost:9000/display](http://localhost:9000/display)

You should see "Messages seen so far: hello:world" message on the page. 

To sum up, two messages were published into channel (one through RedisPublisherActor, another through Redis directly) 
and the same messages were consumed by RedisSubscriberActor and displayed on the webpage.

You can also navigate to [http://localhost:9000/counter] and then to [http://localhost:9000/display] and start refreshing 
the page every 10 seconds. You should see messages "count0", "count1", "count2" appear on the page after every 10 sec refresh.

Details
=======

This file will be packaged with your application when using `activator dist`.

There are several demonstration files available in this template.

Controllers
===========
- RedisController.java:

  Shows how to publish to and display subscribed messages from Redis channel with simple HTTP requests.

- HomeController.java:

  Shows how to handle simple HTTP requests.

- AsyncController.java:

  Shows how to do asynchronous programming when handling a request.

- CountController.java:

  Shows how to inject a component into a controller and use the component when
  handling requests.

Components
==========

- Module.java:

  Shows how to use Guice to bind all the components needed by your application.

- Counter.java:

  An example of a component that contains state, in this case a simple counter.

- ApplicationTimer.java:

  An example of a component that starts when the application starts and stops
  when the application stops.

Filters
=======

- Filters.java:

  Creates the list of HTTP filters used by your application.

- ExampleFilter.java

  A simple filter that adds a header to every response.
  
Actors
======
- RedisSupervisorActor.java:
  
  Akka Actor that supervises RedisPublisherActor and RedisSubscriberActor instances via Akka router (see [http://doc.akka.io/docs/akka/2.4.4/java/routing.html](Akka Documentation).

- RedisPublisherActor.java:

  Akka Actor that publishes passed messages to Redis channel.
  
- RedisSubscriberActor.java:

  Akka Actor that receives messages from its own RedisListener and stores them internally. 
  It also replies with all messages received so far. 
  
Services
========

- RedisListener.java:
  Subscriber to Redis channel. It forwards published messages received from Redis channel to one RedisSubscriberActor (in branch 'master') or to all RedisSubscriberActor instances (in branch 'onesubscriber).
  
Tests
=====

- ApplicationTest.java:
  Contains unrelated tests for HomeController.java
- IntegrationTest.java:
  Contains integration test for RedisController.java that launches the application and tests endpoints. Make sure you have local Redis instance running for this test to succeed.

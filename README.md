About
=====

This is webapplication utilizing Play framework, Akka and Redis pubsub feature.

It creates RedisPublisherActor that listens for GET requests to /publish endpoint and publishes passed
messages to configured Redis channel.

It also creates RedisSubscriberActor that listens for GET requests to /display endpoint and displays
all messages that were received from the same configured Redis channel.

=========================================================================================================

Usage
=====

1. Install and run Redis (redis-servier).
2. add Redis host, port, database, timeout and channel configurations into conf/application.conf
3. run ./bin/activator
4. Open the browser and navigate to http://localhost:9000/publish?message=world
5. run "PUBLISH [channel name from conf/application.conf] hello" through redis-cli (in another terminal)
6. Open the browser and navigate to http://localhost:9000/display

You should see "Messages seen so far: hello:world" message on the page. 

To sum up, two messages were published into channel (one through RedisPublisherActor, another through Redis directly) 
and the same messages were consumed by RedisSubscriberActor and displayed on the webpage.

Details
=======

This file will be packaged with your application when using `activator dist`.

There are several demonstration files available in this template.

Controllers
===========

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

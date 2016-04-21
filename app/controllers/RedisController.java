package controllers;

import actors.RedisPublisherActor;
import actors.RedisSubscriberActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import messages.RedisActorProtocol;
import play.Configuration;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import scala.compat.java8.FutureConverters;

import static akka.pattern.Patterns.ask;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

/**
 * Created by mintik on 4/19/16.
 */
@Singleton
public class RedisController extends Controller {
    private final ExecutorService executorService;
    private ActorSystem actorSystem;
    private Configuration configuration;
    private List<ActorRef> publisherActors;
    private List<ActorRef> subscriberActors;
    private int publisherActorCounter;
    private int subscriberActorCounter;

    @Inject
    public RedisController(ActorSystem actorSystem, Configuration configuration) {
        this.actorSystem = actorSystem;
        this.configuration = configuration;
        /**
         * Separate thread pool for RedisSubscriberActors listeners
         */
        executorService = Executors.newFixedThreadPool(10);
        initializeActors();
    }

    private void initializeActors() {
        publisherActors = new ArrayList<>();
        subscriberActors = new ArrayList<>();
        IntStream.range(0, 10).forEach(value -> {
            ActorRef subscriberActor = actorSystem.actorOf(Props.create(RedisSubscriberActor.class, configuration,
                            executorService),
                    "RedisSubscriberActor-" + value);
            subscriberActors.add(subscriberActor);
            ActorRef publisherActor = actorSystem.actorOf(Props.create(RedisPublisherActor.class, configuration),
                    "RedisPublisherActor-" + value);
            publisherActors.add(publisherActor);
        });
        publisherActorCounter = 0;
        subscriberActorCounter = 0;
    }

    private ActorRef getPublisherActor() {
        return publisherActors.get(publisherActorCounter++ % publisherActors.size());
    }

    private ActorRef getSubscriberActor() {
        return subscriberActors.get(subscriberActorCounter++ % subscriberActors.size());
    }

    public CompletionStage<Result> displayMessages() {
        final ActorRef subscriberActor = getSubscriberActor();
        Logger.info("Calling RedisSubscriberActor {} with message {}", subscriberActor, RedisActorProtocol.DisplayMessages.INSTANCE);
        return FutureConverters.toJava(ask(subscriberActor, RedisActorProtocol.DisplayMessages.INSTANCE, 10000))
                .thenApply(response -> {
                    return ok(response.toString());
                });
    }

    public CompletionStage<Result> publishMessage(String message) {
        final ActorRef publisherActor = getPublisherActor();
        Logger.info("Calling RedisPublisherActor {} with message {}", publisherActor, message);
        return FutureConverters.toJava(ask(publisherActor, new RedisActorProtocol.PublishMessage(message), 10000))
                .thenApply(response -> {
                    return ok(response.toString());
                });
    }
}

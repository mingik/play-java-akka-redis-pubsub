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
import redis.clients.jedis.JedisPool;
import scala.compat.java8.FutureConverters;
import services.RedisListener;

import static akka.pattern.Patterns.ask;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Created by mintik on 4/19/16.
 */
@Singleton
public class RedisController extends Controller {
    private ActorRef subscriberActor;
    private ActorRef publisherActor;
    private ActorSystem actorSystem;
    private Configuration configuration;
    private RedisListener redisListener;

    @Inject
    public RedisController(ActorSystem actorSystem, Configuration configuration, RedisListener redisListener) {
        this.actorSystem = actorSystem;
        this.configuration = configuration;
        this.redisListener = redisListener;
        initializeActors();
    }

    private void initializeActors() {
        subscriberActor = actorSystem.actorOf(Props.create(RedisSubscriberActor.class), "SubscriberActor");
        publisherActor = actorSystem.actorOf(Props.create(RedisPublisherActor.class, configuration), "PublisherActor");
        redisListener.setSubscriberActor(subscriberActor);
    }

    public CompletionStage<Result> displayMessages() {
        Logger.info("Calling RedisSubscriberActor with message = {}", RedisActorProtocol.DisplayMessages.INSTANCE);
        return FutureConverters.toJava(ask(subscriberActor, RedisActorProtocol.DisplayMessages.INSTANCE, 10000))
                .thenApply(response -> ok(response.toString()));
    }

    public CompletionStage<Result> publishMessage(String message) {
        Logger.info("Calling RedisPublisherActor with message = {}", message);
        return FutureConverters.toJava(ask(publisherActor, new RedisActorProtocol.PublishMessage(message), 10000))
                .thenApply(response -> ok(response.toString()));
    }
}

package controllers;

import actors.RedisSupervisorActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.lambdaworks.redis.RedisClient;
import messages.RedisActorProtocol;
import play.Configuration;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import scala.compat.java8.FutureConverters;
import scala.concurrent.ExecutionContextExecutor;
import scala.concurrent.duration.Duration;
import services.Counter;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static akka.pattern.Patterns.ask;

/**
 * Created by mintik on 4/19/16.
 */
@Singleton
public class RedisController extends Controller {
    private final Counter counter;
    private final ExecutionContextExecutor exec;
    private RedisClient redisClient;
    private ActorSystem actorSystem;
    private Configuration configuration;
    private ActorRef redisSupervisorActor;

    @Inject
    public RedisController(ActorSystem actorSystem, Configuration configuration, Counter counter, ExecutionContextExecutor exec) {
        this.actorSystem = actorSystem;
        this.configuration = configuration;
        this.counter = counter;
        this.exec = exec;
        initializeRedisClient();
        initializeRedisSupervisorActor();
    }

    private void initializeRedisClient() {
        redisClient = RedisClient.create(configuration.getString("redis.url"));
    }

    private void initializeRedisSupervisorActor() {
        redisSupervisorActor = actorSystem.actorOf(Props.create(RedisSupervisorActor.class, configuration, redisClient));
    }

    public CompletionStage<Result> displayMessages() {
        Logger.info("Calling RedisSupervisorActor {} with message {}", redisSupervisorActor, RedisActorProtocol.DisplayMessages.INSTANCE);
        return FutureConverters.toJava(ask(redisSupervisorActor, RedisActorProtocol.DisplayMessages.INSTANCE, 10000))
                .thenApply(response -> ok(response.toString()));
    }

    public CompletionStage<Result> publishMessage(String message) {
        Logger.info("Calling RedisPublisherActor {} with message {}", redisSupervisorActor, message);
        return FutureConverters.toJava(ask(redisSupervisorActor, new RedisActorProtocol.PublishMessage(message), 10000))
                .thenApply(response -> ok(response.toString()));
    }

    public Result publishCounter() {
        return ok(dispatchCounter(10, TimeUnit.SECONDS));
    }

    private String dispatchCounter(long interval, TimeUnit timeUnit) {
        /**
         * Make Akka scheduler to call publishMessage with counter-generated messages
         * every 10 seconds.
         */
        actorSystem.scheduler().schedule(
                Duration.create(0, timeUnit),
                Duration.create(interval, timeUnit),
                (Runnable) () -> redisSupervisorActor.tell(
                        new RedisActorProtocol.PublishMessage("count" + counter.nextCount()), ActorRef.noSender()),
                exec
        );
        return "started 10 seconds counter";
    }
}

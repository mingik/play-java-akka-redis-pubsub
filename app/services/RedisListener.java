package services;

import akka.actor.ActorPath;
import akka.actor.ActorRef;
import messages.RedisActorProtocol;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import play.Configuration;
import play.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * This class is a Redis subscriber that also holds an ActorRef.
 * Each message received from Redis channel will be forwarded to
 * the corresponding Actor.
 *
 * Created by mintik on 4/20/16.
 */
@Singleton
public class RedisListener extends JedisPubSub {
    private Jedis jedis;
    private Configuration configuration;
    private Map<ActorPath, ActorRef> subscriberActors;

    @Inject
    public RedisListener(Configuration configuration) {
        this.configuration = configuration;
        initFields();
    }

    private void initFields() {
        jedis = new Jedis(configuration.getString("redis.host"));
        subscriberActors = new HashMap<>();
    }

    public void addSubscriberActor(ActorPath subscriberActorPath, ActorRef subscriberActor) {
        subscriberActors.put(subscriberActorPath, subscriberActor);
    }

    public void removeSubscriberActor(ActorPath subscriberActorPath) {
        subscriberActors.remove(subscriberActorPath);
    }

    public void startListening(ExecutorService exec) {
        /**
         * Start listening for the messages from channel on separate thread pool
         */
        CompletableFuture.runAsync(() -> {
                    jedis.subscribe(this, configuration.getString("redis.channel"));
                    jedis.quit();
                },
                exec);
    }

    @Override
    public void onMessage(String channel, String message) {
        Logger.info("RedisListener onMessage: channel = {}, message = {}", channel, message);
        subscriberActors.values().stream().forEach(actorRef ->
                actorRef.tell(new RedisActorProtocol.SubscribedMessage(message, channel), ActorRef.noSender()));
    }

    @Override
    public void onPMessage(String pattern, String channel, String message) {
        Logger.info("RedisListener onPMessage: pattern = {}, channel = {}, message = {}", pattern, channel, message);
        subscriberActors.values().stream().forEach(actorRef ->
                actorRef.tell(new RedisActorProtocol.SubscribedMessage(message, channel, pattern), ActorRef.noSender()));
    }

    @Override
    public void onSubscribe(String channel, int subscribedChannels) {
        Logger.info("RedisListener onSubscribe: channel = {}, subscribedChannels = {}", channel, subscribedChannels);
    }

    @Override
    public void onUnsubscribe(String channel, int subscribedChannels) {
        Logger.info("RedisListener onUnsubscribe: channel = {}, subscribedChannels = {}", channel, subscribedChannels);
    }

    @Override
    public void onPUnsubscribe(String pattern, int subscribedChannels) {
        Logger.info("RedisListener onPUnsubscribe: pattern = {}, subscribedChannels = {}", pattern, subscribedChannels);
    }

    @Override
    public void onPSubscribe(String pattern, int subscribedChannels) {
        Logger.info("RedisListener onPSubscribe: pattern = {}, subscribedChannels = {}", pattern, subscribedChannels);
    }
}

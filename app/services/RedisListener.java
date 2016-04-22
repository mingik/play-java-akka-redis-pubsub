package services;

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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * This class is a Redis subscriber that also holds an ActorRef.
 * Each message received from Redis channel will be forwarded to
 * the corresponding Actor.
 *
 * Created by mintik on 4/20/16.
 */
public class RedisListener extends JedisPubSub {
    private ActorRef subscriberActor = ActorRef.noSender();
    private Jedis jedis;
    private Configuration configuration;

    public RedisListener(Configuration configuration) {
        this.configuration = configuration;
        initFields();
    }

    private void initFields() {
        jedis = new Jedis(configuration.getString("redis.host"));
    }

    public void setSubscriberActor(ActorRef subscriberActor, ExecutorService exec) {
        this.subscriberActor = subscriberActor;
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
        subscriberActor.tell(new RedisActorProtocol.SubscribedMessage(message, channel), ActorRef.noSender());
    }

    @Override
    public void onPMessage(String pattern, String channel, String message) {
        Logger.info("RedisListener onPMessage: pattern = {}, channel = {}, message = {}", pattern, channel, message);
        subscriberActor.tell(new RedisActorProtocol.SubscribedMessage(message, channel, pattern), ActorRef.noSender());
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

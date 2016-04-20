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

/**
 * Created by mintik on 4/20/16.
 */
@Singleton
public class RedisListener extends JedisPubSub {
    private ActorRef subscriberActor = ActorRef.noSender();
    private JedisPool jedisPool;
    private Jedis jedis;
    private Configuration configuration;

    @Inject
    public RedisListener(Configuration configuration) {
        this.configuration = configuration;
        initFields();
    }

    private void initFields() {
        jedisPool = new JedisPool(new GenericObjectPoolConfig(), configuration.getString("redis.host"), configuration.getInt("redis.port"), configuration.getInt("redis.timeout"), null, configuration.getInt("redis.database"));
        jedis = jedisPool.getResource();
    }

    public void setSubscriberActor(ActorRef subscriberActor) {
        this.subscriberActor = subscriberActor;
        CompletableFuture.runAsync(() -> jedis.subscribe(this, configuration.getString("redis.channel")));
    }

    @Override
    public void onMessage(String channel, String message) {
        Logger.info("RedisListener onMessage: channel = {}, message = {}", channel, message);
        subscriberActor.tell(new RedisActorProtocol.SubscribedMessage(message), ActorRef.noSender());
    }

    @Override
    public void onPMessage(String pattern, String channel, String message) {
        Logger.info("RedisListener onPMessage: pattern = {}, channel = {}, message = {}", pattern, channel, message);
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

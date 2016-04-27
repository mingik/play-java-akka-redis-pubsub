package services;

import akka.actor.ActorRef;
import com.lambdaworks.redis.pubsub.RedisPubSubListener;
import messages.RedisActorProtocol;
import play.Configuration;
import play.Logger;

/**
 * This class is a Redis subscriber that also holds an ActorRef.
 * Each message received from Redis channel will be forwarded to
 * the corresponding Actor.
 *
 * Created by mintik on 4/20/16.
 */
public class RedisListener implements RedisPubSubListener<String, String> {
    private ActorRef subscriberActor;
    private Configuration configuration;

    public RedisListener(Configuration configuration, ActorRef subscriberActor) {
        this.configuration = configuration;
        this.subscriberActor = subscriberActor;
    }

    @Override
    public void message(String channel, String message) {
        Logger.info("On thread {}: Received message: {}", Thread.currentThread(), message);
        subscriberActor.tell(new RedisActorProtocol.SubscribedMessage(message, channel), ActorRef.noSender());
    }

    @Override
    public void message(String pattern, String channel, String message) {

    }

    @Override
    public void subscribed(String channel, long count) {

    }

    @Override
    public void psubscribed(String pattern, long count) {

    }

    @Override
    public void unsubscribed(String channel, long count) {

    }

    @Override
    public void punsubscribed(String pattern, long count) {

    }
}

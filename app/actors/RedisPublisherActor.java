package actors;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.pubsub.StatefulRedisPubSubConnection;
import messages.RedisActorProtocol;
import play.Configuration;
import play.Logger;

/**
 * Created by mintik on 4/19/16.
 */
public class RedisPublisherActor extends UntypedActor {

    private final RedisClient redisClient;
    private StatefulRedisPubSubConnection<String, String> connection;
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private Configuration configuration;

    public RedisPublisherActor(Configuration configuration, RedisClient redisClient) {
        this.configuration = configuration;
        this.redisClient = redisClient;
    }

    @Override
    public void onReceive(Object message) throws Exception {
        log.info("RedisPublisherActor: {} received message: {}", self(), message);

        if (message instanceof RedisActorProtocol.PublishMessage) {
            String publishMessage = ((RedisActorProtocol.PublishMessage) message).publishMessage;
            connection.sync().publish(configuration.getString("redis.channel"), publishMessage);
            sender().tell(RedisActorProtocol.PublishAcknowledged.INSTANCE, self());
        }
    }

    @Override
    public void preStart() throws Exception {
        Logger.info("RedisPublisherActor {} preStart(): Connecting as publisher to Redis", self());
        connection = redisClient.connectPubSub();
    }

    @Override
    public void postStop() throws Exception {
        Logger.info("RedisPublisherActor {} postStop(): Closing connection to Redis", self());
        connection.close();
    }
}

package actors;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.pubsub.RedisPubSubListener;
import com.lambdaworks.redis.pubsub.StatefulRedisPubSubConnection;
import messages.RedisActorProtocol;
import org.apache.commons.lang3.StringUtils;
import play.Configuration;
import play.Logger;
import services.RedisListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mintik on 4/19/16.
 */
public class RedisSubscriberActor extends UntypedActor {

    private final RedisClient redisClient;
    private StatefulRedisPubSubConnection<String, String> connection;
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    final private List<String> receivedMessages = new ArrayList<>();

    private Configuration configuration;

    public RedisSubscriberActor(Configuration configuration, RedisClient redisClient) {
        this.configuration = configuration;
        this.redisClient = redisClient;
    }

    @Override
    public void preStart() throws Exception {
        Logger.info("RedisSubscriberActor {} preStart(): Connecting as subscriber to Redis", self());
        connection = redisClient.connectPubSub();
        connection.addListener(new RedisListener(configuration, self()));
        connection.sync().subscribe(configuration.getString("redis.channel"));
    }

    @Override
    public void postStop() throws Exception {
        Logger.info("RedisSubscriberActor {} postStop(): Closing connection to Redis", self());
        connection.close();
    }

    @Override
    public void onReceive(Object message) throws Exception {
        log.info("On thread: {} RedisSubscriberActor: {} received message: {}", Thread.currentThread(), self(), message);

        if (message instanceof RedisActorProtocol.DisplayMessages) {
            sender().tell(new RedisActorProtocol.ReceivedMessages("Messages seen so far: " +
                            StringUtils.join(receivedMessages.toArray(), ":")),
                    self());
        } else if (message instanceof RedisActorProtocol.SubscribedMessage) {
            receivedMessages.add(((RedisActorProtocol.SubscribedMessage) message).subscribedMessage);
        }
    }
}

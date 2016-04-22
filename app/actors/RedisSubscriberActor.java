package actors;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import messages.RedisActorProtocol;
import org.apache.commons.lang3.StringUtils;
import play.Configuration;
import redis.clients.jedis.JedisPool;
import services.RedisListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

/**
 * Created by mintik on 4/19/16.
 */
public class RedisSubscriberActor extends UntypedActor {

    private final ExecutorService exec;
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    final private List<String> receivedMessages = new ArrayList<>();
    private RedisListener redisListener;
    private Configuration configuration;

    public RedisSubscriberActor(Configuration configuration, ExecutorService exec) {
        this.configuration = configuration;
        this.exec = exec;
        initListener();
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
    }

    private void initListener() {
        redisListener = new RedisListener(configuration);
        /**
         * This will start the listener on another thread.
         * The listener will forward messages received from Redis to
         * this actor instance.
         */
        redisListener.setSubscriberActor(self(), exec);
    }

    @Override
    public void onReceive(Object message) throws Exception {
        log.info("RedisSubscriberActor: {} received message: {}", self(), message);

        if (message instanceof RedisActorProtocol.DisplayMessages) {
            sender().tell(new RedisActorProtocol.ReceivedMessages("Messages seen so far: " +
                            StringUtils.join(receivedMessages.toArray(), ":")),
                    self());
        } else if (message instanceof RedisActorProtocol.SubscribedMessage) {
            receivedMessages.add(((RedisActorProtocol.SubscribedMessage) message).subscribedMessage);
        }
    }
}

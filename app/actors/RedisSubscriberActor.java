package actors;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import messages.RedisActorProtocol;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by mintik on 4/19/16.
 */
public class RedisSubscriberActor extends UntypedActor {

    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    final private List<String> receivedMessages = new ArrayList<>();

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

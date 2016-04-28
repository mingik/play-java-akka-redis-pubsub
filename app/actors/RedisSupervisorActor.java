package actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.ActorRefRoutee;
import akka.routing.RoundRobinRoutingLogic;
import akka.routing.Routee;
import akka.routing.Router;
import messages.RedisActorProtocol;
import play.Configuration;
import redis.clients.jedis.JedisPool;
import services.RedisListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

/**
 * Created by mintik on 4/27/16.
 */
public class RedisSupervisorActor extends UntypedActor {
    private final RedisListener redisListener;
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private final Configuration configuration;
    private final int NUMBER_OF_ACTORS = 10;
    /**
     * Separate thread pool for RedisSubscriberActors listeners
     */
    private final ExecutorService executorService = Executors.newFixedThreadPool(1);
    private final JedisPool jedisPool;
    private Router publisherRouter;
    private ActorRef subscriberActor;

    private void initRoutees() {
        subscriberActor = getContext().actorOf(Props.create(RedisSubscriberActor.class, redisListener),
                "RedisSubscriberActor-single");
        getContext().watch(subscriberActor);

        List<Routee> publisherRoutees = new ArrayList<>();
        IntStream.range(0, NUMBER_OF_ACTORS).forEach(value -> {
            ActorRef publisherActor = getContext().actorOf(Props.create(RedisPublisherActor.class, configuration,
                            jedisPool),
                    "RedisPublisherActor-" + value);
            getContext().watch(publisherActor);
            publisherRoutees.add(new ActorRefRoutee(publisherActor));
        });
        publisherRouter = new Router(new RoundRobinRoutingLogic(), publisherRoutees);
    }

    public RedisSupervisorActor(Configuration configuration, JedisPool jedisPool, RedisListener redisListener) {
        this.configuration = configuration;
        this.jedisPool = jedisPool;
        this.redisListener = redisListener;
        initRoutees();
    }

    @Override
    public void onReceive(Object message) throws Exception {
        log.info("RedisSubscriberActor: {} received message: {}", self(), message);

        if (message instanceof RedisActorProtocol.DisplayMessages) {
            subscriberActor.tell(message, getSender());
        } else if (message instanceof RedisActorProtocol.PublishMessage) {
            publisherRouter.route(message, getSender());
        } else if (message instanceof Terminated) {
            Terminated terminated = (Terminated) message;
            ActorRef terminatedActor = terminated.actor();
            if (terminatedActor.getClass().equals(RedisPublisherActor.class)) {
                publisherRouter = publisherRouter.removeRoutee(terminatedActor);
                ActorRef newPublisherActor = getContext().actorOf(Props.create(RedisPublisherActor.class, configuration,
                        jedisPool), terminatedActor.path().name());
                getContext().watch(newPublisherActor);
                publisherRouter = publisherRouter.addRoutee(new ActorRefRoutee(newPublisherActor));
            } else {
                ActorRef newSubscriberActor = getContext().actorOf(Props.create(RedisSubscriberActor.class, configuration,
                        executorService), terminatedActor.path().name());
                getContext().watch(newSubscriberActor);
                subscriberActor = newSubscriberActor;
            }
        }
    }
}
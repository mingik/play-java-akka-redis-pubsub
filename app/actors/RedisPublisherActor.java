package actors;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import messages.RedisActorProtocol;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import play.Configuration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Created by mintik on 4/19/16.
 */
public class RedisPublisherActor extends UntypedActor {

    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private JedisPool jedisPool;
    private Jedis jedis;
    private Configuration configuration;

    public RedisPublisherActor(Configuration configuration) {
        this.configuration = configuration;
        initializeFields();
    }

    private void initializeFields() {
        jedisPool = new JedisPool(new GenericObjectPoolConfig(), configuration.getString("redis.host"),
                configuration.getInt("redis.port"), configuration.getInt("redis.timeout"), null,
                configuration.getInt("redis.database"));
        jedis = jedisPool.getResource();
    }

    @Override
    public void onReceive(Object message) throws Exception {
        log.info("RedisPublisherActor: {} received message: {}", self(), message);

        if (message instanceof RedisActorProtocol.PublishMessage) {
            String publishMessage = ((RedisActorProtocol.PublishMessage) message).publishMessage;
            jedis.publish(configuration.getString("redis.channel"), publishMessage);
            sender().tell(RedisActorProtocol.PublishAcknowledged.INSTANCE, self());
        }
    }
}

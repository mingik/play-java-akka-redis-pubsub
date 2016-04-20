package messages;

/**
 * Created by mintik on 4/19/16.
 */
public class RedisActorProtocol {
    public static class DisplayMessages {
        public static DisplayMessages INSTANCE = new DisplayMessages();
    }

    public static class SubscribedMessage {
        public final String subscribedMessage;
        public SubscribedMessage(String subscribedMessage) {
            this.subscribedMessage = subscribedMessage;
        }
    }

    public static class ReceivedMessages {
        public final String receivedMessages;
        public ReceivedMessages(String receivedMessages) {
            this.receivedMessages = receivedMessages;
        }

        @Override
        public String toString() {
            return receivedMessages;
        }
    }

    public static class PublishMessage {
        public final String publishMessage;
        public PublishMessage(String publishMessage) {
           this.publishMessage = publishMessage;
        }
    }

    public static class PublishAcknowledged {
        public static PublishAcknowledged INSTANCE = new PublishAcknowledged();

        @Override
        public String toString() {
            return getClass().getCanonicalName();
        }
    }
}

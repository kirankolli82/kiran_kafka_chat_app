package com.kiran.kafka.transport;

import com.kiran.ContactsTopic;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

/**
 * Created by Kiran Kolli on 06-04-2019.
 */
public class MessageSerializationUtil implements Serializer<Message>, Deserializer<Message> {

    private static final String DELIM = "\001";

    @Override
    public Message deserialize(String topic, byte[] data) {
        String messageString = new String(data);
        String[] parts = messageString.split(DELIM);
        return new Message(new ContactsTopic.Contact(parts[0]), parts[1]);
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public byte[] serialize(String topic, Message data) {
        String stringBuffer = data.getFrom().getUserId() +
                DELIM +
                data.getMessage();
        return stringBuffer.getBytes();
    }

    @Override
    public void close() {

    }
}

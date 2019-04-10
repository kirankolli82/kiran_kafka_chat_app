package com.kiran.kafka.transport;

import com.kiran.ContactsTopic;
import com.kiran.ContactsTopic.Contact;
import com.kiran.util.Tuple;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

/**
 * Created by Kiran Kolli on 06-04-2019.
 */
public class MessageKeySerializationUtil implements Serializer<Tuple<Contact, Contact>>, Deserializer<Tuple<Contact, Contact>> {

    private static final String DELIM = ":";

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public byte[] serialize(String topic, Tuple<Contact, Contact> data) {
        return getKey(data.getKey(), data.getValue()).getBytes();
    }

    @Override
    public void close() {

    }

    @Override
    public Tuple<Contact, Contact> deserialize(String topic, byte[] data) {
        String key = new String(data);
        String[] contacts = key.split(DELIM);
        return new Tuple<>(new Contact(contacts[0]), new Contact(contacts[1]));
    }

    private static String getKey(ContactsTopic.Contact contact1, ContactsTopic.Contact contact2) {
        if (contact1.getUserId().compareTo(contact2.getUserId()) < 0) {
            return contact1.getUserId() + DELIM + contact2.getUserId();
        } else {
            return contact2.getUserId() + DELIM + contact1.getUserId();
        }
    }
}

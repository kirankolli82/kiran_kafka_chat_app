package com.kiran.kafka.transport;

import com.kiran.ContactsTopic.Contact;

import java.util.Objects;

/**
 * Created by Kiran Kolli on 06-04-2019.
 */
public class Message {

    private final Contact from;
    private final String message;

    public Message(Contact from, String message) {
        this.from = from;
        this.message = message;
    }

    public Contact getFrom() {
        return from;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message1 = (Message) o;
        return Objects.equals(from, message1.from) &&
                Objects.equals(message, message1.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, message);
    }

    @Override
    public String toString() {
        return "Message{" +
                "from=" + from +
                ", message='" + message + '\'' +
                '}';
    }
}

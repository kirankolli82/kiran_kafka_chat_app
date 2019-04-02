package com.kiran;

import java.util.Objects;

/**
 * Created by Kiran Kolli on 01-04-2019.
 */
public interface ContactsTopic {

    class Contact {
        private final String userId;

        public Contact(String userId) {
            this.userId = userId;
        }

        public String getUserId() {
            return userId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Contact contact = (Contact) o;
            return Objects.equals(userId, contact.userId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId);
        }

        @Override
        public String toString() {
            return "Contact{" +
                    "userId='" + userId + '\'' +
                    '}';
        }
    }

    void subscribe(Subscriber subscriber);

    interface Subscriber {
        Contact getId();

        void onContactAdded(Contact contact);

        void onContactDeleted(Contact contact);
    }
}

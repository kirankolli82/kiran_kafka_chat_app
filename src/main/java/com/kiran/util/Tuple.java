package com.kiran.util;

import java.util.Objects;

/**
 * Created by Kiran Kolli on 06-04-2019.
 */
public class Tuple<KeyTypeT, ValueTypeT> {

    private final KeyTypeT key;
    private final ValueTypeT value;

    public Tuple(KeyTypeT key, ValueTypeT value) {
        this.key = key;
        this.value = value;
    }

    public KeyTypeT getKey() {
        return key;
    }

    public ValueTypeT getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tuple<?, ?> tuple = (Tuple<?, ?>) o;
        return Objects.equals(key, tuple.key) &&
                Objects.equals(value, tuple.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public String toString() {
        return "Tuple{" +
                "key=" + key +
                ", value=" + value +
                '}';
    }
}

package com.kitsoft.freetify.algo.outerapi;

import com.kitsoft.freetify.outerapi.User;

import java.util.Objects;

public class UserImpl implements User {

    private final String name;
    private final int id;

    public UserImpl(String name, int id) {
        this.name = name;
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserImpl user = (UserImpl) o;
        return id == user.id && Objects.equals(name, user.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, id);
    }

    @Override
    public String toString() {
        return "UserImpl{" +
               "name='" + name + '\'' +
               ", id=" + id +
               '}';
    }
}

package com.kitsoft.freetify.algo.outerapi;

import com.kitsoft.freetify.outerapi.Album;
import com.kitsoft.freetify.outerapi.Song;

import java.net.URL;
import java.util.Objects;

public class SongImpl implements Song {

    private final String name;
    private final int id;

    public SongImpl(String name, int id) {
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
    public Album getAlbum() {
        return null;
    }

    @Override
    public String getGenre() {
        return null;
    }

    @Override
    public URL asURL() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SongImpl song = (SongImpl) o;
        return id == song.id && Objects.equals(name, song.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, id);
    }

    @Override
    public String toString() {
        return "SongImpl{" +
               "name='" + name + '\'' +
               ", id=" + id +
               '}';
    }
}

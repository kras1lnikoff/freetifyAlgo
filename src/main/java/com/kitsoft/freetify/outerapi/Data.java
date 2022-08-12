package com.kitsoft.freetify.outerapi;

import java.util.List;

public interface Data {

    List<Song> allSongs();

    List<User> allUsers();

    int getRating(Song song, User user);
}

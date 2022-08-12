package com.kitsoft.freetify.outerapi;

import java.util.List;

public interface Album extends Labeled, List<Song> {

    Artist getArtist();
}

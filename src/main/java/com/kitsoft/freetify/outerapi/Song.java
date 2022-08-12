package com.kitsoft.freetify.outerapi;

import java.net.URL;

public interface Song extends Labeled {

    Album getAlbum();

    String getGenre();

    URL asURL();
}

/*
 * Created by Camment OY on 07/24/2018.
 * Copyright (c) 2018 Camment OY. All rights reserved.
 */

package tv.camment.cammentads;

/**
 * A model representing metadata of a show to get relevant preroll banner from Camment API
 */
public final class CMAShowMetadata {

    private final String uuid;

    private final String genre;

    private final String title;

    private final int length;

    private final boolean isLive;

    public CMAShowMetadata(String uuid, String genre, String title, int length, boolean isLive) {
        this.uuid = uuid;
        this.genre = genre;
        this.title = title;
        this.length = length;
        this.isLive = isLive;
    }

    public String getUuid() {
        return uuid;
    }

    public String getGenre() {
        return genre;
    }

    public String getTitle() {
        return title;
    }

    public int getLength() {
        return length;
    }

    public boolean getIsLive() {
        return isLive;
    }

    public static final class Builder {
        private String uuid;
        private String genre;
        private String title;
        private int length;
        private boolean isLive;

        public Builder setUuid(String uuid) {
            this.uuid = uuid;
            return this;
        }

        public Builder setGenre(String genre) {
            this.genre = genre;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setLength(int length) {
            this.length = length;
            return this;
        }

        public Builder setIsLive(boolean isLive) {
            this.isLive = isLive;
            return this;
        }

        public CMAShowMetadata build() {
            return new CMAShowMetadata(uuid, genre, title, length, isLive);
        }
    }

}

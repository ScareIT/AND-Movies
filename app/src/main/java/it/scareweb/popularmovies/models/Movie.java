package it.scareweb.popularmovies.models;

import java.io.Serializable;

/**
 * Created by luca on 19/02/2018.
 */

                            // "Serializable" allows to pass the object as extra in an Intent
public class Movie implements Serializable {
    private String movieTitle;
    private String moviePicture;

    public Movie(String title, String picture) {
        this.movieTitle = title;
        this.moviePicture = picture;
    }

    public String getPicture() {
        return this.moviePicture;
    }

    public void setPicture(String pictureUrl) {
        this.moviePicture = pictureUrl;
    }

    public String getTitle() {
        return this.movieTitle;
    }

    public void setTitle(String title) {
        this.movieTitle = title;
    }
}

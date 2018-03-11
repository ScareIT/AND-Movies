package it.scareweb.popularmovies.models;

/**
 * Created by luca on 08/03/2018.
 */

public class MovieTrailer {
    public String Name;
    public String Size;
    public String Source;
    public String Type;

    public String Link() {
        return "http://www.youtube.com/watch?v=" + Source;
    }
}

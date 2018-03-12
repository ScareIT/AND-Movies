package it.scareweb.popularmovies.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by luca on 19/02/2018.
 */

public class Movie implements Parcelable {
    private int movieId;
    private String movieTitle;
    private String moviePictureUrl;
    private byte[] movieRawPicture;
    private String moviePlot;
    private float movieVote;
    private String movieVoteStr;
    private String movieReleaseDate;

    public Movie() {}

    public Movie(String title, String picture) {
        this.movieTitle = title;
        this.moviePictureUrl = picture;
    }

    public Movie(Parcel in) {
        this.movieId = in.readInt();
        this.movieTitle = in.readString();
        this.moviePictureUrl = in.readString();
        int _byte = in.readInt();
        if(_byte > 0) {
            this.movieRawPicture = new byte[_byte];
            in.readByteArray(this.movieRawPicture);
        }

        this.moviePlot = in.readString();
        this.movieVoteStr = in.readString();
        this.movieReleaseDate = in.readString();
    }

    public int getId() {
        return this.movieId;
    }

    public void setId(int movieId) {
        this.movieId = movieId;
    }

    public String getPicture() {
        return this.moviePictureUrl;
    }

    public void setPicture(String pictureUrl) {
        this.moviePictureUrl = pictureUrl;
    }

    public String getTitle() {
        return this.movieTitle;
    }

    public void setTitle(String title) {
        this.movieTitle = title;
    }

    public String getMovieVote() { return this.movieVoteStr; }

    public void setVote(String vote) { this.movieVoteStr = vote; }

    public String getMoviePlot() { return this.moviePlot; }

    public void setPlot(String plot) { this.moviePlot = plot; }

    public String getMovieReleaseDate() {return this.movieReleaseDate; }

    public void setMovieReleaseDate(String releaseDate) { this.movieReleaseDate = releaseDate; }

    public byte[] getMovieRawPicture() {return this.movieRawPicture; }

    public void setMovieRawPicture(byte[] bitmap) { this.movieRawPicture = bitmap; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(movieId);
        parcel.writeString(movieTitle);
        parcel.writeString(moviePictureUrl);
        if(this.movieRawPicture != null) {
            int _byte = this.movieRawPicture.length;
            parcel.writeInt(_byte);
            parcel.writeByteArray(this.movieRawPicture);
        } else {
            parcel.writeInt(0);
        }

        parcel.writeString(moviePlot);
        parcel.writeString(movieVoteStr);
        parcel.writeString(movieReleaseDate);
    }

    static final Parcelable.Creator<Movie> CREATOR
            = new Parcelable.Creator<Movie>() {

        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
}

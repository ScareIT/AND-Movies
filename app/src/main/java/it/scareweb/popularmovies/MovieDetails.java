package it.scareweb.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.v4.widget.ImageViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.scareweb.popularmovies.data.MovieProvider;
import it.scareweb.popularmovies.database.DbManager;
import it.scareweb.popularmovies.models.Movie;
import it.scareweb.popularmovies.models.SettingsAPI;

public class MovieDetails extends AppCompatActivity {
    Intent intent;

    @BindView(R.id.details_big_title)
    TextView bigTitle;

    @BindView(R.id.details_movie_vote)
    TextView vote;

    @BindView(R.id.details_movie_plot)
    TextView plot;

    @BindView(R.id.details_movie_release)
    TextView releaseDate;

    @BindView(R.id.details_movie_poster)
    ImageView poster;

    @BindView(R.id.details_movie_poster_clone)
    ImageView posterClone;

    @BindView(R.id.add_to_favourites)
    ImageButton addToFavouritesIcon;

    private int movieId;

    private boolean movieSaved;

    Uri movies;

    Movie selectedMovie;

    MovieDetails() {
        movieSaved = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        ButterKnife.bind(this);

        intent = getIntent();
        selectedMovie = (Movie)intent.getSerializableExtra("MOVIE");
        movieId = selectedMovie.getId();
        bigTitle.setText(selectedMovie.getTitle());
        vote.setText(selectedMovie.getMovieVote());
        plot.setText(selectedMovie.getMoviePlot());
        releaseDate.setText(selectedMovie.getMovieReleaseDate());
        poster.setContentDescription(selectedMovie.getTitle() + " poster");
//        Picasso.with(this)
//                .load(SettingsAPI.IMAGE_URL + SettingsAPI.IMAGE_SIZE_NORMAL + selectedMovie.getPicture())
//                .into(poster);
                Picasso.with(this)
                .load(SettingsAPI.IMAGE_URL + SettingsAPI.IMAGE_SIZE_NORMAL + selectedMovie.getPicture())
                .into(posterClone);

        movies = MovieProvider.BASE_CONTENT_URI;
        movies = movies.buildUpon().appendPath(MovieProvider.PATH_FAVOURITES).build();

        setupAddToFavouritesIcon();
    }

    private void setupAddToFavouritesIcon() {
        movieSaved = movieIsFavourite();
        addToFavouritesIcon.setOnClickListener(addToFavouritesListener());
        if(movieSaved) {
            addToFavouritesIcon.setColorFilter(getResources().getColor(R.color.favouriteIcon));
        }
    }

    private View.OnClickListener addToFavouritesListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageButton ibv = (ImageButton)v;

                if(!movieSaved) {
                    addMovieToFavourites();
                    ibv.setColorFilter(getResources().getColor(R.color.favouriteIcon));
                    movieSaved = true;
                } else {
                    removeMovieFromFavourites();
                    ibv.clearColorFilter();
                    movieSaved = false;
                }

            }
        };
    }

    private void addMovieToFavourites() {
        // Insertion
        ContentValues values = new ContentValues();
        values.put(DbManager.MOVIE_ID, movieId);
        values.put(DbManager.MOVIE_TITLE,
                bigTitle.getText().toString());
        values.put(DbManager.MOVIE_VOTE,
                vote.getText().toString());

        //region Blob experiments

        URLConnection ucon = null;
        InputStream is = null;
        try {
            ucon = new URL(SettingsAPI.IMAGE_URL + SettingsAPI.IMAGE_SIZE_NORMAL + selectedMovie.getPicture()).openConnection();
            is = ucon.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedInputStream bis = new BufferedInputStream(is);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        byte[] data = new byte[50];
        int current = 0;

        try {
            while((current = bis.read(data,0,data.length)) != -1){
                buffer.write(data,0,current);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        poster.setImageBitmap(BitmapFactory.decodeByteArray( buffer.toByteArray(),
                0, buffer.toByteArray().length));

        Bitmap bitmap = ((BitmapDrawable)poster.getDrawable()).getBitmap();
        //ByteArrayOutputStream stream = new ByteArrayOutputStream();
        //bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        //byte[] byteArray = stream.toByteArray();
        int size = bitmap.getRowBytes() * bitmap.getHeight();
        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
        bitmap.copyPixelsToBuffer(byteBuffer);
        byte[] byteArray1 = byteBuffer.array();

        posterClone.setImageBitmap(BitmapFactory.decodeByteArray( byteArray1,
                0, byteArray1.length));

        values.put(DbManager.MOVIE_POSTER,
                buffer.toByteArray());

        //endregion

        Uri uri = getContentResolver().insert(
                movies, values);

        Toast.makeText(getBaseContext(),
                uri.toString(), Toast.LENGTH_LONG).show();
    }

    private void removeMovieFromFavourites() {
        // Deletion
        Uri moviesOne = movies.buildUpon()
                .appendPath(String.valueOf(movieId)).build();

        getContentResolver().delete(
                moviesOne, null, null);
    }

    private boolean movieIsFavourite() {
        Uri moviesOne = movies.buildUpon()
                .appendPath(String.valueOf(movieId)).build();

        Cursor c = getContentResolver().query(moviesOne, null, null, null, null);

        if (c!=null && c.moveToFirst()) {
            return true;
        }

        return false;
    }
}

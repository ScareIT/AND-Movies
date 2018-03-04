package it.scareweb.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

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

    private int movieId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        ButterKnife.bind(this);

        intent = getIntent();
        Movie selectedMovie = (Movie)intent.getSerializableExtra("MOVIE");
        movieId = selectedMovie.getId();
        bigTitle.setText(selectedMovie.getTitle());
        vote.setText(selectedMovie.getMovieVote());
        plot.setText(selectedMovie.getMoviePlot());
        releaseDate.setText(selectedMovie.getMovieReleaseDate());
        poster.setContentDescription(selectedMovie.getTitle() + " poster");
        Picasso.with(this)
                .load(SettingsAPI.IMAGE_URL + SettingsAPI.IMAGE_SIZE_NORMAL + selectedMovie.getPicture())
                .into(poster);

        // *** Test with CProvider ***

        Uri movies = MovieProvider.BASE_CONTENT_URI;
        movies = movies.buildUpon().appendPath(MovieProvider.PATH_FAVOURITES).build();

        // Insertion
        ContentValues values = new ContentValues();
        values.put(DbManager.MOVIE_ID, movieId);
        values.put(DbManager.MOVIE_TITLE,
                bigTitle.getText().toString());
        values.put(DbManager.MOVIE_VOTE,
                vote.getText().toString());

        Uri uri = getContentResolver().insert(
                movies, values);

        Toast.makeText(getBaseContext(),
                uri.toString(), Toast.LENGTH_LONG).show();

        // Retrieve all movie records

        Cursor c = getContentResolver().query(movies, null, null, null, null);

        if (c!=null && c.moveToFirst()) {
            do{
                Log.i("movie", c.getString(c.getColumnIndex(DbManager.MOVIE_ID)) +
                        ", " +  c.getString(c.getColumnIndex( DbManager.MOVIE_TITLE)) +
                        ", " + c.getString(c.getColumnIndex( DbManager.MOVIE_VOTE)));
                Toast.makeText(this,
                        c.getString(c.getColumnIndex(DbManager.MOVIE_ID)) +
                                ", " +  c.getString(c.getColumnIndex( DbManager.MOVIE_TITLE)) +
                                ", " + c.getString(c.getColumnIndex( DbManager.MOVIE_VOTE)),
                        Toast.LENGTH_SHORT).show();
            } while (c.moveToNext());
        }


    }
}

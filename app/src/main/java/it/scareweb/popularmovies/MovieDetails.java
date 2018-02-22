package it.scareweb.popularmovies;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.scareweb.popularmovies.models.Movie;

public class MovieDetails extends AppCompatActivity {
    Intent intent;

    @BindView(R.id.details_big_title)
    TextView bigTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        ButterKnife.bind(this);

        intent = getIntent();
        Movie selectedMovie = (Movie)intent.getSerializableExtra("MOVIE");
        bigTitle.setText(selectedMovie.getTitle());
    }
}

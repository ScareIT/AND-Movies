package it.scareweb.popularmovies;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.JsonReader;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import it.scareweb.popularmovies.models.Movie;

import static android.view.Window.FEATURE_NO_TITLE;

public class MainActivity extends AppCompatActivity {

    private final String MOVIEDB_URL_PREFIX = "http://api.themoviedb.org/3/movie/popular?";

    private List<Movie> movieList;

    private GridLayout movieGrid;
    private TextView title1;
    private TextView title2;

    private MovieListAdapter movieListAdapter;
    private RecyclerView mRecyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        movieList = new ArrayList<>();

        movieListAdapter = new MovieListAdapter();

        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        layoutManager.setOrientation(GridLayoutManager.VERTICAL);
        layoutManager.setReverseLayout(false);

        mRecyclerView = findViewById(R.id.recyclerview_movies);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(movieListAdapter);

//        title1 = findViewById(R.id.title_tv);
//        title2 = findViewById(R.id.title2_tv);
        //movieGrid = findViewById(R.id.movies_gridl);

        try {
            internet();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private void setTitles() {
        String complete = "";
        for (Movie movie:
             movieList) {
            complete += movie.getTitle()  + "\n";
        }



//        Picasso.with(this)
//                .load("http://image.tmdb.org/t/p/w185" + movieList.get(0).getPicture())
//                .into(oneImg);
//        Picasso.with(this)
//                .load("http://image.tmdb.org/t/p/w185" + movieList.get(2).getPicture())
//                .into(oneImg3);
//        Picasso.with(this)
//                .load("http://image.tmdb.org/t/p/w185" + movieList.get(3).getPicture())
//                .into(oneImg4);
//        Picasso.with(this)
//                .load("http://image.tmdb.org/t/p/w185" + movieList.get(1).getPicture())
//                .into(oneImg2);

        title1.setText(complete);
        title2.setText("Seconda riga");
    }

    private void internet() throws MalformedURLException {
        Uri builtUri = Uri.parse(MOVIEDB_URL_PREFIX)
                .buildUpon()
                .appendQueryParameter("api_key", getString(R.string.key_v3_auth))
                .build();

        URL url = new URL(builtUri.toString());

        new GetMovies().execute(url);
    }

    private class GetMovies extends AsyncTask<URL, String, Void> {



        public void internet(URL url) throws IOException {


            HttpURLConnection urlConnection = (HttpURLConnection) url
                    .openConnection();

            InputStream in = urlConnection.getInputStream();
            InputStreamReader inReader = new InputStreamReader(in);


            JsonReader reader = new JsonReader(inReader);

            try {
                readMessagesArray(reader);
            } finally {
                reader.close();
            }

            //    InputStreamReader isw = new InputStreamReader(in);
        }

        private void readMessagesArray(JsonReader reader) throws IOException {


            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (name.equals("results")) {
                    readMovie(reader);
                } else {
                    reader.skipValue();
                }
            }
            reader.endObject();
        }

        public void readMovie(JsonReader reader) throws IOException {
            //List<String> titles = new ArrayList<>();

            reader.beginArray();
            while (reader.hasNext()) {
                 movieList.add(readTitle(reader));
            }

            reader.endArray();
        }

        public Movie readTitle(JsonReader reader) throws IOException {
            String title = "";
            String picture = "";
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (name.equals("title")) {
                    title = reader.nextString();
                } else if (name.equals("poster_path")) {
                    picture = reader.nextString();
                }
                else {
                    reader.skipValue();
                }
            }

            reader.endObject();
            return new Movie(title, picture);
        }

        @Override
        protected Void doInBackground(URL... urls) {
            try {
                internet(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //setTitles();
            movieListAdapter.setMovieTitles(movieList);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }


}

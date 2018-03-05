package it.scareweb.popularmovies;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.JsonReader;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.scareweb.popularmovies.models.Movie;
import it.scareweb.popularmovies.models.SettingsAPI;

public class MainActivity extends AppCompatActivity {

    private final String MovieDbUrl = SettingsAPI.BASE_URL;

    private final String ORDER_BY_KEY = "ORDER_BY";

    private String MovieDbCurrentOption = SettingsAPI.OPTION_POPULAR;

    private List<Movie> movieList;

    private MovieListAdapter movieListAdapter;

    boolean Popular;

    Context context;

    @BindView(R.id.recyclerview_movies)
    RecyclerView mRecyclerView;

    @BindView(R.id.intro)
    TextView tIntro;

    @BindView(R.id.no_connection_alert)
    TextView tNoConnection;

    MenuItem menuFavourites;

    MenuItem menuPopular;

    MenuItem menuTopRated;

    private boolean showFavourites;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showFavourites=false;

        if (savedInstanceState != null) {
            this.Popular = savedInstanceState.getBoolean(ORDER_BY_KEY);
        } else {
            this.Popular = true;
        }

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        context = this;
        movieList = new ArrayList<>();
        movieListAdapter = new MovieListAdapter();

        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        layoutManager.setOrientation(GridLayoutManager.VERTICAL);
        layoutManager.setReverseLayout(false);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(movieListAdapter);

        try {
            internet();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(ORDER_BY_KEY, this.Popular);
    }

    private void config() {
        if(menuPopular != null) {
            if (menuPopular.isChecked()) {
                this.Popular = true;
            } else {
                this.Popular = false;
            }
        }

        if(this.showFavourites) {
            MovieDbCurrentOption = "315837";
            return;
        }


        if(this.Popular) {
            MovieDbCurrentOption = SettingsAPI.OPTION_POPULAR;
        } else {
            MovieDbCurrentOption = SettingsAPI.OPTION_TOP_RATED;
        }
    }



    private void internet() throws MalformedURLException {
        config();
        Uri builtUri = Uri.parse(MovieDbUrl + MovieDbCurrentOption)
                .buildUpon()
                .appendQueryParameter("api_key", SettingsAPI.API_KEY)
                .build();

        URL url = new URL(builtUri.toString());

        if(!isInternetAvailable()) {
            tNoConnection.setVisibility(View.VISIBLE);
            return;
        }

        tIntro.setVisibility(View.VISIBLE);

        new GetMovies().execute(url);
    }

    private boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        if(networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            return true;
        }

        return false;
    }

    private class GetMovies extends AsyncTask<URL, String, Void> {

        public void internet(URL url) throws IOException {

            HttpURLConnection urlConnection = null;
            try {
                urlConnection = (HttpURLConnection) url
                        .openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }

            InputStream in = urlConnection.getInputStream();
            InputStreamReader inReader = new InputStreamReader(in);


            JsonReader reader = new JsonReader(inReader);

            try {
                if(showFavourites) {
                    movieList.add(readTitle(reader));
                } else {
                    readMessagesArray(reader);
                }
            } finally {
                reader.close();
            }

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
            Movie movie = new Movie();
            reader.beginObject();

            while (reader.hasNext()) {
                String name = reader.nextName();
                if (name.equals("title")) {
                    movie.setTitle(reader.nextString());
                } else if (name.equals("id")) {
                    movie.setId(reader.nextInt());
                } else if (name.equals("poster_path")) {
                    movie.setPicture(reader.nextString());
                } else if (name.equals("vote_average")) {
                    movie.setVote(reader.nextString());
                } else if (name.equals("overview")) {
                    movie.setPlot(reader.nextString());
                } else if (name.equals("release_date")) {
                    movie.setMovieReleaseDate(reader.nextString());
                }
                else {
                    reader.skipValue();
                }
            }

            reader.endObject();
            return movie;
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
        menuPopular = menu.findItem(R.id.orderby_popular);
        menuPopular.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                menuPopular.setChecked(true);
                movieList.clear();
                try {
                    internet();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                return false;
            }
        });

        menuTopRated = menu.findItem(R.id.orderby_toprated);
        menuTopRated.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                menuTopRated.setChecked(true);
                movieList.clear();
                try {
                    internet();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                return false;
            }
        });

        menuFavourites = menu.findItem(R.id.show_favourites);
        menuFavourites.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                showFavourites = true;
                movieList.clear();
                try {
                    internet();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                return false;
            }
        });

        if(!this.Popular) {
            menuTopRated.setChecked(true);
        }

        return true;
    }
}

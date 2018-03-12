package it.scareweb.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.JsonReader;
import android.util.JsonToken;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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
import it.scareweb.popularmovies.data.MovieProvider;
import it.scareweb.popularmovies.database.DbManager;
import it.scareweb.popularmovies.models.Movie;
import it.scareweb.popularmovies.models.SettingsAPI;
import it.scareweb.popularmovies.utils.NetworkUtils;

public class MainActivity extends AppCompatActivity implements MovieListAdapter.OnItemClickListener{

    private static final String MovieDbUrl = SettingsAPI.BASE_URL;

    private static final String ORDER_BY_KEY = "ORDER_BY";

    private static final String SHOW_FAVOURITES = "SHOW_FAVOURITES";

    private static final String MOVIE_LIST = "MOVIE_LIST";

    private static final String CURRENT_PAGE = "CURRENT_PAGE";

    private String MovieDbCurrentOption = SettingsAPI.OPTION_POPULAR;

    private ArrayList<Movie> movieList;

    private MovieListAdapter movieListAdapter;

    boolean Popular;

    Context context;

    @BindView(R.id.recyclerview_movies)
    RecyclerView mRecyclerView;

    @BindView(R.id.intro)
    TextView tIntro;

    @BindView(R.id.no_connection_alert)
    TextView tNoConnection;

    @BindView(R.id.no_connection_alert_favourites)
    TextView tNoConnectionAddOn;

    @BindView(R.id.no_favourites_alert)
    TextView tNoFavourites;

    MenuItem menuFavourites;

    MenuItem menuPopular;

    MenuItem menuTopRated;

    private int queryPage;

    private boolean showFavourites;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        context = this;

        movieListAdapter = new MovieListAdapter(this);

        final GridLayoutManager layoutManager = new GridLayoutManager(this, numberOfColumns());

        layoutManager.setOrientation(GridLayoutManager.VERTICAL);
        layoutManager.setReverseLayout(false);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(movieListAdapter);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            boolean loading = true;
            int pastVisiblesItems, visibleItemCount, totalItemCount;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                if(dy > 0) //check for scroll down
                {
                    visibleItemCount = layoutManager.getChildCount();
                    totalItemCount = layoutManager.getItemCount();
                    pastVisiblesItems = layoutManager.findFirstVisibleItemPosition();

                    if (loading)
                    {
                        if ( (visibleItemCount + pastVisiblesItems) >= totalItemCount)
                        {
                            loading = false;
                            queryPage++;
                            getAllMovies(queryPage);
                            loading = true;
                        }
                    }
                }
            }
        });

        if (savedInstanceState != null) {
            this.Popular = savedInstanceState.getBoolean(ORDER_BY_KEY);
            this.showFavourites = savedInstanceState.getBoolean(SHOW_FAVOURITES);
            this.queryPage = savedInstanceState.getInt(CURRENT_PAGE);
            this.movieList = savedInstanceState.getParcelableArrayList(MOVIE_LIST);
            movieListAdapter.setMovieTitles(movieList);
        } else {
            this.Popular = true;
            this.showFavourites = false;
            this.queryPage = 1;
            movieList = new ArrayList<>();
            getAllMovies();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(this.showFavourites) {
            this.movieList.clear();
            getAllMovies();
        }
    }

    /*
    * Get this idea from my reviewer! Great function!
    */
    private int numberOfColumns() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int widthDivider = 300;
        int width = displayMetrics.widthPixels;
        int nColumns = width / widthDivider;
        if (nColumns < 2) {
            return 2;
        }
        return nColumns;
    }

    @Override
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(ORDER_BY_KEY, this.Popular);
        outState.putBoolean(SHOW_FAVOURITES, this.showFavourites);
        outState.putParcelableArrayList(MOVIE_LIST, this.movieList);
        outState.putInt(CURRENT_PAGE, this.queryPage);
    }

    private boolean config() {
        if(menuPopular != null) {
            if (menuPopular.isChecked()) {
                this.Popular = true;
            } else {
                this.Popular = false;
            }
        }

        if(this.showFavourites) {
            return false;
        }

        if(this.Popular) {
            MovieDbCurrentOption = SettingsAPI.OPTION_POPULAR;
        } else {
            MovieDbCurrentOption = SettingsAPI.OPTION_TOP_RATED;
        }
        return true;
    }

    private void getAllMovies() {
        getAllMovies(1);
    }

    private void getAllMovies(int page) {
        this.tNoFavourites.setVisibility(View.GONE);
        boolean isInternetRequired = config();

        if(isInternetRequired) {
            Uri builtUri = Uri.parse(MovieDbUrl + MovieDbCurrentOption)
                    .buildUpon()
                    .appendQueryParameter("api_key", SettingsAPI.API_KEY)
                    .build();

            if (!NetworkUtils.isInternetAvailable(this)) {
                tNoConnection.setVisibility(View.VISIBLE);
                tNoConnectionAddOn.setVisibility(View.VISIBLE);
                return;
            }

            tIntro.setVisibility(View.VISIBLE);

            new GetMovies(page).execute(builtUri);
        } else if (this.showFavourites) {
            tIntro.setVisibility(View.VISIBLE);
            tNoConnection.setVisibility(View.GONE);
            tNoConnectionAddOn.setVisibility(View.GONE);
            getFavouritesMovies();
        }
    }

    @Override
    public void onItemClick(Movie item) {
        Intent intent = new Intent(this.context, MovieDetails.class);
        intent.putExtra("MOVIE", item);
        this.context.startActivity(intent);
    }

    private class GetMovies extends AsyncTask<Uri, String, Void> {

        private int page;

        GetMovies() {
        }

        GetMovies(int page) {
            this.page = page;
        }

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
                readMessagesArray(reader);
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
                if(reader.peek()== JsonToken.NULL) {
                    reader.skipValue();
                    continue;
                }

                if (name.equals("title")) {
                    movie.setTitle(reader.nextString());
                } else if (name.equals("id")) {
                    movie.setId(reader.nextInt());
                } else if (name.equals("poster_path")) {
                    String poster = reader.nextString();
                    movie.setPicture(poster);
                } else if (name.equals("vote_average")) {
                    movie.setVote(reader.nextString());
                } else if (name.equals("overview")) {
                    movie.setPlot(reader.nextString());
                } else if (name.equals("release_date")) {
                    movie.setMovieReleaseDate(reader.nextString());
                } else {
                    reader.skipValue();
                }
            }

            reader.endObject();
            return movie;
        }

        @Override
        protected Void doInBackground(Uri... uris) {

            URL url = null;

            try {
                if(this.page > 1) {
                    uris[0] = uris[0].buildUpon().appendQueryParameter("page", Integer.toString(this.page)).build();
                }
                url = new URL(uris[0].toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }


            try {
                internet(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            movieListAdapter.setMovieTitles(movieList);
        }
    }

    private void getFavouritesMovies() {
        Uri favouriteMovies = MovieProvider.BASE_CONTENT_URI.buildUpon().appendPath(MovieProvider.PATH_FAVOURITES).build();

        Cursor c = getContentResolver().query(favouriteMovies, null, null, null, DbManager.MOVIE_TITLE + " DESC");

        if (c != null && c.moveToFirst()) {
            do{
                Movie favMovie = new Movie();
                favMovie.setId(c.getInt(c.getColumnIndex(DbManager.MOVIE_ID)));
                favMovie.setTitle(c.getString(c.getColumnIndex(DbManager.MOVIE_TITLE)));
                favMovie.setPlot(c.getString(c.getColumnIndex(DbManager.MOVIE_PLOT)));
                favMovie.setVote(c.getString(c.getColumnIndex(DbManager.MOVIE_VOTE)));
                favMovie.setMovieReleaseDate(c.getString(c.getColumnIndex(DbManager.MOVIE_RELEASE_DATE)));
                favMovie.setMovieRawPicture(c.getBlob(c.getColumnIndex(DbManager.MOVIE_POSTER)));
                movieList.add(favMovie);
            } while (c.moveToNext());
        } else {
            this.tNoFavourites.setVisibility(View.VISIBLE);
        }
        movieListAdapter.setMovieTitles(movieList);
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
                showFavourites = false;
                getAllMovies();
                return false;
            }
        });

        menuTopRated = menu.findItem(R.id.orderby_toprated);
        menuTopRated.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                menuTopRated.setChecked(true);
                movieList.clear();
                showFavourites = false;
                getAllMovies();
                return false;
            }
        });

        menuFavourites = menu.findItem(R.id.show_favourites);
        menuFavourites.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                showFavourites = true;
                movieList.clear();
                getAllMovies();
                return false;
            }
        });

        if(!this.Popular) {
            menuTopRated.setChecked(true);
        }

        return true;
    }
}

package it.scareweb.popularmovies;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import it.scareweb.popularmovies.models.Movie;

public class MainActivity extends AppCompatActivity {

    private final String MOVIEDB_URL_PREFIX = "http://api.themoviedb.org/3/movie/popular?";

    private List<Movie> movieList;

    private TextView title1;
    private TextView title2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        movieList = new ArrayList<>();

        title1 = findViewById(R.id.title_tv);
        title2 = findViewById(R.id.title2_tv);

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
            setTitles();
        }
    }


}

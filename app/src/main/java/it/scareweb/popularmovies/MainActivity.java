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

public class MainActivity extends AppCompatActivity {

    private final String MOVIEDB_URL_PREFIX = "http://api.themoviedb.org/3/movie/popular?";

    private TextView title1;
    private TextView title2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        title1 = findViewById(R.id.title_tv);
        title2 = findViewById(R.id.title2_tv);

        try {
            internet();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private void setTitles(List<String> titles) {
            title1.setText(titles.get(0));
            title2.setText(titles.get(1));
    }

    private void internet() throws MalformedURLException {
        Uri builtUri = Uri.parse(MOVIEDB_URL_PREFIX)
                .buildUpon()
                .appendQueryParameter("api_key", "")
                .build();

        URL url = new URL(builtUri.toString());

        new GetMovies().execute(url);
    }

    private class GetMovies extends AsyncTask<URL, String, List<String>> {

        @Override
        protected List<String> doInBackground(URL... urls) {

            try {
                return internet(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<String> strings) {
            super.onPostExecute(strings);
            setTitles(strings);
        }

        public List<String> internet(URL url) throws IOException {


            HttpURLConnection urlConnection = (HttpURLConnection) url
                    .openConnection();

            InputStream in = urlConnection.getInputStream();
            InputStreamReader inReader = new InputStreamReader(in);


            JsonReader reader = new JsonReader(inReader);

            try {
                return readMessagesArray(reader);
            } finally {
                reader.close();
            }

            //    InputStreamReader isw = new InputStreamReader(in);
        }

        private List<String> readMessagesArray(JsonReader reader) throws IOException {
            List<String> messages = null; //= new ArrayList<String>();

            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (name.equals("results")) {
                    messages = readMovie(reader);
                } else {
                    reader.skipValue();
                }
            }
            reader.endObject();
            return messages;
        }

        public List<String> readMovie(JsonReader reader) throws IOException {
            List<String> titles = new ArrayList<>();

            reader.beginArray();
            while (reader.hasNext()) {
                 titles.add(readTitle(reader));
            }

            reader.endArray();
            return titles;
        }

        public String readTitle(JsonReader reader) throws IOException {
            String title = "";
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (name.equals("title")) {
                    title = reader.nextString();
                } else {
                    reader.skipValue();
                }
            }

            reader.endObject();
            return title;
        }
    }


}

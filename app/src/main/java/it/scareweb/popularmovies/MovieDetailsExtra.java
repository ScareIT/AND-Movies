package it.scareweb.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.JsonReader;
import android.util.JsonToken;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import it.scareweb.popularmovies.models.MovieReview;
import it.scareweb.popularmovies.models.MovieTrailer;
import it.scareweb.popularmovies.models.SettingsAPI;
import it.scareweb.popularmovies.utils.NetworkUtils;

/**
 * Created by luca on 08/03/2018.
 */

public class MovieDetailsExtra {

    private OnTaskCompleted listener;

    public interface OnTaskCompleted{
        void onTaskCompleted(List<MovieTrailer> movieTrailers);
    }

    private List<MovieTrailer> trailers = new ArrayList<>();
    private List<MovieReview> reviews = new ArrayList<>();

    private TextView TrailerListView;
    private TextView ReviewListView;

    MovieDetailsExtra() {}

    MovieDetailsExtra(Context context) {
        listener = (OnTaskCompleted) context;
    }

    public void FillTrailers(int id, TextView reviewList) {

        URL urlTrailers = null;
        URL urlReviews = null;


        try {
            Uri builtUri = Uri.parse(SettingsAPI.BASE_URL + id + SettingsAPI.TRAILERS)
                    .buildUpon()
                    .appendQueryParameter("api_key", SettingsAPI.API_KEY)
                    .build();
            urlTrailers = new URL(builtUri.toString());

            builtUri = Uri.parse(SettingsAPI.BASE_URL + id + SettingsAPI.REVIEWS)
                    .buildUpon()
                    .appendQueryParameter("api_key", SettingsAPI.API_KEY)
                    .build();
            urlReviews = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        if (!NetworkUtils.isInternetAvailable(reviewList.getContext())) {
            return;
        }

        ReviewListView = reviewList;
        new GetMovies().execute(urlTrailers, urlReviews);
    }

    private class GetMovies extends AsyncTask<URL, String, Void> {

        private void internet(URL url) throws IOException {
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
                if (name.equals("youtube")
                        // || name.equals("quicktime"))
                        // Quicktime not managed yet...
                    ){
                    readTrailers(reader);
                } else if (name.equals("results")) {
                    readReviews(reader);
                }
                else {
                    reader.skipValue();
                }
            }
            reader.endObject();
        }

        public void readTrailers(JsonReader reader) throws IOException {

            reader.beginArray();
            while (reader.hasNext()) {
                trailers.add(readSingleTrailer(reader));
            }

            reader.endArray();
        }

        public void readReviews(JsonReader reader) throws IOException {

            reader.beginArray();
            while (reader.hasNext()) {
                reviews.add(readSingleReview(reader));
            }

            reader.endArray();
        }

        public MovieTrailer readSingleTrailer(JsonReader reader) throws IOException {
            MovieTrailer trailer = new MovieTrailer();
            reader.beginObject();

            while (reader.hasNext()) {
                String name = reader.nextName();
                if(reader.peek()== JsonToken.NULL) {
                    reader.skipValue();
                    continue;
                }

                if (name.equals("name")) {
                    trailer.Name = reader.nextString();
                } else if (name.equals("id")) {
                    trailer.Size = reader.nextString();
                } else if (name.equals("type")) {
                    trailer.Type = reader.nextString();
                } else if (name.equals("source")) {
                    trailer.Source = reader.nextString();
                }
                else {
                    reader.skipValue();
                }
            }

            reader.endObject();
            return trailer;
        }

        public MovieReview readSingleReview(JsonReader reader) throws IOException {
            MovieReview review = new MovieReview();
            reader.beginObject();

            while (reader.hasNext()) {
                String name = reader.nextName();
                if(reader.peek()== JsonToken.NULL) {
                    reader.skipValue();
                    continue;
                }

                if (name.equals("author")) {
                     review.Author = reader.nextString();
                } else {
                    reader.skipValue();
                }
            }

            reader.endObject();
            return review;
        }

        @Override
        protected Void doInBackground(URL... urls) {
            try {
                internet(urls[0]);
                internet(urls[1]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if(listener!=null)
            listener.onTaskCompleted(trailers);

            for (MovieReview review :
                    reviews) {
                ReviewListView.append(review.Author + "\n");
            }
        }
    }
}

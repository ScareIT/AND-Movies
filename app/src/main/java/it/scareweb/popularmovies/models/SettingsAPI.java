package it.scareweb.popularmovies.models;

import it.scareweb.popularmovies.BuildConfig;

/**
 * Created by luca on 23/02/2018.
 */

public class SettingsAPI {
    public static final String BASE_URL = "http://api.themoviedb.org/3/movie/";

    public static final String IMAGE_URL = "http://image.tmdb.org/t/p/";

    public static final String IMAGE_SIZE_NORMAL = "w185";

    public static final String OPTION_POPULAR = "popular";

    public static final String OPTION_TOP_RATED = "top_rated";

    public static final String API_KEY = BuildConfig.API_V3_KEY;

    public static final String TRAILERS = "/trailers";

    public static final String REVIEWS = "/reviews";
}

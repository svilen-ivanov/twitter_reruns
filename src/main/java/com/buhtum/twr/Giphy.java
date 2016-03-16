package com.buhtum.twr;

import com.google.gson.Gson;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Giphy {
    public static final String DEMO_API_KEY = "dc6zaTOxFJmzC";

    private final static Logger log = LoggerFactory.getLogger(Giphy.class);
    private final String apiKey;
    private final String randomGifEndpoint = "http://api.giphy.com/v1/gifs/random";
    private final Gson gson = new Gson();
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    public Giphy(String apiKey) {
        this.apiKey = apiKey;
    }

    public String findRandomGif() {
        try {
            final HttpUrl url = HttpUrl.parse(randomGifEndpoint).newBuilder()
                    .addQueryParameter("api_key", apiKey).build();

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            final String body = response.body().string();
            log.debug("Giphy response: {}", body);

            GiphyResponse giphyResponse = gson.fromJson(body, GiphyResponse.class);

            if (giphyResponse != null && giphyResponse.data != null) {
                return giphyResponse.data.image_original_url;
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        final Giphy g = new Giphy(DEMO_API_KEY);
        System.out.println(g.findRandomGif());
    }

    private static class GiphyResponse {
        private RandomGif data;

        private static class RandomGif {
            String image_original_url;
        }
    }
}

package secondapp.bignerdranch.com.photogallery;

import android.net.Uri;
import android.util.Log;

import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by SSubra27 on 4/6/16.
 */
public class FlickrFetcher
{
    private static final String TAG = "FlickrFetcher";
    private static final String API_KEY= "8d69bc9c4f7e4b6b75f1c123c6ac429c";
    private static final String FETCH_RECENT_METHOD="flickr.photos.getRecent";
    private static final String SEARCH_METHOD="flickr.photos.search";
    private static final Uri ENDPOINT = Uri.parse("https://api.flickr.com/services/rest/").buildUpon().appendQueryParameter("api_key", API_KEY)
            .appendQueryParameter("format", "json").appendQueryParameter("nojsoncallback","1").appendQueryParameter("extras", "url_s").build();

    public byte[] getUrlBytes(String urlSpec) throws IOException
{
        URL url = new URL(urlSpec);
        HttpURLConnection connection =(HttpURLConnection) url.openConnection();

    try{
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream in = connection.getInputStream();
        if(connection.getResponseCode() != HttpURLConnection.HTTP_OK)
        {
            throw new IOException((connection.getResponseMessage()+ ":with "+ urlSpec));
        }
        int bytesRead = 0;
        byte[] buffer = new byte[1024];
        while((bytesRead = in.read(buffer))>0)
        {
            out.write(buffer,0,bytesRead);
        }
        out.close();
        return out.toByteArray();
    } finally {
        connection.disconnect();
    }
}
    public String getUrlString(String urlSpec) throws IOException
    {
        return new String(getUrlBytes(urlSpec));
    }
    private List<GalleryItem> downloadGalleryItems(String url)

    {
        List<GalleryItem> items = new ArrayList<>();

        try {

            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + jsonString);
            parseItems(jsonString, items);
        }catch (JSONException je)
        {
            Log.e(TAG, "Failed to parse JSON", je);
        } catch (IOException ioe)
        {
            Log.e(TAG, "Failed to fetch items", ioe);
        }
        return items;
    }
    private String buildUrl(String method, String query) {

        Uri.Builder uriBuilder = ENDPOINT.buildUpon().appendQueryParameter("method", method);
        if(method.equals(SEARCH_METHOD))
        {
            uriBuilder.appendQueryParameter("text",query);
        }
        return uriBuilder.build().toString();
    }

    public List<GalleryItem> fetchRecentPhotos() {
        String url = buildUrl(FETCH_RECENT_METHOD, null);
        return downloadGalleryItems(url);
    }
    public List<GalleryItem> searchPhotos(String query) {

        String url = buildUrl(SEARCH_METHOD, query);
        return downloadGalleryItems(url);
    }
}

package secondapp.bignerdranch.com.photogallery;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.List;

/**
 * Created by SSubra27 on 5/5/16.
 */
public class PollServiceHelper {
    public static final String TAG = PollServiceHelper.class.getSimpleName();
    public static final String ACTION_SHOW_NOTIFICATION = "SHOW_NOTIFICATION";
    public static final String REQUEST_CODE = "request_code";
    public static final String NOTIFICATION = "notification";
    public static final String PERM_PRIVATE= "com.bignerdranch.android.photogallery.PRIVATE";

    public static List<GalleryItem> fetchNewItems(Context context)
    {
        String query = QueryPreferences.getStoredQuery(context);

        List<GalleryItem> galleryItems;

        if(query == null)
        {
            galleryItems = new FlickrFetcher().fetchRecentPhotos();
        } else
        {
            galleryItems = new FlickrFetcher().searchPhotos(query);
        }

        return galleryItems;
    }

    public static void sendNotificationIfNewDataFetched(Context context, String resultId)
    {
        String lastRequestId = QueryPreferences.getLastResultId(context);
        if(resultId.equals(lastRequestId
        ))
        {
            Log.i(TAG,"Got an old result "+ resultId);
        } else
        {
            Log.i(TAG,"Got a new result "+ resultId);
            Resources resources = context.getResources();

            PendingIntent pi = PendingIntent.getActivity(context,0, PhotoGalleryActivity.newIntent(context),0);

            Notification notification = new NotificationCompat.Builder(context)
                                            .setTicker(resources.getString(R.string.new_picture_title))
                                            .setSmallIcon(android.R.drawable.ic_menu_upload_you_tube)
                                            .setContentTitle(resources.getString(R.string.new_picture_title))
                                            .setContentText(resources.getString(R.string.new_pictures_text))
                                            .setContentIntent(pi)
                                            .setAutoCancel(true)
                                            .build();
            Intent i = new Intent(ACTION_SHOW_NOTIFICATION);
            i.putExtra(REQUEST_CODE,0);
            i.putExtra(NOTIFICATION,notification);

            context.sendOrderedBroadcast(i, PERM_PRIVATE,null,null, Activity.RESULT_OK, null,null);
        }
        QueryPreferences.setLastResultId(context,resultId);
    }
}

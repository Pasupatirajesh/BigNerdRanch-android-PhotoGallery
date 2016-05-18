package secondapp.bignerdranch.com.photogallery;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.List;

/**
 * Created by SSubra27 on 4/28/16.
 */
public class PollService extends IntentService {
    private static final String TAG= "PollService";
//    private static final long POLL_INTERVAL = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
    private static final long POLL_INTERVAL = 1000*6;// 60 Seconds
    public static final String ACTION_SHOW_NOTIFICATION = "com.bignerdranch.android.photogallery.SHOW_NOTIFICATION";
    public static final String PERM_PRIVATE = "com.bignerdranch.android.photogallery.PRIVATE";
    public static final String REQUEST_CODE = "REQUEST_CODE";
    public static final String NOTIFICATION = "NOTIFICATION";

    public static Intent newIntent(Context context)
    {
        return new Intent(context, PollService.class);
    }
    public static void setServiceAlarm(Context context, boolean isOn)
    {
        Intent i  = PollService.newIntent(context);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, i, 0);
        AlarmManager alarmManager =(AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if(isOn)
        {
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), POLL_INTERVAL, pendingIntent);
        } else
        {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
            QueryPreferences.setAlarmOn(context,isOn);
        }
    }
    public static boolean isServiceAlarmOn(Context context)
    {
        Intent i = PollService.newIntent(context);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_NO_CREATE);
        return pendingIntent != null;
    }
    public PollService()
    {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        if(!isNetworkAvailableAndConnected())
        {
            return;
        }
        Log.i(TAG, "Received an Intent: " + intent);
        String query = QueryPreferences.getStoredQuery(this);
        String lastResultId = QueryPreferences.getLastResultId(this);
        List<GalleryItem> items;
        if(query==null)
        {
            items = new FlickrFetcher().fetchRecentPhotos();
        } else
        {
            items = new FlickrFetcher().searchPhotos(query);
        }
        if(items.size()==0)
        {
            return;
        }
        String resultId = items.get(0).getId();
        if(resultId.equals(lastResultId))
        {
            Log.i(TAG, "Got an old Result: "+ resultId);
        } else
        {
            Log.i(TAG, "Got a new Result: "+ resultId);
            Resources mResources = getResources();
            Intent i = PhotoGalleryActivity.newIntent(this);
            PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
            Notification notification = new NotificationCompat.Builder(this).setTicker(mResources.getString(R.string.new_picture_title))
                    .setSmallIcon(android.R.drawable.ic_menu_report_image).setContentTitle(mResources.getString(R.string.new_picture_title))
                    .setContentText(mResources.getString(R.string.new_pictures_text)).
                            setContentIntent(pi).setAutoCancel(true).build();
//            NotificationManagerCompat mNotificationManagerCompat = NotificationManagerCompat.from(this);
//            mNotificationManagerCompat.notify(0, notification);
//            sendBroadcast(new Intent(ACTION_SHOW_NOTIFICATION), PERM_PRIVATE);
            showBackgroundNotification(0, notification);
        }
        QueryPreferences.setLastResultId(this, resultId);
    }

    public void showBackgroundNotification(int requestCode, Notification notification)
    {
        Intent i = new Intent(ACTION_SHOW_NOTIFICATION);
        i.putExtra(REQUEST_CODE, requestCode);
        i.putExtra(NOTIFICATION, notification);
        sendOrderedBroadcast(i,PERM_PRIVATE,null,null, Activity.RESULT_OK,null,null);

    }
    private boolean isNetworkAvailableAndConnected()
    {
        ConnectivityManager cm =(ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null;
        boolean isNetworkConnected = isNetworkAvailable && cm.getActiveNetworkInfo().isConnected();
        return isNetworkConnected;
    }

}

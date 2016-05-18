package secondapp.bignerdranch.com.photogallery;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

/**
 * Created by SSubra27 on 5/8/16.
 */
public abstract class VisibleFragment extends android.support.v4.app.Fragment {
    private static final String TAG = "VisibleFragment";

    @Override
    public void onStart()
    {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(PollService.ACTION_SHOW_NOTIFICATION);
        getActivity().registerReceiver(mOnShowNotification, intentFilter, PollService.PERM_PRIVATE, null);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        getActivity().unregisterReceiver(mOnShowNotification);
    }

    private BroadcastReceiver mOnShowNotification = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            Toast.makeText(getActivity(), "Got a broadcast: "+ intent.getAction(), Toast.LENGTH_SHORT).show();
            // If we receive this ,we're visible, so cancel the notification
            Log.i(TAG,"canceling notification");
            setResultCode(Activity.RESULT_CANCELED);
        }
    };
}

package secondapp.bignerdranch.com.photogallery;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by SSubra27 on 4/26/16.
 */
public class QueryPreferences {
    private static final String PREF_SEARCH_QUERY = "searchQuery"; // key that is used for QueryPreference
    private static final String PREF_LAST_RESULT = "lastResultId";
    private static final String PREF_IS_ALARM_ON = "isAlarmOn";

    public static String getStoredQuery(Context context)
    {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_SEARCH_QUERY, null);

    }
    public static void setStoredQuery(Context context, String query)
    {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(PREF_SEARCH_QUERY, query).apply();
    }
    public static String getLastResultId(Context context)
    {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_LAST_RESULT,null);
    }
    public static void setLastResultId(Context context, String lastResultId)
    {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(PREF_LAST_RESULT,lastResultId).apply();
    }
    public static boolean isAlarmOn(Context context)
    {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_IS_ALARM_ON, false);
    }
    public static void setAlarmOn(Context context, boolean isOn)
    {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(PREF_IS_ALARM_ON, isOn).apply();
    }
}

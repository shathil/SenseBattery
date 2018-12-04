package ektara.com.sensebattery;

import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;

/**
 * Created by mohoque on 01/01/2017.
 */

public class NetworkMstatistics {

    public NetworkMstatistics(Context context){

        if (Build.VERSION.SDK_INT >= 23) {
            NetworkStatsManager service = context.getSystemService(NetworkStatsManager.class);
           // NetworkStats.Bucket bucket = service.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE, null, from, to);
        }

    }


}

package ektara.com.sensebattery;

import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.HashMap;
import java.util.List;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.os.Build;
import android.util.Log;
import android.util.SparseArray;

import static ektara.com.sensebattery.BatteryConstants.APP_NAME;

/**
 * Created by mohoque on 31/12/2016.
 */

public class TrafficStatistics {

    private long tx = 0;
    private long rx = 0;

    public TrafficStatistics(int uid){
        sampleTrafficStat(uid);
    }




    private void sampleTrafficStat(int uid) {

        tx = TrafficStats.getUidTxBytes(uid);
        rx = TrafficStats.getUidRxBytes(uid);
        if (tx == TrafficStats.UNSUPPORTED || rx == TrafficStats.UNSUPPORTED)
            Log.d(APP_NAME, "Unsupported");
        else
            Log.d(APP_NAME, uid+" "+tx+"Bytes "+rx+"Bytes");
        /*
        if(isMobile == true) {
            mobil_tx = mobil_tx + delta_tx;
            mobil_rx = mobil_rx + delta_rx;
        } else {
            wifi_tx = wifi_tx + delta_tx;
            wifi_rx = wifi_rx + delta_rx;
        }*/
    }
    public long getRXBytes(){
        return this.rx;

    }

    public long getTXBytes(){
        return this.tx;
    }

}

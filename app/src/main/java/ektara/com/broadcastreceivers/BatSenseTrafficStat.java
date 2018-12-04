package ektara.com.broadcastreceivers;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import ektara.com.sensebattery.R;

import static ektara.com.sensebattery.BatteryConstants.APP_NAME;

/**
 * Created by mohoque on 02/01/2017.
 */

public class BatSenseTrafficStat {
    static final String TAG = "BatSenseTrafficStat";
    private static String FILE_PATH = "/proc/net/xt_qtaguid/stats";
    static final String PERMISSION_DENIED = "su rights required to access alarms are not available / were not granted";

    private static final String KEY_IDX = "idx";
    private static final String KEY_IFACE = "iface";
    private static final String KEY_UID = "uid_tag_int";
    private static final String KEY_COUNTER_SET = "cnt_set";
    private static final String KEY_TAG_HEX = "acct_tag_hex";
    private static final String KEY_RX_BYTES = "rx_bytes";
    private static final String KEY_RX_PACKETS = "rx_packets";
    private static final String KEY_TX_BYTES = "tx_bytes";
    private static final String KEY_TX_PACKETS = "tx_packets";
    private static final String appPath1 = "/proc//proc/uid_stat/10118/tcp_rcv";

    private static final String appPath2 ="/sdcard/CaratSamples/";
    public BatSenseTrafficStat(int uid){


     //   appPath1 = "/proc//proc/uid_stat/10118/"+uid+"/tcp_snd";
    }

    public static Long fileLastModified()
    {
        File fr = null;
        Long lastMod = 0L;
        try
        {
            fr = new File(FILE_PATH);
            if (fr.exists()){
                Log.d("Traddic Stat",""+fr.length());
                lastMod = fr.lastModified();
            }


        }
        catch (Exception e)
        {
            Log.d("Traddic Stat","File does not exiist");
        }
        return lastMod;
    }

    public static void readLines() throws java.io.IOException{

        File file = new File(FILE_PATH);
        try {
            Process su = Runtime.getRuntime().exec("cat " + FILE_PATH);
            BufferedReader buffer = new BufferedReader(new InputStreamReader(su.getInputStream()));
            String s = null;
            try {
                while ((s = buffer.readLine()) != null) {
                    Log.d("ReadLine",s);
                }
                buffer.close();
            } catch (Exception e) {
                // Ignore read errors; they mean the process is done.
                Log.d(APP_NAME,e.toString());
            }
        }catch (Exception e)
        {
            Log.d(APP_NAME,e.toString());
        }
    }
}

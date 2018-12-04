package ektara.com.sensebattery;

import android.content.Context;
import android.os.BatteryManager;
import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * Created by mohoque on 12/12/2016.
 */

public class BatteryInformation {

    public static final String TAG="BatterySense";
    static final String currentNowLoc = "/sys/class/power_supply/";

    private double capacity = 0;
    private long current = 0L;
    private long charge = 0;
    private long averageCurrent = 0L;
    private Double voltage = 0D;
    private Context context;
    private BatteryManager mBatteryManager;
    private String oldCurrentPath = "";
    public BatteryInformation(Context context){

        this.context = context;
        this. mBatteryManager = (BatteryManager) this.context.getSystemService(Context.BATTERY_SERVICE);

    }

    public double batteryCapacity() {
        Object mPowerProfile_ = null;

        final String POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile";

        try {
            mPowerProfile_ = Class.forName(POWER_PROFILE_CLASS)
                    .getConstructor(Context.class).newInstance(context);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
             this.capacity = (double) Class.forName(POWER_PROFILE_CLASS).getMethod("getAveragePower", java.lang.String.class).invoke(mPowerProfile_, "battery.capacity");
            //Toast.makeText(MainActivity.this, batteryCapacity + " mah",
            //        Toast.LENGTH_LONG).show();


        } catch (Exception e) {
            e.printStackTrace();
        }

        return this.capacity;
    }


    public Long instantCurrent(){
        //Log.d("BatteryInformation", "Version "+Build.VERSION.SDK_INT+" "+Build.VERSION_CODES.LOLLIPOP);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.current = mBatteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)/1000;
        }else{

            if(this.oldCurrentPath.length()==0)
                this.oldCurrentPath = this.currentSystemPath();
            this.current = this.chargingCurrent(this.oldCurrentPath );

            // We can read from the files

        }
        //Log.d("BAtterySense", "Current "+this.current);
        return this.current;

    }

    public Long batteryCharge(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.charge = mBatteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)/1000;
            return this.charge;
        }else{}

        return this.charge;

    }


    public Long avgCurrent(){
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            this.averageCurrent = mBatteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE)/1000;
        }

        return this.averageCurrent;
    }
    /* This is for the devices lower than Lollipop */

    private String currentSystemPath(){

        boolean fileFound = false;
        String targetFilePath ="";
        File first = new File(currentNowLoc);
        File[] dirs = first.listFiles();
        for (File inDir : dirs) {
            if (inDir.isDirectory()) {
                File second = new File(inDir.getPath());
                File[] files = second.listFiles();
                for (File inFile : files) {
                    if (inFile.isFile() && (inFile.getName().toString().toLowerCase().contains("current"))){

                        fileFound = true;
                        targetFilePath = inFile.getAbsolutePath();

                        break;
                    }

                }// Inner For Loop

            }
            if(fileFound)
                break;
        }
        return targetFilePath;
    }


    public  long chargingCurrent(String currentFile) {

        File first = new File(currentFile);
        if (!first.exists())
            return 0;

        String text = null;
        boolean conversion = false;

        try {
            FileInputStream fs = new FileInputStream(first);
            InputStreamReader sr = new InputStreamReader(fs);
            BufferedReader br = new BufferedReader(sr);

            text = br.readLine();

            if (text.length()>4)
                conversion = true;

            br.close();
            sr.close();
            fs.close();
        } catch (Exception ex) {
            Log.e("BatterySense", ex.getMessage());
            ex.printStackTrace();
        }

        Long value = null;

        if (text != null) {
            try	{
                value = Long.parseLong(text);
            } catch (NumberFormatException nfe) 	{
                Log.e(TAG, nfe.getMessage());
                value = null;
            }

            if (conversion && value != null) {
                value = value / 1000;
            }


        }

        return value;
    }


}

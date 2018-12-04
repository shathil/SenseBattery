package ektara.com.sensebattery;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraManager;
import android.location.GnssMeasurementsEvent;
import android.location.GnssNavigationMessage;
import android.location.GnssStatus;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;


import ektara.com.broadcastreceivers.GnssFuckListner;

import static ektara.com.sensebattery.BatteryConstants.APP_NAME;
import static ektara.com.sensebattery.BatteryConstants.MOB_GPS_STATUS_ON;
import static ektara.com.sensebattery.BatteryConstants.MOB_SIGNAL_STRENGTH;
import static ektara.com.sensebattery.BatteryConstants.MSG_CAMERA0_STATUS;
import static ektara.com.sensebattery.BatteryConstants.MSG_CAMERA1_STATUS;
import static ektara.com.sensebattery.BatteryConstants.WIFI_SIGNAL_STRENGTH;
import static ektara.com.sensebattery.BatteryInformation.currentNowLoc;

/**
 * Created by mohoque on 06/01/2017.
 */

public class DeviceStatus{

    private float wifiSignal = 0;
    private int mobileSignal = 0;
    private boolean cameZstatus = true;
    private boolean cameOstatus = true;

    private BatteryManager mBatteryManager;
    private ConnectivityManager connectivityManager;
    private LocationManager mLocationManager;
    private AudioManager audioManager;
    private CameraManager cameraManager;
    private PowerManager powerManager;
    private Context context;
    private NetworkInfo networkInfo;
    private String oldCurrentPath="";


    public DeviceStatus(Context context){
        this.mBatteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.networkInfo = connectivityManager.getActiveNetworkInfo();
        this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        this.mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean statusOfGPS = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);


        this.cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        this.context = context;

        if(Build.VERSION.SDK_INT< Build.VERSION_CODES.LOLLIPOP){
            this.oldCurrentPath = this.currentSystemPath();
        }

        registerCameraService();
        //registerGlobalPosService(statusOfGPS);
        //registerGlobalPosService(statusOfGPS);
        //GnssFuckListenerA gnssA = new GnssFuckListenerA();



    }

    private void registerCameraService(){


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cameraManager.registerAvailabilityCallback(new CameraManager.AvailabilityCallback() {

                //private Bundle bundle = new Bundle();// cameraZeroAvailable = true;

                @Override
                public void onCameraAvailable(String cameraId) {
                    super.onCameraAvailable(cameraId);
                    if(Integer.parseInt(cameraId) == 0)
                        MessageQueue.getQueueInstance().push(MSG_CAMERA0_STATUS,true);
                    else
                        MessageQueue.getQueueInstance().push(MSG_CAMERA1_STATUS,true);
                }

                @Override
                public void onCameraUnavailable(String cameraId) {

                    super.onCameraUnavailable(cameraId);
                    if(Integer.parseInt(cameraId) == 0)
                        MessageQueue.getQueueInstance().push(MSG_CAMERA0_STATUS,false);
                    else
                        MessageQueue.getQueueInstance().push(MSG_CAMERA1_STATUS,false);

                }
            }, null);
        }else{

        }

    }

    /*
    @RequiresApi(Build.VERSION_CODES.N)
    public final GnssStatus.Callback gnssStatusListener =
        new GnssStatus.Callback() {
            @Override
            public void onStarted() {Log.d(APP_NAME,"GPS started");}

            @Override
            public void onStopped() {Log.d(APP_NAME,"GPS stopped");}

            @Override
            public void onSatelliteStatusChanged(GnssStatus status) {
                Log.d(APP_NAME,"GPS started"+status.toString());
            }
        };
      */




    public void registerGlobalPosService(boolean statusOfGPS) {
        final boolean gpsStatus =  statusOfGPS;
        if(ContextCompat.checkSelfPermission( context, android.Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED){
               // && ContextCompat.checkSelfPermission( context, android.Manifest.permission.ACCESS_COARSE_LOCATION ) == PackageManager.PERMISSION_GRANTED){


            Log.d(APP_NAME, "Build Version "+Build.VERSION.SDK_INT);

            if(Build.VERSION.SDK_INT>= 24)
            {
                Log.d(APP_NAME, "Permnission Granted "+gpsStatus);
                //GnssFuckListner gnssFuckListner = new GnssFuckListner();

               // mLocationManager.registerGnssStatusCallback(gnssStatusListener);
                //mLocationManager.registerGnssMeasurementsCallback(gnssMeasurementsEventListener);
                //mLocationManager.registerGnssNavigationMessageCallback(gnssNavigationMessageListener);

            }
            else
            {
                Log.d(APP_NAME, "Permnission Granted "+gpsStatus);
                mLocationManager.addGpsStatusListener(new GpsStatus.Listener() {
                    @Override
                    public void onGpsStatusChanged(int event) {

                        //if(ContextCompat.checkSelfPermission( context, android.Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED
                         //       && ContextCompat.checkSelfPermission( context, android.Manifest.permission.ACCESS_COARSE_LOCATION ) == PackageManager.PERMISSION_GRANTED) {
                        //mLocationManager.getGpsStatus(mLegacyStatus);

                        switch (event) {
                            case GpsStatus.GPS_EVENT_STARTED:
                                MessageQueue.getQueueInstance().push(MOB_GPS_STATUS_ON,true);
                                Log.d(APP_NAME, "GPS started "+gpsStatus);
                                break;
                            case GpsStatus.GPS_EVENT_STOPPED:
                                MessageQueue.getQueueInstance().push(MOB_GPS_STATUS_ON,false);
                                Log.d(APP_NAME, "GPS stopped "+gpsStatus);
                                break;
                        }
                    }
                });
            }
        }
    }




    public int getBrightness ()throws Settings.SettingNotFoundException{

        int mode = Settings.System.getInt(this.context.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE);
        if(mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC)
            return -1;
        else
            return Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
    }

    public boolean getAudioStatus(){
        boolean audioFlag = false;

        boolean isMusic = audioManager.isMusicActive();
        int audioMode = audioManager.getMode();
        if(isMusic || audioMode == AudioManager.MODE_IN_CALL|| audioMode == AudioManager.MODE_IN_COMMUNICATION){
            //speakerOn = audioManager.isSpeakerphoneOn();
            audioFlag = true;
        }
        return audioFlag;
    }

    public boolean getbackcameraStatus(){
        boolean camZstatus = false;
        Boolean camZ = MessageQueue.getQueueInstance().pop(MSG_CAMERA0_STATUS);
        if (camZ != null){
            this.cameZstatus = camZ;
        }else
            camZstatus = this.cameZstatus;

        return camZstatus;

    }

    public boolean getfrontcameraStatus(){
        boolean camOstatus = false;
        Boolean camZ = MessageQueue.getQueueInstance().pop(MSG_CAMERA1_STATUS);
        if (camZ != null){
            this.cameOstatus = camZ;
        }else
            camOstatus = this.cameOstatus;

        return camOstatus;
    }
    public boolean wifiStatus(){

        boolean wifiEn = (networkInfo != null) && (networkInfo.isConnected()) && (networkInfo.getType() == ConnectivityManager.TYPE_WIFI);
        return wifiEn;
    }

    public int getWifiSignal(){
        int wifiStrength = 0;
        String wisignal = MessageQueue.getQueueInstance().pop(WIFI_SIGNAL_STRENGTH);

        if(wifiStatus())
        {
            if(wisignal!=null){
                this.wifiSignal = Float.parseFloat(wisignal);

            }
            wifiStrength = (int) this.wifiSignal;
            Log.d(APP_NAME, "RSSI "+wifiStrength);
        }
        return wifiStrength;
    }


    public boolean mobileStatus(){
        boolean mobileEnalbed = (networkInfo != null && networkInfo.isConnected() && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE);
        return mobileEnalbed;
    }

    public int getMobileSignal(){
        int mobileStrength = mobileSignal;
        Integer mobilestrength = MessageQueue.getQueueInstance().pop(MOB_SIGNAL_STRENGTH);
        if(mobileStatus()){
            if(mobilestrength!=null){
                this.mobileSignal = mobilestrength;
            }
            mobileStrength = this.mobileSignal;


        }
        return mobileStrength;
    }

    public boolean isScreenON() {

        boolean isScreenOn;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {

            isScreenOn = powerManager.isScreenOn();
        }else
            isScreenOn = powerManager.isInteractive();


        return isScreenOn;

    }

    public int batteryCapacity() {
        Object mPowerProfile_ = null;

        double capacity = 0;
        final String POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile";

        try {
            mPowerProfile_ = Class.forName(POWER_PROFILE_CLASS)
                    .getConstructor(Context.class).newInstance(context);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            capacity = (double) Class.forName(POWER_PROFILE_CLASS).getMethod("getAveragePower", java.lang.String.class).invoke(mPowerProfile_, "battery.capacity");
            //Toast.makeText(MainActivity.this, batteryCapacity + " mah",
            //        Toast.LENGTH_LONG).show();


        } catch (Exception e) {
            e.printStackTrace();
        }

        return (int)capacity;
    }

    public int instantCurrent(){
        //Log.d("BatteryInformation", "Version "+Build.VERSION.SDK_INT+" "+Build.VERSION_CODES.LOLLIPOP);

        int current = 0;
        String oldCurrentPath = "";
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            current = (int)mBatteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)/1000;
        }else{
            current = this.chargingCurrent(this.oldCurrentPath);
        }
        return current;

    }

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

    private int chargingCurrent(String currentFile) {

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

        Integer value = null;

        if (text != null) {
            try	{
                value = Integer.parseInt(text);
            } catch (NumberFormatException nfe) 	{
                Log.e(APP_NAME, nfe.getMessage());
                value = null;
            }

            if (conversion && value != null) {
                value = value / 1000;
            }


        }

        return value;
    }


    public long getBatteryCharge(){

        int charge = 0;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            charge = (int) mBatteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)/1000;
        }else{}

        return charge;

    }


    public int avgCurrent(){

        int current = 0;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            current = (int) mBatteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE)/1000;
        }

        return current;
    }
    /* This is for the devices lower than Lollipop */


}

package ektara.com.sensebattery;


import android.Manifest;
import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.GnssStatus;
import android.location.GpsStatus;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.TrafficStats;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.SparseArray;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static android.location.GpsStatus.GPS_EVENT_STARTED;
import static android.location.GpsStatus.GPS_EVENT_STOPPED;
import static ektara.com.sensebattery.BatteryConstants.*;





/**
 * Created by mohoque on 12/12/2016.
 */

public class BatteryService extends Service{

    private long [] socAverageCurrent = new long[101];
    private double [] socAverageCarate = new double[101];
    public static boolean BAT_CHARGING_STATUS = false;
    private boolean MUTEX_FLAG_BATTERY = true;
    private int batteryLevel = 0;
    static final double MAX_VOLTAGE = 4300; //mV
    private int beginSOC = 0;
    private int socccPhase = 0;
    private long batCapacity = 0;
    private boolean chargeStatus = false;
    private boolean powerStatus = false;
    private long [] socYupdateTime = new long [101];
    private int [] socBatteryVoltage = new int [101];
    private int [] socBatteryCelsius = new int [101];




    private int energyPerveLevel = 0;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(APP_NAME, "onStartCommand");
        //sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        //this. systemupTime = SystemClock.uptimeMillis();
        //socInterrupted.set(0,100,false);

        if(!BAT_CHARGING_STATUS) {


            //SamplingHandler sample = new SamplingHandler(Build.VERSION.SDK_INT);
            //sample.MobileAppUsageHandler(this);
            AppUsageSampling appUsageSampling = new AppUsageSampling(Build.VERSION.SDK_INT);
            appUsageSampling.MobileAppUsageHandler(this);
            BAT_CHARGING_STATUS = true;





        }
        return super.onStartCommand(intent, flags, startId);
    }

    public static String createDirIfNotExists(String senseBatery) {
        File file = new File(senseBatery);
        String ret = "";
        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            // Something else is wrong. It may be one of many other states, but all we need
            //  to know is we can neither read nor write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }
        //File file = new File(Environment.getExternalStorageDirectory(), senseBatery);

        if ((mExternalStorageAvailable)&&(mExternalStorageWriteable)) {

            if (!file.exists()) {
                if (file.mkdirs())
                    Log.d("createDirIfNotExists", "Directory Created");
                else
                    Log.d("createDirIfNotExists", "Directory Could not be created");
            }

            File f = new File(Environment.getExternalStorageDirectory(), senseBatery);
            if (f.exists()) {
                Log.d("createDirIfNotExists", "Directory already exists");
            }
        }
        return ret = file.getAbsolutePath();
    }




    private void startAppStartReading() {
    }

    private void holdbatteryUpdateMutex(){

        while (!MUTEX_FLAG_BATTERY)
            SystemClock.sleep(2);
        MUTEX_FLAG_BATTERY = false;


    }


    //private void releasebatteryUpdateMutex(){MUTEX_FLAG_BATTERY = true;}
    //private boolean getChargeStatus(){return this.chargeStatus;}
    //private boolean getPowerStatus() {return this.powerStatus;}
    //private void setChargingRate (long avgcur){this.socAverageCurrent[this.batteryLevel] = avgcur;}//double crateNew = avgcur/this.batCapacity;


    private int getBatteryLevel(){return  this.batteryLevel;}



    private void resetVariables(boolean status){

        batteryLevel = 0;
        beginSOC = 0;
        socccPhase = 0;
        chargeStatus = status;
        powerStatus = status;
        for (int i = 0; i<101; i++){

            socYupdateTime[i] = 0;
            socBatteryVoltage[i] = 0;
            socBatteryCelsius[i] = 0;
            socAverageCurrent[i] = 0;
            socAverageCarate [i] = 0;

        }


    }



    private final BroadcastReceiver powerConnectReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d(APP_NAME, intent.getAction().toString());
            if  ("android.intent.action.ACTION_POWER_CONNECTED".equals(intent.getAction())){

                MessageQueue.getQueueInstance().push(MSG_POWER_STATUS,true);
                resetVariables(true);
                sendBroadcast(STATUS_NOW, (long)1);

            }
        }


    };

    private final BroadcastReceiver powerDisconnectReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d(APP_NAME, intent.getAction().toString());
            if  ("android.intent.action.ACTION_POWER_DISCONNECTED".equals(intent.getAction())){
                MessageQueue.getQueueInstance().push(MSG_POWER_STATUS, false);
                resetVariables(false);
                sendBroadcast(STATUS_NOW, (long)0);

            }

        }


    };


    /* Whenever the display is On, we check which applicaiton is at the top activity. If the application is not in the list, we add it
    * at that moment.*/

    private class InstalledApplication extends AsyncTask<String, Void, String> {

        final PackageManager battPackageManager = getApplicationContext().getPackageManager();
        final ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        HashMap<String,Integer> idPackages= new HashMap<String,Integer>();
        HashMap<Integer, String> idNames= new HashMap<Integer, String>();


        @Override
        protected String doInBackground(String... params) {


            /*
            List<PackageInfo> packageInfoList = battPackageManager.getInstalledPackages(0);
            Log.d(APP_NAME, " "+packageInfoList.size());
            for(int i = 0;i<packageInfoList.size();++i)
            {
                PackageInfo pkg = packageInfoList.get(i);
                String appnamr = pkg.applicationInfo.loadLabel(battPackageManager).toString();
                int uid = packageInfoList.get(i).applicationInfo.uid;
                Long rxBytes = TrafficStats.getUidRxBytes(uid);
                long txBytes = TrafficStats.getUidTxBytes(uid);
                if((rxBytes>0)|| (txBytes>0)){
                    idPackages.put(pkg.packageName,uid);
                    idNames.put(uid, appnamr);
                }
            }*/
            /*
            if(Build.VERSION.SDK_INT>=21){
                List<AndroidAppProcess> processes = ProcessManager.getRunningForegroundApps(getApplicationContext());
                Collections.sort(processes, new ProcessManager.ProcessComparator());
                for (int i = 0; i <=processes.size()-1 ; i++) {
                    if(processes.get(i).name.equalsIgnoreCase("com.google.android.gms")) { //always the package name above/below this package is the top app
                        if ((i+1)<=processes.size()-1) { //If processes.get(i+1) available, then that app is the top app
                            top = processes.get(i + 1).name;
                        } else if (i!=0) { //If the last package name is "com.google.android.gms" then the package name above this is the top app
                            top = processes.get(i - 1).name;
                        } else{
                            if (i == processes.size()-1) { //If only one package name available
                                top = processes.get(i).name;
                            }
                        }
                        Log.v(TAG, "top app = " + top);
                    }
                }
            }*/


                List<ApplicationInfo> installedApps = battPackageManager.getInstalledApplications(0);

                for (ApplicationInfo ai : installedApps) {

                    if ((ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {

                        //Log.d(APP_NAME,"System "+ai.packageName);


                    } else {
                        // User installed app?
                        Log.d(APP_NAME, "User Installed " + ai.packageName);
                        idPackages.put(ai.packageName, ai.uid);
                        String apnamr = ai.loadLabel(battPackageManager).toString();
                        idNames.put(ai.uid, apnamr);

                    }
                }

            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            if(result.equals("Executed")) {
                MessageQueue.getQueueInstance().push(INSTALLED_UID_APPNAMES, idNames);
                MessageQueue.getQueueInstance().push(INSTALLED_UID_PACKAGES, idPackages);
                Log.d(APP_NAME, "Aynsnc post executed");
            }


        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }



    private final BroadcastReceiver batteryChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.BATTERY_CHANGED".equals(intent.getAction())) {
                //Log.d(APP_NAME, "batteryChangeReceiver "+intent.getAction().toString());
                checkBatteryInformation(intent);
            }
            //Log.d(APP_NAME, intent.getAction().toString());
        }
    };


    private final BroadcastReceiver appaddedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.PACKAGE_ADDED".equals(intent.getAction())) {
                //Log.d(APP_NAME, "appaddedReceiver"+intent.getAction().toString());
                new InstalledApplication().execute("");
            }
            Log.d(APP_NAME, intent.getAction().toString());
        }



    };

    private final BroadcastReceiver appremovedReceiver = new BroadcastReceiver() {


        @Override
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.PACKAGE_REMOVED".equals(intent.getAction())) {
                //Log.d(APP_NAME, "appremovedReceiver"+intent.getAction().toString());
                new InstalledApplication().execute("");

                //checkBatteryInformation(intent);
            }
        }
    };

    private final BroadcastReceiver wifiSignalReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub

            Log.d("Rssi", "RSSI changed");
            float level = intent.getIntExtra(WifiManager.EXTRA_NEW_RSSI, -1);
            MessageQueue.getQueueInstance().push(WIFI_SIGNAL_STRENGTH,""+level);
        }
    };



    public void onCreate() {
        super.onCreate();
        new InstalledApplication().execute("");
        this.registerReceiver(this.batteryChangeReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        this.registerReceiver(this.powerConnectReceiver, new IntentFilter(Intent.ACTION_POWER_CONNECTED));
        this.registerReceiver(this.powerDisconnectReceiver, new IntentFilter(Intent.ACTION_POWER_DISCONNECTED));
        this.registerReceiver(this.appaddedReceiver,new IntentFilter(Intent.ACTION_PACKAGE_ADDED));
        this.registerReceiver(this.appremovedReceiver,new IntentFilter(Intent.ACTION_PACKAGE_REMOVED));
        IntentFilter wifilter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        wifilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        this.registerReceiver(this.wifiSignalReceiver,wifilter);



        //this.sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        new InstalledApplication().execute("");
        this.unregisterReceiver(this.batteryChangeReceiver);
        this.unregisterReceiver(this.powerConnectReceiver);
        this.unregisterReceiver(this.powerDisconnectReceiver);
        this.unregisterReceiver(this.appaddedReceiver);
        this.unregisterReceiver(this.appremovedReceiver);
        this.unregisterReceiver(this.wifiSignalReceiver);
    }



    @Override
    public IBinder onBind(Intent intent) {
        // There are Bound an Unbound Services - you should read something about
        // that. This one is an Unbounded Service.
        return null;


    }

    /* Determining the charging status*/




    private int getConsCurentPhase(){return this.socccPhase;}

    private void checkBatteryInformation(Intent batteryStatus) {

        int locVoltage = batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
        int locTemp = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
        long yupdateTime = System.currentTimeMillis()/1000;

        int currLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int maxLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        //BatteryManager mBatteryManager = (BatteryManager) this.getSystemService(Context.BATTERY_SERVICE);
        double percentage =  Math.round((currLevel * 100.0) / maxLevel);

         /*If the battery is full */
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        this.chargeStatus = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
        this.powerStatus = this.chargeStatus;
        MessageQueue.getQueueInstance().push(MSG_POWER_STATUS, this.powerStatus);
        MessageQueue.getQueueInstance().push(MSG_BATTERY_TEMP, locTemp);
        MessageQueue.getQueueInstance().push(MSG_BATTERY_VOLT, locVoltage);

        //mServiceHandler.dispatchMessage();
        if((percentage != this.batteryLevel) && (this.chargeStatus)&&(this.socccPhase == 0)&&(percentage<50)){

            if(this.batteryLevel == 0)
                this.beginSOC = (int) percentage;

            MessageQueue.getQueueInstance().push(MSG_BATTERY_LEVEL,(int)percentage);
            double deltaTee = (double)yupdateTime-this.socYupdateTime[this.beginSOC];
            Log.d(APP_NAME, "deltaT "+deltaTee + " "+ this.batteryLevel);
            /* We can start estimating the FCC loss from here*/
            double deltaSoc = Math.abs(((double) this.batteryLevel) - percentage);
            double socCrateOld = (36*deltaSoc)/deltaTee;


            long fccPresent = 0;
            Long chargingCurrent = MessageQueue.getQueueInstance().pop(MSG_CHARGING_CURRENT);
            if(chargingCurrent!=null) {
                fccPresent = ((long) (chargingCurrent / socCrateOld));
                Log.d(APP_NAME, "Latest capacity: " + fccPresent + "crate " + socCrateOld + "delta T " + deltaTee);
            }

            this.socAverageCarate[this.batteryLevel] = socCrateOld;




            this.batteryLevel = (int) percentage;

            //Message successMessage = mainHandler.obtainMessage(MSG_BATTERY_LEVEL);
            //bundle = new Bundle();
            //bundle.putInt("batteryLevel",this.batteryLevel);
            //MessageQueue.getQueueInstance().push(MSG_BATTERY_LEVEL,bundle);

            Log.d("Check batteryLevel", "level "+this.batteryLevel);
            this.socYupdateTime[this.batteryLevel] = yupdateTime;
            this.socBatteryVoltage[this.batteryLevel] = locVoltage;
            this.socBatteryCelsius[this.batteryLevel] = locTemp;

            Log.d(APP_NAME, "BatteryVoltage: "+ locVoltage+"V");

            /* Determining the CC phase and set the SOC level*/
            if((locVoltage >= BatteryService.MAX_VOLTAGE) && (this.socccPhase == 0))
                this.socccPhase = ((int) this.batteryLevel);


            sendBroadcast(FCC_NEW, (batCapacity));
            sendBroadcast(FCC_NOW, (fccPresent));

        }else{

            this.batteryLevel = (int) percentage;

        }

        //this.releasebatteryUpdateMutex();
        sendBroadcast(SOC_NOW, ((long) percentage));

    }


    private final void sendBroadcast (String param, Long value){
        Intent intent = new Intent("Battery"); //put the same message as in the filter you used in the activity when registering the receiver
        intent.putExtra(param, value);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

}
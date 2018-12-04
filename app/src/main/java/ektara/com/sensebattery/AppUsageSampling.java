package ektara.com.sensebattery;

import android.app.ActivityManager;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.GnssStatus;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.SparseArray;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.ACTIVITY_SERVICE;
import static ektara.com.sensebattery.BatteryConstants.APP_NAME;
import static ektara.com.sensebattery.BatteryConstants.CURRENT_NOW;
import static ektara.com.sensebattery.BatteryConstants.FCC_NEW;
import static ektara.com.sensebattery.BatteryConstants.INSTALLED_UID_PACKAGES;
import static ektara.com.sensebattery.BatteryConstants.MOB_GPS_STATUS_ON;
import static ektara.com.sensebattery.BatteryConstants.MSG_BATTERY_LEVEL;
import static ektara.com.sensebattery.BatteryConstants.MSG_BATTERY_TEMP;
import static ektara.com.sensebattery.BatteryConstants.MSG_BATTERY_VOLT;
import static ektara.com.sensebattery.BatteryConstants.MSG_CHARGING_CURRENT;
import static ektara.com.sensebattery.BatteryConstants.MSG_POWER_STATUS;
import static ektara.com.sensebattery.BatteryConstants.STATUS_NOW;
import static ektara.com.sensebattery.BatteryConstants.TEMP_NOW;
import static ektara.com.sensebattery.BatteryConstants.VOLTAGE_NOW;

/**
 * Created by mohoque on 13/01/2017.
 */

public class AppUsageSampling {
    private int buildVersion = 0;

    public AppUsageSampling(int buildVersion){
        this.buildVersion = buildVersion;
    }


    public void MobileAppUsageHandler(Context context){

        //final LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        final PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        final Context handlercontext = context;
        final BatteryInformation batInfo = new BatteryInformation(context);
        //final CameraAudioInformation cadInfo = new CameraAudioInformation(context);
        final ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);


        final DeviceStatus devStatus = new DeviceStatus(context);
        /*
        *
        * This is very unreliable to read this from power_profile. Galaxy S3 capacity is 3200mAh. WTF
        * We need to have a service that has the capacity of different Smartphone models.
        *
        * */
        final Handler mainHandler = new Handler();
        mainHandler.post(new Runnable(){

            private  int counter = 1;
            private  long avgCurrent = 0;
            private long curSumma = 0;
            private int socNow = getBatteryLevel();
            //private int socOld = 0;
            private double stdCur = 0.0D;
            private boolean powerStatus = getPowerStatus();
            //private boolean oldPowerStatus = powerStatus;
            private int binIndex = 0;
            private int delayed = 1000;
            private boolean coulombCounter = true;
            private int coulombCharge = 0;
            private int batteryVoltage = 0;
            private int batteryTemperature = 0;

            private HashMap<String, ApplicationUsage> firstAllPackagesInfo = new HashMap<>();
            //private HashMap<String, ApplicationUsage> firstAllTrafficInfo = new HashMap<>();
            private HashMap<String, TrafficStatistics> firstAllTrafficInfo = new HashMap<>();
            private HashMap<String, Integer> localPackageId = new HashMap();

            private HashMap<Integer, SparseArray<ResourseBin>>  usageBin = new HashMap<>();
            SparseArray<ResourseBin> nbins = new SparseArray<>();

            // there can be a bitset for signal strength observed over a foreground period, each position represents
            // a specific signal strength//
            // there can be a bitset for cameras to keep the boolean values,camera availeable or not
            // Similar here, one bitset can be used to reporesent the audio mode
            // Similarly gps

            HashMap<Long,Long> byteRxBins = new HashMap<Long, Long>();//(time, appName, rxBytes)
            HashMap<Long,Long> byteTxBins = new HashMap<Long, Long>(); // (time, appName, txbytes)


            HashMap<Long,Boolean> gpsBins = new HashMap<Long, Boolean>(); // (time, boolean)
            HashMap<Long,Boolean> audioBins = new HashMap<Long, Boolean>(); // (time, audio mode)
            HashMap<Long,Boolean> cameraBBins = new HashMap<Long, Boolean>(); //(time, front/back camera)
            HashMap<Long,Boolean> cameraFBins = new HashMap<Long, Boolean>(); //(time, front/back camera)
            HashMap<Long,Integer> wifiBins = new HashMap<Long, Integer>(); //(time, signal strength)
            HashMap<Long,Integer> mobileBins = new HashMap<Long, Integer>(); //(time, signal, strength)
            HashMap<Long,Integer> displayBins = new HashMap<Long, Integer>(); //(time, signal, strength)
            HashMap<Long,Integer> chargeBins = new HashMap<Long, Integer>(); //(time, signal, strength)
            HashMap<Long,Integer> currentBins = new HashMap<Long, Integer>(); //(time, signal, strength)

            private boolean cameraZavail = true;
            private boolean cameraOavail = true;





            @Override
            public void run() {
                mainHandler.postDelayed(this,delayed); // this time is in milliseconds
                long batCapacity = ((long) batInfo.batteryCapacity());
                sendBroadcast(FCC_NEW, batCapacity);

                // The batteryInfo API should be changed for

                boolean binreset = false;
                long curtime = System.currentTimeMillis();
                long current = batInfo.instantCurrent();
                //long avgcurrent = batInfo.avgCurrent();



                Log.d(APP_NAME,"Current "+current);
                Integer voltage = MessageQueue.getQueueInstance().pop(MSG_BATTERY_VOLT);
                if(voltage!=null)
                {

                    batteryVoltage = voltage.intValue();
                    Log.d(APP_NAME,""+batteryVoltage);
                }
                sendBroadcast(VOLTAGE_NOW,(long) batteryVoltage);
                Integer celcius = MessageQueue.getQueueInstance().pop(MSG_BATTERY_TEMP);
                if(celcius!=null)
                {
                    batteryTemperature = celcius.intValue();
                    Log.d(APP_NAME,""+batteryTemperature);
                }
                sendBroadcast(TEMP_NOW,(long) batteryTemperature);

                if(powerStatus){
                    sendBroadcast(STATUS_NOW,(long)1);

                }else
                {
                    sendBroadcast(STATUS_NOW,(long)0);
                }



                if(this.coulombCounter){
                    long charge = batInfo.batteryCharge();
                    if(charge <= 0 ) // this means that the coulomb counter does not support the charge pushed to the client
                        this.coulombCounter = false;
                    else
                        this.coulombCharge = (int) charge;

                }
                chargeBins.put(curtime,(int)coulombCharge);

                currentBins.put(curtime,(int)current);
                sendBroadcast(CURRENT_NOW,current);
                Log.d(APP_NAME,"Current "+current+ " Charge "+coulombCharge);

                curSumma += current;
                avgCurrent = curSumma / counter;
                stdCur = stdCur + Math.pow(current - avgCurrent, 2);
                counter += 1;


                boolean nwPowerStatus = getPowerStatus();
                /* Taking charging current measurements considering the following conditions */
                if ((current > 0) &&(!isScreenON())&&(getBatteryLevel()<50)&&(nwPowerStatus)){

                    //Bundle bundle= new Bundle();
                    //bundle.putLong("chargingCurrent",this.avgCurrent);
                    //MessageQueue.getQueueInstance().push(MSG_CHARGING_CURRENT,(Long)this.avgCurrent);
                    sendBroadcast(CURRENT_NOW,current);

                    // Log.d(APP_NAME, "current " +current+" "+avgCurrent + " "+stdCur);

                }

                /* avoid charging current measurements when the display is ON */
                if ((current > 0) &&  (nwPowerStatus)&&(isScreenON())){

                    counter = 1;
                    curSumma = 0;
                    avgCurrent = 0;
                    stdCur = 0;
                }

                /*
                Resetting the measurement variables when there is alteration of the power status
                and battery level changes */


                if ((getBatteryLevel()!= this.socNow) || ( nwPowerStatus!= powerStatus)){
                    counter = 1;
                    curSumma = 0;
                    avgCurrent = 0;
                    stdCur = 0;
                    //binreset = true;

                    //sthis.energyStatusSoc =charge;
                    //this.socOld = this.socNow;
                    this.socNow = getBatteryLevel();
                    this.powerStatus = nwPowerStatus;

                    Log.d(APP_NAME, "Reset vars " +current+" "+current +" powered "+getPowerStatus());
                }


                /*
                * Here we only consider the discharigng current. However, discharging can also happen when the device is
                * almost fully charged. This is beacuse of the system designed in a way that it does not care about the system
                * load at the end of charging.
                *
                * **/

                if(!nwPowerStatus){

                    Log.d(APP_NAME, " Discharging");

                /* Check the display brightness
                 * We need to keep record of one hour samples of charging time
                 * We need to keep record of one hour samples of charger
                 * We can keep record per second while the display is on
                 * We can keep record per 5 seconds while display is OFF
                 * Check the application focus
                *
                * */

                    //discount = setdischargeCurrent((int) avgcurrent, discount);
                    setCurrentApplicationUsageStat(curtime, current, coulombCharge, socNow, false);
                    if(!isScreenON()){

                    }
                    sendBroadcast(CURRENT_NOW,current);
                    // check the applications and
                    //Log.d(APP_NAME, "discharging current " +current+" "+avgCurrent+" "+getPowerStatus());

                }

                /* Get the average charging current*/
                setCurrentApplicationUsageStat(curtime, current, coulombCharge, socNow, false);
                ResourseBin tempu = nbins.get(binIndex);
                if(tempu != null)
                    Log.d(APP_NAME,"binindex "+binIndex+ tempu.getTimeZone()+tempu.getAppName());
                //Log.d("Thread ", "status "+ isScreenON()+" "+getPowerStatus()+ " "+getBatteryLevel()+" "+current+" "+avgcurrent);
                //releasebatteryUpdateMutex();



            }

            private int getBatteryLevel(){
                Integer battlevel = MessageQueue.getQueueInstance().pop(MSG_BATTERY_LEVEL);
                if(battlevel!=null){
                    return battlevel;
                }
                else
                    return socNow;
            }

            private boolean getPowerStatus(){

                Boolean powered = MessageQueue.getQueueInstance().pop(MSG_POWER_STATUS);
                if(powered!=null){
                    return powered;
                }
                else
                    return powerStatus;

            }


            private boolean isScreenON() {

                boolean isScreenOn;
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {

                    isScreenOn = pm.isScreenOn();
                }else
                    isScreenOn = pm.isInteractive();


                return isScreenOn;

            }


            private void setCurrentApplicationUsageStat(long start, long dischargeCurrent, long charge, int batteryLevel, boolean binreset){

                Object obj = MessageQueue.getQueueInstance().pop(INSTALLED_UID_PACKAGES);
                if(obj != null){
                    localPackageId = (HashMap) obj;
                    Log.d(APP_NAME, "local not null" +localPackageId.size());
                }
                //HashMap<Integer, String> two = MessageQueue.getQueueInstance().pop(INSTALLED_UID_APPNAMES);


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    sampleAppUsageBuild21(start,dischargeCurrent,charge,batteryLevel);

                }
                if(Build.VERSION.SDK_INT<Build.VERSION_CODES.LOLLIPOP){
                    //sampleAppUsageBuild19(start,dischargeCurrent,charge,batteryLevel, binreset);
                    sampleAppUsageBuildAlter19(start,dischargeCurrent,charge,batteryLevel, binreset);
                }

            }
            private void sampleAppUsageBuild19(long start, long dischargeCurrent, long charge, int batteryLevel, boolean binreset) {

                if(isScreenON()){
                    String activeApp = getForgroundApplicaiton();
                    int id = getForgroundApplicaitonId(activeApp);
                    if(!localPackageId.isEmpty() &&(localPackageId.get(activeApp)==null)){
                        localPackageId.put(activeApp,id);
                    }

                }
                if(!localPackageId.isEmpty()) {
                    //Map<String, Integer> map = new HashMap<String, Integer>();
                    for (Map.Entry<String, Integer> entry : localPackageId.entrySet()) {
                        String packname = entry.getKey();
                        Integer id = entry.getValue();


                        long lastUtime = 0;
                        long delTaPollTime = 0;
                        ApplicationUsage lastInfo = firstAllPackagesInfo.get(packname);
                        if(isApplicationForeground(packname)){
                            if(lastInfo!=null){
                                lastUtime = lastInfo.getLastUsgaeTime();
                                delTaPollTime = start-lastInfo.getPollingTime();
                                if(start-lastUtime<=1100000){
                                    long totaFGTime = delTaPollTime + lastInfo.getForegroundTime();
                                    lastInfo.updateForegroundTime(totaFGTime);

                                    // we have not fixed it yet. updating the lastInfo, in case an application continues in the foreground
                                    ApplicationUsage newInfo = new ApplicationUsage(lastInfo.getPollingTime(), 0, start, dischargeCurrent, batteryLevel, charge);
                                    firstAllPackagesInfo.remove(packname);
                                    firstAllPackagesInfo.put(packname,newInfo);

                                }

                                if(binreset){
                                    // here we need to check whether an application is in foreground but the battery level has changed.
                                    // Spreading the foreground activity among the consecutive battery level
                                    long binStart = lastInfo.getPollingTime()/1000;
                                    long binEnd = start/10000;
                                    generateBin(binStart,binEnd,packname);
                                    this.usageBin.put(lastInfo.getBatteryLevel(), this.nbins);
                                    ApplicationUsage newInfo = new ApplicationUsage(start, 0, start, dischargeCurrent, batteryLevel, charge);
                                    firstAllPackagesInfo.remove(packname);
                                    firstAllPackagesInfo.put(packname,lastInfo);
                                }


                            }else{
                                // The first time adding the usage info for a particular applicaiton.

                                ApplicationUsage newInfo = new ApplicationUsage(start, 1000, start, dischargeCurrent, batteryLevel, charge);
                                firstAllPackagesInfo.put(packname,lastInfo);
                            }
                        }else {
                            if (lastInfo != null) {

                                long binStart = lastInfo.getPollingTime()/1000;
                                long binEnd = binStart+lastInfo.getForegroundTime()/1000;
                                generateBin(binStart,binEnd,packname);
                                //ApplicationUsage newInfo = new ApplicationUsage(0, 0, 0, 0, 0, 0);
                                firstAllPackagesInfo.remove(packname);
                                //firstAllPackagesInfo.put(packname,newInfo);

                            }else{
                                //Map.Entry<String, ApplicationUsage> lastEntry = map.lastEntry();


                            }
                            // We do not consider those if the  application that never had been at the foreground.And remove
                            // those whenever the are moved to background
                        }
                    }

                    // we need to insert all the bins for a particular battery level
                    if((binreset)&&(this.usageBin.get(batteryLevel)==null)){
                        this.usageBin.put(getBatteryLevel(), this.nbins);
                        this.binIndex = 0;
                        this.nbins = new SparseArray<>();
                        //ApplicationUsage newInfo = new ApplicationUsage(start, 0, start, dischargeCurrent, batteryLevel, charge);

                    }


                }
                Log.d(APP_NAME,"Total Execution Time "+(System.currentTimeMillis()-start));


            }
            private void sampleAppUsageBuildAlter19(long start, long dischargeCurrent, long charge, int batteryLevel, boolean binreset) {

                //if(isScreenON()){
                String packname = getForgroundApplicaiton();
                int id = getForgroundApplicaitonId(packname);
                if(packname.length()>0) {
                    long lastUtime = 0;
                    long delTaPollTime = 0;
                    ApplicationUsage lastInfo = firstAllPackagesInfo.get(packname);

                    if(lastInfo!=null){
                        Log.d(APP_NAME,"lastinfo not null1 "+packname);
                        lastUtime = lastInfo.getLastUsgaeTime();
                        delTaPollTime = start-lastInfo.getPollingTime();
                        if(start-lastUtime<=1100000){
                            long totaFGTime = delTaPollTime + lastInfo.getForegroundTime();
                            lastInfo.updateForegroundTime(totaFGTime);

                            Log.d(APP_NAME,"Total Foreground time "+(start-lastInfo.getPollingTime()));

                            // we have not fixed it yet. updating the lastInfo, in case an application continues in the foreground
                            ApplicationUsage newInfo = new ApplicationUsage(lastInfo.getPollingTime(), (start-lastInfo.getPollingTime()), start, dischargeCurrent, batteryLevel, charge);
                            firstAllPackagesInfo.remove(packname);
                            firstAllPackagesInfo.put(packname,newInfo);

                        }

                        if(binreset){
                            // here we need to check whether an application is in foreground but the battery level has changed.
                            // Spreading the foreground activity among the consecutive battery level
                            long binStart = lastInfo.getPollingTime()/1000;
                            long binEnd = start/10000;
                            Log.d(APP_NAME,"Total Foreground time "+(binEnd-binStart)+" "+packname);
                            generateBin(binStart,binEnd,packname);
                            this.usageBin.put(lastInfo.getBatteryLevel(), this.nbins);
                            ApplicationUsage newInfo = new ApplicationUsage(start, 0, start, dischargeCurrent, batteryLevel, charge);
                            firstAllPackagesInfo.remove(packname);
                            firstAllPackagesInfo.put(packname,newInfo);
                        }


                    }else{
                        // remove the last package and enter the new entry for new application
                        for (Map.Entry<String, ApplicationUsage> entry : firstAllPackagesInfo.entrySet()) {
                            ApplicationUsage differUsage = entry.getValue();
                            String puckname = entry.getKey();
                            Log.d(APP_NAME,"Map entry Generated new bin "+puckname +" "+differUsage.getForegroundTime());
                            generateBin(differUsage.getPollingTime(),differUsage.getForegroundTime(),puckname);
                            firstAllPackagesInfo.remove(puckname);
                        }

                        // The first time adding the usage info for a particular applicaiton.


                        ApplicationUsage newInfo = new ApplicationUsage(start, 1000, start, dischargeCurrent, batteryLevel, charge);
                        firstAllPackagesInfo.put(packname,newInfo);

                    }
                }
                // Log.d(APP_NAME,"Total Execution Time "+(System.currentTimeMillis()-start));
            }

            private boolean isApplicationForeground(String packname) {
                List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
                String topPackageName = taskInfo.get(0).topActivity.getPackageName();
                //int uid = taskInfo.get(0).topActivity
                if (!taskInfo.isEmpty()) {
                    if (topPackageName.equals(packname)) {
                        Log.d(APP_NAME,packname+" "+true);
                        return true;
                    }
                }
                return false;
            }



            private String getForgroundApplicaiton() {
                //String packName = "";
                List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
                String topPackageName = taskInfo.get(0).topActivity.getPackageName();
                if (!taskInfo.isEmpty()) {
                    return topPackageName;
                }
                return topPackageName;
            }

            private int getForgroundApplicaitonId(String packname) {
                try {
                    ApplicationInfo app = handlercontext.getPackageManager().getApplicationInfo(packname, 0);
                    return app.uid;

                }catch(PackageManager.NameNotFoundException ne){}
                return 0;
            }

            private int[] getIntBin(long begin, long end, HashMap<Long,Integer> onlyBins){
                int temp [];
                long sim = 0, avg = 0;
                int first = onlyBins.get(begin);
                for (sim = begin; sim <= end; sim++){
                    avg += onlyBins.get(sim);
                }
                avg = avg/(end-begin);
                int lctemp []; //= new int[1];
                if(avg == first){
                    lctemp  = new int[1];
                    lctemp[0] = first;
                    return lctemp;
                }
                else{
                    sim = 0;
                    temp = new int[(int)(end-begin)];
                    for (sim = begin; sim <= end; sim++){
                        temp[(int)(sim-begin)] = onlyBins.get(sim);
                    }
                    return temp;
                }

            }

            // this will return
            private boolean[] getBooleanBin(long begin, long end, HashMap<Long,Boolean> onlyBins){
                boolean temp [];
                boolean tempu [] = new boolean [1];
                long stamp = begin;
                long sim = 0;
                boolean alltrue = true;

                /*
                boolean first = onlyBins.get(begin);
                for (sim = begin; sim <= end; sim++){
                    if (!onlyBins.get(sim)) {
                        avg += (sim - 1 - stamp);
                        stamp = sim -1;
                    }
                }*/
                for (sim = begin; sim <= end; sim++){
                    if (!onlyBins.get(sim)) {
                        alltrue = false;
                        break;
                    }
                }

                if(alltrue){
                    tempu[0] = true;
                    return tempu;
                }
                else{
                    sim = 0;
                    temp = new boolean[(int)(end-begin)];
                    for (sim = begin; sim <= end; sim++){
                        temp[(int)(sim-begin)] = onlyBins.get(sim);
                    }
                    return temp;
                }

            }

            private void generateBin(long binStart, long binEnd, String packname){


                int temp [] = new int[(int)(binEnd-binStart)];
                ResourseBin newbin = new ResourseBin((int)(binEnd-binStart),binStart); // time when the bin Starts and the binduration


                if ((chargeBins.size()>0) && (chargeBins.get(binStart) != null) & (chargeBins.get(binEnd) != null)){
                    // int chargeDelta = chargeBins.get(binEnd) - chargeBins.get(binStart);
                    newbin.setCharge(getIntBin(binStart,binEnd,chargeBins));

                }
                if ((currentBins.size()>0) && (currentBins.get(binStart) != null) & (currentBins.get(binEnd) != null)){
                    //int currentDelta = currentBins.get(binEnd) - currentBins.get(binStart);// we need to find the avg current
                    newbin.setCurrentmA(getIntBin(binStart,binEnd,currentBins));
                }
                if ((displayBins.size()>0) && (displayBins.get(binStart) != null) & (displayBins.get(binEnd) != null)){
                    newbin.setDisplayBright(getIntBin(binStart,binEnd,currentBins));
                }

                if ((cameraBBins.size()>0) && (cameraBBins.get(binStart) != null) & (cameraBBins.get(binEnd) != null)){
                    newbin.setBackeCamStatus(getBooleanBin(binStart,binEnd,cameraBBins));
                }

                if ((cameraFBins.size()>0) && (cameraFBins.get(binStart) != null) & (cameraFBins.get(binEnd) != null)){
                    newbin.setFrontCamStatus(getBooleanBin(binStart,binEnd,cameraFBins));
                }

                if ((audioBins.size()>0) && (audioBins.get(binStart) != null) & (audioBins.get(binEnd) != null)){

                    newbin.setAudioUsage(getBooleanBin(binStart,binEnd,audioBins));
                }

                if ((gpsBins.size()>0) && (gpsBins.get(binStart) != null) & (gpsBins.get(binEnd) != null)){

                    newbin.setGpsUsage(getBooleanBin(binStart,binEnd,gpsBins));
                }

                if ((wifiBins.size()>0) && (wifiBins.get(binStart) != null) & (wifiBins.get(binEnd) != null)){
                    newbin.setWifiUsage(getIntBin(binStart,binEnd,wifiBins));
                }

                if ((mobileBins.size()>0) && (mobileBins.get(binStart) != null) & (mobileBins.get(binEnd) != null)){
                    newbin.setWifiUsage(getIntBin(binStart,binEnd,mobileBins));
                }
                this.nbins.append(binIndex, newbin);
                this.binIndex += 1;

                // We should reset all the local storage



            }
            private void sampleAppUsageBuild21(long start, long dischargeCurrent, long charge, int batteryLevel) {

                Log.d("APPStat","here O ,a ");
                Log.d("APPStat","here O ,a ");
                Calendar calendar = Calendar.getInstance();
                long endTime = calendar.getTimeInMillis();

                calendar.add(Calendar.SECOND,-30);
                long startTime = calendar.getTimeInMillis();

                final UsageStatsManager usageStatsManager = (UsageStatsManager) handlercontext.getSystemService(Context.USAGE_STATS_SERVICE);
                List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, startTime, endTime);


                for (UsageStats u : usageStatsList) {
                    String packname = u.getPackageName();
                    if ((firstAllPackagesInfo.get(packname) != null) && (!localPackageId.isEmpty())) {
                        ApplicationUsage lastInfo = firstAllPackagesInfo.get(packname);
                        long fgDelta = u.getTotalTimeInForeground() - lastInfo.getForegroundTime();
                        long chargeDelta = charge - lastInfo.getChargeStatus();
                        long timeDelta = start - lastInfo.getPollingTime();
                        int blevelDelta = Math.abs(batteryLevel - lastInfo.getBatteryLevel());
                        if ((fgDelta > 0) && (blevelDelta == 0)) {

                            long binStart = (u.getLastTimeUsed() - fgDelta) / 1000;
                            long binEnd = (u.getLastTimeUsed() / 1000);
                            long deltaRx = 0;
                            long deltaTx = 0;


                            // if the audio is active and then its the first application to change the status, then it is the owner of the
                            // audio signal. We need to split this audio to other applications as well if the
                            /*
                            * OwnerX (Audio+display ON) + (audio+displayOff)OwnerX + (audio+displayON)OtherY
                            *
                            *
                            * We also need to get the traffic stat for the user applications when they are in foreground//
                            * */
                            generateBin(binStart,binEnd,packname);
                            /*
                            if ((chargeBins.get(binStart) != null) & (chargeBins.get(binEnd) != null)) {
                                long charge2 = chargeBins.get(binStart);
                                long charge1 = chargeBins.get(binEnd);
                                ResourseBin newbin = new ResourseBin();
                                newbin.setforgroundTime(binEnd - binStart);
                                newbin.setBinepochTime(binStart);
                                newbin.setCharge(charge2 - charge1);
                                newbin.setTxBytes(deltaTx);
                                newbin.setRxBytes(deltaRx);
                                newbin.setAppName(packname);
                                this.nbins.append(binIndex, newbin);
                                this.binIndex += 1;
                                Log.d(APP_NAME, "The charge values are  found" + " " + (charge2 - charge1));
                                Log.d(APP_NAME, "The Traffic Volumes TX found" + " " + deltaRx + " RX " + deltaTx);
                                //firstAllTrafficInfo.remove(packname);
                                //firstAllTrafficInfo.put(packname, newStat);

                            }*/

                            Log.d("AppStat", "" + packname + " " + fgDelta + " " + chargeDelta + " " + timeDelta + " " + blevelDelta);

                        }


                        if ((fgDelta > 0) && (blevelDelta > 0)) {
                            this.usageBin.put(lastInfo.getBatteryLevel(), this.nbins);
                            this.binIndex = 0;
                            this.nbins = new SparseArray<>();
                            //lastInfo.updateResourceBin(packname,fgDelta, chargeDelta, timeDelta, lastInfo.getPollingTime(), true);
                            Log.d(APP_NAME, packname + " " + fgDelta + " " + chargeDelta + " ");
                            Log.d("AppStat", "Resting the bins");
                            //constructUpdateBin(packname,fgDelta, chargeDelta, timeDelta, lastInfo.getPollingTime());

                        }


                        /*
                        if((audioFlag)){

                            if (isMusic)
                                Log.d(APP_NAME,"Music "+nbins.get(binIndex-1).getAppName());
                            if (audioMode == AudioManager.MODE_IN_COMMUNICATION)
                                Log.d(APP_NAME,"VoiP "+ nbins.get(binIndex-1).getAppName());
                            if (audioMode == AudioManager.MODE_IN_CALL)
                                Log.d(APP_NAME,"Voice Call "+ nbins.get(binIndex-1).getAppName());

                        }*/


                            /* while in the background it is possible to have two options
                            * (1) An application uses the speaker/microphone for VOIP communication
                            * (2) An application uses the speaker only for listening music
                            * (3) Or the device is completely idle
                            * */

                            /*
                            * An application statistics is not updated until it is removed from the foreground. So we cannot capture the
                            *
                            * present status of an application. We can only update the
                            *
                            * */


                    }
                    ApplicationUsage latest = new ApplicationUsage(start, u.getTotalTimeInForeground(), u.getLastTimeUsed(), dischargeCurrent, batteryLevel, charge);
                    firstAllPackagesInfo.remove(packname);
                    firstAllPackagesInfo.put(packname, latest);
                    //firstAllPackagesInfo.put(u.getPackageName(),applicationUsage);
                }



                long end = System.currentTimeMillis();
                //Log.d(APP_NAME, "Time took "+ (end-start));

            }


            private void sampleTrafficBuild23(long start, String packname){
                if (Build.VERSION.SDK_INT >= 23) {
                    try {
                        NetworkStatsManager service = handlercontext.getSystemService(NetworkStatsManager.class);


                        NetworkStats netStat = service.queryDetailsForUid(ConnectivityManager.TYPE_WIFI, APP_NAME, 0, start, localPackageId.get(packname));
                        //NetworkStats netStat = service.queryDetails(ConnectivityManager.TYPE_WIFI, APP_NAME, 0, start);


                        while (netStat.hasNextBucket()) {

                            NetworkStats.Bucket bucket = new NetworkStats.Bucket();
                            netStat.getNextBucket(bucket);
                            Long time1 = bucket.getStartTimeStamp();
                            Long time2 = bucket.getEndTimeStamp();
                            int state = bucket.getState();


                            Log.d(APP_NAME, time1 / 1000 + " " + time2 / 1000 + " " + (start - time2) / 1000 + " " + bucket.getTxBytes() + " " + (time2 - time1) / 1000 + "s " + bucket.getRxBytes() + " " + state);
                            //bucket = netStat.getNextBucket(bucket);
                        }


                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    //service.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE, null, from, to);
                } else {

                }

            }

            private void sampleTrafficBuild19(){

            }
            private final void sendBroadcast (String param, Long value){
                Intent intent = new Intent("Battery"); //put the same message as in the filter you used in the activity when registering the receiver
                intent.putExtra(param, value);
                LocalBroadcastManager.getInstance(handlercontext).sendBroadcast(intent);
            }



        });




    }




}


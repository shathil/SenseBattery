package ektara.com.sensebattery;

import android.icu.util.TimeZone;

import java.util.HashMap;

/**
 * Created by mohoque on 28/12/2016.
 */

public class ResourseBin {
    private String appName;
    private int foregroundTime;
    private long localTime; // when a bin of an applicaiton starts.
    private String timeZone;


    private boolean audioFlag = false;
    private boolean displayStatus = false;
    private boolean wifiStatus = false;
    private boolean mobileStatus = false;
    private boolean gpsStatus = false;
    private boolean txStatus = false;
    private boolean rxStatus = false;
    private boolean frontCameraStatus = false;
    private boolean backCameraStatus = false;
    private boolean chareStaus = false;
    private boolean currentStatus = false;
    private boolean powerStatus = false;


    private int [] chargemAh;
    private int [] currentmA;
    private int [] dataReceived;
    private int [] displayBright;
    private int [] dataTrasmitted;
    private int [] wifiSignalStrenth;
    private int [] mobileSignalStrength;
    private boolean [] gpsUsage;
    private boolean [] audioUsage;
    private boolean [] frontCamStatus;
    private boolean [] backeCamStatus;



    public ResourseBin(int sizeinTime, long start){
        this.appName ="";
        this.foregroundTime=sizeinTime;
        this.localTime = start;
        this.timeZone = TimeZone.getDefault().getID();
    }


    public long getforgroundTime(){
        return  this.foregroundTime;
    }
    public long getUsageTimeStamp(){
        return this.localTime;
    }

    public String getTimeZone(){
        return this.timeZone;
    }

    public String getAppName(){

        return this.appName;
    }



    public int []  getDisplayBright(){
        if(displayBright.length>0)
            return displayBright;
        else
            return null;
    }

    public int[] getWifiSignalStrenth() {
        if (wifiSignalStrenth.length>0)
            return wifiSignalStrenth;
        else
            return  null;
    }
    public int[] getMobileSignalStrength(){
        if(mobileSignalStrength.length>0)
            return  mobileSignalStrength;
        else
            return null;

    }
    public int[] getChargemAh(){
        if(chargemAh.length>0)
            return  chargemAh;
        else
            return null;
    }

    public int[] getCurrentmA(){
        if(currentmA.length>0)
            return currentmA;
        else
            return null;
    }

    public int[] getAudio(){
        if(currentmA.length>0)
            return currentmA;
        else
            return null;
    }

    public boolean [] getGps(){
        if(gpsUsage.length>0)
            return gpsUsage;
        else
            return null;
    }

    public int [] getDataTrasmitted(){
        if(dataTrasmitted.length>0)
            return dataTrasmitted;
        else
            return null;
    }
    public int [] getDataReceived(){
        if(dataReceived.length>0)
            return dataReceived;
        else
            return null;
    }

    public int totalFrontCamUseTime(){
        long beginTime = this.localTime ;
        long endTime = this.localTime + this.foregroundTime;
        int sum = 0;
        int stamp = 0;
        if(this.frontCamStatus.length>0){
            for (int sim = 0; sim <= endTime; sim++){
                if (!this.frontCamStatus[sim]) {
                    sum += (sim - 1 - stamp);
                    stamp = sim -1;
                }
            }
        }

        return sum;
    }

    public int totalGpsUseTime(){
        long beginTime = this.localTime ;
        long endTime = this.localTime + this.foregroundTime;
        int sum = 0;
        int stamp = 0;
        if(this.gpsUsage.length>0) {
            for (int sim = 0; sim <= endTime; sim++) {
                if (!this.gpsUsage[sim]) {
                    sum += (sim - 1 - stamp);
                    stamp = sim - 1;
                }
            }
        }

        return sum;
    }

    public int totalAudioUseTime(){
        long beginTime = this.localTime ;
        long endTime = this.localTime + this.foregroundTime;
        int sum = 0;
        int stamp = 0;
        if(this.audioUsage.length>0) {
            for (int sim = 0; sim <= endTime; sim++) {
                if (!this.audioUsage[sim]) {
                    sum += (sim - 1 - stamp);
                    stamp = sim - 1;
                }
            }
        }

        return sum;
    }

    public int averageCurrentmA(){
        long beginTime = this.localTime ;
        long endTime = this.localTime + this.foregroundTime;
        int sum = 0;
        int stamp = 0;
        if(this.currentmA.length>0){
            for (int sim = 0; sim <= endTime; sim++){
                sum += this.currentmA[sim];
            }
        }
        return (sum/this.currentmA.length);

    }

    // this are the critical functions which whould depend on the duraiton of TX, RX, and the appliation foregroundtime

    public int totalWifiOnTime(){ return 0;}
    public int totalmoNetOnTime(){ return 0;}


    public int totalBackeCamUseTime(){

        long beginTime = this.localTime ;
        long endTime = this.localTime + this.foregroundTime;
        int sum = 0;
        int stamp = 0;
        if(this.backeCamStatus.length>0){
            for (int sim = 0; sim <= endTime; sim++){
                if (!this.backeCamStatus[sim]) {
                    sum += (sim - 1 - stamp);
                    stamp = sim -1;
                }
            }
        }
        return sum;
    }
    public int totalChargeUsedmAh(){

        long beginTime = this.localTime ;
        long endTime = this.localTime + this.foregroundTime;
        int sum = 0;
        int stamp = 0;
        if(this.chargemAh.length>0){
            for (int sim = 0; sim <= endTime; sim++){
                sum += chargemAh[sim+1]-chargemAh[sim];
            }
        }
        return sum;
    }

    // As long as the application open, the display remains ON. Well this may not be true for the earlier versions.
    public int totalDisplayUsageTime(){
        return this.foregroundTime;
    }



    public void setforgroundTime(int time){
        this.foregroundTime = time;
    }
    public void setCharge(int [] charge){
        this.chargemAh = new int[this.foregroundTime];
        this.chargemAh = charge;
    }
    public void setChargemAh(boolean charge){
        this.chareStaus = charge;
    }
    public void setCurrentmA(int [] current){
        this.currentmA = new int[current.length];
        this.chargemAh = current;
    }
    public void setCurrentmA(boolean status){
        this.currentStatus = status;
    }
    public void setAppName(String name){
        this.appName = name;
    }
    public void setTxBytes(int [] bytes){
        this.dataTrasmitted = new int[bytes.length];
        this.dataTrasmitted = bytes;
    }
    public void setTxBytes(boolean status){
        this.txStatus = status;
    }
    public void setRxBytes(int [] bytes){
        this.dataReceived = new int[bytes.length];
        this.dataReceived = bytes;
    }
    public void setRxBytes(boolean status){

        this.rxStatus = status;
    }


    public void setDisplayBright(int [] brightness){
        this.displayBright = new int[this.foregroundTime];
        this.displayBright = brightness;
    } // we set here median brightness

    public void setDisplayBright(boolean active){

        this.displayStatus = active;
    }
    public void setFrontCamStatus(boolean [] active){
        this.frontCamStatus = new boolean[active.length];
        this.frontCamStatus = active;
    } // we set here median brightness
    public void setFrontCamStatus (boolean active){

        this.frontCameraStatus = active;
    }
    public void setBackeCamStatus(boolean [] active){
        this.backeCamStatus = new boolean[active.length];
        this.backeCamStatus = active;
    } // we set here median brightness
    public void setBackeCamStatus (boolean active){
        this.backCameraStatus = active;
    }
    public void setGpsUsage(boolean [] gpsActive){
        this.gpsUsage = new boolean[gpsActive.length];
        this.gpsUsage = gpsActive;
    } // we set here median brightness
    public void setGpsUsage (boolean active){
        this.gpsStatus = active;
    }
    public void setAudioUsage(boolean [] audioUsage){
        this.audioUsage = new boolean[audioUsage.length];
        this.audioUsage = audioUsage;
    } // we set here median brightness

    public void setWifiUsage(int [] signalStrength){

        this.wifiSignalStrenth = new int[signalStrength.length];
        this.wifiSignalStrenth = signalStrength;

    }
    public void setWifiUsage(boolean active){
        this.wifiStatus = active;
    }
    public void setMobileUsage(int [] mobileStrength){

        this.mobileSignalStrength = new int[mobileStrength.length];
        this.mobileSignalStrength = mobileStrength;

    }
    public void setMobileiUsage(boolean active){
        this.mobileStatus = active;
    }
    // in this case, we need to find the timestamps during which the audio flag is true
    public void setAudioFlag(HashMap audioStatus){


    }

}

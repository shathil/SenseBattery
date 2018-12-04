package ektara.com.sensebattery;

import static ektara.com.sensebattery.BatteryConstants.MAX_NONZEROBINS_APPS;

/**
 * Created by mohoque on 20/12/2016.
 */
// We save the usage statistics for each battery level or for every 10 seconds for the Coulomb counter devices.
// We find the usage time and energy drain


    /*
    *
    *  t1*App1+ t2*App2+ t3*App3+t4*App4+t5*App5+t6*App6+t7*App7+..... = Y mAh
    *
    * With the number of samples over a SOC update, we can find the contribution of each
    * application with linear programming model given that the objective function is that
    *
    * App1+App2+App3+App4+App5+App6+App7+.... = energy over SOC update.
    *
    *
    * For voltage based devices this would take longer time, as the frequency of variable update
    * is quite small.
    * It can be further utilized to optimize the energy consumption of the devices, based on
    * application popularity, and usage time, give that the remaning energy consumption should be
    * satisfied.
    *
    * Energy consumption per applicaiton can be distrbuted among different hardware components.
    *
    * with another linear system representaiotn of per application
    * /
    // for the voltage based devices, we find the energy drain over a SOC update.
    // we find the application usage time and total eneryg drain.
    // In this case we will need more samples.
    */



public class ApplicationUsage {

    private long totalForegroundTime;
    private long lastUsageTime;
    private long dischargeCurrent;
    private long chargeStatusmah;
    private long pollingTime;
    private int batteryLevel;


    public  ApplicationUsage(long pollingTime, long totalForegroundTime, long lastUsgaeTime, long dischargeCurrent, int batteryLevel, long chargemAh){


        this.pollingTime = pollingTime;
        this.totalForegroundTime = totalForegroundTime;
        this.lastUsageTime = lastUsgaeTime;
        this.dischargeCurrent = dischargeCurrent;
        this.chargeStatusmah  = chargemAh;
        this.batteryLevel = batteryLevel;


    }



    public long getForegroundTime (){
        return this.totalForegroundTime;
    }
    public long getLastUsgaeTime (){
        return this.lastUsageTime;
    }
    public long getDischargeCurrent (){
        return this.dischargeCurrent;
    }
    public long getChargeStatus (){
        return this.chargeStatusmah;
    }
    public long getPollingTime (){
        return this.pollingTime;
    }
    public int getBatteryLevel(){
        return this.batteryLevel;
    }

    public void updateForegroundTime(long time){this.totalForegroundTime = time;}

}

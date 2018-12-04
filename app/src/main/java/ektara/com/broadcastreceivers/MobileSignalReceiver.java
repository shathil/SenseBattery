package ektara.com.broadcastreceivers;

import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.util.Log;

import ektara.com.sensebattery.MessageQueue;

import static ektara.com.sensebattery.BatteryConstants.APP_NAME;
import static ektara.com.sensebattery.BatteryConstants.MOB_SIGNAL_STRENGTH;

/**
 * Created by mohoque on 01/01/2017.
 */


public  class MobileSignalReceiver extends PhoneStateListener
{
    /* Get the Signal strength from the provider, each tiome there is an update */
    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength)
    {
        super.onSignalStrengthsChanged(signalStrength);

        Log.d(APP_NAME, signalStrength.getCdmaDbm()+" "+signalStrength.getGsmSignalStrength()+" "+signalStrength.getGsmBitErrorRate());
        MessageQueue.getQueueInstance().push(MOB_SIGNAL_STRENGTH,signalStrength.getGsmSignalStrength());

    }

}

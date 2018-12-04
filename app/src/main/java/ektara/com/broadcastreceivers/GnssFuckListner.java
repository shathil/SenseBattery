package ektara.com.broadcastreceivers;

import android.location.GnssMeasurementsEvent;
import android.location.GnssStatus;
import android.location.LocationListener;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static ektara.com.sensebattery.BatteryConstants.APP_NAME;

/**
 * Created by mohoque on 07/01/2017.
 */

public class GnssFuckListner extends GnssStatus.Callback{

        private volatile boolean mGpsStatusReceived;
        private GnssStatus mGnssStatus = null;
        // Timeout in sec for count down latch wait
        private static final int TIMEOUT_IN_SEC = 90;
       // private final CountDownLatch mCountDownLatch;
        // Store list of Prn for Satellites.
        private List<List<Integer>> mGpsSatellitePrns;

        public GnssFuckListner() {
            //mCountDownLatch = new CountDownLatch(gpsStatusCountToCollect);
            //mGpsSatellitePrns = new ArrayList<List<Integer>>();
        }
        @Override
        public void onStarted() {
            Log.d(APP_NAME,"Gnss started");
        }
        @Override
        public void onStopped() {
            Log.d(APP_NAME,"Gnss stopped");

        }
        @Override
        public void onFirstFix(int ttffMillis) {
            Log.d(APP_NAME, "GNSS "+ttffMillis);
        }
        @Override
        public void onSatelliteStatusChanged(GnssStatus status) {
            //mCountDownLatch.countDown();
            Log.d(APP_NAME,"Gnss "+status.toString());
        }
        /**
         * Returns the list of PRNs (pseudo-random number) for the satellite.
         *
         * @return list of PRNs number
         */
        public List<List<Integer>> getGpsSatellitePrns() {
            return mGpsSatellitePrns;
        }
        /**
         * Check if GPS Status is received.
         *
         * @return {@code true} if the GPS Status is received and {@code false}
         *         if GPS Status is not received.
         */
        public boolean isGpsStatusReceived() {
            return mGpsStatusReceived;
        }
        /**
         * Get GPS Status.
         *
         * @return mGpsStatus GPS Status
         */
        public GnssStatus getGnssStatus() {
            return mGnssStatus;
        }
    }
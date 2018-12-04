package ektara.com.sensebattery;

import android.Manifest;
import android.animation.Animator;
import android.app.AppOpsManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewAnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;


import butterknife.BindView;
import butterknife.OnClick;

import static android.R.id.content;
import static ektara.com.sensebattery.BatteryConstants.*;
import static ektara.com.sensebattery.R.*;
import static ektara.com.sensebattery.R.attr.progressBarPadding;
import static ektara.com.sensebattery.R.attr.title;

public class BatteryActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.

    static final String TAG = "BatterySense";

    private BatteryService batteryService;
    private LocationManager manager;
    private ProgressBar lossProgrss;
    private ProgressBar nibbleBar;
    private int lossRange = 10;
    private Intent notificationIntent;



    MeasurementItemDecoration decorator;
    private List<MeasurementItem> itemList;
    private MeasurementItemAdapter adapter;
    Toolbar toolbar;
    RecyclerView recycler;
    LinearLayout mainLayout;
    LinearLayout revealLayout;
    FloatingActionButton fab;
    private AdView mAdView;

    Boolean dataSetChanged = false;
    float pixelDensity;
    boolean flag = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);




        MobileAds.initialize(getApplicationContext(), "ca-app-pub-6977025418281364~4324780335");
        mAdView = (AdView) findViewById(R.id.ad_view);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();

        // Start loading the ad in the background.
        mAdView.loadAd(adRequest);


        pixelDensity = getResources().getDisplayMetrics().density;
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        recycler = (RecyclerView) findViewById(R.id.recycler);
        mainLayout = (LinearLayout) findViewById(id.main_layout);
        revealLayout = (LinearLayout) findViewById(id.reveal_layout);
        fab = (FloatingActionButton) findViewById(id.fab);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            toolbar.setTitle("BatterySense");
        }

        itemList = new ArrayList<>();
        prepareMeasurement();

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        decorator = new MeasurementItemDecoration(2, dpToPx(10), true);

        recycler.setLayoutManager(mLayoutManager);
        recycler.addItemDecoration(decorator);
        recycler.setItemAnimator(new DefaultItemAnimator());


        /*
        adapter = new MeasurementItemAdapter(this, itemList);
        recycler.setAdapter(adapter);
        */



        if (this.batteryService == null) {
            // start service
            Intent i = new Intent(this, BatteryService.class);
            startService(i);
        }

        notificationIntent = new Intent(this, BatteryActivity.class);
        manager = (LocationManager) this.getSystemService( Context.LOCATION_SERVICE );
        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                revealLayout();
            }
        });

        this.drawBatteryandButtons();

        lossProgrss = (ProgressBar) findViewById(id.lossBar);
       // nibbleBar = (ProgressBar) findViewById(id.nibblebar);

        Button capButton = (Button)findViewById(R.id.button);
        capButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Integer blevel = MessageQueue.getQueueInstance().pop(MSG_BATTERY_LEVEL);
                IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                Intent batteryStatus = getApplicationContext().registerReceiver(null, ifilter);
                int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL;

                int currLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int maxLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                double percentage =  Math.round((currLevel * 100.0) / maxLevel);

                String msg = "";
                if(!isAirplaneModeOn(getApplicationContext())){
                    msg += "Put the device in airplane mode.";
                }
                if(percentage >20.0){
                    msg += "\nBattery level must be less than 20%.";

                }
                if(!isCharging){
                    msg += "\nThe device must be charging.";

                }

                if(msg.length()>0)
                    Toast.makeText(BatteryActivity.this, msg, Toast.LENGTH_LONG).show();
                else{
                    // Start the service
                }
            }
        });


        String android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        String deviceId = md5(android_id).toUpperCase();

        UsageStatsManager usage = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
        long time = System.currentTimeMillis();
        List<UsageStats> stats = usage.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000*1000, time);
        if (stats.size()==0) {

                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                this.startActivity(intent);

        }

    }
    /**
     * preparing dummy data
     */

    public static final String md5(final String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            Log.d(TAG,e.toString());
        }
        return "";
    }
    private void prepareMeasurement() {


        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = getApplicationContext().registerReceiver(null, ifilter);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        int currLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int maxLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        double percentage =  Math.round((currLevel * 100.0) / maxLevel);

        long locVoltage = batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
        double fineVoltage = 0.0f;
        if(locVoltage > 1000)
            fineVoltage = locVoltage/1000.0;

        if(locVoltage > 1000000){
            fineVoltage = locVoltage/1000000.0;
            Log.d(APP_NAME,"activity "+fineVoltage);
        }



        int locTemp = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
        double fineTemp =0.0;
        if(locTemp>100) {
            fineTemp = locTemp / 10.0;
            Log.d(APP_NAME, "activity "+fineTemp);
        }
        int health = batteryStatus.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
        String bhealth = "";
        switch(health){
            case BatteryManager.BATTERY_HEALTH_COLD: bhealth="Cold"; break;
            case BatteryManager.BATTERY_HEALTH_GOOD: bhealth="Good"; break;
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE: bhealth="Over Voltage"; break;
            case BatteryManager.BATTERY_HEALTH_OVERHEAT: bhealth="Over Heat"; break;
            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE: bhealth="Unspecified Failure"; break;
            case BatteryManager.BATTERY_HEALTH_DEAD: bhealth="Dead"; break;
            case BatteryManager.BATTERY_HEALTH_UNKNOWN: bhealth="Unknown"; break;



        }

        if(itemList.size()>0){
            itemList.clear();
        }

        MeasurementItem item = null;
        this.dataSetChanged = false;
        item = new MeasurementItem("Health", bhealth, "Status");
        itemList.add(item);

        String powerStatus="";
        if(isCharging)
            powerStatus = "Charging";
        else
            powerStatus = "Discharging";

        item = new MeasurementItem("Level", (int)percentage+"%", powerStatus);
        itemList.add(item);

        item = new MeasurementItem("Temp", fineTemp+"C", "Battery Temperature");
        itemList.add(item);

        item = new MeasurementItem("Voltage", new DecimalFormat("#.###").format(fineVoltage)+"V", "Battery Voltage");
        itemList.add(item);


        adapter = new MeasurementItemAdapter(this, itemList);
        recycler.setAdapter(adapter);

        //adapter.notifyDataSetChanged();
        /*
        item = new MeasurementItem("Model", "SM-G920F", "device model");
        itemList.add(item);

        item = new MeasurementItem("Version", "6.0.1", "Android Version");
        itemList.add(item);

        item = new MeasurementItem("Voltage", "3.8 V", "voltage");
        itemList.add(item);

        item = new MeasurementItem("Temp", "28.2 C", "Device temperature");
        itemList.add(item);

        item = new MeasurementItem("Model", "SM-G920F", "device model");
        itemList.add(item);
          */
        //item = new MeasurementItem("Version", "6.0.1", "Android Version");
        //itemList.add(item);
    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    @OnClick(R.id.fab)
    public void revealLayout() {

        int x = mainLayout.getRight();
        int y = mainLayout.getTop();
        x -= ((28 * pixelDensity) + (16 * pixelDensity));

        int hypotenuse = (int) Math.hypot(mainLayout.getWidth(), mainLayout.getHeight());

        if (flag) {

            fab.setBackgroundResource(R.drawable.ic_clear_black_24dp);
            fab.setImageResource(R.drawable.ic_clear_black_24dp);

            FrameLayout.LayoutParams parameters = (FrameLayout.LayoutParams)
                    revealLayout.getLayoutParams();
            parameters.height = mainLayout.getHeight();
            revealLayout.setLayoutParams(parameters);

            Animator anim = ViewAnimationUtils.createCircularReveal(revealLayout, x, y, 0, hypotenuse);
            anim.setDuration(700);

            anim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    mainLayout.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    //TODO display charts
                    LineChart lineChart = (LineChart) findViewById(R.id.vchart);


                    ArrayList<Entry> entries = new ArrayList<>();
                    entries.add(new Entry(0,4f));
                    entries.add(new Entry(1,8f));
                    entries.add(new Entry(2,6f));
                    entries.add(new Entry(3,2f));
                    entries.add(new Entry(4,18f));
                    entries.add(new Entry(5,9f));




                    LineDataSet set1 = new LineDataSet(entries, "Battery Level");
                    set1.setColors(ColorTemplate.COLORFUL_COLORS); //
                    set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                    set1.setDrawFilled(true);


                    LineData data = new LineData(set1);

                    lineChart.setData(data);
                    lineChart.animateY(2000);



                    LineChart vChart = (LineChart) findViewById(R.id.cchart);
                    LineDataSet set2 = new LineDataSet(entries,"Battery Level");
                    set2.setColors(ColorTemplate.COLORFUL_COLORS); //
                    set2.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                    set2.setDrawFilled(true);
                    LineData data2 = new LineData(set2);
                    vChart.setData(data2);
                    vChart.animateY(2000);


                    LineChart TChart = (LineChart) findViewById(R.id.tchart);
                    LineDataSet set3 = new LineDataSet(entries, "Battery Level");
                    set3.setColors(ColorTemplate.COLORFUL_COLORS); //
                    set3.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                    set3.setDrawFilled(true);
                    LineData data3 = new LineData(set3);
                    TChart.setData(data2);
                    TChart.animateY(2000);



                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });

            revealLayout.setVisibility(View.VISIBLE);
            anim.start();

            flag = false;
        } else {

            fab.setBackgroundResource(R.drawable.ic_battery_charging_full_black_24dp);
            fab.setImageResource(R.drawable.ic_battery_charging_full_black_24dp);

            Animator anim = ViewAnimationUtils.createCircularReveal(revealLayout, x, y, hypotenuse, 0);
            anim.setDuration(400);

            anim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    revealLayout.setVisibility(View.GONE);
                    mainLayout.setVisibility(View.VISIBLE);
                    // TODO hide charts
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });

            anim.start();
            flag = true;
        }
    }

    private void drawBatteryandButtons(){


        //RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(380, 20);
        //params.leftMargin=50;
        //params.topMargin = 85;
        lossProgrss = (ProgressBar) findViewById(id.lossProgress);
        //lossProgrss.setLayoutParams(params);
        lossProgrss.setScaleY(12f);
        lossProgrss.setScaleX(1.2f);
        //lossProgrss.setProgress(5);
        //lossProgrss.setBackgroundColor(Color.parseColor("#5ec639"));
        //lossProgrss.getProgressDrawable().setColorFilter(Color.parseColor("#5ec639"), PorterDuff.Mode.SRC_IN);
        //CapacityLossProgress anim = new CapacityLossProgress(lossProgrss, 20, 100);
        //anim.setDuration(5000);
        //anim.setBackgroundColor(Color.BLACK);
        //lossProgrss.startAnimation(anim);

        lossProgrss.getProgressDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
        CapacityLossProgress anim = new CapacityLossProgress(lossProgrss, 1, 100);
        anim.setDuration(1000);
        lossProgrss.startAnimation(anim);

        /*
        nibbleBar = (ProgressBar) findViewById(R.id.nibblebar);
        //params.leftMargin = 300;
        //params.topMargin = 85;
        //nibbleBar.setLayoutParams(params);
        nibbleBar.setScaleY(6f);
        nibbleBar.setScaleX(0.2f);
        anim = new CapacityLossProgress(nibbleBar, 100, 100);
        nibbleBar.getProgressDrawable().setColorFilter(Color.parseColor("#5ec639"), PorterDuff.Mode.SRC_IN);
        nibbleBar.startAnimation(anim);





        final Button desing = (Button)findViewById(id.design);
        //params = new RelativeLayout.LayoutParams(10,10);
        //params.leftMargin = 320;
        //params.topMargin = 100;
        //desing.setLayoutParams(params);

        final Button estimate = (Button)findViewById(id.update);
        //params = new RelativeLayout.LayoutParams(100, 10);
        //params.leftMargin = 360;
        //params.topMargin = 150;
        //estimate.setLayoutParams(params);
        */

    }
    private static boolean isAirplaneModeOn(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }



    private void fillStats() {
        if (hasPermission()){
            //getStats();
        }else{
            requestPermission();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<String, Integer>();
                // Initial
                perms.put(Manifest.permission.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_PHONE_STATE, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                // Check for ACCESS_FINE_LOCATION

                if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) == PackageManager.PERMISSION_GRANTED){

                    if(Build.VERSION.SDK_INT==23){
                        manager.addGpsStatusListener(new GpsStatus.Listener() {
                            @Override
                            public void onGpsStatusChanged(int event) {
                                switch (event) {
                                    case GpsStatus.GPS_EVENT_STARTED:
                                        MessageQueue.getQueueInstance().push(MOB_GPS_STATUS_ON,true);
                                        Log.d(APP_NAME, "GPS started ");
                                        break;
                                    case GpsStatus.GPS_EVENT_STOPPED:
                                        MessageQueue.getQueueInstance().push(MOB_GPS_STATUS_ON,false);
                                        Log.d(APP_NAME, "GPS stopped ");
                                        break;
                                }
                            }
                        });

                    }

                    MessageQueue.getQueueInstance().push(MY_PERMISSIONS_REQUEST_COARSE_LOCATION_STATE,true);
                    Log.d(APP_NAME, "ACCESS_FINE_LOCATION premission granted");

                }else{
                    MessageQueue.getQueueInstance().push(MY_PERMISSIONS_REQUEST_COARSE_LOCATION_STATE,false);
                }


                if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE ) == PackageManager.PERMISSION_GRANTED){
                    MessageQueue.getQueueInstance().push(MY_PERMISSIONS_REQUEST_WRITE_DISK_STATE,true);
                    Log.d(APP_NAME, "ACCESS_FINE_LOCATION premission granted");

                }else{
                    MessageQueue.getQueueInstance().push(MY_PERMISSIONS_REQUEST_WRITE_DISK_STATE,false);
                }

                if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE ) == PackageManager.PERMISSION_GRANTED){
                    MessageQueue.getQueueInstance().push(MY_PERMISSIONS_REQUEST_READ_PHONE_STATE,true);
                    Log.d(APP_NAME, "ACCESS_FINE_LOCATION premission granted");

                }else{
                    MessageQueue.getQueueInstance().push(MY_PERMISSIONS_REQUEST_READ_PHONE_STATE,false);
                }

            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(BatteryActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }


    private void requestAllPersmissions() {

        List<String> permissionsNeeded = new ArrayList<String>();
        final List<String> permissionsList = new ArrayList<String>();
        if (!addPermission(permissionsList, Manifest.permission.ACCESS_COARSE_LOCATION))
            permissionsNeeded.add("GPS Fine");
        if (!addPermission(permissionsList, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            permissionsNeeded.add("Write External");
        if (!addPermission(permissionsList, Manifest.permission.READ_PHONE_STATE))
            permissionsNeeded.add("Read Phone State");

        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {
                // Need Rationale
                String message = "You need to grant access to the permission requests for functioning " + permissionsNeeded.get(0);
                for (int i = 1; i < permissionsNeeded.size(); i++)
                    message = message + ", " + permissionsNeeded.get(i);
                    showMessageOKCancel(message,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                            }
                        });
                return;
            }
            requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            return;
        }

        //insertDummyContact();
    }

    private boolean addPermission(List<String> permissionsList, String permission) {
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!shouldShowRequestPermissionRationale(permission))
                return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("MainActivity", "resultCode " + resultCode);
        switch (requestCode){
            case MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS:
                fillStats();
                break;
        }
    }


    private void requestPermission() {
        Toast.makeText(this, "Need to request permission", Toast.LENGTH_SHORT).show();
        startActivityForResult(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS);
    }

    private boolean hasPermission() {
        AppOpsManager appOps = (AppOpsManager)
                getSystemService(Context.APP_OPS_SERVICE);

        int mode = 0;
        if (Build.VERSION.SDK_INT>=21)
            mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), getPackageName());
        else
            mode = appOps.checkOpNoThrow("android:get_usage_stats", android.os.Process.myUid(), getPackageName());

        return mode == AppOpsManager.MODE_ALLOWED;
    }


    /* We are updating UI inside the broadcast receiver. */

    private BroadcastReceiver BReceiver = new BroadcastReceiver(){
        private void showNotification(Context context){

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                    notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_battery_alert_black_48dp)
                    .setContentTitle("Battery Voltage ")
                    .setContentText("Battery Temperature")
                    .setContentIntent(pendingIntent);

            Notification notification = mBuilder.build();
            notification.defaults |= Notification.DEFAULT_VIBRATE;
            notification.defaults |= Notification.DEFAULT_SOUND;

            // cancel notification after click
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            // show scrolling text on status bar when notification arrives
            notification.tickerText = title + "\n" + content;

            // notifiy the notification using NotificationManager
            NotificationManager notificationManager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(VOLT_NOTIFICATION_ID, notification);

        }

        @Override
        public void onReceive(Context context, Intent intent) {


            /*
            if (intent.getExtras().toString().contains(FCC_NOW)){


                long id = intent.getLongExtra(FCC_NOW,0);
                TextView six = (TextView)findViewById(R.id.current);
                six.setText(Long.toString(id));
            }

            if (intent.getExtras().toString().contains(FCC_NEW)){

                long id = intent.getLongExtra(FCC_NEW,0);
                TextView six = (TextView)findViewById(R.id.newcap);
                six.setText(Long.toString(id)+"mAh");
            }
            if (intent.getExtras().toString().contains(CURRENT_NOW)){


                long id = intent.getLongExtra(CURRENT_NOW,0);
                TextView six = (TextView)findViewById(R.id.current);
                six.setText(Long.toString(id));
            }
            if (intent.getExtras().toString().contains(SOC_NOW)){
                long id = intent.getLongExtra(SOC_NOW,0);
            }

            if (intent.getExtras().toString().contains(VOLTAGE_NOW)){
                long id = intent.getLongExtra(VOLTAGE_NOW,0);
                TextView six = (TextView)findViewById(R.id.voltage);

                six.setText(Long.toString(id));
            }

            if (intent.getExtras().toString().contains(TEMP_NOW)){
                long id = intent.getLongExtra(TEMP_NOW,0);
                TextView six = (TextView)findViewById(R.id.temperature);

                six.setText(Long.toString(id));
            }
            */
            if (intent.getExtras().toString().contains(STATUS_NOW)){

                if(nibbleBar==null) {
                    final ProgressBar butt = (ProgressBar) findViewById(id.lossProgress);
                    int size = butt.getWidth();
                    int topMargin = butt.getHeight()-30/2;
                    int[] array = new int[2];
                    butt.getLocationOnScreen(array);
                    Log.d(APP_NAME,array[0]+" topmargin"+topMargin);



                    //RelativeLayout cradLayout = (RelativeLayout) findViewById(R.id.card_layout);
                    nibbleBar = (ProgressBar)findViewById(id.nibblebar);
                    array = new int[2];
                    nibbleBar.getLocationOnScreen(array);

                    nibbleBar.setScaleY(6f);
                    //nibbleBar.setScaleX(0.2f);

                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(30,30);
                    params.topMargin = topMargin;
                    params.leftMargin = size;
                    nibbleBar.setLayoutParams(params);


                    array = new int[2];
                    nibbleBar.getLocationOnScreen(array);
                    Log.d(APP_NAME,"Scale"+array[0]+" "+array[1]);
                    CapacityLossProgress anim = new CapacityLossProgress(nibbleBar, 100, 100);
                    nibbleBar.getProgressDrawable().setColorFilter(Color.parseColor("#5ec639"), PorterDuff.Mode.SRC_IN);
                    nibbleBar.startAnimation(anim);

                    //cradLayout.addView(nibbleBar);
                }




                long id = intent.getLongExtra(STATUS_NOW,0);



                if(id == 1){

                    if (nibbleBar.getVisibility() == View.VISIBLE){
                        nibbleBar.setVisibility(View.INVISIBLE);
                    }
                    else
                        nibbleBar.setVisibility(View.VISIBLE);



                }
                else{

                    nibbleBar.setVisibility(View.VISIBLE);

                    //showNotification(context);
                }
            }

        }
    };





    protected void onResume(){
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(BReceiver, new IntentFilter("Battery"));
    }

    protected void onPause (){
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(BReceiver);
    }




/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}

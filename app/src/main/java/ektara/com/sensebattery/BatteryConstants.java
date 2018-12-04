package ektara.com.sensebattery;

/**
 * Created by mohoque on 17/12/2016.
 */

public final class BatteryConstants {

    public static final String FCC_NEW = "fccnew";
    public static final String FCC_NOW = "fccnow";
    public static final String SOC_NOW = "socnow";
    public static final String APP_NAME = "BatterySense";
    public static final String CURRENT_NOW = "currentnow";
    public static final String STATUS_NOW = "powernow";
    public static final String VOLTAGE_NOW = "voltagenow";
    public static final String TEMP_NOW = "celciusnow";

    public static final int MAX_NONZEROBINS_APPS = 3600;
    public static final int MSG_BATTERY_LEVEL = 0x0001;
    public static final int MSG_POWER_STATUS = 0x0002;
    public static final int MSG_CAMERA0_STATUS = 0x0003; // Back camera
    public static final int MSG_CAMERA1_STATUS = 0x0003; // Back camera
    public static final int MSG_SCREEN_STATUS = 0x0004;
    public static final int MSG_CHARGING_CURRENT = 0x0005;

    public static  final int MY_PERMISSIONS_REQUEST_FINE_LOCATION_STATE= 0x0006;
    public static  final int MY_PERMISSIONS_REQUEST_COARSE_LOCATION_STATE = 0x0007;
    public static  final int MY_PERMISSIONS_REQUEST_WRITE_DISK_STATE = 0x0008;
    public static  final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 0x0009;

    public static final int MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS = 0x000a;
    public static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 0x000b;

    public static final int USER_CONTEXT_VIDEOCONF = 0x0101;
    public static final int USER_CONTEXT_AUDIOCONF = 0x0102;
    public static final int USER_CONTEXT_AUDIOCALL = 0x0103;
    public static final int USER_CONTEXT_LOCALMUSIC = 0x0104;
    public static final int USER_CONTEXT_REMOTEMUSIC = 0x0105;

    public static final int INSTALLED_UID_PACKAGES = 0x111;
    public static final int INSTALLED_UID_APPNAMES = 0x112;
    public static final int WIFI_SIGNAL_STRENGTH = 0x0113;
    public static final int MOB_SIGNAL_STRENGTH = 0x0114;
    public static final int MOB_GPS_STATUS_ON = 0x0115;
    public static final int MOB_GPS_STATUS_OFF = 0x0116;

    public static final int MSG_BATTERY_TEMP = 0x000c;
    public static final int MSG_BATTERY_VOLT = 0x000d;

    public static final int VOLT_NOTIFICATION_ID = 0x117;
    public static final int CAPA_NOTIFICATION_ID = 0x118;
    public static final int TEMP_NOTIFICATION_ID = 0x118;

    //public static final int PHONE_POWER_STATUS = 0x0115;
    //public static final int PHONE_BATTERY_LEVEL = 0x0116;

}


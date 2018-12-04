package ektara.com.sensebattery;

import android.content.Context;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import static ektara.com.sensebattery.BatteryConstants.*;

/**
 * Created by mohoque on 31/12/2016.
 */

public class CameraAudioInformation {

    // initialize these services here
    private AudioManager audioManager;
    private CameraManager cameraManager;
    private int unavaableCamera = -1;
    private int usersAudioContext = -1;
    private int usersMotionContext = -1;
    private int usersVideoContext = -1;
    private boolean camera0available = true;
    private boolean camera1available = true;
    private Context locontext;

    public CameraAudioInformation(Context context){

        this.locontext = context;
        audioManager = (AudioManager) this.locontext.getSystemService(Context.AUDIO_SERVICE);
        cameraManager = (CameraManager) this.locontext.getSystemService(Context.CAMERA_SERVICE);
        registerCameraService();
    }

    private void registerCameraService(){


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cameraManager.registerAvailabilityCallback(new CameraManager.AvailabilityCallback() {

                private Bundle bundle = new Bundle();// cameraZeroAvailable = true;

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
        }

    }

    public int unaavailableCamera(){

        if(!camera0available)
            unavaableCamera = 0;
        if (!camera1available)
            unavaableCamera = 1;
        if((camera0available)&&(camera1available))
            unavaableCamera = -1;
        return this.unavaableCamera;
    }

    public boolean getAudioStatus(){
        boolean audioFlag = false;
       // boolean speakerOn = false;
        //boolean cam0avail = true;
        //boolean cam1avail = true;

        boolean isMusic = audioManager.isMusicActive();
        int audioMode = audioManager.getMode();
        if(isMusic || audioMode == AudioManager.MODE_IN_CALL|| audioMode == AudioManager.MODE_IN_COMMUNICATION){
            //speakerOn = audioManager.isSpeakerphoneOn();
            audioFlag = true;
            //audioVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            //Log.d(APP_NAME, "AudioFlag "+audioFlag +" "+speakerOn+" "+audioMode+" "+audioVolume+" "+AudioManager.MODE_CURRENT);
        }



        return audioFlag;
    }

    public int getConferenceContext(){

        boolean audioFlag = false;
        boolean speakerOn = false;
        boolean cam0avail = true;
        boolean cam1avail = true;
        int audioVolume = 0;

        boolean isMusic = audioManager.isMusicActive();
        int audioMode = audioManager.getMode();
        if(isMusic || audioMode == AudioManager.MODE_IN_CALL|| audioMode == AudioManager.MODE_IN_COMMUNICATION){
            speakerOn = audioManager.isSpeakerphoneOn();
            audioFlag = true;
            audioVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            Log.d(APP_NAME, "AudioFlag "+audioFlag +" "+speakerOn+" "+audioMode+" "+audioVolume+" "+AudioManager.MODE_CURRENT);
        }

        Object obj = MessageQueue.getQueueInstance().pop(MSG_CAMERA0_STATUS);
        if(obj!=null){
            camera0available = (boolean)obj;
        }

        obj = MessageQueue.getQueueInstance().pop(MSG_CAMERA1_STATUS);
        if(obj!=null){
            camera1available = (boolean)obj;
        }

                    /* User context when the phone is backgorund */
        if((audioFlag) && (unaavailableCamera()>=0) && (audioMode == AudioManager.MODE_IN_COMMUNICATION)){
            Log.d(APP_NAME, "Video Conference Over IP");
            return USER_CONTEXT_VIDEOCONF;
        }
        if((audioFlag)&&(audioMode == AudioManager.MODE_IN_COMMUNICATION)&&(unaavailableCamera()<0))
        {
            Log.d(APP_NAME,"Audio Conference AuIP");
            return USER_CONTEXT_AUDIOCONF;
        }

        if((audioFlag)&&(audioMode == AudioManager.MODE_IN_CALL))
        {
            Log.d(APP_NAME,"Voic Call VoGSM");
            return USER_CONTEXT_AUDIOCALL;
        }

        return 0;
    }


    public int getUsersBasicMotionContext(){

        return 0;
    }

}

package com.mixedtek.elastics.player;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.CheckBox;
import android.widget.Toast;

import com.mixedtek.elastics.player.utils.Preferences;

public class SettupActivity extends AppCompatActivity {

    private static final String TAG = "SettupActivity";

    private TextView mAddress;
    private TextView mPortNumber;
    private TextView mVideoBufferCapacity;
    private TextView mVideoBufferSize;
    private TextView mAudioBufferCapacity;
    private TextView mAudioBufferSize;
    private CheckBox mAudioEnable;
    private CheckBox mGyroEnable;
    private CheckBox mOnDemandConnection;
    private TextView mAppID;
    private CheckBox mControllerEnable;

    private ImageButton mSave;
    private ImageButton mCancel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settup);
        mAddress = (TextView) findViewById(R.id.server_address);
        mPortNumber = (TextView) findViewById(R.id.server_port);
        mVideoBufferCapacity = (TextView) findViewById(R.id.video_buffer_capacity);
        mVideoBufferSize = (TextView) findViewById(R.id.video_buffer_size);
        mAudioBufferCapacity = (TextView) findViewById(R.id.audio_buffer_capacity);
        mAudioBufferSize = (TextView) findViewById(R.id.audio_buffer_size);
        mAudioEnable = (CheckBox) findViewById(R.id.enable_audio);
        mGyroEnable = (CheckBox) findViewById(R.id.enable_gyro);
        mOnDemandConnection = (CheckBox) findViewById(R.id.ondemand_connection);
        mAppID = (TextView) findViewById(R.id.app_id);
        mControllerEnable = (CheckBox) findViewById(R.id.enable_controller);

        mSave = (ImageButton) findViewById(R.id.save_button);
        mCancel = (ImageButton) findViewById(R.id.cancel_button);

        mSave.setOnClickListener(mOnSaveListener);
        mCancel.setOnClickListener(mOnCancelListener);

        String address = Preferences.getServerAddress();
        if (TextUtils.isEmpty(address)) {
            address = PlayerContext.ServerAddress;
        }
        mAddress.setText(address);
        String portNumber = Preferences.getServerPort();
        if (TextUtils.isEmpty(portNumber)) {
            portNumber = Integer.toString(PlayerContext.ServerPortnumber);
        }
        mPortNumber.setText(portNumber);

        int videoBufferCapacity = Preferences.getVideoBufferCapacity();
        mVideoBufferCapacity.setText(Integer.toString(videoBufferCapacity));
        int videoBufferSize = Preferences.getVideoBufferSize();
        mVideoBufferSize.setText(Integer.toString(videoBufferSize));

        int audioBufferCapacity = Preferences.getAudioBufferCapacity();
        mAudioBufferCapacity.setText(Integer.toString(audioBufferCapacity));
        int audioBufferSize = Preferences.getAudioBufferSize();
        mAudioBufferSize.setText(Integer.toString(audioBufferSize ));

        boolean audioEnabled = Preferences.getAudioEnable();
        mAudioEnable.setChecked(audioEnabled);
        boolean gyroEnabled = Preferences.getGyroEnable();
        mGyroEnable.setChecked(gyroEnabled);

        boolean ondemandConnection = Preferences.getOnDemandConnection();
        mOnDemandConnection.setChecked(ondemandConnection);

        boolean controllerEnabled = Preferences.getControllerEnable();
        mControllerEnable.setChecked(controllerEnabled);

        String app_id = Preferences.getAppID();
        if (TextUtils.isEmpty(app_id)) {
            app_id = PlayerContext.AppID;
        }
        mAppID.setText(app_id);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == PlayerContext.REMOTECON_OK) {
            boolean ret = super.onKeyDown(keyCode, event);
            if(save())
                finish();
            return ret;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    private boolean save() {
        String address = "";
        if(mAddress.getText().length() == 0) {
            Toast.makeText(SettupActivity.this, "IP Address is not valid.", Toast.LENGTH_SHORT).show();
            return false;
        }
        address = mAddress.getText().toString();
        Preferences.setServerAddress(address);
        PlayerContext.ServerAddress = address;

        String  portNumber = "";
        if(mPortNumber.getText().length() == 0) {
            Toast.makeText(SettupActivity.this, "PortNumber is not valid", Toast.LENGTH_SHORT).show();
            return false;
        }
        portNumber = mPortNumber.getText().toString();
        Preferences.setServerPort(portNumber);
        PlayerContext.ServerPortnumber = Integer.parseInt(portNumber);

        int  videoBufferCapacity = 0;
        if(mVideoBufferCapacity.getText().length() == 0) {
            Toast.makeText(SettupActivity.this, "VideoBufferCapacity is not valid", Toast.LENGTH_SHORT).show();
            return false;
        }
        videoBufferCapacity = Integer.parseInt(mVideoBufferCapacity.getText().toString());
        Preferences.setVideoBufferCapacity(videoBufferCapacity);
        PlayerContext.VideoBufferCapacity = videoBufferCapacity;

        int  videoBufferSize = 0;
        if(mVideoBufferSize.getText().length() == 0) {
            Toast.makeText(SettupActivity.this, "VideoBufferSize is not valid", Toast.LENGTH_SHORT).show();
            return false;
        }
        videoBufferSize = Integer.parseInt(mVideoBufferSize.getText().toString());
        Preferences.setVideoBufferSize(videoBufferSize);
        PlayerContext.VideoBufferSize = videoBufferSize * 1024;


        int  audioBufferCapacity = 0;
        if(mAudioBufferCapacity.getText().length() == 0) {
            Toast.makeText(SettupActivity.this, "AudioBufferCapacity is not valid", Toast.LENGTH_SHORT).show();
            return false;
        }
        audioBufferCapacity = Integer.parseInt(mAudioBufferCapacity.getText().toString());
        Preferences.setAudioBufferCapacity(audioBufferCapacity);
        PlayerContext.AudioBufferCapacity = audioBufferCapacity;

        int  audioBufferSize = 0;
        if(mAudioBufferSize.getText().length() == 0) {
            Toast.makeText(SettupActivity.this, "AudioBufferSize is not valid", Toast.LENGTH_SHORT).show();
            return false;
        }
        audioBufferSize = Integer.parseInt(mAudioBufferSize.getText().toString());
        Preferences.setAudioBufferSize(audioBufferSize);
        PlayerContext.AudioBufferSize = audioBufferSize * 1024;;

        Preferences.setAudioEnable(mAudioEnable.isChecked());
        PlayerContext.AudioEnabled = mAudioEnable.isChecked();

        Preferences.setGyroEnable(mGyroEnable.isChecked());
        PlayerContext.GyroEnabled = mGyroEnable.isChecked();

        Preferences.setOnDemandConnection(mOnDemandConnection.isChecked());
        PlayerContext.OnDemandConnection = mOnDemandConnection.isChecked();

        Preferences.setControllerEnable(mControllerEnable.isChecked());
        PlayerContext.ControllerEnabled = mControllerEnable.isChecked();

        String appid = "";
        if(mAppID.getText().length() == 0) {
            Toast.makeText(SettupActivity.this, "App ID is not valid.", Toast.LENGTH_SHORT).show();
            return false;
        }
        appid = mAppID.getText().toString();
        Preferences.setAppID(appid);
        PlayerContext.AppID = appid;

        return true;
    }

    private View.OnClickListener mOnSaveListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(save())
                finish();
        }
    };

    private View.OnClickListener mOnCancelListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };
}

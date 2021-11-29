package com.mixedtek.elastics.player;

import com.mixedtek.elastics.player.PlayerDelegator.OnStateChangeListener;
import com.mixedtek.elastics.player.data.Device;
import com.mixedtek.elastics.player.decoder.MediaCodecHelper;
import com.mixedtek.elastics.player.game.ControllerService;
import com.mixedtek.elastics.player.sensor.MatrixF4x4;
import com.mixedtek.elastics.player.sensor.Quaternion;
import com.mixedtek.elastics.player.utils.Preferences;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private SurfaceView mSurfaceView;
    private Surface mSurface = null;
    private ImageButton mSettupButton;
    private ImageButton mPlayButton;
    private TextView mTitle;

    private Point mScreenSize = new Point();
    private Point mLastTouch = new Point();
    private int mTouchStartDistance = 0;
    private int mTouchEndDistance = 0;
    private boolean mSkipMoveUpEvent = false;

    private PlayerDelegator mPlayerDelegator;

    private SensorManager mSensorManager;
    private Sensor mGyroSensor;
    private Sensor mRotationSensor;
    private SensorEventListener mSensorEventListener;

    private int mVideoDisplayWidth = -1;
    private int mVideoDisplayHeight = -1;
    private int mVideoCodecWidth = -1;
    private int mVideoCodecHeight = -1;


    private ServiceLauncher mServiceLauncher;
    private ServiceConnection mServiceConnection = null;
    private boolean mIsBind = false;
    private ControllerService mControllerService = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_main);

        Device.init(this);
        Preferences.init(this.getApplicationContext());
        MediaCodecHelper.initialize(this.getApplicationContext(), "");

        mSettupButton = (ImageButton)findViewById(R.id.settup_button);
        mPlayButton = (ImageButton)findViewById(R.id.play_button);

        mSurfaceView = (SurfaceView)findViewById(R.id.surface_view);
        mTitle = (TextView)findViewById(R.id.title);

        mSettupButton.setOnClickListener(mOnSettupListener);
        mPlayButton.setOnClickListener(mOnPlayListener);
        mSurfaceView.getHolder().addCallback(mSurfaceCallback);

        System.setProperty("tcp_low_latency", "1"); // tcp low latency로 설정
        setVolumeControlStream(AudioManager.STREAM_MUSIC); // 볼륨키 미디어 볼륨에 적용되도록 설정

        //Preferences ip 설정
        String address = Preferences.getServerAddress();
        if(!TextUtils.isEmpty(address)) {
            PlayerContext.ServerAddress = address;
        }
        String port = Preferences.getServerPort();
        if(!TextUtils.isEmpty(port)) {
            PlayerContext.ServerPortnumber = Integer.parseInt(port);
        }
        PlayerContext.VideoBufferCapacity = Preferences.getVideoBufferCapacity();
        PlayerContext.VideoBufferSize = Preferences.getVideoBufferSize() * 1024;
        PlayerContext.AudioBufferCapacity = Preferences.getAudioBufferCapacity();
        PlayerContext.AudioBufferSize = Preferences.getAudioBufferSize() * 1024;
        PlayerContext.AudioEnabled = Preferences.getAudioEnable();
        PlayerContext.GyroEnabled = Preferences.getGyroEnable();
        PlayerContext.OnDemandConnection = Preferences.getOnDemandConnection();
        String appid = Preferences.getAppID();
        if(!TextUtils.isEmpty(appid)) {
            PlayerContext.AppID = appid;
        }
        PlayerContext.ControllerEnabled = Preferences.getControllerEnable();

        android.view.Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(mScreenSize);
        // gyro sensor
        if(PlayerContext.GyroEnabled) {
            mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            mGyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            mRotationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            mSensorEventListener =  new SensorEventListener() {
                static final private int   PANIC_THRESHOLD = 60;
                static final private float NS2S = 1.0f / 1000000000.0f;
                static final private float EPSILON = 0.000000001f;
                static final private float OUTLIER_THRESHOLD = 0.85f;
                static final private float OUTLIER_PANIC_THRESHOLD = 0.75f;
                static final private float INDIRECT_INTERPOLATION_WEIGHT = 0.01f;

                private long timestamp;
                private int panicCounter = 0;
                private double gyroscopeRotationVelocity = 0;
                private final float[] temporaryQuaternion = new float[4];
                private boolean positionInitialised = false;

                private Quaternion quaternionRotationVector = new Quaternion();
                private Quaternion quaternionGyroscope = new Quaternion();

                private final Quaternion interpolatedQuaternion = new Quaternion();
                private final Quaternion deltaQuaternion = new Quaternion();

                private final MatrixF4x4 currentOrientationRotationMatrix = new MatrixF4x4();
                private final Quaternion currentOrientationQuaternion = new Quaternion();

                private final Quaternion correctedQuaternion = new Quaternion();

                @Override
                public void onSensorChanged(SensorEvent event) {

                    switch (event.sensor.getType()) {
                        case Sensor.TYPE_ROTATION_VECTOR:

                            //쿼터니언 비슷한 값인 벡터를 쿼터니언으로 변경.. .
                            SensorManager.getQuaternionFromVector(temporaryQuaternion, event.values);	//벡터값 w

                            quaternionRotationVector.setXYZW(temporaryQuaternion[1], temporaryQuaternion[2], temporaryQuaternion[3], -temporaryQuaternion[0]);

                            if (!positionInitialised) {

                                quaternionGyroscope.set(quaternionRotationVector);
                                positionInitialised = true;
                            }
                            break;
                        case Sensor.TYPE_GYROSCOPE:
                            if (timestamp != 0) {
                                final float dT = (event.timestamp - timestamp) * NS2S;
                                // 이벤트 값을 받음.
                                float axisX = event.values[0];
                                float axisY = event.values[1];
                                float axisZ = event.values[2];

                                // 정규화를 위한 연산
                                gyroscopeRotationVelocity = Math.sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ);

                                if (gyroscopeRotationVelocity > EPSILON) {
                                    axisX /= gyroscopeRotationVelocity;
                                    axisY /= gyroscopeRotationVelocity;
                                    axisZ /= gyroscopeRotationVelocity;
                                }

                                //DT 동안 회전한 각도를 얻기 위해, 자이로에서 샘플링된 각속도를 적분.
                                double thetaOverTwo = gyroscopeRotationVelocity * dT / 2.0f;
                                double sinThetaOverTwo = Math.sin(thetaOverTwo);
                                double cosThetaOverTwo = Math.cos(thetaOverTwo);
                                deltaQuaternion.setX((float) (sinThetaOverTwo * axisX));
                                deltaQuaternion.setY((float) (sinThetaOverTwo * axisY));
                                deltaQuaternion.setZ((float) (sinThetaOverTwo * axisZ));
                                deltaQuaternion.setW(-(float) cosThetaOverTwo);

                                // Move current gyro orientation
                                deltaQuaternion.multiplyByQuat(quaternionGyroscope, quaternionGyroscope);

                                // (if the dot-product is closer to 0 than to 1), because it should be close to 1 if both are the same.
                                //Rot Vector 의 값과 현재 자이로 움직임의 각도를 계산하기 위해 내적.
                                float dotProd = quaternionGyroscope.dotProduct(quaternionRotationVector);

                                // If they have diverged, rely on gyroscope only (this happens on some devices when the rotation vector "jumps").
                                if (Math.abs(dotProd) < OUTLIER_THRESHOLD) {
                                    // Increase panic counter
                                    if (Math.abs(dotProd) < OUTLIER_PANIC_THRESHOLD) {
                                        panicCounter++;
                                    }

                                    // Directly use Gyro
                                    setOrientationQuaternionAndMatrix(quaternionGyroscope);

                                } else {
                                    //정상적인 센서 융합

                                    //자이로와 Rot vector 보간
                                    quaternionGyroscope.slerp(quaternionRotationVector, interpolatedQuaternion, (float) (INDIRECT_INTERPOLATION_WEIGHT * gyroscopeRotationVelocity));

                                    // Use the interpolated value between gyro and rotationVector
                                    setOrientationQuaternionAndMatrix(interpolatedQuaternion);

                                    quaternionGyroscope.copyVec4(interpolatedQuaternion);

                                    // Reset the panic counter because both sensors are saying the same again
                                    panicCounter = 0;
                                }

                                if (panicCounter > PANIC_THRESHOLD) {
                                    Log.d(TAG, "Panic counter is bigger than threshold; this indicates a Gyroscope failure. Panic reset is imminent.");

                                    if (gyroscopeRotationVelocity < 3) {
                                        Log.d(TAG, "Performing Panic-reset. Resetting orientation to rotation-vector value.");

                                        // Manually set position to whatever rotation vector says.
                                        setOrientationQuaternionAndMatrix(quaternionRotationVector);
                                        // Override current gyroscope-orientation with corrected value
                                        quaternionGyroscope.copyVec4(quaternionRotationVector);

                                        panicCounter = 0;
                                    } else {
                                        Log.d(TAG, String.format( "Panic reset delayed due to ongoing motion (user is still shaking the device). "
                                                + "Gyroscope Velocity: %.2f > 3", gyroscopeRotationVelocity));
                                    }
                                }
                            }
                            timestamp =  event.timestamp;
                            break;
                    }
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                }

                public float Axis[] = new float[3];
                private void setOrientationQuaternionAndMatrix(Quaternion quaternion) {

                    correctedQuaternion.set(quaternion);
                    correctedQuaternion.w(-correctedQuaternion.w());
                    // Use gyro only
                    currentOrientationQuaternion.copyVec4(quaternion);

                    if(correctedQuaternion.getW() >= EPSILON)
                    {
                        SensorManager.getRotationMatrixFromVector(currentOrientationRotationMatrix.matrix, correctedQuaternion.array());
                        SensorManager.getOrientation(currentOrientationRotationMatrix.matrix, Axis);
                        mPlayerDelegator.sendGyroRotEvent(Axis[0], Axis[1], Axis[2],0.f);
                    }
                }
            };
        }


        if(PlayerContext.ControllerEnabled) {
            mServiceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName componentName, IBinder service) {
                    ControllerService.ControllerBinder binder = (ControllerService.ControllerBinder)service;
                    mControllerService = binder.getService();
                    mIsBind = true;

                    mPlayerDelegator = new PlayerDelegator(mControllerService);
                    mPlayerDelegator.setStateChangeListener(mStateChangeListener);
                    mPlayerDelegator.setUpdateResolutionListener(mUpdateResolutionListener);

                    Log.i(TAG, String.format("Binding Game Controller Service is completed"));
                }
                @Override
                public void onServiceDisconnected(ComponentName componentName) {
                    mControllerService = null;
                    mIsBind = false;
                    Log.i(TAG, String.format("Unbinding Game Controller Service is completed"));
                }
            };
            mServiceLauncher = new ServiceLauncher(this);
            mServiceLauncher.execute("launching game controller service");

        } else {
            mPlayerDelegator = new PlayerDelegator();
            mPlayerDelegator.setStateChangeListener(mStateChangeListener);
            mPlayerDelegator.setUpdateResolutionListener(mUpdateResolutionListener);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    @Override
    protected void onStop() {
        mPlayerDelegator.stop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mPlayerDelegator.stop();
        try {
            if(PlayerContext.GyroEnabled) {
                mSensorManager.unregisterListener(mSensorEventListener);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(mServiceConnection !=null) {
            if (mIsBind) {
                Log.i(TAG, String.format("Unbind Game Controller Service"));
                unbindService(mServiceConnection);
                mServiceConnection = null;
            }

            Log.i(TAG, String.format("Stop Game Controller Service"));
            Intent intent = new Intent(getApplicationContext(), com.mixedtek.elastics.player.game.ControllerService.class);
            stopService(intent);
        }
        super.onDestroy();
    }

    private void startPlayer() {
        mSurfaceView.setVisibility(View.VISIBLE);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    private boolean mTwoPointTouch = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(mVideoDisplayWidth < 1 || mVideoDisplayHeight < 1)
            return super.onTouchEvent(event);

        int action = event.getAction();
        int x, y, x2 = 0, y2 = 0;
        int sourceX = (int)(event.getX() * mVideoDisplayWidth / mScreenSize.x); // 원본 영상에 맞게 변환한 x값
        int sourceY = (int)(event.getY() * mVideoDisplayHeight / mScreenSize.y); // 원본 영상에 맞게 변환한 y값

        x = (int)event.getX();
        y = (int)event.getY();
        //Log.i(TAG, String.format("TouchEvent_Noti : %.4f %.4f // %d %d // %d %d", event.getX(),  event.getY(), sourceX , sourceY ,  mScreenSize.x ,  mScreenSize.y));
        //mDebugView.setText(String.format("TouchEvent : %.4f %.4f // %d %d // %d %d", event.getX(),  event.getY(), sourceX , sourceY ,  mScreenSize.x ,  mScreenSize.y));
        if(event.getPointerCount() == 2) {
            x2 = (int)event.getX(1);
            y2 = (int)event.getY(1);

            if(!mTwoPointTouch) {
                mTouchStartDistance = calcDistance(x, y, x2, y2);
            }

            mTwoPointTouch = true;
        } else {
            if(mTwoPointTouch) {
                mSkipMoveUpEvent = true;
            }

            mTwoPointTouch = false;
        }

        if(action == MotionEvent.ACTION_DOWN) {
            if(!mTwoPointTouch) {
                mPlayerDelegator.sendMouseDownEvent(sourceX, sourceY);
                mLastTouch.set(x, y);
            }
        } else if(action == MotionEvent.ACTION_UP) {
            Log.e(TAG, event.toString());
            Log.e(TAG, "skipUpEvent : " + mSkipMoveUpEvent);
            if(!mTwoPointTouch) {
                if(mSkipMoveUpEvent) {
                    mSkipMoveUpEvent = false;
                } else {
                    mPlayerDelegator.sendMouseUpEvent(sourceX, sourceY);
                }
            }
        } else if(action == MotionEvent.ACTION_MOVE) {
            if(mTwoPointTouch) {
                mTouchEndDistance = calcDistance(x, y, x2, y2);
                if(mTouchStartDistance - mTouchEndDistance != 0) {
                    mPlayerDelegator.sendPinchZoomEvent(mTouchEndDistance - mTouchStartDistance);
//					Log.e(TAG, "sendPinchZoomEvent :  " + mTouchStartDistance + " " + mTouchEndDistance);
                    mTouchStartDistance = mTouchEndDistance;
                }
            } else {
                if(!mSkipMoveUpEvent) { // 투 터치중 남는 move값은 무시
                    mPlayerDelegator.sendMouseMoveEvent(sourceX, sourceY);
                }
            }
        }

        return super.onTouchEvent(event);
    };

    private int calcDistance(int x1, int y1, int x2, int y2) {
        if(mVideoDisplayWidth < 1 || mVideoDisplayHeight < 1)
            return 1;
        int cx = (x1 - x2) * mVideoDisplayWidth / mScreenSize.x;
        int cy = (y1 - y2) * mVideoDisplayHeight / mScreenSize.y;
        return (int)(Math.sqrt((double)(cx * cx + cy * cy)));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_F11 || keyCode == KeyEvent.KEYCODE_PROG_RED) { // 옵션 버튼과 적색 버튼,
            startPlayer();
            return true;
        } else if(keyCode == KeyEvent.KEYCODE_F12 ||
                   keyCode == KeyEvent.KEYCODE_PROG_GREEN || keyCode == KeyEvent.KEYCODE_BACK) { // 나가기 버튼과 녹색 버튼
            if(mPlayerDelegator.getState() == PlayerDelegator.STATE_DISCONNECT) {
                finish();
            } else {
                mPlayerDelegator.stop();
            }
            return true;
        } else if((keyCode == KeyEvent.KEYCODE_MENU) || (keyCode == KeyEvent.KEYCODE_PROG_BLUE)) {
            MainActivity.this.startActivity(new Intent(MainActivity.this, SettupActivity.class));
            return true;
        }

        if(keyCode==PlayerContext.REMOTECON_OK) {
            mPlayerDelegator.sendMouseDownEvent(320, 360);
            mPlayerDelegator.sendMouseUpEvent(320, 360);
        }

        if(mPlayerDelegator.sendKeyDownEvent(keyCode))
            return true;

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(mPlayerDelegator.sendKeyUpEvent(keyCode))
            return true;
        return super.onKeyUp(keyCode, event);
    }

    private Callback mSurfaceCallback = new Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mSurface = holder.getSurface();
            mPlayerDelegator.setSurface(mSurface);
            Log.e(TAG, "surfaceCreated");
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            mSurface = holder.getSurface();
            mPlayerDelegator.setSurface(mSurface);

            // 삼성 디바이스는 surface생성후 바로 시작하면 mediacodec에서 오류가 발생함. 1초 delay함
            /*
            try {
                Thread.sleep(100);
            } catch(InterruptedException e) {}
            */
            //SystemClock.sleep(1000);
            mPlayerDelegator.start();
            Log.e(TAG, "surfaceChanged");
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            mPlayerDelegator.stop();

            mPlayButton.setFocusableInTouchMode(true);
            mPlayButton.requestFocus();

            mSurface = null;
            Log.e(TAG, "surfaceDestroyed");
        }
    };

    private OnStateChangeListener mStateChangeListener = new OnStateChangeListener() {
        @Override
        public void onStateChanged(final int state) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(state == PlayerDelegator.STATE_DISCONNECT){
                        mTitle.setVisibility(View.VISIBLE);
                        mSettupButton.setVisibility(View.VISIBLE);
                        mPlayButton.setVisibility(View.VISIBLE);
                        mSurfaceView.setVisibility(View.INVISIBLE);
                        try {
                            if(PlayerContext.GyroEnabled) {
                                mSensorManager.unregisterListener(mSensorEventListener);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        mTitle.setVisibility(View.INVISIBLE);
                        mSettupButton.setVisibility(View.INVISIBLE);
                        mPlayButton.setVisibility(View.INVISIBLE);

                        if(state == PlayerDelegator.STATE_CONTROL_CONNECTED) {
                           if(PlayerContext.GyroEnabled) {
                               mSensorManager.registerListener(mSensorEventListener, mGyroSensor, SensorManager.SENSOR_DELAY_GAME);
                               mSensorManager.registerListener(mSensorEventListener, mRotationSensor, SensorManager.SENSOR_DELAY_GAME);
                           }
                        }
                    }
                }
            });
        }
    };

    private PlayerDelegator.OnUpdateResolutionListener mUpdateResolutionListener = new PlayerDelegator.OnUpdateResolutionListener() {
        @Override
        public void onResolutionUpdated(final int videoDisplayWidth, final int videoDisplayHeight, final int videoCodecWidth, final int videoCodecHeight) {
            mVideoDisplayWidth = videoDisplayWidth;
            mVideoDisplayHeight = videoDisplayHeight;
            mVideoCodecWidth = videoCodecWidth;
            mVideoCodecHeight = videoCodecHeight;
        }
    };

    private OnClickListener mOnSettupListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            MainActivity.this.startActivity(new Intent(MainActivity.this, SettupActivity.class));
        }
    };

    private OnClickListener mOnPlayListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            startPlayer();
            //setSystemUiVisibility();
        }
    };

    public ServiceConnection getServiceConnection() {
        return mServiceConnection;
    }

    public boolean isServiceBind() {
        return mIsBind;
    }
}

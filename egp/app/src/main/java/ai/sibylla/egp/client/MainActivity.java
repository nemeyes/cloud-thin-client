package ai.sibylla.egp.client;

import ai.sibylla.egp.client.ClientDelegator.OnStateChangeListener;
import ai.sibylla.egp.client.data.Device;
import ai.sibylla.egp.client.decoder.MediaCodecHelper;
import ai.sibylla.egp.client.game.ControllerService;
import ai.sibylla.egp.client.sensor.MatrixF4x4;
import ai.sibylla.egp.client.sensor.Quaternion;
import ai.sibylla.egp.client.utils.Preferences;

import android.app.UiModeManager;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.res.Configuration;
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
import android.view.InputDevice;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
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

    private ClientDelegator mPlayerDelegator;

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

    private Button mWKey;
    private Button mAKey;
    private Button mSKey;
    private Button mDKey;
    private Button mAction1Key;
    private Button mAction2Key;
    private Button mAction3Key;
    private Button mAction4Key;

    private boolean mEnableOverlayKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_main);

        Device.init(this);
        Preferences.init(this.getApplicationContext());
        MediaCodecHelper.initialize(this.getApplicationContext(), "");

        UiModeManager uiModeManager = (UiModeManager) getSystemService(UI_MODE_SERVICE);
        if (uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION) {
            mEnableOverlayKey = false;
        } else {
            mEnableOverlayKey = true;
        }

        mSettupButton = (ImageButton)findViewById(R.id.settup_button);
        mPlayButton = (ImageButton)findViewById(R.id.play_button);

        mSurfaceView = (SurfaceView)findViewById(R.id.surface_view);
        mTitle = (TextView)findViewById(R.id.title);

        mWKey = (Button)findViewById(R.id.w);
        mAKey = (Button)findViewById(R.id.a);
        mSKey = (Button)findViewById(R.id.s);
        mDKey = (Button)findViewById(R.id.d);

        mAction1Key = (Button)findViewById(R.id.action1);
        mAction2Key = (Button)findViewById(R.id.action2);
        mAction3Key = (Button)findViewById(R.id.action3);
        mAction4Key = (Button)findViewById(R.id.action4);


        mSettupButton.setOnClickListener(mOnSettupListener);
        mPlayButton.setOnClickListener(mOnPlayListener);
        mSurfaceView.getHolder().addCallback(mSurfaceCallback);

        mWKey.setOnTouchListener(mWKeyListener);
        mAKey.setOnTouchListener(mAKeyListener);
        mSKey.setOnTouchListener(mSKeyListener);
        mDKey.setOnTouchListener(mDKeyListener);
        mAction1Key.setOnTouchListener(mAction1KeyListener);
        mAction2Key.setOnTouchListener(mAction2KeyListener);
        mAction3Key.setOnTouchListener(mAction3KeyListener);
        mAction4Key.setOnTouchListener(mAction4KeyListener);

        mSettupButton.setZ(0.f);
        mPlayButton.setZ(1.f);
        mSurfaceView.setZ(2.f);
        mWKey.setZ(3.f);
        mAKey.setZ(4.f);
        mSKey.setZ(5.f);
        mDKey.setZ(6.f);
        mAction1Key.setZ(7.f);
        mAction2Key.setZ(8.f);
        mAction3Key.setZ(9.f);
        mAction4Key.setZ(10.f);

        mWKey.setVisibility(View.INVISIBLE);
        mAKey.setVisibility(View.INVISIBLE);
        mSKey.setVisibility(View.INVISIBLE);
        mDKey.setVisibility(View.INVISIBLE);
        mAction1Key.setVisibility(View.INVISIBLE);
        mAction2Key.setVisibility(View.INVISIBLE);
        mAction3Key.setVisibility(View.INVISIBLE);
        mAction4Key.setVisibility(View.INVISIBLE);

        System.setProperty("tcp_low_latency", "1"); // tcp low latency로 설정
        setVolumeControlStream(AudioManager.STREAM_MUSIC); // 볼륨키 미디어 볼륨에 적용되도록 설정

        //Preferences ip 설정
        String address = Preferences.getServerAddress();
        if(!TextUtils.isEmpty(address)) {
            ClientContext.ServerAddress = address;
        }
        String port = Preferences.getServerPort();
        if(!TextUtils.isEmpty(port)) {
            ClientContext.ServerPortnumber = Integer.parseInt(port);
        }
        ClientContext.VideoBufferCapacity = Preferences.getVideoBufferCapacity();
        ClientContext.VideoBufferSize = Preferences.getVideoBufferSize() * 1024;
        ClientContext.AudioBufferCapacity = Preferences.getAudioBufferCapacity();
        ClientContext.AudioBufferSize = Preferences.getAudioBufferSize() * 1024;
        ClientContext.AudioEnabled = Preferences.getAudioEnable();
        ClientContext.GyroEnabled = Preferences.getGyroEnable();
        String appid = Preferences.getAppID();
        if(!TextUtils.isEmpty(appid)) {
            ClientContext.AppID = appid;
        }
        ClientContext.ControllerEnabled = Preferences.getControllerEnable();

        android.view.Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(mScreenSize);
        // gyro sensor
        if(ClientContext.GyroEnabled) {
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


        if(ClientContext.ControllerEnabled) {
            mServiceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName componentName, IBinder service) {
                    ControllerService.ControllerBinder binder = (ControllerService.ControllerBinder)service;
                    mControllerService = binder.getService();
                    mIsBind = true;

                    mPlayerDelegator = new ClientDelegator(mControllerService);
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
            mPlayerDelegator = new ClientDelegator();
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
            if(ClientContext.GyroEnabled) {
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
            Intent intent = new Intent(getApplicationContext(), ai.sibylla.egp.client.game.ControllerService.class);
            stopService(intent);
        }
        super.onDestroy();
    }

    /*
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
    */

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_VOLUME_UP) {
            ((AudioManager)getSystemService(AUDIO_SERVICE)).adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
        } else if(keyCode==KeyEvent.KEYCODE_VOLUME_DOWN) {
            ((AudioManager)getSystemService(AUDIO_SERVICE)).adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
        } else {
            if (keyCode == KeyEvent.KEYCODE_F11 || keyCode == KeyEvent.KEYCODE_PROG_RED) { // 옵션 버튼과 적색 버튼,
                //startPlayer();
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_F12 ||
                    keyCode == KeyEvent.KEYCODE_PROG_GREEN || keyCode == KeyEvent.KEYCODE_BACK) { // 나가기 버튼과 녹색 버튼
                if (mPlayerDelegator.getState() == ClientDelegator.STATE_DISCONNECT) {
                    finish();
                } else {
                    mPlayerDelegator.stop();
                }
                return true;
            } else if ((keyCode == KeyEvent.KEYCODE_MENU) || (keyCode == KeyEvent.KEYCODE_PROG_BLUE)) {
                MainActivity.this.startActivity(new Intent(MainActivity.this, SettupActivity.class));
                return true;
            }

            switch(keyCode)
            {
                case KeyEvent.KEYCODE_DPAD_UP :
                    mPlayerDelegator.sendKeyDown(ClientContext.DIK_UP);
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN :
                    mPlayerDelegator.sendKeyDown(ClientContext.DIK_DOWN);
                    break;
                case KeyEvent.KEYCODE_DPAD_LEFT :
                    mPlayerDelegator.sendKeyDown(ClientContext.DIK_LEFT);
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT :
                    mPlayerDelegator.sendKeyDown(ClientContext.DIK_RIGHT);
                    break;
            }
            //return true;
            /*
            if (mPlayerDelegator.sendKeyDown(keyCode)) {
                return true;
            }
            */
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_VOLUME_UP) {
            ((AudioManager)getSystemService(AUDIO_SERVICE)).adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI);
        } else if(keyCode==KeyEvent.KEYCODE_VOLUME_DOWN) {
            ((AudioManager)getSystemService(AUDIO_SERVICE)).adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI);
        } else {

            switch(keyCode)
            {
                case KeyEvent.KEYCODE_DPAD_CENTER :
                case KeyEvent.KEYCODE_A :
                    mPlayerDelegator.sendKeyDown(ClientContext.DIK_Y);
                    try {
                        Thread.sleep(5);
                    } catch(InterruptedException e) {}
                    mPlayerDelegator.sendKeyUp(ClientContext.DIK_Y);
                    break;
                case KeyEvent.KEYCODE_DPAD_UP :
                    mPlayerDelegator.sendKeyUp(ClientContext.DIK_UP);
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN :
                    mPlayerDelegator.sendKeyUp(ClientContext.DIK_DOWN);
                    break;
                case KeyEvent.KEYCODE_DPAD_LEFT :
                    mPlayerDelegator.sendKeyUp(ClientContext.DIK_LEFT);
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT :
                    mPlayerDelegator.sendKeyUp(ClientContext.DIK_RIGHT);
                    break;
                case KeyEvent.KEYCODE_MEDIA_REWIND :
                    mPlayerDelegator.sendKeyDown(ClientContext.DIK_X);
                    try {
                        Thread.sleep(5);
                    } catch(InterruptedException e) {}
                    mPlayerDelegator.sendKeyUp(ClientContext.DIK_X);
                    break;
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE :
                    mPlayerDelegator.sendKeyDown(ClientContext.DIK_A);
                    try {
                        Thread.sleep(5);
                    } catch(InterruptedException e) {}
                    mPlayerDelegator.sendKeyUp(ClientContext.DIK_A);
                    break;
                case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD :
                    mPlayerDelegator.sendKeyDown(ClientContext.DIK_B);
                    try {
                        Thread.sleep(5);
                    } catch(InterruptedException e) {}
                    mPlayerDelegator.sendKeyUp(ClientContext.DIK_B);
                    break;

                /*
                case ClientContext.REMOTECON_OK:
                    mPlayerDelegator.sendKeyUp(ClientContext.DIK_Y);
                    break;
                case ClientContext.REMOTECON_UP:
                    mPlayerDelegator.sendKeyUp(ClientContext.DIK_UP);
                    break;
                case ClientContext.REMOTECON_DOWN:
                    mPlayerDelegator.sendKeyUp(ClientContext.DIK_DOWN);
                    break;
                case ClientContext.REMOTECON_LEFT:
                    mPlayerDelegator.sendKeyUp(ClientContext.DIK_LEFT);
                    break;
                case ClientContext.REMOTECON_RIGHT:
                    mPlayerDelegator.sendKeyUp(ClientContext.DIK_RIGHT);
                    break;
                */
            }

            //return true;
            /*
            if (mPlayerDelegator.sendKeyUp(keyCode)) {
                return true;
            }
            */
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /*
    @Override
    public boolean dispatchGenericMotionEvent(android.view.MotionEvent motionEvent) {

        return false;
    }

    @Override
    public boolean dispatchKeyEvent(android.view.KeyEvent keyEvent) {

        if ((keyEvent.getSource() & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD) {
            switch (keyEvent.getKeyCode()) {
                case KeyEvent.KEYCODE_BUTTON_Y:
                    mPlayerDelegator.sendKeyDown(ClientContext.DIK_Y);
                    mPlayerDelegator.sendKeyUp(ClientContext.DIK_Y);
                    break;
                case KeyEvent.KEYCODE_BUTTON_X:
                    mPlayerDelegator.sendKeyDown(ClientContext.DIK_X);
                    mPlayerDelegator.sendKeyUp(ClientContext.DIK_X);
                    break;
                case KeyEvent.KEYCODE_BUTTON_B:
                    mPlayerDelegator.sendKeyDown(ClientContext.DIK_B);
                    mPlayerDelegator.sendKeyUp(ClientContext.DIK_B);
                    break;
                case KeyEvent.KEYCODE_BUTTON_A:
                    mPlayerDelegator.sendKeyDown(ClientContext.DIK_A);
                    mPlayerDelegator.sendKeyUp(ClientContext.DIK_A);
                    break;
            }
        } else if ((keyEvent.getSource() & InputDevice.SOURCE_KEYBOARD) == InputDevice.SOURCE_KEYBOARD) {
            KeyCharacterMap charmap = KeyCharacterMap.load(keyEvent.getDeviceId());
            //KeyEvent[] events = charmap.get()
        }
        return super.dispatchKeyEvent(keyEvent);
    }
    */

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
                    if(state == ClientDelegator.STATE_DISCONNECT) {

                        try {
                            if(ClientContext.GyroEnabled) {
                                mSensorManager.unregisterListener(mSensorEventListener);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        mTitle.setVisibility(View.VISIBLE);
                        mSettupButton.setVisibility(View.VISIBLE);
                        mPlayButton.setVisibility(View.VISIBLE);

                        mSurfaceView.setVisibility(View.INVISIBLE);

                        if(mEnableOverlayKey) {
                            mWKey.setVisibility(View.INVISIBLE);
                            mAKey.setVisibility(View.INVISIBLE);
                            mSKey.setVisibility(View.INVISIBLE);
                            mDKey.setVisibility(View.INVISIBLE);
                            mAction1Key.setVisibility(View.INVISIBLE);
                            mAction2Key.setVisibility(View.INVISIBLE);
                            mAction3Key.setVisibility(View.INVISIBLE);
                            mAction4Key.setVisibility(View.INVISIBLE);
                        }

                    } else if(state == ClientDelegator.STATE_CONTROL_CONNECTING) {

                        mTitle.setVisibility(View.INVISIBLE);
                        mSettupButton.setVisibility(View.INVISIBLE);
                        mPlayButton.setVisibility(View.INVISIBLE);

                        /*
                        if(mEnableOverlayKey) {
                            mWKey.setVisibility(View.INVISIBLE);
                            mAKey.setVisibility(View.INVISIBLE);
                            mSKey.setVisibility(View.INVISIBLE);
                            mDKey.setVisibility(View.INVISIBLE);
                            mAction1Key.setVisibility(View.INVISIBLE);
                            mAction2Key.setVisibility(View.INVISIBLE);
                            mAction3Key.setVisibility(View.INVISIBLE);
                            mAction4Key.setVisibility(View.INVISIBLE);
                        }
                        */

                    } else if(state == ClientDelegator.STATE_CONTROL_CONNECTED) {

                       if(ClientContext.GyroEnabled) {
                           mSensorManager.registerListener(mSensorEventListener, mGyroSensor, SensorManager.SENSOR_DELAY_GAME);
                           mSensorManager.registerListener(mSensorEventListener, mRotationSensor, SensorManager.SENSOR_DELAY_GAME);
                       }

                        mTitle.setVisibility(View.INVISIBLE);
                        mSettupButton.setVisibility(View.INVISIBLE);
                        mPlayButton.setVisibility(View.INVISIBLE);

                        if(mEnableOverlayKey) {
                            mWKey.setVisibility(View.VISIBLE);
                            mAKey.setVisibility(View.VISIBLE);
                            mSKey.setVisibility(View.VISIBLE);
                            mDKey.setVisibility(View.VISIBLE);
                            mAction1Key.setVisibility(View.VISIBLE);
                            mAction2Key.setVisibility(View.VISIBLE);
                            mAction3Key.setVisibility(View.VISIBLE);
                            mAction4Key.setVisibility(View.VISIBLE);
                        }

                    } else if(state == ClientDelegator.STATE_STREAM_CONNECTING) {

                        mTitle.setVisibility(View.INVISIBLE);
                        mSettupButton.setVisibility(View.INVISIBLE);
                        mPlayButton.setVisibility(View.INVISIBLE);

                        if(mEnableOverlayKey) {
                            mWKey.setVisibility(View.VISIBLE);
                            mAKey.setVisibility(View.VISIBLE);
                            mSKey.setVisibility(View.VISIBLE);
                            mDKey.setVisibility(View.VISIBLE);
                            mAction1Key.setVisibility(View.VISIBLE);
                            mAction2Key.setVisibility(View.VISIBLE);
                            mAction3Key.setVisibility(View.VISIBLE);
                            mAction4Key.setVisibility(View.VISIBLE);
                        }

                    } else if(state == ClientDelegator.STATE_STREAM_PLAYING) {

                        mTitle.setVisibility(View.INVISIBLE);
                        mSettupButton.setVisibility(View.INVISIBLE);
                        mPlayButton.setVisibility(View.INVISIBLE);

                        if(mEnableOverlayKey) {
                            mWKey.setVisibility(View.VISIBLE);
                            mAKey.setVisibility(View.VISIBLE);
                            mSKey.setVisibility(View.VISIBLE);
                            mDKey.setVisibility(View.VISIBLE);
                            mAction1Key.setVisibility(View.VISIBLE);
                            mAction2Key.setVisibility(View.VISIBLE);
                            mAction3Key.setVisibility(View.VISIBLE);
                            mAction4Key.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });
        }
    };

    private ClientDelegator.OnUpdateResolutionListener mUpdateResolutionListener = new ClientDelegator.OnUpdateResolutionListener() {
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
            mSurfaceView.setVisibility(View.VISIBLE);
            if(mEnableOverlayKey) {
                mWKey.setVisibility(View.VISIBLE);
                mAKey.setVisibility(View.VISIBLE);
                mSKey.setVisibility(View.VISIBLE);
                mDKey.setVisibility(View.VISIBLE);
                mAction1Key.setVisibility(View.VISIBLE);
                mAction2Key.setVisibility(View.VISIBLE);
                mAction3Key.setVisibility(View.VISIBLE);
                mAction4Key.setVisibility(View.VISIBLE);
            }
        }
    };

    private View.OnTouchListener mWKeyListener = new View.OnTouchListener() { //0x26
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mPlayerDelegator.sendKeyDown(ClientContext.DIK_UP);
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    mPlayerDelegator.sendKeyUp(ClientContext.DIK_UP);
                    break;
            }
            return false;
        }
    };
    private View.OnTouchListener mAKeyListener = new View.OnTouchListener() { //0x25
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mPlayerDelegator.sendKeyDown(ClientContext.DIK_LEFT);
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    mPlayerDelegator.sendKeyUp(ClientContext.DIK_LEFT);
                    break;
            }
            return false;
        }
    };
    private View.OnTouchListener mSKeyListener = new View.OnTouchListener() { //0x28
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mPlayerDelegator.sendKeyDown(ClientContext.DIK_DOWN);
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    mPlayerDelegator.sendKeyUp(ClientContext.DIK_DOWN);
                    break;
            }
            return false;
        }
    };
    private View.OnTouchListener mDKeyListener = new View.OnTouchListener() { //0x27
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mPlayerDelegator.sendKeyDown(ClientContext.DIK_RIGHT);
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    mPlayerDelegator.sendKeyUp(ClientContext.DIK_RIGHT);
                    break;
            }
            return false;
        }
    };
    private View.OnTouchListener mAction1KeyListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) { //Y
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mPlayerDelegator.sendKeyDown(ClientContext.DIK_Y);
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    mPlayerDelegator.sendKeyUp(ClientContext.DIK_Y);
                    break;
            }
            return false;
        }
    };
    private View.OnTouchListener mAction2KeyListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) { //X
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mPlayerDelegator.sendKeyDown(ClientContext.DIK_X);
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    mPlayerDelegator.sendKeyUp(ClientContext.DIK_X);
                    break;
            }
            return false;
        }
    };
    private View.OnTouchListener mAction3KeyListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) { //B
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mPlayerDelegator.sendKeyDown(ClientContext.DIK_B);
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    mPlayerDelegator.sendKeyUp(ClientContext.DIK_B);
                    break;
            }
            return false;
        }
    };
    private View.OnTouchListener mAction4KeyListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {  //A
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mPlayerDelegator.sendKeyDown(ClientContext.DIK_A);
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    mPlayerDelegator.sendKeyUp(ClientContext.DIK_A);
                    break;
            }
            return false;
        }
    };

    public ServiceConnection getServiceConnection() {
        return mServiceConnection;
    }

    public boolean isServiceBind() {
        return mIsBind;
    }
}

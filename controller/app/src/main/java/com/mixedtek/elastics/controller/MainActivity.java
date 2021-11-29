package com.mixedtek.elastics.controller;

import com.mixedtek.elastics.controller.ControllerDelegator.OnStateChangeListener;

import android.annotation.SuppressLint;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mixedtek.elastics.controller.view.GameControllerView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainActivity extends AppCompatActivity {
    private Button mDiscoveryKey;
    private Button mQKey;
    private Button mWKey;
    private Button mEKey;
    private Button mRKey;
    private Button mAKey;
    private Button mSKey;
    private Button mDKey;
    private Button mFKey;
    //private Button mUKey;
    private Button mLCKey;
    private Button mLSKey;
    private Button mSPCKey;
    private Button mLBKey;
    private Button mRBKey;

    private TextView mTextViewAngleRight;
    private TextView mTextViewStrengthRight;
    private TextView mTextViewCoordinateRight;
    private GameControllerView mJoystick;

    private int mJoystickPosX = 0;
    private int mJoystickPosY = 0;
    private int mLastMousePosX = 0;
    private int mLastMousePosY = 0;
    private boolean mJoystickMoved = false;

    private ControllerDelegator mControllerDelegator;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mJoystickMoved = false;
        mDiscoveryKey = (Button) findViewById(R.id.discovery);
        mQKey = (Button) findViewById(R.id.q);
        mWKey = (Button) findViewById(R.id.w);
        mEKey = (Button) findViewById(R.id.e);
        mRKey = (Button) findViewById(R.id.r);
        mAKey = (Button) findViewById(R.id.a);
        mSKey = (Button) findViewById(R.id.s);
        mDKey = (Button) findViewById(R.id.d);
        mFKey = (Button) findViewById(R.id.f);
        //mUKey = (Button) findViewById(R.id.u);
        mLCKey = (Button) findViewById(R.id.lc);
        mLSKey = (Button) findViewById(R.id.ls);
        mSPCKey = (Button) findViewById(R.id.space);
        mLBKey = (Button) findViewById(R.id.lb);
        mRBKey = (Button) findViewById(R.id.rb);
        mTextViewAngleRight = (TextView) findViewById(R.id.textView_angle_right);
        mTextViewStrengthRight = (TextView) findViewById(R.id.textView_strength_right);
        mTextViewCoordinateRight = findViewById(R.id.textView_coordinate_right);
        mJoystick = (GameControllerView) findViewById(R.id.joystickView_right);

        mControllerDelegator = new ControllerDelegator(this);
        mControllerDelegator.setStateChangeListener(mStateChangeListener);

        mDiscoveryKey.setOnClickListener(mDiscoveryKeyListener);
        mQKey.setOnTouchListener(mQKeyListener);
        mWKey.setOnTouchListener(mWKeyListener);
        mEKey.setOnTouchListener(mEKeyListener);
        mRKey.setOnTouchListener(mRKeyListener);
        mAKey.setOnTouchListener(mAKeyListener);
        mSKey.setOnTouchListener(mSKeyListener);
        mDKey.setOnTouchListener(mDKeyListener);
        mFKey.setOnTouchListener(mFKeyListener);
        //mUKey.setOnTouchListener(mUKeyListener);
        mLCKey.setOnTouchListener(mLCKeyListener);
        mLSKey.setOnTouchListener(mLSKeyListener);
        mSPCKey.setOnTouchListener(mSPCKeyListener);
        mLBKey.setOnTouchListener(mLBKeyListener);
        mRBKey.setOnTouchListener(mRBKeyListener);
        mJoystick.setOnMoveListener(mJoystickListener);
        mJoystick.setOnTouchListener(mJoystickTouchListener);

        mDiscoveryKey.setVisibility(View.VISIBLE);
        mQKey.setVisibility(View.INVISIBLE);
        mWKey.setVisibility(View.INVISIBLE);
        mEKey.setVisibility(View.INVISIBLE);
        mRKey.setVisibility(View.INVISIBLE);
        mAKey.setVisibility(View.INVISIBLE);
        mSKey.setVisibility(View.INVISIBLE);
        mDKey.setVisibility(View.INVISIBLE);
        mFKey.setVisibility(View.INVISIBLE);
        //mUKey.setVisibility(View.INVISIBLE);
        mLCKey.setVisibility(View.INVISIBLE);
        mLSKey.setVisibility(View.INVISIBLE);
        mSPCKey.setVisibility(View.INVISIBLE);
        mLBKey.setVisibility(View.INVISIBLE);
        mRBKey.setVisibility(View.INVISIBLE);
        mTextViewAngleRight.setVisibility(View.INVISIBLE);
        mTextViewStrengthRight.setVisibility(View.INVISIBLE);
        mTextViewCoordinateRight.setVisibility(View.INVISIBLE);
        mJoystick.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onStop() {
        mControllerDelegator.stop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mControllerDelegator.stop();
        super.onDestroy();
    }

    private OnStateChangeListener mStateChangeListener = new OnStateChangeListener() {
        @Override
        public void onStateChanged(final int state) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(state == ControllerDelegator.STATE_DISCONNECT) {

                        mDiscoveryKey.setVisibility(View.VISIBLE);
                        mQKey.setVisibility(View.INVISIBLE);
                        mWKey.setVisibility(View.INVISIBLE);
                        mEKey.setVisibility(View.INVISIBLE);
                        mRKey.setVisibility(View.INVISIBLE);
                        mAKey.setVisibility(View.INVISIBLE);
                        mSKey.setVisibility(View.INVISIBLE);
                        mDKey.setVisibility(View.INVISIBLE);
                        mFKey.setVisibility(View.INVISIBLE);
                        //mUKey.setVisibility(View.INVISIBLE);
                        mLCKey.setVisibility(View.INVISIBLE);
                        mLSKey.setVisibility(View.INVISIBLE);
                        mSPCKey.setVisibility(View.INVISIBLE);
                        mLBKey.setVisibility(View.INVISIBLE);
                        mRBKey.setVisibility(View.INVISIBLE);
                        mTextViewAngleRight.setVisibility(View.INVISIBLE);
                        mTextViewStrengthRight.setVisibility(View.INVISIBLE);
                        mTextViewCoordinateRight.setVisibility(View.INVISIBLE);
                        mJoystick.setVisibility(View.INVISIBLE);

                    } else if(state==ControllerDelegator.STATE_PLAYER_DISCOVERYING) {
                        mDiscoveryKey.setVisibility(View.VISIBLE);
                        mDiscoveryKey.setText("Discovering...");

                        mQKey.setVisibility(View.INVISIBLE);
                        mWKey.setVisibility(View.INVISIBLE);
                        mEKey.setVisibility(View.INVISIBLE);
                        mRKey.setVisibility(View.INVISIBLE);
                        mAKey.setVisibility(View.INVISIBLE);
                        mSKey.setVisibility(View.INVISIBLE);
                        mDKey.setVisibility(View.INVISIBLE);
                        mFKey.setVisibility(View.INVISIBLE);
                        //mUKey.setVisibility(View.INVISIBLE);
                        mLCKey.setVisibility(View.INVISIBLE);
                        mLSKey.setVisibility(View.INVISIBLE);
                        mSPCKey.setVisibility(View.INVISIBLE);
                        mLBKey.setVisibility(View.INVISIBLE);
                        mRBKey.setVisibility(View.INVISIBLE);
                        mTextViewAngleRight.setVisibility(View.INVISIBLE);
                        mTextViewStrengthRight.setVisibility(View.INVISIBLE);
                        mTextViewCoordinateRight.setVisibility(View.INVISIBLE);
                        mJoystick.setVisibility(View.INVISIBLE);

                    } else if(state==ControllerDelegator.STATE_PLAYER_DISCOVERED) {

                        mDiscoveryKey.setVisibility(View.VISIBLE);
                        mDiscoveryKey.setText("Player is Found");

                        mQKey.setVisibility(View.INVISIBLE);
                        mWKey.setVisibility(View.INVISIBLE);
                        mEKey.setVisibility(View.INVISIBLE);
                        mRKey.setVisibility(View.INVISIBLE);
                        mAKey.setVisibility(View.INVISIBLE);
                        mSKey.setVisibility(View.INVISIBLE);
                        mDKey.setVisibility(View.INVISIBLE);
                        mFKey.setVisibility(View.INVISIBLE);
                        //mUKey.setVisibility(View.INVISIBLE);
                        mLCKey.setVisibility(View.INVISIBLE);
                        mLSKey.setVisibility(View.INVISIBLE);
                        mSPCKey.setVisibility(View.INVISIBLE);
                        mLBKey.setVisibility(View.INVISIBLE);
                        mRBKey.setVisibility(View.INVISIBLE);
                        mTextViewAngleRight.setVisibility(View.INVISIBLE);
                        mTextViewStrengthRight.setVisibility(View.INVISIBLE);
                        mTextViewCoordinateRight.setVisibility(View.INVISIBLE);
                        mJoystick.setVisibility(View.INVISIBLE);

                    } else if(state==ControllerDelegator.STATE_PLAYER_CLIENT_CONNECTING) {

                        mDiscoveryKey.setVisibility(View.VISIBLE);
                        mDiscoveryKey.setText("Pairing....");

                        mQKey.setVisibility(View.INVISIBLE);
                        mWKey.setVisibility(View.INVISIBLE);
                        mEKey.setVisibility(View.INVISIBLE);
                        mRKey.setVisibility(View.INVISIBLE);
                        mAKey.setVisibility(View.INVISIBLE);
                        mSKey.setVisibility(View.INVISIBLE);
                        mDKey.setVisibility(View.INVISIBLE);
                        mFKey.setVisibility(View.INVISIBLE);
                        //mUKey.setVisibility(View.INVISIBLE);
                        mLCKey.setVisibility(View.INVISIBLE);
                        mLSKey.setVisibility(View.INVISIBLE);
                        mSPCKey.setVisibility(View.INVISIBLE);
                        mLBKey.setVisibility(View.INVISIBLE);
                        mRBKey.setVisibility(View.INVISIBLE);
                        mTextViewAngleRight.setVisibility(View.INVISIBLE);
                        mTextViewStrengthRight.setVisibility(View.INVISIBLE);
                        mTextViewCoordinateRight.setVisibility(View.INVISIBLE);
                        mJoystick.setVisibility(View.INVISIBLE);

                    } else if(state==ControllerDelegator.STATE_PLAYER_CLIENT_CONNECTED) {

                        mDiscoveryKey.setVisibility(View.VISIBLE);
                        mDiscoveryKey.setText("Pairing Completed\r\nRun Game in Player");

                        mQKey.setVisibility(View.INVISIBLE);
                        mWKey.setVisibility(View.INVISIBLE);
                        mEKey.setVisibility(View.INVISIBLE);
                        mRKey.setVisibility(View.INVISIBLE);
                        mAKey.setVisibility(View.INVISIBLE);
                        mSKey.setVisibility(View.INVISIBLE);
                        mDKey.setVisibility(View.INVISIBLE);
                        mFKey.setVisibility(View.INVISIBLE);
                        //mUKey.setVisibility(View.INVISIBLE);
                        mLCKey.setVisibility(View.INVISIBLE);
                        mLSKey.setVisibility(View.INVISIBLE);
                        mSPCKey.setVisibility(View.INVISIBLE);
                        mLBKey.setVisibility(View.INVISIBLE);
                        mRBKey.setVisibility(View.INVISIBLE);
                        mTextViewAngleRight.setVisibility(View.INVISIBLE);
                        mTextViewStrengthRight.setVisibility(View.INVISIBLE);
                        mTextViewCoordinateRight.setVisibility(View.INVISIBLE);
                        mJoystick.setVisibility(View.INVISIBLE);

                    } else if(state==ControllerDelegator.STATE_CONTROLLER_CLIENT_CONNECTING) {

                        mDiscoveryKey.setVisibility(View.INVISIBLE);

                        mQKey.setVisibility(View.INVISIBLE);
                        mWKey.setVisibility(View.INVISIBLE);
                        mEKey.setVisibility(View.INVISIBLE);
                        mRKey.setVisibility(View.INVISIBLE);
                        mAKey.setVisibility(View.INVISIBLE);
                        mSKey.setVisibility(View.INVISIBLE);
                        mDKey.setVisibility(View.INVISIBLE);
                        mFKey.setVisibility(View.INVISIBLE);
                        //mUKey.setVisibility(View.INVISIBLE);
                        mLCKey.setVisibility(View.INVISIBLE);
                        mLSKey.setVisibility(View.INVISIBLE);
                        mSPCKey.setVisibility(View.INVISIBLE);
                        mLBKey.setVisibility(View.INVISIBLE);
                        mRBKey.setVisibility(View.INVISIBLE);
                        mTextViewAngleRight.setVisibility(View.INVISIBLE);
                        mTextViewStrengthRight.setVisibility(View.INVISIBLE);
                        mTextViewCoordinateRight.setVisibility(View.INVISIBLE);
                        mJoystick.setVisibility(View.INVISIBLE);

                    } else if(state==ControllerDelegator.STATE_CONTROLLER_CLIENT_CONNECTED) {

                        mDiscoveryKey.setVisibility(View.INVISIBLE);
                        mQKey.setVisibility(View.VISIBLE);
                        mWKey.setVisibility(View.VISIBLE);
                        mEKey.setVisibility(View.VISIBLE);
                        mRKey.setVisibility(View.VISIBLE);
                        mAKey.setVisibility(View.VISIBLE);
                        mSKey.setVisibility(View.VISIBLE);
                        mDKey.setVisibility(View.VISIBLE);
                        mFKey.setVisibility(View.VISIBLE);
                        //mUKey.setVisibility(View.VISIBLE);
                        mLCKey.setVisibility(View.VISIBLE);
                        mLSKey.setVisibility(View.VISIBLE);
                        mSPCKey.setVisibility(View.VISIBLE);
                        mLBKey.setVisibility(View.VISIBLE);
                        mRBKey.setVisibility(View.VISIBLE);

                        mTextViewAngleRight.setVisibility(View.VISIBLE);
                        mTextViewStrengthRight.setVisibility(View.VISIBLE);
                        mTextViewCoordinateRight.setVisibility(View.VISIBLE);
                        mJoystick.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    };

    private View.OnClickListener mDiscoveryKeyListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mControllerDelegator.start();
        }
    };

    private View.OnTouchListener mQKeyListener = new View.OnTouchListener() { //0x51
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mControllerDelegator.sendKeyDownEvent(81);
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    mControllerDelegator.sendKeyUpEvent(81);
                    break;
            }
            return false;
        }
    };
    private View.OnTouchListener mWKeyListener = new View.OnTouchListener() { //0x57
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mControllerDelegator.sendKeyDownEvent(87);
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    mControllerDelegator.sendKeyUpEvent(87);
                    break;
            }
            return false;
        }
    };
    private View.OnTouchListener mEKeyListener = new View.OnTouchListener() { //0x45
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mControllerDelegator.sendKeyDownEvent(69);
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    mControllerDelegator.sendKeyUpEvent(69);
                    break;
            }
            return false;
        }
    };
    private View.OnTouchListener mRKeyListener = new View.OnTouchListener() { //0x52
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mControllerDelegator.sendKeyDownEvent(82);
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    mControllerDelegator.sendKeyUpEvent(82);
                    break;
            }
            return false;
        }
    };

    private View.OnTouchListener mAKeyListener = new View.OnTouchListener() { //0x41
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mControllerDelegator.sendKeyDownEvent(65);
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    mControllerDelegator.sendKeyUpEvent(65);
                    break;
            }
            return false;
        }
    };
    private View.OnTouchListener mSKeyListener = new View.OnTouchListener() { //0x53
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mControllerDelegator.sendKeyDownEvent(83);
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    mControllerDelegator.sendKeyUpEvent(83);
                    break;
            }
            return false;
        }
    };
    private View.OnTouchListener mDKeyListener = new View.OnTouchListener() { //0x44
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mControllerDelegator.sendKeyDownEvent(68);
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    mControllerDelegator.sendKeyUpEvent(68);
                    break;
            }
            return false;
        }
    };
    private View.OnTouchListener mFKeyListener = new View.OnTouchListener() { //0x46
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mControllerDelegator.sendKeyDownEvent(70);
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    mControllerDelegator.sendKeyUpEvent(70);
                    break;
            }
            return false;
        }
    };
    private View.OnTouchListener mUKeyListener = new View.OnTouchListener() { //0x55
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mControllerDelegator.sendKeyDownEvent(85);
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    mControllerDelegator.sendKeyUpEvent(85);
                    break;
            }
            return false;
        }
    };

    private View.OnTouchListener mLCKeyListener = new View.OnTouchListener() { //0xA2
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mControllerDelegator.sendKeyDownEvent(162);
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    mControllerDelegator.sendKeyUpEvent(162);
                    break;
            }
            return false;
        }
    };
    private View.OnTouchListener mLSKeyListener = new View.OnTouchListener() { //0xA0
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mControllerDelegator.sendKeyDownEvent(160);
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    mControllerDelegator.sendKeyUpEvent(160);
                    break;
            }
            return false;
        }
    };
    private View.OnTouchListener mSPCKeyListener = new View.OnTouchListener() { //0x20
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mControllerDelegator.sendKeyDownEvent(32);
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    mControllerDelegator.sendKeyUpEvent(32);
                    break;
            }
            return false;
        }
    };

    private View.OnTouchListener mLBKeyListener = new View.OnTouchListener() { //0x01
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if(!mJoystickMoved) {
                        mControllerDelegator.sendMouseMoveEvent(mLastMousePosX, mLastMousePosY);
                        mJoystickMoved = true;
                    }
                    mControllerDelegator.sendLMouseDownEvent(mLastMousePosX, mLastMousePosY);
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    if(!mJoystickMoved) {
                        mControllerDelegator.sendMouseMoveEvent(mLastMousePosX, mLastMousePosY);
                        mJoystickMoved = true;
                    }
                    mControllerDelegator.sendLMouseUpEvent(mLastMousePosX, mLastMousePosY);
                    break;
            }
            return false;
        }
    };
    private View.OnTouchListener mRBKeyListener = new View.OnTouchListener() { //0x02
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if(!mJoystickMoved) {
                        mControllerDelegator.sendMouseMoveEvent(mLastMousePosX, mLastMousePosY);
                        mJoystickMoved = true;
                    }
                    mControllerDelegator.sendRMouseDownEvent(mLastMousePosX, mLastMousePosY);
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    if(!mJoystickMoved) {
                        mControllerDelegator.sendMouseMoveEvent(mLastMousePosX, mLastMousePosY);
                        mJoystickMoved = true;
                    }
                    mControllerDelegator.sendRMouseUpEvent(mLastMousePosX, mLastMousePosY);
                    break;
            }
            return false;
        }
    };

    private GameControllerView.OnMoveListener mJoystickListener = new GameControllerView.OnMoveListener() {
        @SuppressLint("DefaultLocale")
        @Override
        public void onMove(int angle, int strength) {

            int normX = 1920/50;
            int normY = 1080/50;
            mJoystickPosX = normX * (mJoystick.getNormalizedX() - 50);
            mJoystickPosY = normY * (mJoystick.getNormalizedY() - 50);
            mTextViewAngleRight.setText(angle + "°");
            mTextViewStrengthRight.setText(strength + "%");
            mTextViewCoordinateRight.setText(String.format("normx%03d:normy%03d x%03d:y%03d", mJoystick.getNormalizedX(), mJoystick.getNormalizedY(), mJoystickPosX, mJoystickPosY));
        }
    };

    private GameControllerView.OnTouchListener mJoystickTouchListener = new GameControllerView.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    break;
                case MotionEvent.ACTION_MOVE:
                    mLastMousePosX = mJoystickPosX;
                    mLastMousePosY = mJoystickPosY;
                    mControllerDelegator.sendMouseMoveEvent(mLastMousePosX, mLastMousePosY);
                    if(!mJoystickMoved)
                        mJoystickMoved = true;
                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }
            return false;
        }
    };
}

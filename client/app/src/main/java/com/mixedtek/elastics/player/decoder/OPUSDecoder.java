package com.mixedtek.elastics.player.decoder;

import android.media.MediaCodec;

import com.mixedtek.elastics.player.renderer.AudioRenderer;

import java.nio.ByteBuffer;

public class OPUSDecoder extends AudioDecoder {

    public static final String TAG = "OPUSDecoder";
    private int mChannels;
    private int mBitDepth;
    private int mSamplerate;
    private boolean mbIsRun;
    private static AudioRenderer mRenderer = new AudioRenderer();
    private AudioBuffer mAudioBuffer = null;

    static {
        System.loadLibrary("opus-decoder");
    }

    public static native void create(int samplerate, int ditdepth, int channels, int streams, int coupled, String map);
    public static native int decode(byte [] encoded, int offset, int length, byte [] pcm);
    public static native void destroy();

    public OPUSDecoder(OnAudioDecoderListener listener, int channels, int bitdepth, int samplerate, int bufferCapacity, int bufferSize) {
        mDecoderListener = listener;
        mChannels = channels;
        mBitDepth = bitdepth;
        mSamplerate = samplerate;
        mAudioBuffer = new AudioBuffer(bufferCapacity, bufferSize);
    }

    @Override
    public void start() {
        mRenderer.initialize(mChannels);
        create(mSamplerate, 0, mChannels,1, 1, "");

        mRenderThread.setName("OPUS Audio Render Thread");
        mbIsRun = true;
        mRenderThread.start();
    }

    @Override
    public void preStop() {
        mbIsRun = false;
        /*
        if(mDecodeInputThread!=null) {
            mDecodeInputThread.interrupt();
        }
        */
        if(mRenderThread !=null) {
            mRenderThread.interrupt();
        }
    }

    @Override
    public void stop() {
        preStop();
        try {
            mRenderThread.join();
        } catch(InterruptedException ignored) {}

        destroy();
        mRenderer.release();
    }

    @Override
    public void decode(byte[] data, int offset, int length, long ts) {
        if(mbIsRun) {
            mAudioBuffer.push(data, offset, length, ts);
        }
    }

    private Thread mRenderThread = new Thread(new Runnable () {
        @Override
        public void run() {
            AudioElement ae = null;
            byte[] pcm = new byte[1024 * 10];
            while(mbIsRun) {
                ae = mAudioBuffer.pop();
                if(ae!=null) {
                    int length = decode(ae.mData, ae.mOffset, ae.mLength, pcm);
                    mRenderer.render(pcm, 0, length);
                } else {
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {}
                    continue;
                }
            }
            pcm = null;
        }
    });
}



package com.mixedtek.elastics.player.decoder;

import java.nio.ByteBuffer;

import android.util.Log;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaFormat;
import android.os.Build;

public class AACDecoder extends AudioDecoder {

    public static final String TAG = "AACDecoder";
    private int mChannels;
    private int mBitDepth;
    private int mSamplerate;
    private boolean mbIsRun;
    private MediaCodec mDecoder;
    private AudioTrack mAudioTrack;
    private ByteBuffer[] mOutputBuffer;
    private AudioBuffer mAudioBuffer = null;

    public AACDecoder(OnAudioDecoderListener listener, int channels, int bitdepth, int samplerate, int bufferCapacity, int bufferSize) {
        mDecoderListener = listener;
        mChannels = channels;
        mBitDepth = bitdepth;
        mSamplerate = samplerate;
        //mAudioBuffer  = new AudioBuffer(5, 1024 * 512);
        mAudioBuffer  = new AudioBuffer(bufferCapacity, bufferSize);
    }

    @Override
    public void start() {
        try {
            String mime = "audio/mp4a-latm";
            MediaFormat format = MediaFormat.createAudioFormat(mime, mSamplerate, mChannels);
            format.setInteger(MediaFormat.KEY_IS_ADTS, 1);
            format.setByteBuffer("csd-0", ByteBuffer.wrap(new byte[]{(byte) 0x12, (byte) 0x10})); // 변경 필요 확인
            mDecoder = MediaCodec.createDecoderByType(mime);
            mDecoder.configure(format, null, null, 0);
            mDecoder.start();

            mOutputBuffer = mDecoder.getOutputBuffers();

            int channels = AudioFormat.CHANNEL_OUT_MONO;
            int bitdepth = AudioFormat.ENCODING_PCM_16BIT;
            switch(mChannels) {
                case 1 :
                    channels = AudioFormat.CHANNEL_OUT_MONO;
                    break;
                case 2 :
                    channels = AudioFormat.CHANNEL_OUT_STEREO;
                    break;
            }
            int bufferSize = AudioTrack.getMinBufferSize(mSamplerate, channels, bitdepth);
            mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mSamplerate, channels, bitdepth, bufferSize, AudioTrack.MODE_STREAM);
            mAudioTrack.play();

            mRenderThread.setName("AAC Audio Render Thread");
            mDecodeInputThread.setName("AAC Audio VideoDecoder Input Thread");

            mbIsRun = true;
            mRenderThread.start();
            mDecodeInputThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void preStop() {
        mbIsRun = false;
        if(mDecodeInputThread!=null) {
            mDecodeInputThread.interrupt();
        }

        if(mRenderThread !=null) {
            mRenderThread.interrupt();
        }
    }

    @Override
    public void stop() {
        preStop();
        try {
            try {
                mRenderThread.join();
                mDecodeInputThread.join();
            } catch (InterruptedException ignored) { }

            mDecoder.stop();
            mDecoder.release();
            mAudioTrack.stop();
            mAudioTrack.release();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void decode(byte[] data, int offset, int length, long ts) {
        if(mbIsRun) {
            mAudioBuffer.push(data, offset, length, ts);
        }
    }

    private Thread mDecodeInputThread = new Thread(new Runnable() {
        @Override
        public void run() {
            int inputBufferId = -1;
            AudioElement ae = null;
            ByteBuffer buffer = null;
            while(mbIsRun) {

                try {
                    inputBufferId = mDecoder.dequeueInputBuffer(10000);
                    if(mbIsRun && (inputBufferId >= 0)) {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                            buffer = mDecoder.getInputBuffers()[inputBufferId];
                            buffer.clear();
                        } else {
                            buffer = mDecoder.getInputBuffer(inputBufferId);
                        }

                        if(buffer!=null) {

                            while (mbIsRun) {
                                ae = mAudioBuffer.pop();
                                if(ae!=null)
                                    break;

                                try {
                                    Thread.sleep(5);
                                } catch (InterruptedException e) {
                                    break;
                                }
                            }

                            if(mbIsRun && ae!=null) {
                                buffer.put(ae.mData, ae.mOffset, ae.mLength);
                                mDecoder.queueInputBuffer(inputBufferId, 0, ae.mLength, 0, 0);
                                ae = null;
                            }
                        }
                    }
                } catch (IllegalStateException e) {
                    Log.i(TAG, "Audio Decode Input Thread IllegalStateException");
                    //mDecoder.flush();
                }
            }
        }
    });

    private Thread mRenderThread = new Thread(new Runnable () {
        @Override
        public void run() {
            BufferInfo info = new BufferInfo();
            byte[] pcm = new byte[1024 * 10];
            while(mbIsRun) {
                try {
                    int outputBufferId = mDecoder.dequeueOutputBuffer(info, 50000);
                    if(outputBufferId >= 0 ) {
                        ByteBuffer outBuffer = mOutputBuffer[outputBufferId];
                        outBuffer.get(pcm, 0, info.size);
                        mAudioTrack.write(pcm, 0, info.size);
                        outBuffer.clear();
                        mDecoder.releaseOutputBuffer(outputBufferId, false);
                    } else if(outputBufferId ==  MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                        mOutputBuffer = mDecoder.getOutputBuffers();
                    } else if(outputBufferId == MediaCodec.INFO_TRY_AGAIN_LATER) {

                    }
                } catch (IllegalStateException lse) {
                    //lse.printStackTrace();
                    //Log.e(TAG, "Video VideoDecoder IllegalStateException");
                } catch (Exception e) {
                    //e.printStackTrace();
                    //Log.e(TAG, "Video VideoDecoder Exception");
                }
            }
            pcm = null;
        }
    });
}

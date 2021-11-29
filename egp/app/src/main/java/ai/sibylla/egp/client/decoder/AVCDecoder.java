package ai.sibylla.egp.client.decoder;

import java.nio.ByteBuffer;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;
import android.os.Build;

public class AVCDecoder extends VideoDecoder {

    private static final boolean USE_FRAME_RENDER_TIME = false;
    private static final boolean FRAME_RENDER_TIME_ONLY = USE_FRAME_RENDER_TIME && false;

    public static final String TAG = "AVCDecoder";
    private boolean mbIsRun;
    private MediaCodecInfo mDecoderInfo;
    private MediaCodec mDecoder;
    private Surface mSurface;
    private int mWidth;
    private int mHeight;
    // Used on versions < 5.0
    private boolean mbLegacyFrameDropRendering = false;

    private VideoBuffer mVideoBuffer = null;

    private boolean mbAdaptivePlayback;
    private boolean mbDirectSubmit;
    private boolean mbNeedsSpsBitstreamFixup;
    private boolean mbNeedsBaselineSpsHack;
    private boolean mbConstrainedHighProfile;
    private boolean mbIsExynos4;
    private boolean mbRefFrameInvalidation;
    private long mDecoderTimeMs;
    private long mTotalTimeMs;
    private int mTotalFramesRendered;

    public AVCDecoder(Surface surface, OnVideoDecoderListener listener, int width, int height, int videoBufferCapacity, int videoBufferSize) {
        mSurface = surface;
        mDecoderListener = listener;
        mWidth = width;
        mHeight = height;
        mDecoderInfo = findDecoder();
        int consecutiveCrashCount = 0;
        if (mDecoderInfo != null) {
            mbDirectSubmit = MediaCodecHelper.decoderCanDirectSubmit(mDecoderInfo.getName());
            mbAdaptivePlayback = MediaCodecHelper.decoderSupportsAdaptivePlayback(mDecoderInfo);
            mbRefFrameInvalidation = MediaCodecHelper.decoderSupportsRefFrameInvalidationAvc(mDecoderInfo.getName(), mHeight);

            if (consecutiveCrashCount % 2 == 1) {
                mbRefFrameInvalidation = false;
            }
        }
        //mVideoBuffer  = new VideoBuffer(width, height,30, 1024 * 1024);
        Log.i(TAG, "videoBufferCapacity is "+ videoBufferCapacity);
        Log.i(TAG, "videoBufferSize is "+ videoBufferSize);
        mVideoBuffer  = new VideoBuffer(width, height,videoBufferCapacity, videoBufferSize);
    }

    @Override
    public void start() {
        try {
            String mimeType = "video/avc";
            String selectedDecoderName = mDecoderInfo.getName();

            mbNeedsSpsBitstreamFixup = MediaCodecHelper.decoderNeedsSpsBitstreamRestrictions(selectedDecoderName);
            mbNeedsBaselineSpsHack = MediaCodecHelper.decoderNeedsBaselineSpsHack(selectedDecoderName);
            mbConstrainedHighProfile = MediaCodecHelper.decoderNeedsConstrainedHighProfile(selectedDecoderName);
            mbIsExynos4 = MediaCodecHelper.isExynos4Device();

            mVideoBuffer.setParameter(mbDirectSubmit, mbAdaptivePlayback, mbRefFrameInvalidation, mbNeedsSpsBitstreamFixup, mbNeedsBaselineSpsHack, mbConstrainedHighProfile, mbIsExynos4);

            try {
                mDecoder = MediaCodec.createByCodecName(selectedDecoderName);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            MediaFormat format = MediaFormat.createVideoFormat("video/avc", mWidth, mHeight);

            if(mbAdaptivePlayback && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                format.setInteger(MediaFormat.KEY_MAX_WIDTH, mWidth);
                format.setInteger(MediaFormat.KEY_MAX_HEIGHT, mHeight);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                format.setInteger(MediaFormat.KEY_OPERATING_RATE, Short.MAX_VALUE);
            }

            mDecoder.configure(format, mSurface, null, 0);
            //mDecoder.setVideoScalingMode(MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT);
            mDecoder.start();

            mRenderThread.setName("Video Render Thread");
            mRenderThread.setPriority(Thread.NORM_PRIORITY + 2);
            mDecodeInputThread.setName("Video VideoDecoder Input Thread");

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

        if(mRenderThread!=null) {
            mRenderThread.interrupt();
        }
    }

    @Override
    public void stop() {
        preStop();
        try {
            /* while(true) {
                int inputBufferId = mDecoder.dequeueInputBuffer(10000);
                if (inputBufferId >= 0) {
                    mDecoder.queueInputBuffer(inputBufferId, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    Log.e(TAG, "Push BUFFER_FLAG_END_OF_STREAM");
                    break;
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ignore) {

                }
            } */
            try {
                mRenderThread.join();
                mDecodeInputThread.join();
            } catch (InterruptedException ignored) {}

            //mDecoder.stop();
            mDecoder.release();
            //mDecoder = null;
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void decode(byte[] data, int offset, int length, long ts) {
        if(mbIsRun) {
            mVideoBuffer.push(data, offset, length, ts);
        }
    }

    private Thread mDecodeInputThread = new Thread(new Runnable() {
        @Override
        public void run() {
            int inputBufferId = -1;
            VideoElement ve = null;
            ByteBuffer buffer  = null;
            while(mbIsRun) {

                try {
                    inputBufferId = mDecoder.dequeueInputBuffer(10000);
                    if (mbIsRun && (inputBufferId >= 0)) {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                            buffer = mDecoder.getInputBuffers()[inputBufferId];
                            buffer.clear();
                        } else {
                            buffer = mDecoder.getInputBuffer(inputBufferId);
                        }

                        if(buffer!=null) {

                            while (mbIsRun) {
                                ve = mVideoBuffer.pop();
                                if(ve!=null)
                                    break;

                                try {
                                    Thread.sleep(5);
                                } catch (InterruptedException e) {
                                    break;
                                }
                            }

                            if(mbIsRun && ve!=null) {
                                buffer.put(ve.mData, ve.mOffset, ve.mLength);
                                mDecoder.queueInputBuffer(inputBufferId, 0, ve.mLength, 0, 0);
                                ve = null;
                            }
                        }
                    }
                } catch(IllegalStateException e) {
                    Log.i(TAG, "Video Decode Input Thread IllegalStateException");
                    //mDecoder.flush();
                }
            }
        }
    });

    private Thread mRenderThread = new Thread(new Runnable () {
        @Override
        public void run() {
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            while(mbIsRun) {
                try {
                    int outIndex = mDecoder.dequeueOutputBuffer(info, 50000);
                    if (outIndex >= 0) {
                        long presentationTimeUs = info.presentationTimeUs;
                        int lastIndex = outIndex;

                        while ((outIndex = mDecoder.dequeueOutputBuffer(info, 0)) >= 0) {
                            mDecoder.releaseOutputBuffer(lastIndex, false);
                            lastIndex = outIndex;
                            presentationTimeUs = info.presentationTimeUs;
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            if (mbLegacyFrameDropRendering) {
                                mDecoder.releaseOutputBuffer(lastIndex, System.nanoTime());
                            } else {
                                mDecoder.releaseOutputBuffer(lastIndex, 0);
                            }
                        } else {
                            mDecoder.releaseOutputBuffer(lastIndex, true);
                        }
                        mTotalFramesRendered++;

                        long delta = MediaCodecHelper.getMonotonicMillis() - (presentationTimeUs / 1000);
                        if (delta >= 0 && delta < 1000) {
                            mDecoderTimeMs += delta;
                            if (!USE_FRAME_RENDER_TIME) {
                                mTotalTimeMs += delta;
                            }
                        }
                    } else {
                        switch (outIndex) {
                            case MediaCodec.INFO_TRY_AGAIN_LATER:
                                break;
                            case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                                break;
                            default:
                                break;
                        }
                    }
                } catch (IllegalStateException lse) {
                    //lse.printStackTrace();
                    //Log.e(TAG, "Video VideoDecoder IllegalStateException");
                } catch (Exception e) {
                    //e.printStackTrace();
                    //Log.e(TAG, "Video VideoDecoder Exception");
                }
            }
        }
    });

    private MediaCodecInfo findDecoder() {
        MediaCodecInfo decoder = MediaCodecHelper.findProbableSafeDecoder("video/avc", MediaCodecInfo.CodecProfileLevel.AVCProfileHigh);
        if (decoder == null) {
            decoder = MediaCodecHelper.findFirstDecoder("video/avc");
        }
        return decoder;
    }

    private int dequeueInputBuffer() {
        int index = -1;
        long startTime;

        startTime = MediaCodecHelper.getMonotonicMillis();

        try {
            while (index < 0 && mbIsRun) {
                index = mDecoder.dequeueInputBuffer(10000);
            }
        } catch (Exception e) {
            return MediaCodec.INFO_TRY_AGAIN_LATER;
        }

        int deltaMs = (int)(MediaCodecHelper.getMonotonicMillis() - startTime);
        if (index < 0) {
            return index;
        }
        return index;
    }

    private boolean queueInputBuffer(int inputBufferIndex, int offset, int length, long timestampUs, int codecFlags) {
        try {
            mDecoder.queueInputBuffer(inputBufferIndex, offset, length, timestampUs, codecFlags);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    /*
    @SuppressWarnings("deprecation")
    public void decode(byte[] bitstream, int offset, int length, long ts) {
        int inputBufferId = -1;
        int codecFlags = 0;
        if(mbIsRun)
        {
            if(!mKeyRecieved) {
                if(bitstream[offset + 4]==0x67) {
                    Log.e(TAG, "SPS means that IDR is Received");
                    mKeyRecieved = true;
                } else {
                    return;
                }
            }

            if(bitstream[offset + 4]==0x67) {
                ByteBuffer spsBuf = ByteBuffer.wrap(bitstream, offset, length);
                SeqParameterSet sps = H264Utils.readSPS(spsBuf);

                if(!mbRefFrameInvalidation) {
                    if (mWidth <= 720 && mHeight <= 480) {
                        // Max 5 buffered frames at 720x480x60
                        sps.levelIdc = 31;
                    }
                    else if (mWidth <= 1280 && mHeight <= 720) {
                        // Max 5 buffered frames at 1280x720x60
                        sps.levelIdc = 32;
                    }
                    else if (mWidth <= 1920 && mHeight <= 1080) {
                        // Max 4 buffered frames at 1920x1080x64
                        sps.levelIdc = 42;
                    }
                    sps.numRefFrames = 1;
                }

                if(sps.vuiParams!=null) {
                    sps.vuiParams.videoSignalTypePresentFlag = false;
                    sps.vuiParams.colourDescriptionPresentFlag = false;
                    sps.vuiParams.chromaLocInfoPresentFlag = false;
                    if ((mbNeedsSpsBitstreamFixup || mbIsExynos4) && !mbRefFrameInvalidation) {
                        if (sps.vuiParams.bitstreamRestriction == null) {
                            sps.vuiParams.bitstreamRestriction = new VUIParameters.BitstreamRestriction();
                            sps.vuiParams.bitstreamRestriction.motionVectorsOverPicBoundariesFlag = true;
                            sps.vuiParams.bitstreamRestriction.log2MaxMvLengthHorizontal = 16;
                            sps.vuiParams.bitstreamRestriction.log2MaxMvLengthVertical = 16;
                            sps.vuiParams.bitstreamRestriction.numReorderFrames = 0;
                        }
                        sps.vuiParams.bitstreamRestriction.maxDecFrameBuffering = sps.numRefFrames;
                        sps.vuiParams.bitstreamRestriction.maxBytesPerPicDenom = 2;
                        sps.vuiParams.bitstreamRestriction.maxBitsPerMbDenom = 1;
                    } else {
                        sps.vuiParams.bitstreamRestriction = null;
                    }
                }
                if (mbNeedsBaselineSpsHack) {
                    sps.profileIdc = 66;
                }
                if (sps.profileIdc == 100 && mbConstrainedHighProfile) {
                    sps.constraintSet4Flag = true;
                    sps.constraintSet5Flag = true;
                } else {
                    sps.constraintSet4Flag = false;
                    sps.constraintSet5Flag = false;
                }
                ByteBuffer escapedNalu = H264Utils.writeSPS(sps, length);
                mSpsBuffer = new byte[5 + escapedNalu.limit()];
                System.arraycopy(bitstream, offset, mSpsBuffer, 0, 5);
                escapedNalu.get(mSpsBuffer, 5, escapedNalu.limit());

                //inputBufferId = dequeueInputBuffer();
                //inputBufferId = mDecoder.dequeueInputBuffer(10000);
                codecFlags |= MediaCodec.BUFFER_FLAG_CODEC_CONFIG;
            } else {
                //inputBufferId = mDecoder.dequeueInputBuffer(10000);
                codecFlags = 0;
            }


            inputBufferId = mDecoder.dequeueInputBuffer(10000);
            if (inputBufferId >= 0) {
                ByteBuffer buffer;
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    buffer = mDecoder.getInputBuffers()[inputBufferId];
                    buffer.clear();
                }
                else
                {
                    buffer = mDecoder.getInputBuffer(inputBufferId);
                }
                if(buffer!=null) {
                    buffer.put(bitstream, offset, length);
                    mDecoder.queueInputBuffer(inputBufferId, 0, length, 0, codecFlags);
                }
            }
        }
    }
    */

}

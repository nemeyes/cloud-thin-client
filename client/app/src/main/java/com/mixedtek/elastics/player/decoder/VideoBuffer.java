package com.mixedtek.elastics.player.decoder;

import android.media.MediaCodec;
import android.os.SystemClock;
import android.util.Log;

import org.jcodec.codecs.h264.H264Utils;
import org.jcodec.codecs.h264.io.model.SeqParameterSet;
import org.jcodec.codecs.h264.io.model.VUIParameters;

import java.nio.ByteBuffer;
import java.util.*;

class VideoElement {
    public boolean mbIsIDR;
    public byte mData[];
    public int mOffset;
    public int mLength;
    public long mTimestamp;
    public int mCodecFlags;
    public VideoElement(int size) {
        mbIsIDR = false;
        mData = new byte[size];
        mOffset = 0;
        mLength = 0;
        mTimestamp = 0;
        mCodecFlags = 0;
    }
}

public class VideoBuffer {
    private static final String TAG = "VideoBuffer";
    private VideoElement mBuffer[];
    private int mWidth;
    private int mHeight;
    private int mSize;
    private int mCapacity;
    private int mHead, mTail;
    //private Object mLock = new Object();
    private boolean mKeyRecieved;
    private byte[] mSpsBuffer;

    private boolean mbAdaptivePlayback;
    private boolean mbDirectSubmit;
    private boolean mbNeedsSpsBitstreamFixup;
    private boolean mbNeedsBaselineSpsHack;
    private boolean mbConstrainedHighProfile;
    private boolean mbIsExynos4;
    private boolean mbRefFrameInvalidation;

    public VideoBuffer(int width, int height, int capacity, int size) {
        mWidth = width;
        mHeight = height;
        mBuffer = new VideoElement[capacity];
        for(int i=0; i<capacity; i++) {
            mBuffer[i] = new VideoElement(size);
        }
        mSize = size;
        mCapacity = capacity;
        mHead = -1;
        mTail = -1;
    }

    public void setParameter(boolean directSubmit, boolean adaptivePlayback, boolean refFrameInvalidation, boolean needsSpsBitstreamFixup, boolean needsBaselineSpsHack, boolean constrainedHighProfile, boolean isExynos4) {
        mbDirectSubmit = directSubmit;
        mbAdaptivePlayback = adaptivePlayback;
        mbRefFrameInvalidation = refFrameInvalidation;
        mbNeedsSpsBitstreamFixup = needsSpsBitstreamFixup;
        mbNeedsBaselineSpsHack = needsBaselineSpsHack;
        mbConstrainedHighProfile = constrainedHighProfile;
        mbIsExynos4 = isExynos4;
    }

    public synchronized void empty() {
        mHead = -1;
        mTail = -1;
        mKeyRecieved = false;
    }

    public synchronized boolean isEmpty() {
        return mHead == mTail;
    }

    public synchronized boolean isFull() {
        int tmp = (mTail  + 1) % mCapacity;
        return (tmp == mHead);
    }

    public synchronized boolean push(byte[] data, int offset, int length, long ts) {
        boolean isIDR = false;
        if(data[offset + 4]==0x67) {
            Log.e(TAG, "SPS means that IDR is Received");
            isIDR = true;
            mKeyRecieved = true;
        }

        if(!mKeyRecieved) {
            return true;
        }

        int tmp = (mTail  + 1) % mCapacity;
        if(tmp == mHead) {
            if(isIDR) {
                mHead = -1;
                mTail = -1;
            } else {
                mHead = -1;
                mTail = -1;
                mKeyRecieved = false;
                return false;
            }
        }

        int codecFlags = 0;
        /*
        if(data[offset + 4]==0x67) {
            ByteBuffer spsBuf = ByteBuffer.wrap(data, offset, length);
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
            System.arraycopy(data, offset, mSpsBuffer, 0, 5);
            escapedNalu.get(mSpsBuffer, 5, escapedNalu.limit());
            codecFlags |= MediaCodec.BUFFER_FLAG_CODEC_CONFIG;
        } else {
            codecFlags = 0;
        }
        */

        mTail = (++mTail % mCapacity);
        mBuffer[mTail].mbIsIDR = isIDR;
        mBuffer[mTail].mLength = length;
        System.arraycopy(data, 0, mBuffer[mTail].mData, 0, data.length);
        mBuffer[mTail].mOffset = offset;
        mBuffer[mTail].mTimestamp = ts;
        mBuffer[mTail].mCodecFlags = codecFlags;
        return true;
    }

    public synchronized VideoElement pop() {
        if( mHead == mTail) {
            return null;
        }
        mHead = (++mHead % mCapacity);
        //return mBuffer[mHead];

        VideoElement ve = new VideoElement(mSize);
        ve.mbIsIDR = mBuffer[mHead].mbIsIDR;
        ve.mLength = mBuffer[mHead].mLength;
        System.arraycopy(mBuffer[mHead].mData, 0, ve.mData, 0, ve.mData.length);
        ve.mOffset = mBuffer[mHead].mOffset;
        ve.mTimestamp = mBuffer[mHead].mTimestamp;
        ve.mCodecFlags = mBuffer[mHead].mCodecFlags;
        return ve;
    }
}

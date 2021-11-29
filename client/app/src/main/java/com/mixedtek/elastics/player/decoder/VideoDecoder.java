package com.mixedtek.elastics.player.decoder;

public abstract class VideoDecoder {
    abstract public void start();
    abstract public void preStop();
    abstract public void stop();
    abstract public void decode(byte[] data, int offset, int length, long ts);

    protected OnVideoDecoderListener mDecoderListener;

    public interface OnVideoDecoderListener {
        public void onError(int error);
    }

    protected void onError(int error) {
        if(mDecoderListener != null) {
            mDecoderListener.onError(error);
        }
    }
}
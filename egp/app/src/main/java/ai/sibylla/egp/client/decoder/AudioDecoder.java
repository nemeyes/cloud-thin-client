package ai.sibylla.egp.client.decoder;

public abstract class AudioDecoder {
    abstract public void start();
    abstract public void preStop();
    abstract public void stop();
    abstract public void decode(byte[] data, int offset, int length, long ts);

    protected OnAudioDecoderListener mDecoderListener;

    public interface OnAudioDecoderListener {
        public void onError(int error);
    }

    protected void onError(int error) {
        if(mDecoderListener != null) {
            mDecoderListener.onError(error);
        }
    }
}

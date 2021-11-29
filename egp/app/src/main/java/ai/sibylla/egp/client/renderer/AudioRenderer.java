package ai.sibylla.egp.client.renderer;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;

public class AudioRenderer {
    private AudioTrack mTrack;

    private AudioTrack createAudioTrack(int channelConfig, int bufferSize, boolean lowLatency) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return new AudioTrack(AudioManager.STREAM_MUSIC,
                    48000,
                    channelConfig,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize,
                    AudioTrack.MODE_STREAM);
        } else {
            AudioAttributes.Builder attributesBuilder = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME);
            AudioFormat format = new AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(48000)
                    .setChannelMask(channelConfig)
                    .build();

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                // Use FLAG_LOW_LATENCY on L through N
                if (lowLatency) {
                    attributesBuilder.setFlags(AudioAttributes.FLAG_LOW_LATENCY);
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                AudioTrack.Builder trackBuilder = new AudioTrack.Builder()
                        .setAudioFormat(format)
                        .setAudioAttributes(attributesBuilder.build())
                        .setTransferMode(AudioTrack.MODE_STREAM)
                        .setBufferSizeInBytes(bufferSize);

                // Use PERFORMANCE_MODE_LOW_LATENCY on O and later
                if (lowLatency) {
                    trackBuilder.setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY);
                }

                return trackBuilder.build();
            } else {
                return new AudioTrack(attributesBuilder.build(),
                        format,
                        bufferSize,
                        AudioTrack.MODE_STREAM,
                        AudioManager.AUDIO_SESSION_ID_GENERATE);
            }
        }
    }

    public int initialize(int nchannels) {
        int channelConfig;
        int bytesPerFrame;

        switch (nchannels)
        {
            case 2 :
                channelConfig = AudioFormat.CHANNEL_OUT_STEREO;
                bytesPerFrame = 2 * 240 * 2;
                break;
            case 51 :
                channelConfig = AudioFormat.CHANNEL_OUT_5POINT1;
                bytesPerFrame = 6 * 240 * 2;
                break;
            default:
                return -1;
        }

        // We're not supposed to request less than the minimum
        // buffer size for our buffer, but it appears that we can
        // do this on many devices and it lowers audio latency.
        // We'll try the small buffer size first and if it fails,
        // use the recommended larger buffer size.
        for (int i = 0; i < 4; i++) {
            boolean lowLatency;
            int bufferSize;
            // We will try:
            // 1) Small buffer, low latency mode
            // 2) Large buffer, low latency mode
            // 3) Small buffer, standard mode
            // 4) Large buffer, standard mode
            switch (i) {
                case 0:
                case 1:
                    lowLatency = true;
                    break;
                case 2:
                case 3:
                    lowLatency = false;
                    break;
                default:
                    // Unreachable
                    throw new IllegalStateException();
            }
            switch (i) {
                case 0:
                case 2:
                    bufferSize = bytesPerFrame * 2;
                    break;
                case 1:
                case 3:
                    // Try the larger buffer size
                    bufferSize = Math.max(AudioTrack.getMinBufferSize(48000,
                            channelConfig,
                            AudioFormat.ENCODING_PCM_16BIT),
                            bytesPerFrame * 2);

                    // Round to next frame
                    bufferSize = (((bufferSize + (bytesPerFrame - 1)) / bytesPerFrame) * bytesPerFrame);
                    break;
                default:
                    // Unreachable
                    throw new IllegalStateException();
            }

            // Skip low latency options if hardware sample rate isn't 48000Hz
            if (AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC) != 48000 && lowLatency) {
                continue;
            }

            try {
                mTrack = createAudioTrack(channelConfig, bufferSize, lowLatency);
                mTrack.play();

                // Successfully created working AudioTrack. We're done here.
                break;
            } catch (Exception e) {
                // Try to release the AudioTrack if we got far enough
                e.printStackTrace();
                try {
                    if (mTrack != null) {
                        mTrack.release();
                        mTrack = null;
                    }
                } catch (Exception ignored) {}
            }
        }
        if (mTrack == null) {
            // Couldn't create any audio track for playback
            return -2;
        }
        return 0;
    }

    public int release() {
        mTrack.pause();
        mTrack.flush();
        mTrack.release();
        return 0;
    }

    public void render(byte[] pcm, int offset, int length) {
        mTrack.write(pcm, offset, length);
    }
}

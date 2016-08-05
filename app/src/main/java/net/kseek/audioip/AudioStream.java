package net.kseek.audioip;

import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.LinkedList;

/**
 * Created by teocci on 8/3/16.
 */
public class AudioStream
{
    private static final String TAG = "AudioStream";

    private static final byte CHANNEL_IN_MONO = 0x10;
    private static final byte CHANNEL_IN_STEREO = 0x0c;

    private static final byte ENCODING_PCM_8BIT = 0x3;
    private static final byte ENCODING_PCM_16BIT = 0x2;

    private static final byte SOURCE_CAMCORDER = 0x05;
    private static final byte SOURCE_DEFAULT = 0x00;
    private static final byte SOURCE_MIC = 0x01;
    private static final byte SOURCE_VOICE_CALL = 0x04;
    private static final byte SOURCE_VOICE_COMMUNICATION = 0x07;
    private static final byte SOURCE_VOICE_DOWNLINK = 0x03;
    private static final byte SOURCE_VOICE_RECOGNITION = 0x06;
    private static final byte SOURCE_VOICE_UPLINK = 0x02;

    public static final int[] AUDIO_SAMPLING_RATES = {
            96000, // 0
            88200, // 1
            64000, // 2
            48000, // 3
            44100, // 4
            32000, // 5
            24000, // 6
            22050, // 7
            16000, // 8
            12000, // 9
            11025, // 10
            8000,  // 11
            7350,  // 12
    };

    private static final int MAX_BUFFER = 15;

    private int channel;
    private int encoding;
    private int source;

    private int rate;
    private int bufferSize;

    private Audio audioRecord = null;

    private LinkedList<byte[]> queue = new LinkedList<>();
    private byte[] audioChunk = null;

    public AudioStream()
    {
        if (!AACStreamingSupported()) {
            Log.e(TAG, "AAC not supported on this phone");
            throw new RuntimeException("AAC not supported by this phone !");
        } else {
            Log.d(TAG, "AAC supported on this phone");
            this.channel = CHANNEL_IN_STEREO;
            this.encoding = ENCODING_PCM_16BIT;
            this.source = SOURCE_MIC;
            this.rate = AUDIO_SAMPLING_RATES[4];
            this.audioRecord = new Audio();
        }

        this.audioRecord.setAudioListener(new AudioInterface() {
            @Override
            public void onAudioRecord(ByteBuffer buffer, int bufsize)
            {

            }

        });
    }

    public int getRate()
    {
        return rate;
    }

    public int getChannel()
    {
        return channel;
    }

    public int getEncoding()
    {
        return encoding;
    }

    public int getBuffersize()
    {
        return bufferSize;
    }

    public int getSource()
    {
        return source;
    }

    public void setRate(int rate)
    {
        this.rate = rate;
    }

    public void setChannel(int channel)
    {
        this.channel = channel;
    }

    public void setSource(int source)
    {
        this.source = source;
    }

    public void onPause()
    {
        if (audioRecord != null) {
            audioRecord.stopRecording();
        }
        resetBuff();
    }

    public byte[] getAudioBuffer()
    {
        synchronized (queue) {
            if (queue.size() > 0) {
                audioChunk = queue.poll();
            }
        }

        return audioChunk;
    }

    private void resetBuff()
    {
        synchronized (queue) {
            queue.clear();
            audioChunk = null;
        }
    }

    private static boolean AACStreamingSupported()
    {
        if (Build.VERSION.SDK_INT < 14) return false;
        try {
            MediaRecorder.OutputFormat.class.getField("AAC_ADTS");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

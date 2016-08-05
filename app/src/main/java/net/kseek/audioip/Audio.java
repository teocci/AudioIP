package net.kseek.audioip;

import android.media.AudioRecord;
import android.util.Log;

import java.nio.ByteBuffer;

/**
 * Created by teocci on 8/4/16.
 */
public class Audio extends Thread {
    private static String TAG = "AudioIn";

    public static final byte CHANNEL_IN_MONO = 0x10;
    public static final byte CHANNEL_IN_STEREO = 0x0c;

    public static final byte ENCODING_PCM_8BIT = 0x3;
    public static final byte ENCODING_PCM_16BIT = 0x2;

    public static final byte SOURCE_CAMCORDER = 0x05;
    public static final byte SOURCE_DEFAULT = 0x00;
    public static final byte SOURCE_MIC = 0x01;
    public static final byte SOURCE_VOICE_CALL = 0x04;
    public static final byte SOURCE_VOICE_COMMUNICATION = 0x07;
    public static final byte SOURCE_VOICE_DOWNLINK = 0x03;
    public static final byte SOURCE_VOICE_RECOGNITION = 0x06;
    public static final byte SOURCE_VOICE_UPLINK = 0x02;

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

    private boolean stopped = false;

    public static Audio instance = null;
    public static AudioInterface audioListerner;

    private int channel;
    private int encoding;
    private int source;

    private int rate;
    private int bufferSize;

    public Audio() {
        this.audioListerner = null;
    }

    public Audio(int source, int bufferSize, int rate, int channel, int encoding) {
        this.source = source;
        this.rate = rate;
        this.channel = channel;
        this.encoding = encoding;
        this.bufferSize = bufferSize;

        this.audioListerner = null;
    }

    @Override
    public void run() {
        AudioRecord recorder = null;
        ByteBuffer buffer;
        int N = 0;

        Log.d(TAG, "Starting audio recording thread");

        stopped = false;

        try {
            // ... initialize
            int minBufferSize = AudioRecord.getMinBufferSize(this.rate, this.channel, this.encoding);
            if ( this.bufferSize <= minBufferSize )
                this.bufferSize = minBufferSize;
            Log.d(TAG, String.format("Audio bufferSize is %d bytes", bufferSize));

            buffer = ByteBuffer.allocateDirect(bufferSize);
            recorder = new AudioRecord(this.source,
                    this.rate, this.channel, this.encoding, this.bufferSize);

            Log.d(TAG, "Recording started");
            recorder.startRecording();

            // ... loop

            while (!stopped) {
                N = recorder.read(buffer, bufferSize);
                audioListerner.onAudioRecord(buffer, N);
            }

        } catch(Throwable x) {
            Log.w(TAG, "Error reading voice audio", x);
        } finally {
            if ( recorder != null ) recorder.stop();
            recorder = null;
        }
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

    public void close() {
        stopped = true;
    }

    public static Audio startRecording(int source, int bufferSize, int rate, int channel, int encoding) {
        instance = new Audio(source, bufferSize, rate, channel, encoding);
        instance.start();
        return instance;
    }

    public static void stopRecording() {
        if ( instance != null ) {
            instance.close();
            instance = null;
        }
    }

    public static void setAudioListener(AudioInterface listener) {
        audioListerner = listener;
    }
}
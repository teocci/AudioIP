package net.kseek.audioip;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.LinkedList;

public class AudioManager
{
	private static final String TAG = "audio_manager";

	private Context context;


	private static int channel;
	private static int encoding;
	private static int source;

	private static int rate;
	private static int bufferSize;

	private Audio audioRecord = null;

	private LinkedList<byte[]> queue = new LinkedList<>();
	private byte[] audioChunk = null;

	public AudioManager(Context contxt) {
		context = contxt;
		// Create an instance of Audio

		if (!AACStreamingSupported()) {
			Log.e(TAG, "AAC not supported on this phone");
			throw new RuntimeException("AAC not supported by this phone !");
		} else {
			Log.d(TAG, "AAC supported on this phone");
			this.channel = Audio.CHANNEL_IN_STEREO;
			this.encoding = Audio.ENCODING_PCM_16BIT;
			this.source = Audio.SOURCE_MIC;
			this.rate = Audio.AUDIO_SAMPLING_RATES[4];
			this.audioRecord = getAudioInstance();
		}

		this.audioRecord.setAudioListener(new AudioInterface() {
			@Override
			public void onAudioRecord(ByteBuffer buffer, int bufferSize)
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

	public Audio getAudio() {
		return audioRecord;
	}
	
	public void onResume() {
		if (audioRecord == null) {
			audioRecord = new Audio();
		}
		
		Toast.makeText(context,
				"buffer size = " +
					getBuffersize() + " | channel = " +
					getChannel(),
				Toast.LENGTH_LONG).show();
	}

	public void onPause() {
		releaseAudio();
	}

	private void releaseAudio() {
		if (audioRecord != null) {
			// release the camera for other applications
			audioRecord.stopRecording();
			audioRecord = null;
		}
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
	
	/** A safe way to get an instance of the Audio object. */
	private static Audio getAudioInstance(){
		Audio a = null;
	    try {
	        a = Audio.startRecording(source, bufferSize, rate, channel, encoding); // attempt to get a Audio instance
	    }
	    catch (Exception e){
	        // Audio is not availablev (in use or does not exist)
	    }
	    return a; // returns null if camera is unavailable
	}
}

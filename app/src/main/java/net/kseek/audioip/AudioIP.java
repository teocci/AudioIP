package net.kseek.audioip;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

public class AudioIP extends Activity {
    private CameraPreview preview;
    private CameraManager cameraManager;

	private Audio audio;
	private AudioManager audioManager;

    private boolean started = true;

    private SocketVideo threadVideo;
	private SocketAudio threadAudio;

    private Button button;
    private String remoteIP;
    private int remotePortVideo;
	private int remotePortAudio;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);


		SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.preference_file_key),
				Context.MODE_PRIVATE);
		remoteIP = sharedPref.getString(getString(R.string.ip), "192.168.1.160");
		remotePortVideo = sharedPref.getInt(getString(R.string.port), 8880);
		remotePortAudio = sharedPref.getInt(getString(R.string.port), 8890);

		button = (Button) findViewById(R.id.button_capture);

		button.setOnClickListener(
		    new View.OnClickListener() {
		        @Override
		        public void onClick(View v) {
		            // get an image from the camera
		          if (started) {
		        	  if (remoteIP == null) {
						  threadVideo = new SocketVideo(preview);
						  threadAudio = new SocketAudio(audioManager);
		        	  }
		        	  else {
						  threadVideo = new SocketVideo(preview, remoteIP, remotePortVideo);
						  threadAudio = new SocketAudio(audioManager, remoteIP, remotePortAudio);
		        	  }
		              
		              started = false;
		              button.setText(R.string.stop);
		          }
		          else {
		              closeSocketClient();
		              reset();
		          }
		        }
		    }
		);
		cameraManager = new CameraManager(this);
        // Create our Preview view and set it as the content of our activity.
        preview = new CameraPreview(this, cameraManager.getCamera());
        FrameLayout flPreview = (FrameLayout) findViewById(R.id.camera_preview);
		flPreview.addView(preview);

		audioManager = new AudioManager(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.ipcamera, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		int id = item.getItemId();
		switch (id) {
		case R.id.action_settings:
			setting();
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	private void setting() {
		LayoutInflater factory = LayoutInflater.from(this);
        final View textEntryView = factory.inflate(R.layout.server_setting, null);
		EditText ipEdit = (EditText)textEntryView.findViewById(R.id.ip_edit);
		EditText portEdit = (EditText)textEntryView.findViewById(R.id.port_edit);

		ipEdit.setText(remoteIP);
		portEdit.setText("" + remotePortVideo);

        AlertDialog dialog =  new AlertDialog.Builder(AudioIP.this)
            .setIconAttribute(android.R.attr.alertDialogIcon)
            .setTitle(R.string.setting_title)
            .setView(textEntryView)
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                	EditText ipEdit = (EditText)textEntryView.findViewById(R.id.ip_edit);
                	EditText portEdit = (EditText)textEntryView.findViewById(R.id.port_edit);

					remoteIP = ipEdit.getText().toString();
					remotePortVideo = Integer.parseInt(portEdit.getText().toString());

					SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key),
							Context.MODE_PRIVATE);
					SharedPreferences.Editor editor = sharedPref.edit();
					editor.clear();
					editor.putString(getString(R.string.ip), remoteIP);
					editor.putInt(getString(R.string.port), remotePortVideo);
					editor.commit();

                	Toast.makeText(AudioIP.this, "New address: " + remoteIP + ":" + remotePortVideo, Toast.LENGTH_LONG).show();
                }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                    /* User clicked cancel so do some stuff */
                }
            })
            .create();
        dialog.show();
	}
	
	@Override
    protected void onPause() {
        super.onPause();
        closeSocketClient();
        preview.onPause();
		audioManager.onPause();
        cameraManager.onPause();              // release the camera immediately on pause event
        reset();
    }
	
	private void reset() {
		button.setText(R.string.start);
        started = true;
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		cameraManager.onResume();
		preview.setCamera(cameraManager.getCamera());
	}
	
	private void closeSocketClient() {
		if (threadVideo == null)
			return;

		if (threadAudio == null)
			return;

		threadVideo.interrupt();
		threadAudio.interrupt();
        try {
			threadVideo.join();
			threadAudio.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		threadVideo = null;
		threadAudio = null;
	}
}

package com.craigsc.blowme;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.apphorde.client.AppHordeAd;

public class BlowMe extends Activity {
    private WakeLock wl;
    private boolean recording;
    private AudioThread thread;
    private View view;
    private WindowManager.LayoutParams lp;
    private int bg;
    private static int[] samplingRates = {8000, 11025, 16000, 22050, 44100};
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //no status bars
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); 
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.color);
        AppHordeAd ad = new AppHordeAd(this, "sh1vs35dt1ufcsp5evne");
        layout.addView(ad);
        view = findViewById(R.id.color);
        view.setBackgroundColor(Prefs.getColor(this));
        //wake lock
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, this.getClass().getName());
        //max screen brightness
        lp = getWindow().getAttributes();
        lp.screenBrightness = 1;
        getWindow().setAttributes(lp);
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	wl.acquire();
    	bg = Prefs.getColor(this);
    	if (lp.screenBrightness == 1) {
    		view.setBackgroundColor(bg);
    	}
    	thread = new AudioThread(Prefs.getSensitivity(this), new Handler() {
        	private long last = 0l;
        	public void handleMessage(Message m) {
        		long now = System.currentTimeMillis();
        		if (now-last < 1000) {
        			return;
        		} else {
        			last = now;
        		}
        		if (lp.screenBrightness == .01f) {
        			lp.screenBrightness = 1;
        			view.setBackgroundColor(bg);
        		}
        		else {
        			view.setBackgroundDrawable(null);
        			lp.screenBrightness = .01f;
        		}
        		getWindow().setAttributes(lp);
        	}
        });
    	thread.start();
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	wl.release();
    	recording = false;
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater mi = getMenuInflater();
		mi.inflate(R.menu.menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.settings:
			startActivity(new Intent(this, Prefs.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

    private class AudioThread extends Thread {
    	private Handler handler;
    	private int minVolume;
    	
    	public AudioThread(int level, Handler h) {
    		handler = h;
    		switch (level) {
    		case 0:
    			minVolume = 30000;
    			break;
    		case 1:
    			minVolume = 25000;
    			break;
    		case 2:
    			minVolume = 20000;
    			break;
    		}
    	}
    	
    	public void run() {
    		recording = true;
    		analyze();
    	}
    	
    	public void analyze() {
			for (int i = 0; i < samplingRates.length; i++) {
				try {
					int minSize = AudioRecord.getMinBufferSize(
							samplingRates[i],
							AudioFormat.CHANNEL_CONFIGURATION_MONO,
							AudioFormat.ENCODING_PCM_16BIT);
					AudioRecord ar = new AudioRecord(
							MediaRecorder.AudioSource.MIC, samplingRates[i],
							AudioFormat.CHANNEL_CONFIGURATION_MONO,
							AudioFormat.ENCODING_PCM_16BIT, minSize);
					if (ar.getState() == AudioRecord.STATE_INITIALIZED) {
						short[] buffer = new short[minSize];
						ar.startRecording();
						while (recording) {
							ar.read(buffer, 0, minSize);
							for (short s : buffer) {
								if (Math.abs(s) > minVolume) {
									handler.sendEmptyMessage(0);
								}
							}
						}
						ar.stop();
						i = samplingRates.length;
					}
					ar.release();
					ar = null;
				} catch (Exception e) {
					// just try the next sampling rate
				}
    		}
    	}
    }
}
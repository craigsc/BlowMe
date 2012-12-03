package com.craigsc.blowme;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.craigsc.blowme.ColorPickerDialog.OnColorChangedListener;

public class Prefs extends PreferenceActivity implements OnColorChangedListener {
	public static final int COLOR_DIALOG = 0;
	public static final int SENSITIVITY_DIALOG = 1;
	private SharedPreferences prefs;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
		
		prefs = getPreferenceManager().getSharedPreferences();
		Preference color = findPreference("color");
		color.setOnPreferenceClickListener(new OnPreferenceClickListener() {	
			public boolean onPreferenceClick(Preference preference) {
				showDialog(COLOR_DIALOG);
				return true;
			}
		});
		Preference sensitivity = findPreference("sensitivity");
		sensitivity.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				showDialog(SENSITIVITY_DIALOG);
				return true;
			}
		});
	}
	
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		switch (id) {
		case COLOR_DIALOG:
			dialog = new ColorPickerDialog(this, this, getColor(this));
			break;
		case SENSITIVITY_DIALOG:
			final CharSequence[] items = {"Low", "Medium", "High"};

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Mic Sensitivity");
			builder.setSingleChoiceItems(items, prefs.getInt("sensitivity", 1),
					new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int item) {
			        saveSensitivity(item);
			        dialog.dismiss();
			    }
			});
			dialog = builder.create();
			break;
		}
		return dialog;
	}

	public void saveSensitivity(int level) {
		prefs.edit().putInt("sensitivity", level).commit();
	}
	
	public void colorChanged(int color) {
		prefs.edit().putInt("color", color).commit();
	}
	
	public static int getSensitivity(Context c) {
		return PreferenceManager.getDefaultSharedPreferences(c)
				.getInt("sensitivity", 1);
	}
	
	public static int getColor(Context c) {
		return PreferenceManager.getDefaultSharedPreferences(c)
				.getInt("color", Color.MAGENTA);//default of magenta
	}

}

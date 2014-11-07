package com.myMinistry.ui;

import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;

import com.myMinistry.R;
import com.myMinistry.util.HelpUtils;

public class SettingsActivity extends PreferenceActivity {
	private String versionName = "0";
	
	@SuppressLint("InflateParams")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.preferences);
		
		try {
			PackageInfo pi = getPackageManager().getPackageInfo(
					getPackageName(), 0);
			versionName = pi.versionName;
		} catch (NameNotFoundException e) {}
		
		if(!versionName.endsWith("W")) {
			PreferenceScreen ps = getPreferenceScreen();
			ps.removePreference(findPreference("checkForUpdate"));
		}
		
		findPreference("version").setTitle(getApplicationContext().getString(R.string.app_name) + " " + versionName);
        
        findPreference("recalculate_ro_time").setOnPreferenceClickListener(new OnPreferenceClickListener() {
        	public boolean onPreferenceClick(final Preference preference) {
    			final ProgressDialog ringProgressDialog = ProgressDialog.show(preference.getContext(), getResources().getString(R.string.updating_app), getResources().getString(R.string.please_be_patient), true);
        		ringProgressDialog.setCancelable(true);
        		new Thread(new Runnable() {
        			@Override
        			public void run() {
        				try {
        					HelpUtils.processRolloverTime(getApplicationContext());
        				} catch (Exception e) {

        				}
        				ringProgressDialog.dismiss();
        			}
        		}).start();
				return true;
        	}
        });
        
        findPreference("email_developer").setOnPreferenceClickListener(new OnPreferenceClickListener() {
        	public boolean onPreferenceClick(final Preference preference) {
        		AlertDialog.Builder builder = new AlertDialog.Builder(preference.getContext());
        		final String[] items = preference.getContext().getResources().getStringArray(R.array.contact_email_entries);
        		builder.setTitle(preference.getContext().getString(R.string.menu_options));
        		builder.setItems(items, new DialogInterface.OnClickListener() {
        			public void onClick(DialogInterface dialog, int which) {
        				String emailFooter = "\n\n\n" + preference.getContext().getString(R.string.email_content_version_with_space) + versionName
        									+ "\n" + preference.getContext().getString(R.string.email_content_brand_with_space) + android.os.Build.BRAND
        									+ "\n" + preference.getContext().getString(R.string.email_content_model_with_space) + android.os.Build.MODEL
        									+ "\n" + preference.getContext().getString(R.string.email_content_sdk_with_space) + android.os.Build.VERSION.SDK_INT
        									+ "\n" + preference.getContext().getString(R.string.email_content_locale_with_space) + Locale.getDefault().toString();
        				
        				String[] values = preference.getContext().getResources().getStringArray(R.array.contact_email_entryValues);
                		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                		emailIntent.setType("text/plain");
                		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] {values[which]});
                		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, items[which]);
                		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, emailFooter);
                		startActivity(Intent.createChooser(emailIntent, null));
        			}
        		});
        	    builder.create().show();
				return true;
        	}
        });
        
        findPreference("donate").setOnPreferenceClickListener(new OnPreferenceClickListener() {
        	public boolean onPreferenceClick(Preference preference) {
        		String url = getApplicationContext().getString(R.string.link_donate);  
        		Intent i = new Intent(Intent.ACTION_VIEW);  
        		i.setData(Uri.parse(url));  
        		startActivity(i);
        		return true;
        		
        	}
        });
        
        findPreference("licences").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					HelpUtils.showOpenSourceLicenses(SettingsActivity.this);
				} else {
					LayoutInflater li = LayoutInflater.from(SettingsActivity.this);
	        		View promptsView = li.inflate(R.layout.d_webview, null);
	        		
	        		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SettingsActivity.this);
	        		alertDialogBuilder.setTitle(R.string.pref_about_licenses);
	        		alertDialogBuilder.setView(promptsView);
	        		final WebView webview = (WebView) promptsView.findViewById(R.id.webview_dialog);
	        		webview.loadUrl("file:///android_asset/licenses.html");
	        		final AlertDialog alertDialog = alertDialogBuilder.create();
	        		alertDialog.show();
				}
        		return true;
        	}
        });
        
        findPreference("changelog").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
        		String url = getApplicationContext().getString(R.string.link_changelog);  
        		Intent i = new Intent(Intent.ACTION_VIEW);  
        		i.setData(Uri.parse(url));  
        		startActivity(i);
        		return true;
        	}
        });
        
		if (findPreference("checkForUpdate") != null) {
			findPreference("checkForUpdate").setOnPreferenceClickListener(new OnPreferenceClickListener() {
				public boolean onPreferenceClick(Preference preference) {
					String url = getApplicationContext().getString(R.string.link_download_with_slash) + versionName;
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(Uri.parse(url));
					startActivity(i);
					return true;
				}
			});
		}
    }
}
/*Copyright (C) 2015 The ResurrectionRemix Project
     Licensed under the Apache License, Version 2.0 (the "License");
     you may not use this file except in compliance with the License.
     You may obtain a copy of the License at

          http://www.apache.org/licenses/LICENSE-2.0

     Unless required by applicable law or agreed to in writing, software
     distributed under the License is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     See the License for the specific language governing permissions and
     limitations under the License.
*/
package com.android.settings.rr;

import android.app.Activity;
import android.content.Context;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.ListPreference;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

import cyanogenmod.providers.CMSettings;

import com.android.internal.logging.MetricsProto.MetricsEvent;

public class BatterySettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {


    private static final String PREF_BATT_BAR = "battery_bar_list";
    private static final String PREF_BATT_BAR_STYLE = "battery_bar_style";
    private static final String PREF_BATT_BAR_WIDTH = "battery_bar_thickness";
	private static final String PREF_BATT_ANIMATE = "battery_bar_animate";
    private static final String STATUS_BAR_BATTERY_STYLE = "status_bar_battery_style";
    private static final String STATUS_BAR_SHOW_BATTERY_PERCENT = "status_bar_show_battery_percent";



    private static final int STATUS_BAR_BATTERY_STYLE_HIDDEN = 4;
    private static final int STATUS_BAR_BATTERY_STYLE_TEXT = 6;

    private ListPreference mStatusBarBattery;
    private ListPreference mStatusBarBatteryShowPercent;
    private ListPreference mBatteryBar;
    private ListPreference mBatteryBarStyle;
    private ListPreference mBatteryBarThickness;
	private SwitchPreference mBatteryBarChargingAnimation;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.rr_battery);

        final ContentResolver resolver = getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();
        final Resources res = getResources();

        mBatteryBar = (ListPreference) findPreference(PREF_BATT_BAR);
        mBatteryBar.setOnPreferenceChangeListener(this);
        mBatteryBar.setValue((Settings.System.getInt(resolver,
                        Settings.System.STATUSBAR_BATTERY_BAR, 0)) + "");

        mBatteryBarStyle = (ListPreference) findPreference(PREF_BATT_BAR_STYLE);
        mBatteryBarStyle.setOnPreferenceChangeListener(this);
        mBatteryBarStyle.setValue((Settings.System.getInt(resolver,
                Settings.System.STATUSBAR_BATTERY_BAR_STYLE, 0)) + "");

        mBatteryBarChargingAnimation = (SwitchPreference) findPreference(PREF_BATT_ANIMATE);
        mBatteryBarChargingAnimation.setChecked(Settings.System.getInt(resolver,
                Settings.System.STATUSBAR_BATTERY_BAR_ANIMATE, 0) == 1);

        mBatteryBarThickness = (ListPreference) findPreference(PREF_BATT_BAR_WIDTH);
        mBatteryBarThickness.setOnPreferenceChangeListener(this);
        mBatteryBarThickness.setValue((Settings.System.getInt(resolver,
		Settings.System.STATUSBAR_BATTERY_BAR_THICKNESS, 1)) + "");

        mStatusBarBattery = (ListPreference) findPreference(STATUS_BAR_BATTERY_STYLE);
        mStatusBarBatteryShowPercent =
                (ListPreference) findPreference(STATUS_BAR_SHOW_BATTERY_PERCENT);

        int batteryStyle = CMSettings.System.getInt(resolver,
                CMSettings.System.STATUS_BAR_BATTERY_STYLE, 0);
        mStatusBarBattery.setValue(String.valueOf(batteryStyle));
        mStatusBarBattery.setSummary(mStatusBarBattery.getEntry());
        mStatusBarBattery.setOnPreferenceChangeListener(this);

        int batteryShowPercent = CMSettings.System.getInt(resolver,
                CMSettings.System.STATUS_BAR_SHOW_BATTERY_PERCENT, 0);
        mStatusBarBatteryShowPercent.setValue(String.valueOf(batteryShowPercent));
        mStatusBarBatteryShowPercent.setSummary(mStatusBarBatteryShowPercent.getEntry());
        enableStatusBarBatteryDependents(batteryStyle);
        mStatusBarBatteryShowPercent.setOnPreferenceChangeListener(this);
    }


 	public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mBatteryBar) {
            int val = Integer.parseInt((String) newValue);
            return Settings.System.putInt(resolver,
                    Settings.System.STATUSBAR_BATTERY_BAR, val);
        } else if (preference == mBatteryBarStyle) {
            int val = Integer.parseInt((String) newValue);
            return Settings.System.putInt(resolver,
                    Settings.System.STATUSBAR_BATTERY_BAR_STYLE, val);
        } else if (preference == mBatteryBarThickness) {
            int val = Integer.parseInt((String) newValue);
            return Settings.System.putInt(resolver,
                    Settings.System.STATUSBAR_BATTERY_BAR_THICKNESS, val);
		} else if (preference == mStatusBarBattery) {
            int batteryStyle = Integer.valueOf((String) newValue);
            int index = mStatusBarBattery.findIndexOfValue((String) newValue);
            CMSettings.System.putInt(
                    resolver, CMSettings.System.STATUS_BAR_BATTERY_STYLE, batteryStyle);
            mStatusBarBattery.setSummary(mStatusBarBattery.getEntries()[index]);
            enableStatusBarBatteryDependents(batteryStyle);
            return true;
        } else if (preference == mStatusBarBatteryShowPercent) {
            int batteryShowPercent = Integer.valueOf((String) newValue);
            int index = mStatusBarBatteryShowPercent.findIndexOfValue((String) newValue);
            CMSettings.System.putInt(
                    resolver, CMSettings.System.STATUS_BAR_SHOW_BATTERY_PERCENT, batteryShowPercent);
            mStatusBarBatteryShowPercent.setSummary(
                    mStatusBarBatteryShowPercent.getEntries()[index]);
            return true;
    }
	return false;
	}

   public boolean onPreferenceTreeClick(Preference preference) {
        ContentResolver resolver = getActivity().getContentResolver();
        boolean value;

        if (preference == mBatteryBarChargingAnimation) {
            Settings.System.putInt(resolver,
                    Settings.System.STATUSBAR_BATTERY_BAR_ANIMATE,
                    ((SwitchPreference) preference).isChecked() ? 1 : 0);
            return true;
        }
        return false;
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.APPLICATION;
    }

    private void enableStatusBarBatteryDependents(int batteryIconStyle) {
        if (batteryIconStyle == STATUS_BAR_BATTERY_STYLE_HIDDEN ||
                batteryIconStyle == STATUS_BAR_BATTERY_STYLE_TEXT) {
            mStatusBarBatteryShowPercent.setEnabled(false);
        } else {
            mStatusBarBatteryShowPercent.setEnabled(true);
        }
    }

}

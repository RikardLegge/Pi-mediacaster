package com.rikardlegge.mediacaster;

import java.io.ByteArrayInputStream;

import com.rikardlegge.mediacaster.fragments.ImageFragment;
import com.rikardlegge.mediacaster.fragments.SetupFragment;
import com.rikardlegge.mediacaster.fragments.VideoFragment;
import com.rikardlegge.mediacaster.helpers.Commandid;
import com.rikardlegge.mediacaster.helpers.CustomViewPager;
import com.rikardlegge.mediacaster.helpers.SendHandle;
import com.rikardlegge.mediacaster.helpers.Settings;
import com.rikardlegge.mediacaster.helpers.SocketCallback;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;

public class MainActivity extends FragmentActivity {

	static SectionsPagerAdapter sectionsPagerAdapter;
	CustomViewPager viewPager;

	public static SendHandle sendHandle;
	public static String state = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_controller);

		sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
		viewPager = (CustomViewPager) findViewById(R.id.pager);
		viewPager.setAdapter(sectionsPagerAdapter);

		OnPageChangeListener onPageChangeListener = new OnPageChangeListener() {

			public void onPageSelected(int page) {
				GetState();
			}

			public void onPageScrolled(int arg0, float arg1, int arg2) {}

			public void onPageScrollStateChanged(int arg0) {}
		};

		viewPager.setOnPageChangeListener(onPageChangeListener);

		// Load settings specified in client aplication
		// Needs to be called for the settingsvariables to be set.
		new Settings(getSharedPreferences("Settings", Context.MODE_PRIVATE), this);
		sendHandle = new SendHandle();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.main, menu);
		return false;
	}

	@Override
	public boolean onKeyDown(int keycode, KeyEvent e) {
		switch (keycode) {
			case KeyEvent.KEYCODE_BACK:
				if (viewPager.getCurrentItem() > 0) {
					viewPager.setCurrentItem(0);
					return true;
				}
		}
		return super.onKeyDown(keycode, e);
	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int index) {

			switch (index) {
				case 0:
					return new SetupFragment();
				case 1:
					return new ImageFragment();
				case 2:
					return new VideoFragment();
			}

			return null;
		}

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
				case 0:
					return getString(R.string.Setup);
				case 1:
					if (state.contains("image"))
						return "( " + getString(R.string.Image) + " )";
					else
						return getString(R.string.Image);
				case 2:
					if (state.contains("video"))
						return "( " + getString(R.string.Video) + " )";
					else
						return getString(R.string.Video);
			}
			return null;
		}

	}

	public static void GetState() {
		String str = "state¤";
		ByteArrayInputStream iStream = new ByteArrayInputStream(str.getBytes());
		MainActivity.sendHandle.sendData((byte) Commandid.GetInfo.Id(), iStream, false, new SocketCallback() {
			public void response(String str) {
				str = str.substring(0, str.length() - 1);
				if (state != str) sectionsPagerAdapter.notifyDataSetChanged();
				state = str;
			}
		});
	}

	public void TitleStrip_OnClick(View v) {
		PagerTitleStrip titleStrip = (PagerTitleStrip) findViewById(R.id.pager_title_strip);
		if (viewPager.getPagingEnabled()) {
			viewPager.setPagingEnabled(false);
			titleStrip.setBackgroundColor(Color.parseColor("#222222"));
		} else {
			viewPager.setPagingEnabled(true);
			titleStrip.setBackgroundColor(Color.parseColor("#33b5e5"));
		}
	}
}

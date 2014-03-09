package com.rikardlegge.mediacaster.fragments;

import java.io.ByteArrayInputStream;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.rikardlegge.mediacaster.MainActivity;
import com.rikardlegge.mediacaster.R;
import com.rikardlegge.mediacaster.helpers.Commandid;

public class VideoFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_video, container, false);

		final ImageButton playPause = (ImageButton) root.findViewById(R.id.Button_playpause);
		final Button forward600s = (Button) root.findViewById(R.id.Button_Forward600);
		final Button back600s = (Button) root.findViewById(R.id.Button_back600);
		final Button forward30s = (Button) root.findViewById(R.id.Button_forward30);
		final Button back30s = (Button) root.findViewById(R.id.Button_back30);

		OnClickListener clickListener = new OnClickListener() {
			public void onClick(View v) {
				if (v.getId() == playPause.getId())
					PlayPause();
				else if (v.getId() == forward600s.getId())
					forward600s();
				else if (v.getId() == back600s.getId())
					back600s();
				else if (v.getId() == forward30s.getId())
					forward30s();
				else if (v.getId() == back30s.getId())
					back30s();
				else if (v.getId() == forward600s.getId()) forward600s();
			}
		};

		playPause.setOnClickListener(clickListener);
		forward600s.setOnClickListener(clickListener);
		back600s.setOnClickListener(clickListener);
		forward30s.setOnClickListener(clickListener);
		back30s.setOnClickListener(clickListener);

		return root;
	}

	public void PlayPause() {
		ByteArrayInputStream iStream = new ByteArrayInputStream(new String("pause").getBytes());
		MainActivity.sendHandle.sendData((byte) Commandid.Video_Controll.Id(), iStream, false);
	}

	public void forward600s() {
		ByteArrayInputStream iStream = new ByteArrayInputStream(new String("seek600").getBytes());
		MainActivity.sendHandle.sendData((byte) Commandid.Video_Controll.Id(), iStream, false);
	}

	public void back600s() {
		ByteArrayInputStream iStream = new ByteArrayInputStream(new String("seek-600").getBytes());
		MainActivity.sendHandle.sendData((byte) Commandid.Video_Controll.Id(), iStream, false);
	}

	public void forward30s() {
		ByteArrayInputStream iStream = new ByteArrayInputStream(new String("seek30").getBytes());
		MainActivity.sendHandle.sendData((byte) Commandid.Video_Controll.Id(), iStream, false);
	}

	public void back30s() {
		ByteArrayInputStream iStream = new ByteArrayInputStream(new String("seek-30").getBytes());
		MainActivity.sendHandle.sendData((byte) Commandid.Video_Controll.Id(), iStream, false);
	}
}

package com.rikardlegge.mediacaster.fragments;

import java.io.ByteArrayInputStream;

import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.rikardlegge.mediacaster.MainActivity;
import com.rikardlegge.mediacaster.R;
import com.rikardlegge.mediacaster.helpers.Commandid;
import com.rikardlegge.mediacaster.helpers.SocketCallback;

public class ImageFragment extends Fragment implements OnTouchListener {

	// these matrices will be used to move and zoom image
	private Matrix matrix = new Matrix();
	private Matrix savedMatrix = new Matrix();
	// we can be in one of these 3 states
	private static final int NONE = 0;
	private static final int DRAG = 1;
	private static final int ZOOM = 2;
	private int mode = NONE;
	// remember some things for zooming
	private PointF start = new PointF();
	private PointF mid = new PointF();
	private float oldDist = 1f;
	private float d = 0f;
	private float newRot = 0f;
	private float[] lastEvent = null;

	private PointF position = new PointF(0, 0);
	private float rotation = 0;
	private PointF size = new PointF(0, 0);
	private float scale = 1;

	private PointF originalSize = new PointF(0, 0);
	private PointF screenSize = new PointF(0, 0);

	private long lastSend = 0;
	private boolean instant = true;
	View root;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		root = inflater.inflate(R.layout.fragment_image, container, false);

		ImageView view = (ImageView) root.findViewById(R.id.contentImageView);
		view.setOnTouchListener(this);

		final ImageButton autoScale = (ImageButton) root.findViewById(R.id.Button_AutoScale);
		final ImageButton reset = (ImageButton) root.findViewById(R.id.Button_Reset);
		final ImageButton toggleInstant = (ImageButton) root.findViewById(R.id.Button_ToggleInstant);

		OnClickListener clickListener = new OnClickListener() {
			public void onClick(View v) {
				if (v.getId() == autoScale.getId())
					AutoScale();
				else if (v.getId() == reset.getId())
					Reset();
				else if (v.getId() == toggleInstant.getId()) ToggleInstant();
			}
		};

		GetScreenSize();

		autoScale.setOnClickListener(clickListener);
		reset.setOnClickListener(clickListener);
		toggleInstant.setOnClickListener(clickListener);

		return root;
	}

	public void onResume() {
		super.onStart();
		GetImageProperties();
		GetScreenSize();
	}

	public void onStop() {
		super.onStop();
		originalSize.x = 0;
		originalSize.y = 0;
	}

	protected void GetImageProperties() {
		String str = "imagecontent¤";
		ByteArrayInputStream iStream = new ByteArrayInputStream(str.getBytes());
		MainActivity.sendHandle.sendData((byte) Commandid.GetInfo.Id(), iStream, false, new SocketCallback() {
			public void response(String str) {
				String split[] = new String[5];
				int vals[] = new int[5];

				try {
					Integer.parseInt(str.substring(str.length() - 1, str.length()));
				} catch (Exception e) {
					str = str.substring(0, str.length() - 1);
				}

				split = str.split(":");
				try {
					vals[0] = Integer.parseInt(split[0]);
					vals[1] = Integer.parseInt(split[1]);
					vals[2] = Integer.parseInt(split[2]);
					vals[3] = Integer.parseInt(split[3]);
					vals[4] = Integer.parseInt(split[4]);

					position.x = vals[0];
					position.y = vals[1];
					originalSize.x = vals[2];
					originalSize.y = vals[3];
					rotation = vals[4];
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	protected void GetScreenSize() {
		String str = "screensize¤";
		ByteArrayInputStream iStream = new ByteArrayInputStream(str.getBytes());
		MainActivity.sendHandle.sendData((byte) Commandid.GetInfo.Id(), iStream, false, new SocketCallback() {
			public void response(String str) {
				String split[] = new String[5];
				int vals[] = new int[5];

				try {
					Integer.parseInt(str.substring(str.length() - 1, str.length()));
				} catch (Exception e) {
					str = str.substring(0, str.length() - 1);
				}

				split = str.split(":");
				try {
					vals[0] = Integer.parseInt(split[0]);
					vals[1] = Integer.parseInt(split[1]);

					screenSize.x = vals[0];
					screenSize.y = vals[1];
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	protected void AutoScale() {

		if (screenSize.x == 0 || screenSize.y == 0) {
			GetScreenSize();
			return;
		}

		if (originalSize.x * scale == screenSize.x)
			scale = screenSize.y / originalSize.y;
		else
			scale = screenSize.x / originalSize.x;

		System.out.println(scale);

		position.x = 0;
		position.y = 0;

		update(true);
	}

	protected void ToggleInstant() {
		ImageButton toggleInstant = (ImageButton) root.findViewById(R.id.Button_ToggleInstant);

		instant = !instant;
		if (instant)
			toggleInstant.setImageResource(android.R.drawable.presence_online);
		else
			toggleInstant.setImageResource(android.R.drawable.presence_offline);

	}

	protected void Reset() {
		position.x = 0;
		position.y = 0;

		rotation = 0;
		scale = 1;

		update(true);
	}

	protected void update(boolean relese) {
		if ((instant || relese) && System.currentTimeMillis() - lastSend > 50) {
			lastSend = System.currentTimeMillis();

			if (originalSize.x == 0 || originalSize.y == 0) {
				GetImageProperties();
				return;
			}

			size.x = (int) (originalSize.x * scale);
			size.y = (int) (originalSize.y * scale);

			String str = "full:" + (int) position.x + ":" + (int) position.y + ":" + (int) size.x + ":" + (int) size.y + ":"
					+ (int) rotation + "¤";
			ByteArrayInputStream iStream = new ByteArrayInputStream(str.getBytes());
			MainActivity.sendHandle.sendData((byte) Commandid.Image_Controll.Id(), iStream, false);
		}
	}

	public boolean onTouch(View v, MotionEvent event) {
		// handle touch events here
		ImageView view = (ImageView) v;
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				savedMatrix.set(matrix);
				start.set(event.getX(), event.getY());
				mode = DRAG;
				lastEvent = null;
			break;
			case MotionEvent.ACTION_POINTER_DOWN:
				oldDist = spacing(event);
				if (oldDist > 10f) {
					savedMatrix.set(matrix);
					midPoint(mid, event);
					mode = ZOOM;
				}
				lastEvent = new float[4];
				lastEvent[0] = event.getX(0);
				lastEvent[1] = event.getX(1);
				lastEvent[2] = event.getY(0);
				lastEvent[3] = event.getY(1);
				d = rotation(event);
			break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
				mode = NONE;
				lastEvent = null;
				update(true);
			break;
			case MotionEvent.ACTION_MOVE:
				if (mode == DRAG) {
					matrix.set(savedMatrix);
					float dx = event.getX() - start.x;
					float dy = event.getY() - start.y;
					start.x = event.getX();
					start.y = event.getY();
					matrix.postTranslate(dx, dy);
					position.x += dx;
					position.y += dy;
				} else if (mode == ZOOM) {
					float newDist = spacing(event);
					if (newDist > 10f) {
						matrix.set(savedMatrix);
						float scale = (newDist / oldDist);
						oldDist = newDist;
						matrix.postScale(scale, scale, mid.x, mid.y);
						this.scale += (scale - 1);
					}
					if (lastEvent != null && event.getPointerCount() == 3) {
						newRot = rotation(event);
						float r = newRot - d;
						d = newRot;
						float[] values = new float[9];
						matrix.getValues(values);
						float tx = values[2];
						float ty = values[5];
						float sx = values[0];
						float xc = (view.getWidth() / 2) * sx;
						float yc = (view.getHeight() / 2) * sx;
						matrix.postRotate(r, tx + xc, ty + yc);
						rotation += r;
					}
				}
			break;
		}
		update(false);
		// view.setImageMatrix(matrix);
		return true;
	}

	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return (float) Math.sqrt(x * x + y * y);
	}

	private void midPoint(PointF point, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}

	private float rotation(MotionEvent event) {
		double delta_x = (event.getX(0) - event.getX(1));
		double delta_y = (event.getY(0) - event.getY(1));
		double radians = Math.atan2(delta_y, delta_x);
		return (float) Math.toDegrees(radians);
	}
}

package kz.crystalspring.livewallpaper;

import com.boomerang.drawobjects.MyPoint;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

public class GravityManager implements SensorEventListener
{
	Context context;
	private SensorManager mgr;
	private Sensor accelerometer;
	private float[] gravity = new float[3];
	private float[] motion = new float[3];
	private double ratio;
	static double mAngle;
	private int counter = 0;

	public GravityManager(Context context)
	{
		this.context = context;
		mgr = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		accelerometer = mgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	}

	public void beginRegister()
	{
		mgr.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);

	}

	public void pauseRegister()
	{
		mgr.unregisterListener(this, accelerometer);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void onSensorChanged(SensorEvent event)
	{
		for (int i = 0; i < 3; i++)
		{
			gravity[i] = (float) (0.1 * event.values[i] + 0.9 * gravity[i]);
			motion[i] = event.values[i] - gravity[i];
		}

		double gr_y = gravity[1] / 10;
		double gr_x = gravity[0] / 10;

		mAngle = (Math.toDegrees(Math.atan(gr_y / gr_x)));
		if (gr_x < 0)
			mAngle = 180 + mAngle;
		else if (gr_y < 0)
			mAngle = 360 + mAngle;

		mAngle=(360 + mAngle-270+getRotation()) % 360;
		
		if (counter++ % 10 == 0)
		{
			String msg = String.format("Raw values\nX: %8.4f\nY: %8.4f\nZ: %8.4f\n", event.values[0], event.values[1], event.values[2]);
			msg = String.format("Gravity\nX: %8.4f\nY: %8.4f\n", gr_x, gr_y);
			msg = String.format("Angle\n %8.4f\n", mAngle);
			counter = 1;
			if (Math.abs(gravity[0]) + Math.abs(gravity[1]) > 1)
			{
				MyPoint.x_gravity = gravity[0];
				MyPoint.y_gravity = gravity[1];
				MyPoint.setAngle(mAngle);
			}
		}
	}

	public int getRotation()
	{
		int orientation = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getOrientation();

		switch (orientation)
		{
		case Surface.ROTATION_0:
			return 0;
		case Surface.ROTATION_90:
			return 90;
		case Surface.ROTATION_180:
			return 180;
		case Surface.ROTATION_270:
			return 270;
		default:
			return 0;
		}
	}

	public double getAngle()
	{
		return mAngle;
	}
}

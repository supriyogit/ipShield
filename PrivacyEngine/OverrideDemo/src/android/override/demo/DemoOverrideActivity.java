package android.override.demo;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.OverrideSensorManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.ILocationManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.OverrideLocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ServiceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class DemoOverrideActivity extends Activity {

  private static final String TAG = "DemoOverrideActivity";

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    onCreateLocationStuff();
    onCreateAccelerometerStuff();
    onCreateOverrideStuff();
  }


  @Override
  protected void onResume() {
    super.onResume();
    onResumeLocationStuff();
  }

  @Override
  protected void onPause() {
    onPauseLocationStuff();
    super.onPause();

  }
  // ----------------------------------------------------------------------------------------------
  //        ACCELEROMETER STUFF
  // ----------------------------------------------------------------------------------------------
  private SensorManager mSensorManager;
  private void onCreateAccelerometerStuff(){
    //mSensorManager = new OverrideSensorManager(this, this.getMainLooper());
    mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

    mSensorManager.registerListener(sensorEventListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_NORMAL);
  }

  SensorEventListener sensorEventListener = new SensorEventListener(){
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
      String accData = "suppressed";
      if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
        accData = Double.toString(sensorEvent.values[0]);
      }
      TextView textView = (TextView)findViewById(R.id.txtAccel);
      textView.setText(accData);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
  };


  // ----------------------------------------------------------------------------------------------
  //        LOCATION STUFF
  // ----------------------------------------------------------------------------------------------
  private LocationManager mLocationManager;

  private void onCreateLocationStuff() {
    IBinder binder = ServiceManager.getService(LOCATION_SERVICE);
    ILocationManager service = ILocationManager.Stub.asInterface(binder);
    mLocationManager = new OverrideLocationManager(this, service);
  }


  private void onPauseLocationStuff() {
    mLocationManager.removeUpdates(locationListener);
  }

  private void onResumeLocationStuff() {
    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                                            locationListener);
  }


  LocationListener locationListener = new LocationListener() {
    @Override
    public void onLocationChanged(Location location) {
      String
          locationText =
          "lat = " + location.getLatitude() + ", lon = " + location.getLongitude();
      Log.d(TAG, locationText);
      TextView textView = (TextView) findViewById(R.id.txtLocation);
      textView.setText(locationText);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
  };

  // ----------------------------------------------------------------------------------------------
  //        SUPPRESS / PERTURB / ALLOW LOCATION UPDATES TO THIS APP
  // ----------------------------------------------------------------------------------------------

  private void onCreateOverrideStuff() {
    {
      Button b = (Button) findViewById(R.id.btnSuppress);
      b.setOnClickListener(onSuppressClickListener);
    }

    {
      Button b = (Button) findViewById(R.id.btnReset);
      b.setOnClickListener(onResetClickListener);
    }

    {
      Button b = (Button) findViewById(R.id.btnPerturb);
      b.setOnClickListener(onPerturbClickListener);
    }

    {
      Button b = (Button) findViewById(R.id.btnAccRelease);
      b.setOnClickListener(onAccReleaseClickListener);
    }

    {
      Button b = (Button) findViewById(R.id.btnAccSuppress);
      b.setOnClickListener(onAccSuppressClickListener);
    }

    {
      EditText e = (EditText) findViewById(R.id.edtPackage);
      e.setText(getEnteredPackageName());
    }

    {
      //TextView t = (TextView) findViewById(R.id.txtInstalledPackages);
      //t.setText(getInstalledPackages());
    }
  }

  private String getEnteredPackageName() {
    return "com.lul.accelerometer";
    //return "com.facebook.katana";
    //Log.d("PACKAGE_NAME",getPackageName());
    //return getPackageName();
    //EditText e = (EditText) findViewById(R.id.edtPackage);
    //return e.getText().toString();
  }

  private String getInstalledPackages() {
    StringBuilder builder = new StringBuilder();
    PackageManager pm = getPackageManager();
    for (PackageInfo info : pm.getInstalledPackages(1000)) {
      builder.append(info.packageName);
      builder.append(",");
    }
    return builder.toString();
  }

  android.view.View.OnClickListener onSuppressClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      Intent suppressIntent = new Intent("android.override.OverrideCommanderService");
      suppressIntent.putExtra("COMMAND", OverrideLocationManager.COMMAND_SUPPRESS);
      suppressIntent.putExtra("PACKAGE", getEnteredPackageName());
      startService(suppressIntent);
    }
  };

  android.view.View.OnClickListener onResetClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      Intent releaseIntent = new Intent("android.override.OverrideCommanderService");
      releaseIntent.putExtra("COMMAND", OverrideLocationManager.COMMAND_RELEASE);
      releaseIntent.putExtra("PACKAGE", getEnteredPackageName());
      startService(releaseIntent);
    }
  };

  android.view.View.OnClickListener onPerturbClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      Intent perturbIntent = new Intent("android.override.OverrideCommanderService");
      perturbIntent.putExtra("COMMAND", OverrideLocationManager.COMMAND_PERTURB);
      perturbIntent.putExtra("perturb_variance", 0.1);
      perturbIntent.putExtra("PACKAGE", getEnteredPackageName());
      startService(perturbIntent);
    }
  };
  android.view.View.OnClickListener onAccSuppressClickListener = new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent accSuppressIntent = new Intent("android.override.OverrideCommanderService");
        accSuppressIntent.putExtra("COMMAND", OverrideSensorManager.COMMAND_SUPPRESS);
        accSuppressIntent.putExtra("PACKAGE", getEnteredPackageName());
        startService(accSuppressIntent);
      }
  };

  android.view.View.OnClickListener onAccReleaseClickListener = new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent accReleaseIntent = new Intent("android.override.OverrideCommanderService");
        accReleaseIntent.putExtra("COMMAND", OverrideSensorManager.COMMAND_RELEASE);
        accReleaseIntent.putExtra("PACKAGE", getEnteredPackageName());
        startService(accReleaseIntent);
      }
  };
}

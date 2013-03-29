package android.hardware;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.override.IOverrideCommandListener;
import android.override.IOverrideCommander;
import android.util.Log;

import java.util.HashMap;
import java.util.List;

public class OverrideSensorManager extends SensorManager{
  private static final String TAG = "OverrideSensorManager";

  public static final int COMMAND_UNDEFINED = 0;
  public static final int COMMAND_RELEASE = 1;
  public static final int COMMAND_SUPPRESS = 2;
  // generate samples which match a given variance.
  public static final int COMMAND_PERTURB = 3;

  private Context mContext;
  private HashMap<SensorEventListener, SensorEventListener> mWrappedListeners;
  private IOverrideCommander mCommander;
  private int mCommandState;
  //private HashMap<Sensor, SensorEvent> mLastSensorEvent = new HashMap<Sensor, SensorEvent>()

  public OverrideSensorManager(Context context, Looper looper){
    super(looper);
    mContext = context;
    mWrappedListeners = new HashMap<SensorEventListener, SensorEventListener>();
    mCommandState = COMMAND_UNDEFINED;
    Intent bindIntent = new Intent("android.override.OverrideCommanderService");
    mContext.bindService(bindIntent, mCommanderConnection,
                         Context.BIND_AUTO_CREATE | Context.BIND_DEBUG_UNBIND);
  }

  @Override
  public void finalize() throws Throwable {
    if (mCommander != null) {
      mCommander.removeCommandListener(mContext.getPackageName(), mCommandListener);
      mContext.unbindService(mCommanderConnection);
    }
    super.finalize();
  }

  @Override
  public List<Sensor> getSensorList(int type) {
    return super.getSensorList(type);

  }

  @Override
  public Sensor getDefaultSensor(int type) {
    return super.getDefaultSensor(type);

  }

  @Override
  public void unregisterListener(SensorEventListener listener, Sensor sensor) {
    final SensorEventListener wrappedListener = getWrappedListener(listener);
    super.unregisterListener(wrappedListener, sensor);
    mWrappedListeners.remove(listener);
  }

  @Override
  public void unregisterListener(SensorEventListener listener) {
    final SensorEventListener wrappedListener = getWrappedListener(listener);
    super.unregisterListener(wrappedListener);
    mWrappedListeners.remove(listener);
  }

  @Override
  public boolean registerListener(SensorEventListener listener, Sensor sensor, int rate) {
    final SensorEventListener wrappedListener = getWrappedListener(listener);
    return super.registerListener(wrappedListener, sensor, rate);
  }

  @Override
  public boolean registerListener(SensorEventListener listener, Sensor sensor, int rate,
                                  Handler handler) {
    final SensorEventListener wrappedListener = getWrappedListener(listener);
    return super.registerListener(wrappedListener, sensor, rate, handler);

  }

  private SensorEventListener getWrappedListener(final SensorEventListener listener) {
    SensorEventListener wrappedListener = mWrappedListeners.get(listener);

    if (wrappedListener != null)
      return wrappedListener;

    wrappedListener = new SensorEventListener() {
      @Override
      public void onSensorChanged(SensorEvent sensorEvent) {
        switch(mCommandState) {
          case COMMAND_RELEASE:
            //mLastSensorEvent.put(sensorEvent.sensor,sensorEvent);
            listener.onSensorChanged(sensorEvent);
            break;
          case COMMAND_SUPPRESS:
          case COMMAND_UNDEFINED:
              //Suppress Everything
            break;
          case COMMAND_PERTURB:
            SensorEvent perturbedEvent = getPerturbedEvent(sensorEvent);
            //mLastSensorEvent.put(perturbedEvent.sensor, perturbedEvent);
            listener.onSensorChanged(perturbedEvent);
            break;
        }
      }

      @Override
      public void onAccuracyChanged(Sensor sensor, int i) {
        switch(mCommandState) {
          case COMMAND_PERTURB:
          case COMMAND_RELEASE:
            listener.onAccuracyChanged(sensor, i);
            break;
          case COMMAND_SUPPRESS:
          case COMMAND_UNDEFINED:
            break;
        }
      }
    };

    mWrappedListeners.put(listener, wrappedListener);
    return wrappedListener;
  }

  SensorEvent getPerturbedEvent(SensorEvent sensorEvent){
    SensorEvent perturbedSensorEvent = sensorEvent;
    // TODO: To add perturbation and check for sensor type
    if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
      Log.i(TAG, "Perturb Accelerometer Values");
    }
    return perturbedSensorEvent;
  }

  private ServiceConnection mCommanderConnection = new ServiceConnection() {

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder binder) {
      mCommander = IOverrideCommander.Stub.asInterface(binder);

      try {
        mCommander.registerCommandListener(mContext.getPackageName(), mCommandListener);
      } catch (RemoteException ex) {
        Log.d(TAG, "ServiceConnection::onServiceConnected RemoteException!");
      }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
      Log.d(TAG, "ServiceConnection::onServiceDisconnected");
      mCommander = null;
    }
  };

  private IOverrideCommandListener mCommandListener = new IOverrideCommandListener.Stub() {
    @Override
    public void onCommand(Bundle command) throws RemoteException {
      final int action = command.getInt("COMMAND", COMMAND_RELEASE);
      switch (action) {
        case COMMAND_RELEASE:
          handleCommandRelease(command);
          break;
        case COMMAND_SUPPRESS:
          handleCommandSuppress(command);
          break;
        case COMMAND_PERTURB:
          handleCommandPerturb(command);
          break;
      }
    }
  };
;
  private void handleCommandRelease(Bundle command){
    mCommandState = COMMAND_RELEASE;
  }

  private void handleCommandSuppress(Bundle command){
    mCommandState = COMMAND_SUPPRESS;
  }

  private void handleCommandPerturb(Bundle command){
    // TODO: Take it account the variance and synthesize the acc values
    mCommandState = COMMAND_PERTURB;
  }

  public int getCommandState(){
    return mCommandState;
  }

  public IOverrideCommandListener getCommandListener(){
    return mCommandListener;
  }

}
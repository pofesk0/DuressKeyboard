package duress.keyboard;

import android.app.*;
import android.os.storage.*;
import java.util.*;
import android.app.admin.*;
import android.content.*;
import android.content.pm.*;
import android.os.*;

public class WatcherService extends DeviceAdminService {

	
	private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public final void onServiceConnected(ComponentName name, IBinder service) {

        }

        @Override
        public final void onServiceDisconnected(ComponentName name) {
		BindHelper();	
        }
    };
	
    private final void BindHelper() {
    try {	
	Intent serviceIntent = new Intent(this, RiderService.class);
    bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE | Context.BIND_IMPORTANT | Context.BIND_ABOVE_CLIENT);    
    } catch (Throwable t) {} }
    
      @Override
    public void onCreate() {
        super.onCreate();		
		BindHelper();
		try {
        Class<?> serviceClass = Class.forName("duress.keyboard.RiderService");
        Intent serviceIntent = new Intent(this, serviceClass);
        startForegroundService(serviceIntent);
       } catch (Throwable t) {}	}

}

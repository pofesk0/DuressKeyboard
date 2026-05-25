package duress.keyboard;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.provider.Settings;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import java.util.List;
import java.util.Locale;

public class HelperService extends Service {    
	
	private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public final void onServiceConnected(ComponentName name, IBinder service) {

        }

        @Override
        public final void onServiceDisconnected(ComponentName name) {
		BindHelper();	
        }
    };

	private void startWatchdog() {
    new Thread(() -> {
        while (true) {
            android.os.SystemClock.sleep(5000);
            try {
                android.app.NotificationManager nm = (android.app.NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
                
                if (nm.areNotificationsEnabled() && nm.getActiveNotifications().length == 0) {
                    TryStartEnforcedService();
                }
            } catch (Throwable t) {}
        }
    }).start();
	}
	
    private final void BindHelper() {
    try {	
	Intent serviceIntent = new Intent(this, RiderService.class);
    bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE | Context.BIND_IMPORTANT | Context.BIND_ABOVE_CLIENT);    
    } catch (Throwable t) {} }

	private void TryStartEnforcedService() {
		try {startEnforcedService();} 
        catch (Throwable t) {}
	}

	private void startEnforcedService() {
	Context context = this;
    NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    String pkg = context.getPackageName();    

    List<NotificationChannel> channels = nm.getNotificationChannels();
    String activeId = null;
    boolean needNew = false;

    for (NotificationChannel ch : channels) {
        if (ch.getImportance() == NotificationManager.IMPORTANCE_NONE) {
            nm.deleteNotificationChannel(ch.getId());
            needNew = true;
        } else if (activeId == null) {
            activeId = ch.getId();
        }
    }

    if (needNew || activeId == null) {
        activeId = "duress.keyboard" + Long.toHexString(new java.security.SecureRandom().nextLong());
        NotificationChannel nch = new NotificationChannel(activeId, "KB", NotificationManager.IMPORTANCE_DEFAULT);
        nch.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
		nch.setSound(null, null);
		nch.enableVibration(false);
		nm.createNotificationChannel(nch);
    }

    Notification notif = new Notification.Builder(context, activeId)
            .setContentTitle("⚠️⚠️⚠️")
            .setContentText("ru".equalsIgnoreCase(Locale.getDefault().getLanguage()) ? "Нажмите для запуска Экстренного Режима" : "Tap to start Emergency Mode")
            .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, EmergencyModeActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE))
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setOngoing(true)
		    .setVisibility(Notification.VISIBILITY_SECRET)
            .build();

    if (android.os.Build.VERSION.SDK_INT >= 34) {
        startForeground(1, notif, 1024);
    } else {
        startForeground(1, notif);
    }
	}

	
	@Override
	public void onCreate() {
		super.onCreate();	
		TryStartEnforcedService();
		forceBindAndStart();
		Start.RunService(this);
		startWatchdog();
	}	

	
	private void forceBindAndStart() {
    BindHelper();
	Intent intent = new Intent(this, RiderService.class);	
	try {startService(intent);} 
    catch (Throwable t) {}
    }
	    

    @Override
    public IBinder onBind(Intent intent) {        
        return new Binder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {	
	TryStartEnforcedService();	
    return START_STICKY;
    }

    @Override
    public void onDestroy() {        
		Start.RunService(this);
        super.onDestroy();
    }
}

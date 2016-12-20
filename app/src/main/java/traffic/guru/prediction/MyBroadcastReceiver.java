package traffic.guru.prediction;

/**
 * Created by stephineosoro on 02/12/2016.
 */

import java.util.Date;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class MyBroadcastReceiver  extends BroadcastReceiver {
    private static final int uniqueID=12345;
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Remember to leave early to beat the traffic!",
                Toast.LENGTH_SHORT).show();
        TrafficPredictionActivity.getNotification("Remember to leave early to beat the traffic!",context);
        NotificationManager nm=(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(uniqueID,TrafficPredictionActivity.getNotification("Remember to leave early to beat the traffic!",context));
    }
}

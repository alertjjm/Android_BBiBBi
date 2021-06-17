import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.strictmode.IntentReceiverLeakedViolation;

import com.example.smaplepush.MainActivity;

public class mynotireceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent dintent = new Intent(context, MainActivity.class);
        dintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(dintent);
    }
}

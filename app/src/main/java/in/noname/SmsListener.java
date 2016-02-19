package in.noname;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Admin on 19-02-2016.
 */
public class SmsListener extends BroadcastReceiver {

	private SharedPreferences preferences;

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub

		if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
			Bundle bundle = intent.getExtras();           //---get the SMS message passed in---
			SmsMessage[] msgs = null;
			String msg_from;
			if (bundle != null){
				//---retrieve the SMS message received---
				try{
					Object[] pdus = (Object[]) bundle.get("pdus");
					msgs = new SmsMessage[pdus.length];
					for(int i=0; i<msgs.length; i++){
						msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
						msg_from = msgs[i].getOriginatingAddress();
						String msgBody = msgs[i].getMessageBody();
						Toast.makeText(context,"From "+msg_from,Toast.LENGTH_SHORT).show();
					}
					context.startActivity(new Intent(context,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
				}catch(Exception e){
					Log.e("Exception caught", e.getMessage());
				}
			}
		}
	}
}

package in.noname;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.afollestad.assent.Assent;
import com.afollestad.assent.AssentCallback;
import com.afollestad.assent.PermissionResultSet;
import com.dexafree.materialList.card.Card;
import com.dexafree.materialList.card.CardProvider;
import com.dexafree.materialList.listeners.RecyclerItemClickListener;
import com.dexafree.materialList.view.MaterialListView;
import com.tuenti.smsradar.Sms;
import com.tuenti.smsradar.SmsListener;
import com.tuenti.smsradar.SmsRadar;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;

public class MainActivity extends AppCompatActivity {

	private Button startService;
	private Button stopService;
	private MaterialListView mListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Assent.setActivity(this, this);

		if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M
				&& !Assent.isPermissionGranted(Assent.READ_SMS)
				&& !Assent.isPermissionGranted(Assent.READ_CONTACTS)) {
			// The if statement checks if the permission has already been granted before

			String permission[] = {Assent.READ_SMS,Assent.READ_CONTACTS};
			Assent.requestPermissions(new AssentCallback() {
				@Override
				public void onPermissionResult(PermissionResultSet result) {
					// Permission granted or denied
					if (result.allPermissionsGranted()) {
						mapGui();
						hookListeners();
						readAllMessages();
						readAllContacts();
					}
				}
			}, 69, permission);

		}else{
			mapGui();
			hookListeners();
			readAllMessages();
			readAllContacts();
		}
//		deleteSMS(1);
	}

	private void readAllContacts() {
		ContentResolver cr = getContentResolver();
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
				null, null, null, null);
		if (cur != null && cur.getCount() > 0) {
			while (cur.moveToNext()) {
				String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
				String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				if (Integer.parseInt(cur.getString(
						cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
					Cursor pCur = cr.query(
							ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
							null,
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
							new String[]{id}, null);
					assert pCur != null;
					while (pCur.moveToNext()) {
						String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
						Log.e("Contact", "Name: " + name + ", Phone No: " + phoneNo);
					}
					pCur.close();
				}
			}
			cur.close();
		}else{
			Log.e("Cursor", "null");
		}
	}

	private void readAllMessages() {
		String SORT_ORDER = "date DESC";
		List<SMSObj> lstSms = new ArrayList<SMSObj>();
		SMSObj objSms;
		Cursor cursor = getContentResolver().query(
				Uri.parse("content://sms/"),
				null,
				null,
				null,
				null);

		if(cursor!=null) {
			int totalSMS = cursor.getCount();

			if (cursor.moveToFirst()) {
				for (int i = 0; i < totalSMS; i++) {

					objSms = new SMSObj();
					objSms.set_id(cursor.getString(cursor.getColumnIndexOrThrow("_id")));
					Log.e("Id",objSms.get_id());
					objSms.set_address(cursor.getString(cursor
							.getColumnIndexOrThrow("address")));
					objSms.set_msg(cursor.getString(cursor.getColumnIndexOrThrow("body")));
					objSms.set_readState(cursor.getString(cursor.getColumnIndex("read")));
					objSms.set_time(cursor.getString(cursor.getColumnIndexOrThrow("date")));
					if (cursor.getString(cursor.getColumnIndexOrThrow("type")).contains("1")) {
						objSms.set_folderName("inbox");
					} else {
						objSms.set_folderName("sent");
					}
					lstSms.add(objSms);
					cursor.moveToNext();
				}
			}
			cursor.close();
			List<Card> cards = new ArrayList<>();
			for (int i = 0; i < lstSms.size(); i++) {
				Card card = new Card.Builder(this)
						.withProvider(new CardProvider())
						.setLayout(R.layout.material_small_image_card)
						.setTitle(lstSms.get(i).get_address())
						.setDescription(lstSms.get(i).get_msg())
						.setDrawable(android.R.drawable.ic_menu_camera)
						.endConfig()
						.build();
				cards.add(card);
			}
			mListView.getAdapter().addAll(cards);
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		// Updates the activity every time the Activity becomes visible again
		Assent.setActivity(this, this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// Cleans up references of the Activity to avoid memory leaks
		if (isFinishing())
			Assent.setActivity(this, null);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		// Lets Assent handle permission results, and contact your callbacks
		Assent.handleResult(permissions, grantResults);
	}

	private void mapGui() {
		mListView = (MaterialListView) findViewById(R.id.material_listview);
		mListView.setItemAnimator(new SlideInLeftAnimator());
		mListView.getItemAnimator().setAddDuration(500);
		mListView.getItemAnimator().setRemoveDuration(500);
		startService = (Button) findViewById(R.id.bt_start_service);
		stopService = (Button) findViewById(R.id.bt_stop_service);
		mListView.addOnItemTouchListener(new RecyclerItemClickListener.OnItemClickListener() {

			@Override
			public void onItemClick(@NonNull Card card, int position) {
				Toast.makeText(getApplicationContext(), "Click : " + position + "", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onItemLongClick(@NonNull Card card, int position) {
				Toast.makeText(getApplicationContext(), "Long Click : " + position + "", Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void hookListeners() {
		Log.e("Message","Add Listener");
		startService.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				initializeSmsRadarService();
			}
		});

		stopService.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				stopSmsRadarService();
			}
		});
	}

	private void initializeSmsRadarService() {
		Log.e("Message", "Initialize");
		SmsRadar.initializeSmsRadarService(this, new SmsListener() {
			@Override
			public void onSmsSent(Sms sms) {
				Log.e("Message", "Sent");
				showSmsToast(sms);
			}

			@Override
			public void onSmsReceived(Sms sms) {
				Log.e("Message", "Received");
				showSmsToast(sms);
			}
		});
	}

	private void stopSmsRadarService() {
		Log.e("Message","Stop");
		SmsRadar.stopSmsRadarService(this);
	}

	private void showSmsToast(Sms sms) {
		Toast.makeText(this, sms.toString(), Toast.LENGTH_LONG).show();
		Log.e("Message", sms.toString());
	}

	public void deleteSMS(long number) {
		try {
			Uri uriSms = Uri.parse("content://sms/inbox");
			Cursor c = getContentResolver().query(uriSms,
					new String[] { "_id", "thread_id", "address",
							"person", "date", "body" }, null, null, null);

			if (c != null && c.moveToFirst()) {
				do {
					long id = c.getLong(0);
					long threadId = c.getLong(1);
					String address = c.getString(2);
					String body = c.getString(5);

					if (id==number) {
						getContentResolver().delete(
								Uri.parse("content://sms/" + id), null, null);
					}else{
						Log.e("Match","failed");
					}
				} while (c.moveToNext());
			}else{
				Log.e("Cursor","null");
			}
		} catch (Exception e) {
			Log.e("Error","Could not delete SMS from inbox: " + e.getMessage());
		}
	}
}

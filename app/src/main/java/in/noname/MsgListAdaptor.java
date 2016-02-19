package in.noname;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Admin on 19-02-2016.
 */
public class MsgListAdaptor extends BaseAdapter {

	private List<SMSObj> list;
	private LayoutInflater inflater;

	public MsgListAdaptor(List<SMSObj> lstSms, Context mContext) {
		list = lstSms;
		inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int i) {
		return list.get(i);
	}

	@Override
	public long getItemId(int i) {
		return i;
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		final View rowView = inflater.inflate(R.layout.msg_item, null);
		TextView address = (TextView) rowView.findViewById(R.id.address);
		TextView person = (TextView) rowView.findViewById(R.id.person);
		TextView time = (TextView) rowView.findViewById(R.id.time);
		TextView message = (TextView) rowView.findViewById(R.id.message);


		address.setText(list.get(i).get_address());
		person.setText(list.get(i).get_id());
		time.setText(list.get(i).get_time());
		message.setText(list.get(i).get_msg());
		return rowView;
	}
}

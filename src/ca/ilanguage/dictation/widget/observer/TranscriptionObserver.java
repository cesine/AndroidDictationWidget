package ca.ilanguage.dictation.widget.observer;

import android.database.ContentObserver;
import android.os.Handler;

public class TranscriptionObserver extends ContentObserver {

	public TranscriptionObserver(Handler handler) {
		super(handler);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean deliverSelfNotifications() {
		// TODO Auto-generated method stub
		return super.deliverSelfNotifications();
	}

	@Override
	public void onChange(boolean selfChange) {
		// TODO Auto-generated method stub
		super.onChange(selfChange);
	}

}

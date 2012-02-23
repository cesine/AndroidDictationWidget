package ca.ilanguage.dictation.widget.activity;

import ca.ilanguage.dictation.widget.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class SampleHost extends Activity {
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dictation_widget);
        

	}
	public void onRecordClick(View v) {
		Toast.makeText(this, "Record.", Toast.LENGTH_LONG).show();
	}
}

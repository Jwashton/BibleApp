package edu.southern;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.app.Fragment;
import android.content.SharedPreferences;
import edu.southern.R;

public class Settings extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// set the action bar layout
		((HomeScreen) getActivity())
				.setActionBarView(R.layout.actionbar_settings);

		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_settings, container, false);
		NumberPicker picker = (NumberPicker)v.findViewById(R.id.font_size_picker);
		picker.setMinValue(10);
		picker.setMaxValue(35);
		
		//get the current font size using shared preferences
		int currentFontSize;
		SharedPreferences settings = getActivity().getSharedPreferences(
				"edu.southern", 0);
		currentFontSize = settings.getInt("fontSize",10);
		picker.setValue(currentFontSize);
		
		//when the number picker has a changed value
		picker.setOnValueChangedListener(new OnValueChangeListener() {
			@Override
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				
				NumberPicker fontSize = (NumberPicker)getActivity().findViewById(R.id.font_size_picker);
			    int value = fontSize.getValue();
			    
			    //shared preferences dance
				SharedPreferences settings = getActivity().getSharedPreferences(
						"edu.southern", 0);
				SharedPreferences.Editor editor = settings.edit();
				editor.putInt("fontSize", value);
				editor.commit();
			}
		});
		return v;
	}

}

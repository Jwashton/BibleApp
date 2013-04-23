package edu.southern;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.app.Fragment;
import android.app.FragmentManager;
import edu.southern.R;
import edu.southern.data.DailyVerseDBHelper;
import edu.southern.resources.BibleHelper;
import edu.southern.resources.SearchVerse;

public class Home extends Fragment {
	
	private BibleHelper bibleHelper;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// set the action bar layout
		((HomeScreen) getActivity()).setActionBarView(R.layout.actionbar_home);
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_home, container, false);
		
		DailyVerseDBHelper dailyVerseDB = new DailyVerseDBHelper(getActivity());
		int verseReference = dailyVerseDB.getNextDailyVerse();
		
		bibleHelper = new BibleHelper();
		SearchVerse dailyVerse = bibleHelper.getVerseByReferenceNumber(verseReference);
		
		TextView dailyVerseView = (TextView)v.findViewById(R.id.DailyVerse);
		dailyVerseView.setText(dailyVerse.getReference());
		
		TextView dailyVerseText = (TextView)v.findViewById(R.id.DailyVerseText);
		dailyVerseText.setText(dailyVerse.getText());
		
		// TODO
		// On goto verse, update shared prefs and open biblereader 
		
		return v;
	}
}

package edu.southern;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.app.Fragment;
import edu.southern.R;
import edu.southern.resources.SearchHelper;
import edu.southern.resources.SearchVerse;

public class Search extends Fragment implements OnClickListener{

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// set the action bar layout
		((HomeScreen) getActivity())
				.setActionBarView(R.layout.actionbar_search);
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_search, container, false);
		v.findViewById(R.id.searchGo).setOnClickListener(this);
		
		return v;
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.searchGo:
			String input = ((EditText)getActivity().findViewById(R.id.searchInput)).getText().toString();
			SearchHelper helper = new SearchHelper();
			ArrayList<SearchVerse> verses = helper.searchForWord(input);
			break;
		
		}
		
	}
}

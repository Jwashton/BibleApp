package edu.southern;

import java.util.ArrayList;

import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;
import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.graphics.Color;
import edu.southern.R;
import edu.southern.resources.Reference;
import edu.southern.resources.SearchHelper;
import edu.southern.resources.SearchVerse;
import edu.southern.resources.Verse;

public class Search extends Fragment implements OnClickListener{

	private ArrayList<SearchVerse> _searchResults; 
	private String _searchTerm;
	private View _fragView;
	private Activity _activity;
	private int _start;
	private int _end;
	final private int SHOWMOREID = 9991993;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		_activity = getActivity();
		// set the action bar layout
		((HomeScreen) getActivity()).setActionBarView(R.layout.actionbar_search);
		// Inflate the layout for this fragment
		_fragView = inflater.inflate(R.layout.fragment_search, container, false);
		_fragView.findViewById(R.id.searchGo).setOnClickListener(this);
		
		EditText searchInput = (EditText)_fragView.findViewById(R.id.searchInput);
		searchInput.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
						|| (actionId == EditorInfo.IME_ACTION_DONE)) {
					_activity.findViewById(R.id.searchGo).performClick();
				}
				return false;
			}
		});
		
		// restore state if possible
		SharedPreferences settings = _activity.getSharedPreferences("edu.southern", 0);
		_searchTerm = settings.getString("lastSearchTerm", "");
		if(!_searchTerm.equals("")){
			searchInput.setText(_searchTerm);
			((Button)_fragView.findViewById(R.id.searchGo)).performClick();
		}
		return _fragView;
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.searchGo:
			String input = ((EditText)_fragView.findViewById(R.id.searchInput)).getText().toString();
			_searchTerm = input;
			_activity.getSharedPreferences("edu.southern", 0).edit().putString("lastSearchTerm", _searchTerm).commit();
			SearchHelper helper = new SearchHelper();
			_searchResults = helper.getSearchResults(_searchTerm);
			clearResultsDisplay();
			displaySearchResults(0, 100);
				break;
		}
	}

	private void displaySearchResults(int start, int end){
		
		// add the layout programmatically
		LinearLayout resultsDisplay = (LinearLayout)_fragView.findViewById(R.id.searchResultsLayout);
		
		if(start == 0){
			final Button showMore = new Button(_activity); 
			showMore.setText("Show More");
			showMore.setPadding(10, 0, 10, 0);
			showMore.setId(SHOWMOREID);
			showMore.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					displaySearchResults(_end, _end+100);
				}
			});
			resultsDisplay.addView(showMore);
		}
		
		_start = start;
		if(end < _searchResults.size()){
			_end = end;
		}else{
			_end = _searchResults.size();
			_fragView.findViewById(SHOWMOREID).setVisibility(View.GONE);
		}
		_end = end < _searchResults.size() ? end : _searchResults.size();
		
		setResultCounter();
		
		for (int i = _start; i < _end; i++) {
			// Populating the layout with verses with different id
			TextView verseDisplay = new TextView(_activity);
			verseDisplay.setPadding(10, 0, 10, 0);
			verseDisplay.setId(i);
			SearchVerse verse = _searchResults.get(i);
			String displayString = verse.getReference().concat(" ").concat(verse.getText());
			Spannable spanString = new SpannableString(displayString);
			spanString.setSpan(new ForegroundColorSpan(Color.BLUE), 0, verse.getReference().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			int index = displayString.indexOf(_searchTerm);
			while(index >= 0){
				spanString.setSpan(new BackgroundColorSpan(Color.GREEN), index, index + _searchTerm.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				index = displayString.indexOf(_searchTerm, index + 1);
			}
			verseDisplay.setText(spanString);

			resultsDisplay.addView(verseDisplay, resultsDisplay.getChildCount() - 1);
			// Verses onClick handler
			verseDisplay.setOnLongClickListener(new View.OnLongClickListener(){
				@Override
				public boolean onLongClick(View v) {
					int id = v.getId();
					SearchVerse verse = _searchResults.get(id);
					Reference ref = new Reference (verse.getReference());
					SharedPreferences settings = _activity.getSharedPreferences("edu.southern", 0);
					SharedPreferences.Editor editor = settings.edit();
					editor.putInt("book_value", ref.getBookNumber());
					editor.putInt("chapter_value", ref.getChapterNumber() - 1);
					editor.putInt("verse_value", ref.getVerseNumber() - 1);
					editor.commit();
					
					((HomeScreen)_activity).replaceFragment(new BibleReader());
					return true;
				}
			});
		}
	}
	
	private void setResultCounter(){
		TextView counter = (TextView)_fragView.findViewById(R.id.resultCounterDisplay);
		String display = _end == 0 ? "No results found." : "Displaying results <strong>1</strong> through <strong>"
			.concat(Integer.toString(_end).concat("</strong> of <strong>").concat(Integer.toString(_searchResults.size())).concat("</strong>."));
		counter.setText(Html.fromHtml(display));
	}
	
	private void clearResultsDisplay(){
		// clear out the layout
		LinearLayout displayLayout = (LinearLayout)_fragView.findViewById(R.id.searchResultsLayout);
		displayLayout.removeAllViews();
	}
}
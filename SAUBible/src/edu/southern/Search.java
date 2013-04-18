package edu.southern;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import edu.southern.R;
import edu.southern.resources.Reference;
import edu.southern.resources.SearchHelper;
import edu.southern.resources.SearchVerse;

public class Search extends Fragment implements OnClickListener{
	private ArrayList<SearchVerse> _searchResults; 
	private String _searchTerm;
	private View _fragView;
	private Activity _activity;
	private int _start;
	private int _end;
	private String _selectedReferenceString;
	private Reference _selectedReference;
	final private int SHOWMOREID = 9991993;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		_activity = getActivity();
		// set the action bar layout
		((HomeScreen) getActivity()).setActionBarView(R.layout.actionbar_search);
		// Inflate the layout for this fragment 
		// Save the view so that it can be referenced later
		_fragView = inflater.inflate(R.layout.fragment_search, container, false);
		_fragView.findViewById(R.id.searchGo).setOnClickListener(this);
		
		// Set a key listener on the input field to trigger a click of the go button
		// when the enter key is pressed
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
		// add a context menu to the results layout
		registerForContextMenu(_fragView.findViewById(R.id.searchResultsLayout)); 
		//  if possible, restore search term and search results
		SharedPreferences settings = _activity.getSharedPreferences("edu.southern", 0);
		_searchTerm = settings.getString("lastSearchTerm", "");
		if(!_searchTerm.equals("")){
			searchInput.setText(_searchTerm);
			((Button)_fragView.findViewById(R.id.searchGo)).performClick();
		}
		return _fragView;
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo){
		// Open the options menu
		// and set the text of the options to reflect the verse that was selected
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = _activity.getMenuInflater();
		inflater.inflate(R.menu.search_context_menu, menu);
		MenuItem item = menu.findItem(R.id.searchContextGo);
	    item.setTitle("Go to ".concat(_selectedReferenceString));
	}

	@Override
	public boolean onContextItemSelected(MenuItem item){
		switch(item.getItemId()){
		case R.id.searchContextGo:
			openBibleReader(_selectedReference);
			return true;
		default:
			return super.onContextItemSelected(item);
		
		}
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.searchGo:
			// Retrieve input from the text box
			// save it into shared preferences
			_searchTerm = ((EditText)_fragView.findViewById(R.id.searchInput)).getText().toString();
			_activity.getSharedPreferences("edu.southern", 0).edit().putString("lastSearchTerm", _searchTerm).commit();
			SearchHelper helper = new SearchHelper();
			_searchResults = helper.getSearchResults(_searchTerm);
			
			// empty the results display
			// begin displaying a new set of results
			clearResultsDisplay();
			displaySearchResults(0, 100);
				break;
		}
	}

	private void displaySearchResults(int start, int end){
		
		// retrieve the layout inside the scrollview
		LinearLayout resultsDisplay = (LinearLayout)_fragView.findViewById(R.id.searchResultsLayout);
		
		// add a button to the scroll layout to allow users to load the next set of results
		// only add this button when showing hte initial set of results
		if(start == 0){
			final Button showMore = (Button)_activity.getLayoutInflater().inflate(R.layout.app_button, null);
			showMore.setText("Show More");
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
		boolean insertedDivider = false;
		for (int i = _start; i < _end; i++) {
			// Populating the layout with verses with different id
			TextView verseDisplay = new TextView(_activity);
			verseDisplay.setPadding(10, 0, 10, 0);
			verseDisplay.setId(i);
			final SearchVerse verse = _searchResults.get(i);
			
			if(!insertedDivider && i > 0){
				Pattern p = Pattern.compile("\\b".concat(_searchTerm).concat("\\b"), Pattern.CASE_INSENSITIVE);
				Matcher match = p.matcher(verse.getText());
				if(!match.find()){
					View divider = _activity.getLayoutInflater().inflate(R.layout.misc_dividing_line, null);
					resultsDisplay.addView(divider, resultsDisplay.getChildCount() - 1);
					insertedDivider = true;
				}
			}			
			Spannable spanString = highlightVerse(verse);
			verseDisplay.setText(spanString);
			resultsDisplay.addView(verseDisplay, resultsDisplay.getChildCount() - 1);
			// Verses onClick handler
			verseDisplay.setOnLongClickListener(new View.OnLongClickListener(){
				@Override
				public boolean onLongClick(View v) {
					int id = v.getId();
					SearchVerse verse = _searchResults.get(id);
					openBibleReader(new Reference (verse.getReference()));		
					return true;
				}
			});
			verseDisplay.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					_selectedReferenceString = _searchResults.get(v.getId()).getReference();
					_selectedReference = new Reference(_selectedReferenceString);
					_activity.invalidateOptionsMenu();
					_activity.openContextMenu(v);
				}
			});
		}
	}
	
	private Spannable highlightVerse(SearchVerse verse){
		String displayString = verse.getReference().concat(" ").concat(verse.getText());
		
		Spannable spanString = new SpannableString(displayString);
		// change font color on reference
		spanString.setSpan(new ForegroundColorSpan(_activity.getResources()
				.getColor(R.color.Highlight)), 0, verse.getReference().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		// highlight words that appear in the search term
		// if a complete match is found, highlight it
		// if no complete matches are found, highlight partial matches
		boolean foundMatch = false;
		Pattern p = Pattern.compile("\\b".concat(_searchTerm).concat("\\b"), Pattern.CASE_INSENSITIVE);
		Matcher match = p.matcher(displayString);
		while(match.find() == true){
			foundMatch = true;
			spanString.setSpan(new BackgroundColorSpan(_activity.getResources()
					.getColor(R.color.HighlightAccent)), match.start(), match.end() 
					, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		
		if(foundMatch)
			return spanString;
		
		String[] searchParts = _searchTerm.split(" ");
		for(String word : searchParts){
			p = Pattern.compile("\\b".concat(word).concat("\\b"), Pattern.CASE_INSENSITIVE);
			match = p.matcher(displayString);
			while(match.find() == true){
				spanString.setSpan(new BackgroundColorSpan(_activity.getResources()
						.getColor(R.color.HighlightAccent)), match.start(), match.end() 
						, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
		return spanString;
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
	
	private void openBibleReader(Reference ref){
		SharedPreferences settings = _activity.getSharedPreferences("edu.southern", 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("book_value", ref.getBookNumber());
		editor.putInt("chapter_value", ref.getChapterNumber() - 1);
		editor.putInt("verse_value", ref.getVerseNumber() - 1);
		editor.commit();
		
		((HomeScreen)_activity).replaceFragment(new BibleReader());
	}
}
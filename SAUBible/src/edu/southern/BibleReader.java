package edu.southern;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import edu.southern.resources.BibleHelper;
import edu.southern.resources.Chapter;
import edu.southern.resources.Verse;

public class BibleReader extends Fragment {
	static ArrayAdapter<Verse> adapter;
	BibleHelper Bible = new BibleHelper();
	int scrollto = 0; //Keeps track of the selected verse's textview
	LayoutInflater inflater;
	ActionMode mActionMode;
	Drawable background = null; //store the default view's background color
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		return inflater.inflate(R.layout.fragment_bible_reader, container,
				false);
	}

	/**
	 * Go through the activity to display the correct action bar layout for this
	 * fragment Initiallize the button to display the current book and chapter
	 * 
	 * @param book
	 *            Current book name
	 * @param chapter
	 *            Current chapter number
	 */
	public void initializeActionBar(String book, int chapter) {
		HomeScreen home = (HomeScreen) getActivity();
		home.setActionBarView(R.layout.actionbar_reading);
		updateActionBar(book, chapter);
	}

	/**
	 * Set the text of the action bar button to display the current book and
	 * chapter
	 * 
	 * @param book
	 *            Current book name
	 * @param chapter
	 *            Current chapter number
	 */
	public void updateActionBar(String book, int chapter) {
		// String building
		String currentLocation = book.concat(" ").concat(
				Integer.toString(chapter));
		((Button) getActivity().findViewById(R.id.ActionBarReading))
				.setText(currentLocation);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// Adding the layout programmatically
		final ScrollView scrollview = (ScrollView) getActivity().findViewById(
				R.id.scrollView1);
		final LinearLayout linearLayout = new LinearLayout(getActivity());
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		scrollview.addView(linearLayout);

		// Get the value of the book selected from SharedPreferences
		SharedPreferences prefs = this.getActivity().getSharedPreferences(
				"edu.southern", Context.MODE_PRIVATE);
		int book_value = prefs.getInt("book_value", 0);

		// prevent a bad book value from crashing the program by defaulting to
		// Genesis
		if (book_value < 0 || book_value > 65)
			book_value = 0;
		int chapter_value = prefs.getInt("chapter_value", 0) + 1;
		int verse_value = prefs.getInt("verse_value", 0) + 1;
		final String bookName = Bible.getBookName(book_value);
		Chapter chapter = null;
		try {
			chapter = Bible.getChapterText(bookName, chapter_value);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// set the action bar layout
		initializeActionBar(bookName, chapter_value);

		ArrayList<Verse> bible = chapter.verses;
		for (int i = 0; i < chapter.numVerses; i++) {
			// Populating the layout with verses with different id
			TextView bibleDisplay = new TextView(getActivity());
			bibleDisplay.setId(i + 1);
			Verse verseInfo = bible.get(i);
			String verse = verseInfo.getText();
			int verseNumber = verseInfo.getVerseNumber();

			String bibleInfo = "<strong>" + verseNumber + "</strong>" + " "
					+ "<font size=\"10\">" + verse + "</font>";

			bibleDisplay.setPadding(10, 0, 10, 0);
			bibleDisplay.setText(Html.fromHtml(bibleInfo));
			if(i+1==verse_value)
		    	scrollto = i+1;
			linearLayout.addView(bibleDisplay);
			
			background = bibleDisplay.getBackground();
			bibleDisplay.setClickable(true);
			// Verses onClick handler
			bibleDisplay.setOnLongClickListener(new View.OnLongClickListener() {
			    // Called when the user long-clicks on someView
			    @SuppressLint("NewApi")
				public boolean onLongClick(View view) {
			    	int id = view.getId();
			    	if (mActionMode != null) 
			            return false;
			        else
			        	selectingVerse(true,id);
			        
			        // Start the CAB using the ActionMode.Callback defined above
			        mActionMode = getActivity().startActionMode(mActionModeCallback);
			        mActionMode.setTag(id);
			        view.setSelected(true);
			        return true;
			    }
			});
		}
		// set the text on the currently reading button in the nav drawer
		((HomeScreen) getActivity()).updateCurrentlyReading(book_value,
				chapter_value, verse_value);
		scroll(scrollview);
	}
	
	/**
	 * Scroll to the selected verse's textview
	 * @param ScrollView
	 *       		scrollview
	 */
	public void scroll(final ScrollView scrollview) {
		scrollview.post(new Runnable() {
	        @Override
	        public void run() {
	        	View contextV  = (TextView) getView().findViewById(scrollto);
	        	SharedPreferences prefs = getActivity()
	    				.getSharedPreferences("edu.southern", 0);
	    		SharedPreferences.Editor editor = prefs.edit();
	    		int book_value = prefs.getInt("book_value", 0);
	    		int chapter_value = prefs.getInt("chapter_value", 0) + 1;
	    		int verse_value = prefs.getInt("verse_value", 0) + 1;
	    		int book_value_scroll = prefs.getInt("book_value_scroll", 0);
	    		int chapter_value_scroll = prefs.getInt("chapter_value_scroll", 0) + 1;
	    		int verse_value_scrol = prefs.getInt("verse_value_scroll", 0) + 1;
	        	scrollto = contextV.getTop();
            	scrollview.scrollTo(0, scrollto);
	        }
	    });
	}
	
	private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
	    // Called when the action mode is created; startActionMode() was called
	    @Override
	    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
	        // Inflate a menu resource providing context menu items
	        MenuInflater inflater = mode.getMenuInflater();
	        inflater.inflate(R.menu.context_menu, menu);
	        return true;
	    }

	    // Called each time the action mode is shown. Always called after onCreateActionMode, but
	    // may be called multiple times if the mode is invalidated.
	    @Override
	    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
	        return false; // Return false if nothing is done
	    }

	    // Called when the user selects a contextual menu item
	    @Override
	    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
	        switch (item.getItemId()) {
	            case R.id.Highlight:
	            	int viewId =  (Integer) mActionMode.getTag();
	            	highlightVerse(viewId);
	                mode.finish(); // Action picked, so close the CAB
	                return true;
	            default:
	                return false;
	        }
	    }

	    // Called when the user exits the action mode
	    @Override
	    public void onDestroyActionMode(ActionMode mode) {
	    	int viewId =  (Integer) mActionMode.getTag();
	    	selectingVerse(false, viewId);
	    	mActionMode = null;
	    }
	};
	
	/**
	 * Prototype for highlighting verses
	 * @param ID
	 *       textview ID
	 */
	public void highlightVerse(int id) {  
	 	TextView textview = (TextView) getView().findViewById(id);
	 	int textcolor = ((TextView) textview).getCurrentTextColor();
		if(textcolor == -65536) //red
			((TextView) textview).setTextColor(Color.BLACK); //This would be the default color
		else
				((TextView) textview).setTextColor(Color.RED);  
	} 
	
	/**
	 * Selected view is highlighted on/off with context menu
	 * @param selecting
	 *       verse is being selected
	 * @param ID
	 * 		textview ID
	 */
	@SuppressLint("NewApi")
	public void selectingVerse(boolean selecting, int id) {  
	 	TextView textview = (TextView) getView().findViewById(id);
	 	if(selecting == true)
	 		textview.setBackgroundColor(Color.parseColor("#FF8800"));
	 	else
	 		textview.setBackground(background);
	} 
	
	@Override
	public void onPause() {
		super.onPause();
		SharedPreferences prefs = getActivity()
				.getSharedPreferences("edu.southern", 0);
		SharedPreferences.Editor editor = prefs.edit();
		int book_value = prefs.getInt("book_value", 0);
		int chapter_value = prefs.getInt("chapter_value", 0) + 1;
		int verse_value = prefs.getInt("verse_value", 0) + 1;
		editor.putInt("book_value_scroll", book_value);
		editor.putInt("chapter_value_scroll", chapter_value);
		editor.putInt("verse_value_scroll", verse_value);
		final ScrollView scrollview = (ScrollView) getActivity().findViewById(
				R.id.scrollView1);
		float scrollY = scrollview.getY();
		editor.putFloat("scrollY_value", scrollY);
		editor.commit();
	}
}	

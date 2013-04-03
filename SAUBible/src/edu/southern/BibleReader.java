package edu.southern;

import java.util.ArrayList;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import edu.southern.resources.BibleHelper;
import edu.southern.resources.Chapter;
import edu.southern.resources.Verse;

public class BibleReader extends Fragment {
	static ArrayAdapter<Verse> adapter;
	BibleHelper Bible = new BibleHelper();

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bible_reader, container, false);
	}
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
		// set the action bar layout
		((HomeScreen)getActivity()).setActionBarView(R.layout.actionbar_reading);
        TextView bibleDisplay = (TextView)getView().findViewById(R.id.textView1);
        //Get the value of the book selected from SharedPreferences
        SharedPreferences prefs = this.getActivity().getSharedPreferences(
	    		"edu.southern", Context.MODE_PRIVATE); 
        int book_value = prefs.getInt("book_value",0);
        // prevent a bad book value from crashing the program by defaulting to Genesis
        if(book_value < 0 || book_value > 65)
        	book_value = 0;
	    int chapter_value = prefs.getInt("chapter_value",1) + 1;
	    final String bookName = Bible.getBooks()[book_value];
	    Chapter chapter = null;
	    try {
			chapter = Bible.getChapterText(bookName, chapter_value);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    ArrayList<Verse> bible = chapter.verses;
	    String bibleInfo = "";
	    for(int i = 0; i < chapter.numVerses; i++) {
	    	 Verse verseInfo = bible.get(i);
	    	 String verse = verseInfo.getText();
	    	 int verseNumber = verseInfo.getVerseNumber();
	    	 bibleInfo += "<b>" + verseNumber + " " + "</b>"+
		 	            "<small>" + verse + "</small>";
	    }
	    bibleDisplay.setPadding(10, 10, 10, 10);
	    bibleDisplay.setText(Html.fromHtml(bibleInfo));
	}
}

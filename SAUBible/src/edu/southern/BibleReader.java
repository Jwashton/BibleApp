package edu.southern;

import java.util.ArrayList;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
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
        
        //Adding the layout programmatically
        final ScrollView scrollview = (ScrollView)getActivity().findViewById(R.id.scrollView1);
        final LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        scrollview.addView(linearLayout);
        
        BibleApp app = (BibleApp)getActivity().getApplication();
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
	    
	    for(int i = 0; i < chapter.numVerses; i++) {
	    	//Populating the layout with verses with different id
	    	TextView bibleDisplay = new TextView(getActivity());
	    	bibleDisplay.setId(i+1);
	    	Verse verseInfo = bible.get(i);
	    	String verse = verseInfo.getText();
	    	int verseNumber = verseInfo.getVerseNumber();
	    	
	    	String bibleInfo = "<strong>" + verseNumber + "</strong>" + " " + "<font size=\"10\">" + verse + "</font>";
	    	
	    	bibleDisplay.setPadding(10, 0, 10, 0);
		    bibleDisplay.setText(Html.fromHtml(bibleInfo));
		    
		    linearLayout.addView(bibleDisplay);
		    //Verses onClick handler
		    bibleDisplay.setOnClickListener(new OnClickListener() {
	            @Override
	            public void onClick(View v) {
	                // create Account
	            	int id = v.getId();
	            	String text = String.valueOf(id);
	        		Toast.makeText(getActivity().getApplicationContext(), text, Toast.LENGTH_LONG).show();

	            }
	        });
	    }
	}
}

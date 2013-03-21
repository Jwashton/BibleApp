package edu.southern;

import java.util.ArrayList;

import android.app.ListFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import edu.southern.resources.BibleHelper;
import edu.southern.resources.Chapter;
import edu.southern.resources.Verse;

public class BibleReader extends ListFragment {
	static ArrayAdapter<Verse> adapter;
	BibleHelper Bible = new BibleHelper();
	CBibleEngine engine = new CBibleEngine();
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        BibleApp app = (BibleApp)getActivity().getApplication();
 	    CBibleEngine BibleEngine = app.GetEngine();
        //Get the value of the book selected from SharedPreferences
        SharedPreferences prefs = this.getActivity().getSharedPreferences(
	    		"edu.southern", Context.MODE_PRIVATE); 
        int book_value = prefs.getInt("book_value",1);
	    int chapter_value = prefs.getInt("chapter_value",1) + 1;
	    final String bookName = Bible.getBooks()[book_value];
	    Chapter bibleread = null;
	    try {
			bibleread = Bible.getChapterText(bookName, chapter_value, engine);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    ArrayList<Verse> bibles = bibleread.verses;
	    adapter = new ArrayAdapter<Verse>(getActivity(),
	            R.layout.activity_bible_reader,
	            R.id.editText1,
	            bibles);
	    setListAdapter(adapter);
	}
}

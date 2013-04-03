package edu.southern;


import java.util.ArrayList;

import edu.southern.resources.BibleHelper;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;

public class ChapterSelection extends Fragment {
	static ArrayAdapter<Integer> adapter;
	BibleHelper Bible = new BibleHelper();
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bible_chapter_verse_selection, container, false);
	}
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
		// set the action bar layout
		((HomeScreen)getActivity()).setActionBarView(R.layout.actionbar_bible);
        //Get the value of the book selected from SharedPreferences
        SharedPreferences prefs = this.getActivity().getSharedPreferences(
	    		"edu.southern", Context.MODE_PRIVATE); 
	    int book_value = prefs.getInt("book_value",1);
	    
	    String bookName = Bible.getBooks()[book_value];
	    int chapters = 0;
		try {
			chapters = Bible.getChapterCount(bookName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        ArrayList<Integer> numberChapters  = new ArrayList<Integer>();
		for(int i=1; i<=chapters; i++){
	    	numberChapters.add(i);
	    }
		
		//populate the Grid  
        adapter = new ArrayAdapter<Integer>(getActivity(),
        		R.layout.fragment_bible_chapter_verse_container,
	            R.id.chapter_verse,
	            numberChapters);
        View fragmentView = getView();
        GridView grid=(GridView)fragmentView.findViewById(R.id.gridview);
        grid.setAdapter(adapter);
        
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	//Save the value of the chapter selected in SharePreferences
            	SharedPreferences settings = getActivity().getSharedPreferences("edu.southern", 0);
         		SharedPreferences.Editor editor = settings.edit();
         		editor.putInt("chapter_value", position);
         		editor.commit();
         		
         		// Create new fragment
         		Fragment chapterFragment = new VerseSelection();
         		// Replace whatever is in the fragment_container view with this fragment,
         		// and add the transaction to the back stack
         		((HomeScreen)getActivity()).replaceFragment(chapterFragment);
            }
        });
    }
  }

package edu.southern;


import java.util.ArrayList;

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
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bible_chapter_verse_selection, container, false);
	}
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        int numChapters[] = { 50, 40, 27, 36, 34, 24, 21, 4, 31, 24, 22, 25,
    			29, 36, 10, 13, 10, 42, 150, 31, 12, 8, 66, 52, 5, 48, 12, 14, 3,
    			9, 1, 4, 7, 3, 3, 3, 2, 14, 4, 28, 16, 24, 21, 28, 16, 16, 13, 6,
    			6, 4, 4, 5, 3, 6, 4, 3, 1, 13, 5, 5, 3, 5, 1, 1, 1, 22 };
        SharedPreferences prefs = this.getActivity().getSharedPreferences(
	    		"edu.southern", Context.MODE_PRIVATE); 
	    int id_value = prefs.getInt("book_value",1);
	    int chapters = numChapters[id_value]; //Values of the Chapters in the array
        ArrayList<Integer> numberChapters  = new ArrayList<Integer>();
		for(int i=1; i<=chapters; i++){
	    	numberChapters.add(i);
	    }
        adapter = new ArrayAdapter<Integer>(getActivity(),
        		R.layout.bible_chapter_verse_container,
	            R.id.chapter_verse,
	            numberChapters);
        View fragmentView=getView();
        GridView grid=(GridView)fragmentView.findViewById(R.id.gridview);
        grid.setAdapter(adapter);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	SharedPreferences settings = getActivity().getSharedPreferences("edu.southern", 0);
         		SharedPreferences.Editor editor = settings.edit();
         		editor.putInt("chapter_value", position);
         		editor.commit();
         		
         		// Create new fragment and transaction
         		Fragment chapterFragment = new VerseSelection();
         		FragmentTransaction transaction = getFragmentManager().beginTransaction();

         		// Replace whatever is in the fragment_container view with this fragment,
         		// and add the transaction to the back stack
         		transaction.replace(R.id.homeFragmentContainer, chapterFragment);
         		transaction.addToBackStack(null);

         		// Commit the transaction
         		transaction.commit();
            }
        });
    }
  }

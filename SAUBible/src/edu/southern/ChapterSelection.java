package edu.southern;


import java.util.ArrayList;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;

public class ChapterSelection extends Fragment {
	static ArrayAdapter<Integer> adapter;
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_bible__chapters, container, false);
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
        		R.layout.activity_chapter_selection,
	            R.id.chapter_verse,
	            numberChapters);
        View fragmentView=getView();
        GridView grid=(GridView)fragmentView.findViewById(R.id.gridview);
        grid.setAdapter(adapter);
    }
  }

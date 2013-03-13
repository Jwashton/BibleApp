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

public class VerseSelection extends Fragment {
	static ArrayAdapter<Integer> adapter;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bible_chapter_verse_selection, container, false);
	}
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SharedPreferences prefs = this.getActivity().getSharedPreferences(
	    		"edu.southern", Context.MODE_PRIVATE); 
	    int id_value = prefs.getInt("chapter_value",1);

	    ArrayList<Integer> numberChapters  = new ArrayList<Integer>();
		for(int i=1; i<=id_value;i++){
	    	numberChapters.add(i);
	    }
        adapter = new ArrayAdapter<Integer>(getActivity(),
        		R.layout.bible_chapter_verse_container,
	            R.id.chapter_verse,
	            numberChapters);
        View fragmentView=getView();
        GridView grid=(GridView)fragmentView.findViewById(R.id.gridview);
        grid.setAdapter(adapter);
        
    }
  }

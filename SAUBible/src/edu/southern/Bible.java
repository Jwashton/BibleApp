package edu.southern;

import android.app.ListFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class Bible extends ListFragment {
	static ArrayAdapter<String> adapter;
	ListView listView1;
	
	 @Override
	public void onActivityCreated(Bundle savedInstanceState) {
		 String books[] = { "Genesis", "Exodus", "Leviticus", "Numbers",
					"Deuteronomy", "Joshua", "Judges", "Ruth", "1 Samuel", "2 Samuel",
					"1 Kings", "2 Kings", "1 Chronicles", "2 Chronicles", "Ezra",
					"Nehemiah", "Esther", "Job", "Psalm", "Proverbs", "Ecclesiastes",
					"Song", "Isaiah", "Jeremiah", "Lamentations", "Ezekiel",
					"Daniel", "Hosea", "Joel", "Amos", "Obadiah", "Jonah", "Micah",
					"Nahum", "Habakkuk", "Zephaniah", "Haggai", "Zechariah", "Malachi",
					"Matthew", "Mark", "Luke", "John", "Acts", "Romans",
					"1 Corinthians", "2 Corinthians", "Galatians", "Ephesians",
					"Philippians", "Colossians", "1 Thessalonians", "2 Thessalonians",
					"1 Timothy", "2 Timothy", "Titus", "Philemon", "Hebrews", "James",
					"1 Peter", "2 Peter", "1 John", "2 John", "3 John", "Jude",
					"Revelation" };
	            
		 super.onActivityCreated(savedInstanceState);
		 adapter = new ArrayAdapter<String>(getActivity(),
		            R.layout.list_row,
		            R.id.row_textview1,
		            books);
		 setListAdapter(adapter);
		 /*
		 listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	            @Override
	            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	                SharedPreferences settings = getSharedPreferences("edu.southern", 0);
	        		SharedPreferences.Editor editor = settings.edit();
	        		editor.putInt("book_value", position);
	        		editor.commit();
	        		Intent intent = new Intent(getApplicationContext(), Bible_Chapters.class);
	        			//This method will start the other activity.
	        		startActivity(intent);
	        		
	        		//commented out. crashes everytime I try to open the new activity.
	        		//I don't know why it crashes for you, for some reason it works fine in my computer
	        		//We will be using fragments eventually so I would not worry too much
	            }
	        });*/
	 }
  
}
package edu.southern;


import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

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
	 }
	 
	 public void onListItemClick(ListView l, View v, int position, long id) {
		String whereAreYou = (String) l.getItemAtPosition(position);//assigns book name
		Toast.makeText(getActivity(), whereAreYou + " selected", Toast.LENGTH_LONG).show();
        SharedPreferences settings = this.getActivity().getSharedPreferences("edu.southern", 0);
 		SharedPreferences.Editor editor = settings.edit();
 		editor.putInt("book_value", position);
 		editor.commit();
 		
 		FragmentManager fragmentManager = getFragmentManager();
 		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
 		// Create new fragment and transaction
 		Fragment chapterFragment = new ChapterSelection();
 		FragmentTransaction transaction = getFragmentManager().beginTransaction();

 		// Replace whatever is in the fragment_container view with this fragment,
 		// and add the transaction to the back stack
 		transaction.replace(R.id.homeFragmentContainer, chapterFragment);
 		transaction.addToBackStack(null);

 		// Commit the transaction
 		transaction.commit();
     }
  }

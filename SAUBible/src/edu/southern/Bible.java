package edu.southern;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import edu.southern.resources.BibleHelper;

public class Bible extends ListFragment {
	static ArrayAdapter<String> adapter;
	ListView listView1;
	BibleHelper bible = new BibleHelper();

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// populate the List
		adapter = new ArrayAdapter<String>(getActivity(),
				R.layout.fragment_bible_selection, R.id.row_textview1,
				bible.getBooks());
		setListAdapter(adapter);

		// set the action bar layout
		((HomeScreen) getActivity()).setActionBarView(R.layout.actionbar_bible);
	}

	public void onListItemClick(ListView l, View v, int position, long id) {
		// Save the value of the book selected in SharedPreferences
		HomeScreen home = ((HomeScreen)getActivity());
		home.changeReadingBook(position);

		// Create new fragment and transaction
		Fragment chapterFragment = new ChapterSelection();
		// Replace whatever is in the fragment_container view with this
		// fragment,
		// and add the transaction to the back stack
		((HomeScreen) getActivity()).replaceFragment(chapterFragment);
	}
}

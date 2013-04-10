package edu.southern;

import android.os.Bundle;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import edu.southern.R;
import edu.southern.resources.BibleHelper;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;

public class NavDrawer extends Fragment {

	FragmentManager fragmentManager = getFragmentManager();
	FragmentTransaction fragmentTransaction;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_nav_drawer, container, false);
	}

}

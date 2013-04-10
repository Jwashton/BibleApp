package edu.southern;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.app.Fragment;
import android.app.FragmentManager;
import edu.southern.R;

public class Home extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// set the action bar layout
		((HomeScreen) getActivity()).setActionBarView(R.layout.actionbar_home);
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_home, container, false);
	}
}

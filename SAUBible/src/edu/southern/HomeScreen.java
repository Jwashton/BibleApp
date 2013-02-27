package edu.southern;

import android.os.Bundle;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import edu.southern.R;
import edu.southern.resources.*;


public class HomeScreen extends Activity {

  FragmentManager fragmentManager = getFragmentManager();
  FragmentTransaction fragmentTransaction;
	
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home_screen);
    // copy files to device
    transferAssetFiles();
    // initiallize the engine and store it on the application
    initiallizeBibleEngine();
    
    // Add Home fragment to view group as default view
    fragmentTransaction = fragmentManager.beginTransaction();
    
    Home homeFragment = new Home();
    fragmentTransaction.add(R.id.homeFragmentContainer, homeFragment);
    fragmentTransaction.commit();
    
  }
  
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
  	// Adding the Primary Navigation to the Action Bar
    getMenuInflater().inflate(R.menu.primary_nav_menu, menu);    
    
    return true;
  }
  
  
  // Switch Fragments When menu buttons in action bar selected
  public void onViewMenuSelection(MenuItem item) {
  	
  	fragmentTransaction = fragmentManager.beginTransaction();
  	
  	// Switch to appropriate fragment
	  switch (item.getItemId()) {
		  case R.id.Home:
			  Home homeFragment = new Home();
			  fragmentTransaction.replace(R.id.homeFragmentContainer, homeFragment);
			  break;
		  case R.id.Bible:
			  Bible bibleFragment = new Bible();
			  fragmentTransaction.replace(R.id.homeFragmentContainer, bibleFragment);
			  break;
		  default:
			  break;
	  }
	  
	  fragmentTransaction.commit();
  }
  
  protected void initiallizeBibleEngine(){
	  // create a bible engine and start the various aspects of it
	  CBibleEngine engine = new CBibleEngine();
	  String DATA_SOURCE = "data/data/edu.southern/lighthouse/";
	  engine.StartEngine(DATA_SOURCE, "KJV");
	  engine.StartLexiconEngine(DATA_SOURCE);
	  engine.StartMarginEngine(DATA_SOURCE);
	  // store the app on the application class
	  BibleApp app = (BibleApp)getApplication();
	  app.SetEngine(engine);
	  /*
	   * IMPORTANT
	   * In order to get the Bible Engine, use these lines
	   * BibleApp app = (BibleApp)getApplication();
	   * CBibleEngine BibleEngine = app.GetEngine();
	   */
  }
  
  protected void transferAssetFiles(){
	  // copy the asset files necessary for the bible engine to the device
	  BEngineFileMover fileMover = new BEngineFileMover(getApplicationContext());
	  fileMover.copyDataFiles();
  }
  
}

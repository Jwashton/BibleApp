package edu.southern;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import edu.southern.R;

public class HomeScreen extends Activity {
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home_screen);
    // copy files to device
    transferAssetFiles();
    // initiallize the engine and store it on the application
    initiallizeBibleEngine();
  }
  
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.activity_home_screen, menu);
    return true;
  }
  
  protected void initiallizeBibleEngine(){
	  // create a bible engine and start the various aspects of it
	  CBibleEngine BibleEngine = new CBibleEngine();
	  String DATA_SOURCE = "data/data/edu.southern/lighthouse/";
	  BibleEngine.StartEngine(DATA_SOURCE, "KJV");
	  BibleEngine.StartLexiconEngine(DATA_SOURCE);
	  BibleEngine.StartMarginEngine(DATA_SOURCE);
	  // store the app on the application class
	  BibleApp app = (BibleApp)getApplication();
	  app.SetEngine(BibleEngine);
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

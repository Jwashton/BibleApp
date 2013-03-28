package edu.southern;
import android.app.Application;

public class BibleApp extends Application{
    private CBibleEngine BibleEngine;
    private boolean hasEngine;
    
    public CBibleEngine GetEngine() { return BibleEngine; }
    public void SetEngine(CBibleEngine newEngine) {
    	BibleEngine = newEngine;
    	hasEngine = true;
    }
    
    public boolean getHasEngine(){return hasEngine;}
	
}

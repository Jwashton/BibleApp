package edu.southern;
import android.app.Application;

public class BibleApp extends Application{
    private CBibleEngine BibleEngine;
    
    public CBibleEngine GetEngine() { return BibleEngine; }
    public void SetEngine(CBibleEngine newEngine) { BibleEngine = newEngine; }
	
}

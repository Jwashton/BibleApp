package edu.southern.resources;

import java.util.ArrayList;
import java.util.Locale;

import edu.southern.CBibleEngine;

public class SearchHelper {
	
	private StaticBibleInfo staticInfo;
	private CBibleEngine engine;

	public SearchHelper() {
		this(true);
	}

	/**
	 * Only call this method if you do not want the constructor to create the
	 * CBibleEngine
	 * 
	 * @param createEngine
	 *            Specify whether or not the constructor should create and
	 *            initiallize a bible engine
	 */
	public SearchHelper(boolean createEngine) {
		staticInfo = new StaticBibleInfo();
		if (createEngine) {
			engine = new CBibleEngine();
			String DATA_SOURCE = "data/data/edu.southern/lighthouse/";
			engine.StartEngine(DATA_SOURCE, "KJV");
			engine.StartLexiconEngine(DATA_SOURCE);
			engine.StartMarginEngine(DATA_SOURCE);
		}
	}

	/**
	 * Set the helper's bible engine
	 * 
	 * @param engine
	 *            A RUNNING bible engine
	 */
	public void setEngine(CBibleEngine engine) {
		this.engine = engine;
	}
	
	/**
	 * Return a list of verses containing the provided input string
	 * 
	 * @param input
	 * 		A single word to search for verses containing
	 */
	public ArrayList<SearchVerse> getSearchResults(String input){
		input = input.toLowerCase(Locale.ENGLISH).trim(); // the search is case sensitive and seems to expect lower case
		ArrayList<SearchVerse> result = new ArrayList<SearchVerse>();
		long wordNum = engine.FindWord(input);
		byte occurrences[] = engine.GetWordBitMap(wordNum);
		ArrayList<Integer> referenceNumbers = new ArrayList<Integer>();
		//byte currentByte;
		//byte mask = 0x01;
		int next = engine.GetNextConcordanceReference(0, occurrences);
		while(next!=0){
			referenceNumbers.add(next);
			next = engine.GetNextConcordanceReference(next, occurrences);
		}
		
		for(int ref : referenceNumbers){
			result.add(new SearchVerse(engine.ConvertReferenceToString(ref), engine.GetReference(ref)));
		}
		
		return result;
	}
}

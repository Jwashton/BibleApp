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
	 * Return a list of verses containing the provided word
	 * 
	 * @param word
	 * 		A single word to search for verses containing
	 */
	public ArrayList<SearchVerse> searchForWord(String word){
		word = word.toLowerCase(Locale.ENGLISH); // the search is case sensitive and seems to expect lower case
		ArrayList<SearchVerse> result = new ArrayList<SearchVerse>();
		long wordNum = engine.FindWord(word);
		byte occurrences[] = engine.GetWordBitMap(wordNum);
		ArrayList<Integer> referenceNumbers = new ArrayList<Integer>();
		//byte currentByte;
		//byte mask = 0x01;
		int next = engine.GetNextConcordanceReference(0, occurrences);
		while(next!=0){
			referenceNumbers.add(next);
			next = engine.GetNextConcordanceReference(next, occurrences);
		}
		
		String text;
		SearchVerse v;
		for(int ref : referenceNumbers){
			v = new SearchVerse();
			v.reference = engine.ConvertReferenceToString(ref);
			v.text = engine.GetReference(ref);				
			result.add(v);
		}
		/*for(int i = 0; i < occurrences.length; i++){
			currentByte = occurrences[i];
			for(int j = 1; j < 9; j++){
				int bit = currentByte & mask;
				if(bit == 1){
					referenceNumbers.add(i * 8 + j);
				}
				currentByte = (byte)(currentByte >>> 1) ;
			}
		}
		*/
		return result;
	}
}

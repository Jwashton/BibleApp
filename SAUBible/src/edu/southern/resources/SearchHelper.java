package edu.southern.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	public SearchResult getSearchResults(String input){
		input = input.toLowerCase(Locale.ENGLISH).trim(); // the search is case sensitive and expects lower case
		SearchResult results = new SearchResult();
		ArrayList<byte[]> byteArrays = new ArrayList<byte[]>();
		// Split the input string into individual words
		// for each word, get a byte array of the verses where it occurs
		String inputWords[] = input.split(" ");
		// No need to continue if we don't have any words
		if(inputWords.length==0)
			return results;
		for(String word : inputWords){
			getWordBytes(results, byteArrays, word);
		}
		if(byteArrays.size()==0)
			return results;
		// Combine the array(s) into a single array of verses to load
		byte resultBytes[] = combineByteArrays(byteArrays);
		ArrayList<Integer> referenceNumbers = new ArrayList<Integer>();
		int next = engine.GetNextConcordanceReference(0, resultBytes);
		while(next!=0){
			referenceNumbers.add(next);
			next = engine.GetNextConcordanceReference(next, resultBytes);
		}
		for(int ref : referenceNumbers){
			results.verses.add(new SearchVerse(engine.ConvertReferenceToString(ref), engine.GetReference(ref)));
		}
		
		sortByRelevance(results, input);
		
		//TODO ? if no results found, broaden search
		return results;
	}
	
	private void sortByRelevance(SearchResult results, String seek){
		int insertPosition = 0;
		ArrayList<SearchVerse> verses = results.verses;
		Pattern p = Pattern.compile(seek, Pattern.CASE_INSENSITIVE);
		Matcher m;
		for(int i=0; i < verses.size(); i++){
			// if the seek string is containd in the verse
			// rotate the sublist so that all verses that match
			// the seek string go to the beginning of the results
			m = p.matcher(verses.get(i).text);
			if(m.find()){
				Collections.rotate(verses.subList(insertPosition, i+1), 1);
				insertPosition++;
			}
		}
	}
	
	private void getWordBytes(SearchResult searchResults, ArrayList<byte[]> results, String word){
		long wordNum = engine.FindWord(word);
		
		// return empty array if no word matches found
		if(wordNum > 0){
			byte[] occurences = engine.GetWordBitMap(wordNum);
			for(int i=0; i < occurences.length; i++){
				if(occurences[i] != 0){
					searchResults.terms.add(word);
					results.add(occurences);
					return;
				}
			}
		}
	}
	
	/**
	 * Perform bitwise ANDs on a set of of byte arrays
	 * The set must contain at least one array 
	 * @param arrays a set of byte arrays, not changed by this operation
	 * @return The result of ANDing all arrays in the set together.
	 */
	private byte[] combineByteArrays(ArrayList<byte[]> arrays){
		byte result[] = new byte[engine.GetWordBitMap(1).length];
		System.arraycopy(arrays.get(0), 0, result, 0, result.length);
		for(int i = 1; i < arrays.size(); i++){
			byte[] cursor = arrays.get(i);
			for(int j = 0; j < cursor.length; j++){
				result[j] = (byte) (result[j] & cursor[j]);
			}
		}
		return result;
	}
}

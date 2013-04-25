package edu.southern.resources;

import java.util.ArrayList;

public class SearchResult {
	ArrayList<SearchVerse> verses;
	ArrayList<String> terms;
	
	public ArrayList<SearchVerse> getVerses(){
		return verses;
	}
	
	public ArrayList<String> getTerms(){
		return terms;
	}
	
	SearchResult(){
		verses = new ArrayList<SearchVerse>();
		terms = new ArrayList<String>();
	}
}

package edu.southern.resources;

import java.util.ArrayList;

/**
 * Data storage class to hold 
 * information about a Bible chapter
 * Stores:
 * 	the book name
 * 	chapter number within book
 * 	number of verses in the chapter
 * 	list of the chapter's verses
 * @author Isaac
 *
 */
public class Chapter {
	public String bookName;
	public int chapterNumber;
	public int numVerses;
	public ArrayList<Verse> verses;

	public Chapter() {}
}

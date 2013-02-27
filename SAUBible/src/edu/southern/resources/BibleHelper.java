package edu.southern.resources;

import java.util.ArrayList;

import edu.southern.CBibleEngine;

/**
 * Libary class designed to provide helpful information 
 * regarding Bible books, chapters, and verses
 * @author Isaac
 */
public class BibleHelper {
	
	private StaticBibleInfo staticInfo; 
	
	public BibleHelper(){
		staticInfo = new StaticBibleInfo();
	}
	
	/**
	 * Get all book names from the Bible
	 * @return
	 * A string array containing the names of 
	 * all Bible books in the order in which they appear
	 */
	public String[] getBooks(){
		return staticInfo.books;
	}
	
	/**
	 * Find the book number for the provided book name
	 * @param bookName Book name to find position of. ex. "Matthew" or "Genesis"
	 * @return The index for the specified book
	 * Index for Genesis is 0
	 */
	public int getBookNumber(String bookName){
		int bookIndex = -1;	
		for(int i=0; i < staticInfo.books.length; i++){
			if(staticInfo.books[i].equalsIgnoreCase(bookName)){
				bookIndex = i;
				break;
			}
		}
		return bookIndex;
	}
	
	/**
	 * Get the number of chapters in the specified book.
	 * @param bookName The book to get a chapter count for, ex. "Psalms"
	 * @return integer number of chapters in the specified book
	 * Throws an exception if the specified book name does not exist
	 * @throws Exception 
	 */
	public int getChapterCount(String bookName) throws Exception{	
		int bookIndex = getBookNumber(bookName);
		if(bookIndex==-1)
			throw new Exception("Invalid book name");
		return staticInfo.numChapters[bookIndex];
	}
	
	/**
	 * Return the number of verses in the specified book and chapter
	 * @param bookName The name of the book. ex. "John"
	 * @param chapterNumber Chapter to get verses for. ex. 3
	 * @return the number of verses for the book and chapter
	 * @throws Exception if the book name is invalid or the chapter number is 
	 * less than 0 or greater than the number of chapters in the bok
	 */
	public int getVersesInChapter(String bookName, int chapterNumber) throws Exception{
		int bookNumber = getBookNumber(bookName);
		boolean badBook = bookNumber==-1;
		boolean badChapter = chapterNumber > staticInfo.numChapters[bookNumber] || chapterNumber < 1;
		if(badBook)
			throw new Exception("Invalid book name.");
		if(badChapter)
			throw new Exception("Invalid chapter number, must be greater than 0 and less than number of chapters in book.");
		return staticInfo.verses[bookNumber][chapterNumber-1];
	}
	
	/**
	 * Find the number of verses given a book name and chapter number
	 * @param bookAndChapter Book and chapter in a single string, ex. "Matthew 5"
	 * @return
	 * @throws Exception if the book name is invalid or the chapter number is 
	 * Less than 0 or greater than the number of chapters in the book
	 */
	public int getVersesInChapter(String bookAndChapter) throws Exception{
		String parts[] = bookAndChapter.split(" ");
		int chapterNumber = tryParseInt(parts[parts.length-1]);
		String bookName = "";
		for(int i=0; i < parts.length-1; i++)
			bookName += parts[i];
		return getVersesInChapter(bookName, chapterNumber);
	}
	
	private int tryParseInt(String value) throws Exception{
		try	{
			return Integer.parseInt(value);
		} catch(NumberFormatException e){
			throw new Exception("Invalid input string. Should be \"bookName chapterNumber\"");
		}
	}
	
	/** 
	 * Get a Chapter object containing information about
	 * the requested Bible chapter
	 * @param bookName Name of desired book, ex. "Hosea"
	 * @param chapterNumber Desired chapter number
	 * @param engine A running CBibleEngine
	 * @return A Chapter object containing with the book name and 
	 * @return chapter number, the number of verses in the chapter, and
	 * @return the text of the chapter in the form of an ArrayList of Verse objects
	 * @throws Exception
	 */
	public Chapter getChapterText(String bookName, int chapterNumber, CBibleEngine engine) throws Exception{
		// create the chapter and set information fields
		Chapter chapter = new Chapter();
		// getVersesInChapter() will throw an exception if bad input was provided
		chapter.numVerses = getVersesInChapter(bookName, chapterNumber);
		chapter.bookName = bookName;
		chapter.chapterNumber = chapterNumber;
		
		// build a string to get the initial reference number
		String refString = bookName + " " + chapterNumber + ":1";
		long reference = engine.ConvertStringToReference(refString);
		// get all verses from the chapter
		chapter.verses = new ArrayList<Verse>();
		for(int i=1; i <= chapter.numVerses; i++){
			Verse verse = new Verse();
			verse.verseNumber = i;
			// get verse text and increment reference counter
			verse.text = engine.GetReference(reference++);
			chapter.verses.add(verse);
		}
		return chapter;
	}
	
	/** 
	 * Get a Chapter object containing information about
	 * the requested Bible chapter
	 * @param bookAndChapter combined book name and chapter number, ex. "Matthew 1" 
	 * @param engine A running CBibleEngine
	 * @return A Chapter object containing with the book name and 
	 * @return chapter number, the number of verses in the chapter, and
	 * @return the text of the chapter in the form of an ArrayList of Verse objects
	 * @throws Exception
	 */
	public Chapter getChapterText(String bookAndChapter, CBibleEngine engine) throws Exception{
		String parts[] = bookAndChapter.split(" ");
		int chapterNumber = tryParseInt(parts[parts.length-1]);
		String bookName = "";
		for(int i=0; i < parts.length-1; i++)
			bookName += parts[i];
		return getChapterText(bookName, chapterNumber, engine);
	}
}


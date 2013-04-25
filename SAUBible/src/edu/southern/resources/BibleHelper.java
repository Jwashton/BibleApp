package edu.southern.resources;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.southern.CBibleEngine;

/**
 * Libary class designed to provide helpful information regarding Bible books,
 * chapters, and verses
 * 
 * @author Isaac
 */
public class BibleHelper {

	private StaticBibleInfo staticInfo;
	private CBibleEngine engine;

	public BibleHelper() {
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
	public BibleHelper(boolean createEngine) {
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
	 * Get all book names from the Bible
	 * 
	 * @return A string array containing the names of all Bible books in the
	 *         order in which they appear
	 */
	public String[] getBooks() {
		return staticInfo.books;
	}

	/**
	 * Get book name for book number
	 * 
	 * @return A string containing the Bible book name
	 */
	public String getBookName(int bookNumber) {
		return getBooks()[bookNumber];
	}

	/**
	 * Find the book number for the provided book name
	 * 
	 * @param bookName
	 *            Book name to find position of. ex. "Matthew" or "Genesis"
	 * @return The index for the specified book Index for Genesis is 0
	 */
	public int getBookNumber(String bookName) {
		int bookIndex = -1;
		for (int i = 0; i < staticInfo.books.length; i++) {
			if (staticInfo.books[i].equalsIgnoreCase(bookName)) {
				bookIndex = i;
				break;
			}
		}
		return bookIndex;
	}

	/**
	 * Get the number of chapters in the specified book.
	 * 
	 * @param bookName
	 *            The book to get a chapter count for, ex. "Psalms"
	 * @return integer number of chapters in the specified book Throws an
	 *         exception if the specified book name does not exist
	 * @throws Exception
	 */
	public int getChapterCount(String bookName) throws Exception {
		int bookIndex = getBookNumber(bookName);
		if (bookIndex == -1)
			throw new Exception("Invalid book name");
		return staticInfo.numChapters[bookIndex];
	}
	
	public int getChapterCount(int bookNumber) throws Exception {
		if (bookNumber == -1 || bookNumber >= getBooks().length)
			throw new Exception("Invalid book number");
		return staticInfo.numChapters[bookNumber];
	}

	/**
	 * Return the number of verses in the specified book and chapter
	 * 
	 * @param bookName
	 *            The name of the book. ex. "John"
	 * @param chapterNumber
	 *            Chapter to get verses for. ex. 3
	 * @return the number of verses for the book and chapter
	 * @throws Exception
	 *             if the book name is invalid or the chapter number is less
	 *             than 0 or greater than the number of chapters in the bok
	 */
	public int getVersesInChapter(String bookName, int chapterNumber)
			throws Exception {
		int bookNumber = getBookNumber(bookName);
		boolean badBook = bookNumber == -1;
		boolean badChapter = chapterNumber > staticInfo.numChapters[bookNumber]
				|| chapterNumber < 1;
		if (badBook)
			throw new Exception("Invalid book name.");
		if (badChapter)
			throw new Exception(
					"Invalid chapter number, must be greater than 0 and less than number of chapters in book.");
		return staticInfo.verses[bookNumber][chapterNumber - 1];
	}

	/**
	 * Find the number of verses given a book name and chapter number
	 * 
	 * @param bookAndChapter
	 *            Book and chapter in a single string, ex. "Matthew 5"
	 * @return
	 * @throws Exception
	 *             if the book name is invalid or the chapter number is Less
	 *             than 0 or greater than the number of chapters in the book
	 */
	public int getVersesInChapter(String bookAndChapter) throws Exception {
		String parts[] = bookAndChapter.split(" ");
		int chapterNumber = tryParseInt(parts[parts.length - 1]);
		String bookName = "";
		for (int i = 0; i < parts.length - 1; i++)
			bookName += parts[i];
		return getVersesInChapter(bookName, chapterNumber);
	}

	private int tryParseInt(String value) throws Exception {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			throw new Exception(
					"Invalid input string. Should be \"bookName chapterNumber\"");
		}
	}

	/**
	 * 
	 * @param refString
	 *            A reference string to parse into a reference object using the
	 *            BibleEngine
	 * @return A reference object containing a book number, chapter number, and
	 *         verse number
	 */
	public Reference parseReference(String refString)
			throws ReferenceStringException {
		long refNumber = engine.ConvertStringToReference(refString);
		// if no result, try again again attempting to prettify the string
		if (refNumber == -1) {
			refString = completeReferenceString(refString);
			refNumber = engine.ConvertStringToReference(refString);
			// if still no result, bad input, throw exception
			// TODO create more detailed error messages to show to the user
			// for example, tell them what part of the reference was bad and why
			if (refNumber == -1) {
				throw new ReferenceStringException(
						"Reference not found, please enter a reference such as \"Exodus\", \"John 3\" or \"Matthew 3 17\"");
			}
		}
		String parsedReference = engine.ConvertReferenceToString(refNumber);
		Reference reference = new Reference(parsedReference);
		return reference;
	}

	public String completeReferenceString(String refString)
			throws ReferenceStringException {
		String prefix = "", book = "", chapter = "", verse = "";
		String patString = "(\\d{0,1})\\W*(\\w{2,})(?:\\W+(\\d{1,3})(?:\\D+(\\d{1,3})){0,1}\\W*){0,1}";
		Pattern p = Pattern.compile(patString);
		Matcher m = p.matcher(refString);
		if (m.find()) {
			int count = m.groupCount();
			if(count > 0){
				prefix = m.group(1) == null ? "" : m.group(1).concat(" ");
			}
			// must at least have a book or things will eventually fail
			if (count > 1) {
				book = m.group(2) == null ? "" : m.group(2);
			}
			// default chapter to 1
			if (count > 2) {
				chapter = m.group(3) == null ? "1" : m.group(3);
			}
			// default verse to 1
			if (count > 3) {
				verse = m.group(4) == null ? "1" : m.group(4);
			}
		}
		// The bible engine doesn't recognize 'psalms'
		if (book.toLowerCase(Locale.ENGLISH).contains("psalm")) {
			book = "Psalm";
		}
		refString = prefix.concat(book).concat(" ").concat(chapter).concat(":").concat(verse);
		return refString;
	}

	/**
	 * Get a Chapter object containing information about the requested Bible
	 * chapter
	 * 
	 * @param bookName
	 *            Name of desired book, ex. "Hosea"
	 * @param chapterNumber
	 *            Desired chapter number
	 * @return A Chapter object containing with the book name and
	 * @return chapter number, the number of verses in the chapter, and
	 * @return the text of the chapter in the form of an ArrayList of Verse
	 *         objects
	 * @throws Exception
	 */
	public Chapter getChapterText(String bookName, int chapterNumber)
			throws Exception {
		// create the chapter and set information fields
		Chapter chapter = new Chapter();
		// getVersesInChapter() will throw an exception if bad input was
		// provided
		chapter.numVerses = getVersesInChapter(bookName, chapterNumber);
		chapter.bookName = bookName;
		chapter.chapterNumber = chapterNumber;

		// build a string to get the initial reference number
		String refString = bookName + " " + chapterNumber + ":1";
		long reference = engine.ConvertStringToReference(refString);
		// get all verses from the chapter
		chapter.verses = new ArrayList<Verse>();
		for (int i = 1; i <= chapter.numVerses; i++) {
			Verse verse = new Verse();
			verse.verseNumber = i;
			// get verse text and increment reference counter
			verse.text = engine.GetReference(reference++);
			chapter.verses.add(verse);
		}
		return chapter;
	}

	/**
	 * Get a Chapter object containing information about the requested Bible
	 * chapter
	 * 
	 * @param bookAndChapter
	 *            combined book name and chapter number, ex. "Matthew 1"
	 * @return A Chapter object containing with the book name and
	 * @return chapter number, the number of verses in the chapter, and
	 * @return the text of the chapter in the form of an ArrayList of Verse
	 *         objects
	 * @throws Exception
	 */
	public Chapter getChapterText(String bookAndChapter) throws Exception {
		String parts[] = bookAndChapter.split(" ");
		int chapterNumber = tryParseInt(parts[parts.length - 1]);
		String bookName = "";
		for (int i = 0; i < parts.length - 1; i++)
			bookName += parts[i];
		return getChapterText(bookName, chapterNumber);
	}
}

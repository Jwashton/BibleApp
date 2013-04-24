package edu.southern.resources;

/**
 * Data class to hold reference information for a Bible verse
 * Stores:
 * 	the book number
 * 	the chapter number
 * 	and the verse number
 * @author Isaac
 *
 */
public class Reference {
	private int bookNumber;
	private int chapterNumber;
	private int verseNumber;

	public int getBookNumber() {
		return bookNumber;
	}

	public int getChapterNumber() {
		return chapterNumber;
	}

	public int getVerseNumber() {
		return verseNumber;
	}

	/**
	 * Parse a reference string into a reference object
	 * By determining the book, chapter, and verse numbers
	 * @param refString
	 *            a reference string in the following form: "Matthew 1:1"
	 */
	public Reference(String refString) {
		BibleHelper helper = new BibleHelper(false);
		String[] refParts = refString.split(" ");
		// Ref parts consist of {bookname, chapter & verse}
		// or {prefix number, book name, chapter & verse}
		String bookName = refParts.length == 3 ? refParts[0].concat(" ").concat(refParts[1]) : refParts[0];
		bookNumber = helper.getBookNumber(bookName);
		if (refParts.length > 1) {
			String[] chapterAndVerse = refParts[refParts.length - 1].split(":");
			chapterNumber = Integer.parseInt(chapterAndVerse[0]);
			if (chapterAndVerse.length > 1) {
				verseNumber = Integer.parseInt(chapterAndVerse[1]);
			} else {
				verseNumber = 1;
			}
		} else {
			chapterNumber = 1;
			verseNumber = 1;
		}
	}
}

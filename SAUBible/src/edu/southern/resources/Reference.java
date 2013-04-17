package edu.southern.resources;

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
	 * Convert a reference string to a reference object
	 * 
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

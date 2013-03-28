package edu.southern.resources;

public class Reference {
	private int bookNumber;
	private int chapterNumber;
	private int verseNumber;
	
	public int getBookNumber(){
		return bookNumber;
	}
	
	public int getChapterNumber(){
		return chapterNumber;
	}
	
	public int getVerseNumber(){
		return verseNumber;
	}
	
	/**
	 * Convert a reference string to a reference object
	 * @param refString a reference string in the following form: "Matthew 1:1"
	 */
	Reference(String refString){
		BibleHelper helper = new BibleHelper();
		String[] refParts = refString.split(" ");
		bookNumber = helper.getBookNumber(refParts[0]);
		if(refParts.length > 1){
			String[] chapterAndVerse = refParts[1].split(":");
			chapterNumber = Integer.parseInt(chapterAndVerse[0]);
			if(chapterAndVerse.length > 1){
				verseNumber = Integer.parseInt(chapterAndVerse[1]);
			}
			else{
				verseNumber = 1;
			}
		}
		else{
			chapterNumber = 1;
			verseNumber = 1;
		}
	}
}

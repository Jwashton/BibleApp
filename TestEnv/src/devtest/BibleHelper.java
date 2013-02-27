package devtest;

/**
 * Libary class designed to provide helpful information 
 * regarding Bible books, chapters, and verses
 * @author Isaac
 */
public class BibleHelper {
	
	private StaticBibleInfo staticInfo; 
	
	BibleHelper(){
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
		int chapterNumber=0;
		try	{
			chapterNumber = Integer.parseInt(parts[parts.length-1]);
		} catch(NumberFormatException e){
			throw new Exception("Invalid input string. Should be \"bookName chapterNumber\"");
		}
		String bookName = "";
		for(int i=0; i < parts.length-1; i++)
			bookName += parts[i];
		return getVersesInChapter(bookName, chapterNumber);
	}
	
}


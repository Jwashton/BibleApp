package devtest;

public class Test {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		BibleHelper bible = new BibleHelper();
		print(bible.getBookNumber("Exodus")); // 2
		String names[] = bible.getBooks();
		for(String name : names)
			printSameLine(name);
		print("");
		print(bible.getChapterCount("Matthew")); // 28
		print(bible.getVersesInChapter("Genesis 1"));
		print(bible.getVersesInChapter("Genesis", 1));
		print(bible.getChapterCount("asdflkj")); // throws
	}

	public static void print(String input){
		System.out.println(input);
	}
	
	public static void printSameLine(String input){
		System.out.print(input+" ");
	}
	
	public static void print(int input){
		System.out.println(input);
	}

}
;
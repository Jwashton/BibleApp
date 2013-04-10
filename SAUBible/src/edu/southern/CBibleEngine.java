package edu.southern;

import edu.southern.R;

public class CBibleEngine {
	static {
		System.loadLibrary("BibleEngine");
	}

	// Verse and concordance engine
	public native long StartEngine(String Path, String BookCode); // Start the
																	// verse and
																	// concordance
																	// information
																	// engine.
																	// BookCode
																	// = "KJV"
																	// or "NIV"
																	// or "NAS"

	public native int StopEngine(); // Shutdown the engine

	public native int GetTotalVerses(); // Get the total number of verses in the
										// given version of the Bible (ie.
										// BookCode)

	public native int GetTotalWords(); // Get the total unique word count for
										// the given version of the Bible

	public native long ConvertStringToReference(String RefStr); // Convert a
																// reference
																// string such
																// as "Phi 2:13"
																// to a
																// reference
																// number

	public native long ConvertStringToReferenceLen(String RefStr); // Determine
																	// the
																	// number of
																	// verses
																	// described
																	// by the
																	// given
																	// reference
																	// string
																	// (eg. Phi
																	// 2:13-3:2)

	public native String ConvertReferenceToString(long Reference); // Convert a
																	// verse
																	// reference
																	// number to
																	// a string
																	// reference
																	// (eg.
																	// 23146 ->
																	// Matthew
																	// 1:1)

	public native String GetReference(long Reference); // Get the given verse
														// text

	public native int FindWord(String Target); // Get the WordNum for the given
												// target word, if it exists

	public native String FindFirstWord(String Target); // Get the closest word
														// that is equal or
														// greater than target
														// word, alphabetically

	public native String FindNextWord(String Target); // Get the next word from
														// the given target word

	public native String FindPreviousWord(String Target); // Get the previous
															// word from the
															// given target word

	public native int GetOccurences(long WordNum); // Get the number of verses
													// that contain the given
													// WordNum

	public native int GetBitMapOccurences(long StartVerse, byte[] BitMap); // Determine
																			// the
																			// number
																			// of
																			// verses
																			// from
																			// the
																			// StartVerse
																			// to
																			// the
																			// end
																			// of
																			// the
																			// Bible
																			// in
																			// the
																			// given
																			// concordance
																			// bitmap

	public native byte[] GetWordBitMap(long WordNum); // Get the concordance
														// bitmap for the given
														// WordNum (1 bit per
														// verse starting from
														// Genesis 1:1 to the
														// end of the Bible)

	public native byte[] GetStrongsWordBitMap(char fNewTestament, long WordNum); // Get
																					// the
																					// concordance
																					// bitmap
																					// for
																					// a
																					// Strongs
																					// number

	public native int GetNextConcordanceReference(long CurrentVerse,
			byte[] BitMap); // Get the next verse in the given bitmap from
							// location CurrentVerse

	public native int GetPreviousConcordanceReference(long CurrentVerse,
			byte[] BitMap); // Get the previous verse in the given bitmap from
							// location CurrentVerse

	// Marginal References engine
	public native byte StartMarginEngine(String Path); // Start margin
														// information engine

	public native void StopMarginEngine(); // Stop margin engine

	public native int GetTotalMarginVerses(); // Get number of marginal
												// references for the current
												// reference - must call
												// GetFirstMargin or GetMargin
												// first

	public native long GetFirstMargin(long Reference); // Get the first marginal
														// reference for the
														// given verse

	public native long GetNextMargin(); // Get the next marginal reference -
										// must call GetFirstMargin first

	public native long GetPreviousMargin(); // Get the previous marginal
											// reference - must call
											// GetNextMargin first

	public native long GetMargin(long Reference, int MarginNumber); // Get the
																	// specific
																	// marginal
																	// reference
																	// for the
																	// given
																	// verse

	// Strongs Lexicon engine
	public native char StartLexiconEngine(String Path); // Startup the strongs
														// information engine

	public native void StopLexiconEngine(); // Close down the strongs
											// information engine

	public native int GetStrongs(long Reference, char Index); // Get the strong
																// # for the
																// word at index
																// in the given
																// verse (index
																// 1 is first
																// word)

	public native int GetNumStrongs(long Reference); // Number of words in this
														// verse that have
														// lexicon definitions

	public native String GetDef(long Reference, int StrongsNum); // Get the
																	// lexicon
																	// definition
																	// for the
																	// given
																	// strongs #
																	// in the
																	// given
																	// verse

}

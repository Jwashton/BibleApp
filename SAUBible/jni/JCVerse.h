#ifndef __LENGINE_H
#define __LENGINE_H


#define TRUE 1
#define FALSE 0

#define NUM_STRONGS_OLDT 8674
#define NUM_STRONGS_NEWT 5624
#define NUM_VERSES_OLDT 23145

typedef struct {
	unsigned long dwStart;
	unsigned short uNumEntries;
} SrchStrongsRec;

class TEngine {
	struct BookStats {
		unsigned long Root;
		unsigned short TotalVerses;
		unsigned short UniqueWords;
		long TotalWords;
		char BibleFlag;
	};

  	/* Lzw compression table */
  struct {
  	short int PrevChar;
    short int FollChar;
 
  } StringTable[4096];

	#define MaxKeysInNode 4
	#define MaxKeySize 20

	struct Node {
		short int KeyCount;
		long BackLink;
		char Keys[MaxKeysInNode+1][MaxKeySize];
		long Label[MaxKeysInNode+1];
		long Branch[MaxKeysInNode+1];
		char Disk[MaxKeysInNode+1];
	};

 FILE *VerseFile;			/* Handles for each file for the file handler */
 FILE *VerseIndexFile;
 FILE *WordFile;
 FILE *WordNdxFile;
 FILE *CIndexFile;
 FILE *ConcordFile;
 FILE *CSrchStrongsIndexFile;
 FILE *CSrchStrongsFile;

	/* Word list variables */
 long TargetNode;
 short int TargetPos;
 char Found;
 char EndOfList;
 char BeginningOfList;
 Node *NodeBuf;
 char EngineStarted;   // True if StartEngine has been called
											 // False if StopEngine has been called

 unsigned long CurrentIndex;
 unsigned long CurrentReference;
 int CurrentReferenceSize;
 char *FilePath;
 char BookCode[4];
 char CurrentBibleBook[10];
 int CurrentChapter;

 BookStats *Stats;

public:
	TEngine(void);
	~TEngine(void);
	unsigned GetTotalVerses(void) { return (Stats->TotalVerses); };
	unsigned GetTotalWords(void) { return (Stats->UniqueWords); };
	char GetBibleFlag(void) { return (Stats->BibleFlag); };
	int GetCurrentChapter(void) { return CurrentChapter; };	
	char *GetCurrentBibleBook(void) { return (CurrentBibleBook); };
	unsigned long	GetCurrentReferenceNumber(void) { return CurrentReference; };
	char *GetBookCode(void) { return BookCode; };
	int StartEngine(const char *Path, const char *BookCode);
	int StopEngine(void);
	int GetBookStats(BookStats *Stats);
	long ConvertBibleString(char *ReferenceStr);
	long ConvertStringToReference(char *ReferenceStr);	
	unsigned char ConvertStringToReference(char *pReferenceStr, unsigned short *RefStart, unsigned short *RefLen); 
	char *ConvertReferenceToString(unsigned long Reference); 
	void DeCompressBuffer(char *InBuffer, unsigned NumBytes, char *OutBuffer, unsigned& OutBytes);
	char *GetReference(unsigned long Reference); 
	char *GetReferenceEx(char *pReferenceStr);
	int GetReference2(unsigned long Reference, unsigned& CompressedReferenceLength, char *Buffer, unsigned& ReferenceSize);
	int GetReferenceLength(unsigned long Reference, unsigned& ReferenceLength);
	int GetCurrentReference(char *Buffer, unsigned& ReferenceSize);
	int GetRecord(long RecordNum, Node& NodeBuf) ;
	long SearchNode(Node NodeBuf, char *Target, short int& TargetPos) ;
	long SearchTree(char *Target, long Start, short int& TargetPos, long& TargetNode) ;
	unsigned FindWord(const char *Target);	
	int FindFirstWord(char *Target, long& Label); 
	int FindNextWord(char *Target, long& Label);
	int FindPreviousWord(char *Target, long& Label);
	int GetOccurences(unsigned long WordNum);
	int GetBitMapOccurences(unsigned StartVerse, char *BitMap); 
	int GetWordBitMap(unsigned long WordNum, char *BitMap, int& Occurences); 
	int GetStrongsWordBitMap(unsigned char fNewTestament, unsigned long WordNum, char *BitMap, int& Occurences); // yes
	unsigned GetNextConcordanceReference(unsigned CurrentVerse, char *BitMap); 
	unsigned GetPreviousConcordanceReference(unsigned CurrentVerse, char *BitMap); 
};


#endif


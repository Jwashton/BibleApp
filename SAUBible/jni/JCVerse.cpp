/*********************************************************************
*  Program: Bible Lookup and search engine
*  Author : Michael W. Dant
*  Date   : August, 1991
*  Purpose: To provide tools for accessing and searching Bible/Book data
*  Interface :
*		 int StartEngine(char *Path, char *BookCode)
*
*
*/
//*** Test cases for GetReferenceEx
//BibleEngine[0]->GetReferenceEx(("mat 1 8");
//BibleEngine[0]->GetReferenceEx(("MAT 1:8");
//BibleEngine[0]->GetReferenceEx(("mat 1 25");
//BibleEngine[0]->GetReferenceEx(("mat 1 26");
//BibleEngine[0]->GetReferenceEx(("matthew 1:2-3");
//BibleEngine[0]->GetReferenceEx(("mat 1:15-2:4");
//BibleEngine[0]->GetReferenceEx(("mat 1:2, 6; 3:5; 6:7-8");
//BibleEngine[0]->GetReferenceEx(("1 co 1 2");
//BibleEngine[0]->GetReferenceEx(("1 corinthians 2 3");
//BibleEngine[0]->GetReferenceEx(("1co 1 2");
//BibleEngine[0]->GetReferenceEx(("KJV 1co 1 2");

#include <edu_southern_CBibleEngine.h>
#include <jni.h>
#include <android/log.h>

#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include <ctype.h>
#include "JCVerse.h"
#include "JCeVerseTbl.h"

#define MaxKeysInNode 4
#define MinKeysInNode 2
#define MaxKeySize 20
#define TRUE 1
#define FALSE 0
#define MAX_REFS 20
#define MAX_ENGINES 5

typedef struct {
	long RefStart;
	int RefLen;
} ReferenceType;

//TEngine *pEngine[MAX_ENGINES] = { NULL, NULL, NULL, NULL, NULL};
TEngine *pEngine = NULL;

void log(int level, const char *format, ...)
 {
   va_list arglist;

   va_start(arglist, format);
   __android_log_vprint(level, "Bible", format, arglist);
   va_end(arglist);

   return;
}

void loga(const char *format, ...)
 {
   va_list arglist;

   va_start(arglist, format);
   __android_log_vprint(ANDROID_LOG_VERBOSE, "Bible", format, arglist);
   va_end(arglist);

   return;
}


void tstrcpy(char *Dest, char *Src) {
    for (unsigned i = 0; i <= strlen(Src); i++)
      Dest[i] = Src[i];
}

int StrCmpIgnoreCase(const char *str1, const char *str2) {
	char AStr1[30], AStr2[30];
	
	strcpy(AStr1, str1);
	strcpy(AStr2, str2);
	
	for (unsigned i = 0; i <= strlen(AStr1); i++)
		AStr1[i] = tolower(AStr1[i]);
	for (unsigned i = 0; i <= strlen(AStr2); i++)
		AStr2[i] = tolower(AStr2[i]);
	
	return strcmp(AStr1, AStr2);
}

int StrCmpIgnoreCaseLen(const char *str1, const char *str2, int Len) {
	char AStr1[30], AStr2[30];
	
	strcpy(AStr1, str1);
	strcpy(AStr2, str2);
	
	for (unsigned i = 0; i <= strlen(AStr1); i++)
		AStr1[i] = tolower(AStr1[i]);
	for (unsigned i = 0; i <= strlen(AStr2); i++)
		AStr2[i] = tolower(AStr2[i]);
	
	AStr1[Len] = 0;
	AStr2[Len] = 0;

	return strcmp(AStr1, AStr2);
}

TEngine::TEngine(void) {
	Stats = new BookStats;

	TargetNode = 0; TargetPos = 0; Found = FALSE;

	NodeBuf = new Node;

	CurrentReference = 0;
	*BookCode = 0;

	EngineStarted = FALSE;

}

TEngine::~TEngine(void) {
	delete Stats;
	delete NodeBuf;

	StopEngine();  // if not properly stopped, shut it down
};

int TEngine::StartEngine(const char *Path, const char *Code) {
  FILE *LzwFile;
  unsigned long BytesRead;
  int Status;

	char FileName[60];

	FilePath = new char [strlen(Path)+1];
	strcpy(FilePath,Path);
	strcpy(BookCode,Code);

  	/* Read decompression table */
	strcpy(FileName,Path);
	strcat(FileName, "LZWTABLE.");
	strcat(FileName,BookCode);

  LzwFile = fopen(FileName, "rb");	// CreateFile(FileName, GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);

  if (LzwFile == NULL) return -1;

  fread(StringTable, sizeof(StringTable), 1, LzwFile);

  fclose(LzwFile);

		/* Open verse file */
	strcpy(FileName,Path);
	strcat(FileName, "VERSE.");
	strcat(FileName,BookCode);

	VerseFile = fopen(FileName, "rb"); // CreateFile(FileName, GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);

	if (VerseFile == NULL) return(-1); /* Error */

	strcpy(FileName,Path);
	strcat(FileName, "VINDEX.");
	strcat(FileName,BookCode);
	VerseIndexFile = fopen(FileName, "rb"); // CreateFile(FileName, GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);

	if (VerseIndexFile == NULL) return(-1);  /* Error */

	Status = GetBookStats((BookStats *)Stats);

	if (Status != 0) return(-1);   /* Error */

	strcpy(FileName,Path);
	strcat(FileName, "WORDS.");
	strcat(FileName,BookCode);

	WordFile = fopen(FileName, "rb"); // CreateFile(FileName, GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);

	if (WordFile == NULL) return(-1); /* Error */

	strcpy(FileName,Path);
	strcat(FileName, "WORDSNDX.");
	strcat(FileName,BookCode);

	WordNdxFile = fopen(FileName, "rb"); // CreateFile(FileName, GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);

	if (WordNdxFile == NULL) return(-1); /* Error */

	strcpy(FileName,Path);
	strcat(FileName, "CINDEX.");
	strcat(FileName,BookCode);

	CIndexFile = fopen(FileName, "rb"); // CreateFile(FileName, GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);

	if (CIndexFile == NULL) return(-1); /* Error */

	strcpy(FileName,Path);
	strcat(FileName, "CONCORD.");
	strcat(FileName,BookCode);

	ConcordFile = fopen(FileName, "rb"); // CreateFile(FileName, GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);

	if (ConcordFile == NULL) return(-1); /* Error */

	strcpy(FileName,Path);
	strcat(FileName, "SRCHSTRONGS.NDX");

	CSrchStrongsIndexFile = fopen(FileName, "rb"); // CreateFile(FileName, GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);

	strcpy(FileName,Path);
	strcat(FileName, "SRCHSTRONGS.DAT");

	CSrchStrongsFile = fopen(FileName, "rb"); // CreateFile(FileName, GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);

	EngineStarted = TRUE;

	loga("Engine started");

	return(0);  /* Successful */
}

int TEngine::StopEngine(void) {

		// Check if the engine is started
	if (!EngineStarted) return -1;

	fclose(VerseFile);
	fclose(VerseIndexFile);
	fclose(WordFile);
	fclose(WordNdxFile);
	fclose(CIndexFile);
	fclose(ConcordFile);
	if (CSrchStrongsIndexFile) fclose(CSrchStrongsIndexFile);
	if (CSrchStrongsFile) fclose(CSrchStrongsFile);

	delete[] FilePath;

	EngineStarted = FALSE;

	return(0);
}

int TEngine::GetBookStats(BookStats *Stats) {
	FILE *StatFile;
	unsigned long BytesRead;

	char FileName[70];

	strcpy(FileName,FilePath);
	strcat(FileName,"STATS.");
	strcat(FileName,BookCode);

	StatFile = fopen(FileName, "rb"); // CreateFile(FileName, GENERIC_READ, 0, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);

	if (StatFile == NULL) return(-1);  /* Error opening file */

	fread(&Stats->Root,sizeof(Stats->Root), 1, StatFile);
	fread(&Stats->TotalVerses,sizeof(Stats->TotalVerses), 1, StatFile);
	fread(&Stats->UniqueWords,sizeof(Stats->UniqueWords), 1, StatFile);
	fread(&Stats->TotalWords,sizeof(Stats->TotalWords), 1, StatFile);
	fread(&Stats->BibleFlag,sizeof(Stats->BibleFlag), 1, StatFile);

	fclose(StatFile);

	return(0);
}

	// Old function.  See ConvertBibleString below
long TEngine::ConvertBibleString(char *ReferenceStr) {
	int BookNum;
	char Book[4];
	char Chapter[4];
	int ChapterNum;
	char *Pos;
	char *NewPos;
	char Verse[5];
	int VerseNum;
	long Reference;
	int Loop;

	/* Find book in Books array */

	strncpy(Book,ReferenceStr,3);
	*(Book+3) = 0;  /* End of string marker */

	for (unsigned i = 0; i < strlen(Book); i++)
		Book[i] = toupper(Book[i]);

	for (BookNum = 1; BookNum <= 66; BookNum++)
		if (strcmp(Books[BookNum-1],Book) == 0) break;

	if (strcmp(Books[BookNum-1],Book) == 0) {
		strcpy(CurrentBibleBook,Book);

    	// Find start of chapter
    Pos = ReferenceStr + 3;
    while (*Pos != 0 && !isdigit(*Pos)) Pos++;

    	// Find end of chapter
    NewPos = Pos;
    while (*NewPos != 0 && isdigit(*NewPos)) NewPos++;

		strncpy(Chapter,Pos,NewPos-Pos);
		Chapter[NewPos-Pos] = 0;

		ChapterNum = atoi(Chapter);

		if (ChapterNum > Chapters[BookNum-1]) ChapterNum = 0;

		if (ChapterNum != 0) {    /* Ok, now get verse */
			CurrentChapter = ChapterNum;

			NewPos = strchr(Pos,' ');  /* Find start of verse by finding space */
			if (NewPos == NULL) NewPos = strchr(Pos,':');

      if (NewPos != NULL)
				strcpy(Verse,NewPos+1);
      else
      	strcpy(Verse,"1");

			VerseNum = atoi(Verse);

			if (VerseNum > Verses[BookStart[BookNum-1]+ChapterNum-2]) VerseNum = 0;

			if (VerseNum != 0) {
				Reference = 0;

				for (Loop = 0; Loop < (BookStart[BookNum-1]-1)+(ChapterNum-1); Loop++)
					Reference += Verses[Loop];

				Reference += VerseNum;

				return(Reference);
			}
				else return(-1);
		}
			else return(-1);
	}
		else return (-1);
}



long TEngine::ConvertStringToReference(char *ReferenceStr) {
	long Reference;
	long lNumRefs;
	unsigned short RefStart[MAX_REFS];
	unsigned short RefLen[MAX_REFS];

	lNumRefs = ConvertStringToReference(ReferenceStr, RefStart, RefLen);

	if (!lNumRefs) Reference = -1;
		else Reference = RefStart[0];

/* Old version
	char Temp[80];

		// Remove book code from ReferenceStr if it exists 
	if (_wcsnicmp(BookCode,ReferenceStr,strlen(BookCode)) == 0) {
		strcpy(Temp,ReferenceStr+strlen(BookCode));
	}
		else strcpy(Temp,ReferenceStr);

		// Remove all leading spaces 
	while (Temp[0] == ' ') strcpy(Temp,&Temp[1]);

	if (Stats->BibleFlag) {  // Convert to Bible reference 

		Reference = ConvertBibleString(Temp);

	}
		else  // Convert to Book reference 
	{

		Reference = _wtol(Temp);
	}

*/


	return(Reference);
}

char *TEngine::ConvertReferenceToString(unsigned long Reference) {
	static char Temp[80];
	char Temp2[10];
	unsigned long Counter;
	int Index, Loop;
	int Chapter;

	if (Stats->BibleFlag) {  /* Convert to Bible String */

		Index = 0;
		Counter = 0;
		while (Counter < Reference) {
			Counter += Verses[Index];
			Index++;
		}
		Index--;
		Counter -= Verses[Index];

		Loop = 0;
		while (Loop < 65 && Index >= BookStart[Loop+1]-1)
			Loop++;

//		strcpy(Temp,BookCode);
//		strcat(Temp," ";
		strcpy(Temp,FullBooksLower[Loop]);
		strcat(Temp, " ");

		Counter = Reference - Counter;
		Chapter = Index - BookStart[Loop] + 2;

		while (Counter > Verses[Index]) {
			Chapter++;
			Counter -= Verses[Index];
		}

		sprintf(Temp2,"%d", Chapter);	// itoa(Chapter,Temp2,10)
		strcat(Temp, Temp2);
		strcat(Temp,":");
		sprintf(Temp2,"%d", (int) Counter);	// itoa((int) Counter,Temp2,10)
		strcat(Temp,Temp2);
	}
		else  /* Convert to Book String */
	{
		strcpy(Temp,BookCode);
		strcat(Temp," ");
		sprintf(Temp2,"%d", (int) Reference);	// itoa(Reference,Temp2,10)
		strcat(Temp,Temp2);
	}

	return((char *)Temp);
}

void TEngine::DeCompressBuffer(char *InBuffer, unsigned NumBytes, char *OutBuffer, unsigned& OutBytes) {

	int Index;
	unsigned BufPos;
	unsigned char TempBuf;
  char Alternate = FALSE;
	char OutStr[30];
	int OutStrPos;
  int ITemp;

	BufPos = 0;
	TempBuf = 0;

  OutBytes = 0;

	while (BufPos < NumBytes) {
		if (!Alternate) {
			Index = *(InBuffer+BufPos) & 0xFF;
			Index <<= 4;
			TempBuf = *(InBuffer+BufPos+1);
			Index += ((TempBuf >> 4) & 0x00F);
			TempBuf <<= 4;
			BufPos += 2;
			Alternate = TRUE;
		}
			else
		{
			Index = TempBuf &0xFF;
			Index <<= 4;
			TempBuf = *(InBuffer+BufPos);
			BufPos++;
		    ITemp = TempBuf & 0xFF;
			Index = (Index | ITemp) & 0x0FFF;
			TempBuf = 0;
	      Alternate = FALSE;
	    }

		*(OutStr+29) = 0;
		OutStrPos = 28;

		do {
			*(OutStr+OutStrPos--) = (char) StringTable[Index].FollChar;

			Index = StringTable[Index].PrevChar;
		} while (Index != 0x7FFF);  /* 0x7FFF is a special code meaning no previous char in table */

	    OutStrPos++;

		strcpy(OutBuffer+OutBytes,OutStr+OutStrPos);
		OutBytes += strlen(OutStr+OutStrPos);
	}

}

unsigned char TEngine::ConvertStringToReference(char *pReferenceStr, unsigned short*RefStart, unsigned short*RefLen) {

	typedef enum { ST_START, ST_BOOK_1, ST_BOOK_FOUND, ST_CHAPTER_FOUND, ST_VERSE_FOUND, ST_RANGE_FOUND, ST_RANGE_CHAPTER_FOUND } RefStates;
	RefStates CurState = ST_START;
	char * pRef = pReferenceStr;
	char Book[50];
	unsigned char bBookNdx = 0;
	char ChapterStr[50];
	unsigned short Chapter;
	unsigned short Verse;
	char TStr[100];
	unsigned char BookPart = 0;
	char BookPartStr[10];
	unsigned char fDone = FALSE;
	unsigned char fError = FALSE;
	long lRefCount = -1;

	while (!fDone && *pRef) {
			// Skip leading white space
		while (*pRef && isspace(*pRef)) pRef++;
		
		char * pStart = pRef;

		if (*pRef && ispunct(*pRef)) {
			TStr[0] = *pRef;
			TStr[1] = 0;
			pRef++;	// Skip punctuation
		}
			else
		{
			while (*pRef && isalnum(*pRef)) pRef++;

			strncpy(TStr, pStart, (unsigned long) pRef- (unsigned long) pStart);

			TStr[((unsigned long) pRef- (unsigned long) pStart) / sizeof(char)] = 0;
		}

		switch (CurState) {
			case ST_RANGE_CHAPTER_FOUND:	// Looking for verse
			{
				unsigned long dwValue = atol(TStr);
				unsigned short VersesInChapter = Verses[BookStart[bBookNdx] + Chapter - 1 - 1];
		
				if (strlen(TStr) == 1 && TStr[0] == ':') { }
					else
				if (dwValue && VersesInChapter >= dwValue) {
					CurState = ST_VERSE_FOUND;
					Verse = (unsigned short) dwValue;

					char RefStr[100];
					strcpy(RefStr, Books[bBookNdx]);
					strcat(RefStr, " ");
					strcat(RefStr, ChapterStr);
					strcat(RefStr, " ");
					strcat(RefStr, TStr);

					long RefVal = ConvertBibleString(RefStr);
					if (RefVal >= 0) {
						RefLen[lRefCount] = RefVal-RefStart[lRefCount]+1;

						CurState = ST_VERSE_FOUND;
					}
						else
					{
						fDone = TRUE;
						fError = TRUE;
					}

				}
					else	// Error: chapter too big for this book
				{
					fDone = TRUE;
					fError = TRUE;
				}
			}
			break;

			case ST_RANGE_FOUND:	// Complete range of verses for previous verse
			{
				unsigned long dwRangeVal = atol(TStr);
				unsigned short VersesInChapter = Verses[BookStart[bBookNdx] + Chapter - 1 - 1];
				
				// Determine if next character is colon.  If so, then range ends in new chapter
				char * pRange = pRef;

				while (*pRange && isspace(*pRange)) pRange++;

				if (*pRange == ':') {	// range ends in new chapter
					if (dwRangeVal && Chapters[bBookNdx] >= dwRangeVal) {
						CurState = ST_RANGE_CHAPTER_FOUND;
						Chapter = (unsigned short) dwRangeVal;
						strcpy(ChapterStr, TStr);
					}
						else	// Error: chapter too big for this book
					{
						fDone = TRUE;
						fError = TRUE;
					}

				}
					else	// simple range

				if (dwRangeVal > Verse	&& dwRangeVal <= VersesInChapter) {
					CurState = ST_VERSE_FOUND;
					
					RefLen[lRefCount] = (unsigned short) (dwRangeVal - Verse + 1);
				}
					else
				{
					fDone = TRUE;
					fError = TRUE;
				}
			}
			break;

			case ST_VERSE_FOUND:	// Looking for range of verses, another verse or new chapter
			{
				if (strlen(TStr) == 1) {
					if (TStr[0] == '-') {	// range found
						CurState = ST_RANGE_FOUND;
					}
						else
					if (TStr[0] == ',') {	// another verse found
						CurState = ST_CHAPTER_FOUND;
					}
						else
					if (TStr[0] == ';') {	// new chapter found
						CurState = ST_BOOK_FOUND;
					}
				}
					else
				{
					fDone = TRUE;
					fError = TRUE;
				}
			}
			break;

			case ST_CHAPTER_FOUND:	// Looking for verse
			{
				unsigned long dwValue = atol(TStr);
				unsigned short VersesInChapter = Verses[BookStart[bBookNdx] + Chapter - 1 - 1];

				if (strlen(TStr) == 1 && TStr[0] == ':') { }
					else
				if (dwValue && VersesInChapter >= dwValue) {
					CurState = ST_VERSE_FOUND;
					Verse = (unsigned short) dwValue;

					char RefStr[100];
					strcpy(RefStr, Books[bBookNdx]);
					strcat(RefStr, " ");
					strcat(RefStr, ChapterStr);
					strcat(RefStr, " ");
					strcat(RefStr, TStr);

					unsigned long RefVal = ConvertBibleString(RefStr);
					if (RefVal >= 0) {
						lRefCount++;
						RefStart[lRefCount] = (unsigned short) RefVal;
						RefLen[lRefCount] = 1;

						CurState = ST_VERSE_FOUND;
					}
						else
					{
						fDone = TRUE;
						fError = TRUE;
					}

				}
					else	// Error: chapter too big for this book
				{
					fDone = TRUE;
					fError = TRUE;
				}
			}
			break;

			case ST_BOOK_FOUND:	// Looking for chapter
			{
				unsigned long dwValue = atol(TStr);
		
				if (dwValue && Chapters[bBookNdx] >= dwValue) {
					CurState = ST_CHAPTER_FOUND;
					Chapter = (unsigned short) dwValue;
					strcpy(ChapterStr, TStr);
				}
					else	// Error: chapter too big for this book
				{
					fDone = TRUE;
					fError = TRUE;
				}
			}
			break;

			case ST_BOOK_1:		// Looking for second part of book
			{
				char TStr2[100];
				unsigned char fBookFound = FALSE;
				unsigned char BookNum;

				strcpy(TStr2, BookPartStr);
				strcat(TStr2, TStr);
				for (BookNum = 0; BookNum < 66; BookNum++)
					if (StrCmpIgnoreCase(Books[BookNum], TStr2) == 0) { fBookFound = TRUE; break; }

				if (!fBookFound) {	// Check full book names
					for (BookNum = 0; BookNum < 66; BookNum++)
						if (StrCmpIgnoreCase(FullBooks[BookNum], TStr2) == 0) { fBookFound = TRUE; break; }
				}

				if (!fBookFound) {  // Check full book names again with space between first and second parts of book name
					strcpy(TStr2, BookPartStr);
					strcat(TStr2, " ");
					strcat(TStr2, TStr);

					for (BookNum = 0; BookNum < 66; BookNum++)
						if (StrCmpIgnoreCase(FullBooks[BookNum], TStr2) == 0) { fBookFound = TRUE; break; }

				}

				if (fBookFound) {
					CurState = ST_BOOK_FOUND;
					strcpy(Book, TStr2);
					bBookNdx = BookNum;
				}

			}
			break;	

			case ST_START:
				if (StrCmpIgnoreCaseLen(TStr, BookCode, strlen(BookCode)) == 0) ;	// Stay in Start state
					else
				if (strlen(TStr) == 1 && isdigit(TStr[0])) {				// First part of book found (eg. 1 Samuel)
					BookPart = (unsigned char) atol(TStr);
					if (BookPart > 0) {
						strcpy(BookPartStr, TStr);
						CurState = ST_BOOK_1;	
					}
				}
				else  // check for book (eg Jam, James)
				{
					unsigned char fBookFound = FALSE;
					unsigned char BookNum;

						// Check 3-char book codes
					for (BookNum = 0; BookNum < 66; BookNum++)
						if (StrCmpIgnoreCase(Books[BookNum], TStr) == 0) { fBookFound = TRUE; break; }

					if (!fBookFound) {	// Check full book names
						for (BookNum = 0; BookNum < 66; BookNum++)
							if (StrCmpIgnoreCase(FullBooks[BookNum], TStr) == 0) { fBookFound = TRUE; break; }
					}

					if (fBookFound) {
						CurState = ST_BOOK_FOUND;
						strcpy(Book, TStr);
						bBookNdx = BookNum;
					}
				}

				break;

		}
	}

	return lRefCount+1;

}

#define MAX_REFS 20
char *TEngine::GetReferenceEx(char *pReferenceStr) {
	static char *pBuffer = NULL;
	long lNumRefs;

	unsigned short RefStart[MAX_REFS];
	unsigned short RefLen[MAX_REFS];

	if (pBuffer) delete[] pBuffer;

	lNumRefs = ConvertStringToReference(pReferenceStr, RefStart, RefLen);

		// Load buffer with all founnd references
	if (lNumRefs) {
		// First calculate total size of verse buffer
		unsigned long dwBufSize = 0;
		for (unsigned char i = 0; i < lNumRefs; i++) {
			for (unsigned short Ref = RefStart[i]; Ref < RefStart[i] + RefLen[i]; Ref++) {
				unsigned Len;
				if (GetReferenceLength(Ref, Len) == 0)
					dwBufSize += (2 * Len) + 2;
			}
		}

		// Allocate destination buffer
		pBuffer = (char *) new unsigned char[dwBufSize + ((lNumRefs) * 4)];

		if (pBuffer) {
			*pBuffer = 0;

			for (unsigned char i = 0; i < lNumRefs; i++) {
				for (unsigned short Ref = RefStart[i]; Ref < RefStart[i] + RefLen[i]; Ref++) {
					unsigned Len;
					if (GetReferenceLength(Ref, Len) == 0) {
						char *pVerseBuf = GetReference(Ref);

						int a = strlen(pVerseBuf);

						if (pVerseBuf) {

							strcat(pBuffer + strlen(pBuffer), pVerseBuf);
							strcat(pBuffer, " ");

						int b = strlen(pBuffer);
						}
					}
				}
			}

		}
	}
		else pBuffer = NULL;

	return pBuffer;

}

char *TEngine::GetReference(unsigned long Reference) {
  static char *Buffer = NULL;
  unsigned ReferenceLength, Junk;

  if (Buffer) delete []Buffer;

  GetReferenceLength(Reference, ReferenceLength);

  Buffer = new char[ReferenceLength + 10];

  GetReference2(Reference, Junk, Buffer, ReferenceLength);

  	// Get rid of preceding space
  strcpy(Buffer, Buffer+1);

  return Buffer;
}

int TEngine::GetReference2(unsigned long Reference, unsigned& CompressedReferenceLength, char *Buffer, unsigned& ReferenceLength) {
	unsigned char Result;
	unsigned long Index;
	unsigned long NextIndex;
  char *CompressedBuffer;
  unsigned CompressedBufferSize;
  unsigned long BytesRead;

		/* Check if reference is within range */
//	if (Reference > Stats->TotalVerses) return(-1);

		/* Read index for this verse */
	if (fseek(VerseIndexFile, (unsigned long) Reference*4, SEEK_SET) != 0) return(-1);
	Result = fread(&Index, sizeof(Index), 1, VerseIndexFile);
	if (!Result) return (-1);  /* Read error */

  ReferenceLength = (Index >> 24) * 50;
  Index &= 0x00FFFFFFl;  /* reset the top byte */

		/* Read index for next verse */
	Result = fread(&NextIndex, sizeof(NextIndex), 1, VerseIndexFile);
	if (Result == 0) return (-1);  /* Read error */

  NextIndex &= 0x00FFFFFFl;  /* reset the top byte */

	CurrentIndex = Index;
	CompressedBufferSize = NextIndex-Index;

  CompressedBuffer = new char[CompressedBufferSize];

  if (CompressedBuffer != NULL) {

  		/* Read verse into buffer */
    if (fseek(VerseFile,CurrentIndex,SEEK_SET) != 0) return(-1);
    Result = fread(CompressedBuffer, CompressedBufferSize, 1, VerseFile);
    if (!Result) return (-1);  /* Read error */

    CurrentReference = Reference;
    CurrentReferenceSize = CompressedBufferSize;  /* size of compressed text */
    ReferenceLength = CurrentReferenceSize;  /* size of compressed text */

    DeCompressBuffer(CompressedBuffer,CompressedBufferSize,Buffer,ReferenceLength);

    delete[] CompressedBuffer;
  }

 	else return(-1);  /* Insufficient memory */

	return(0);
}

	// Returns size on 
int TEngine::GetReferenceLength(unsigned long Reference, unsigned& ReferenceLength) {
	unsigned char Result;
	unsigned long Index;
	unsigned long NextIndex;
	unsigned long BytesRead;

		/* Check if reference is within range */
	if (Reference > Stats->TotalVerses) return(-1);

		/* Read index for this verse */
	if (fseek(VerseIndexFile,(unsigned long) Reference*4,SEEK_SET) != 0) return(-1);
	Result = fread(&Index, sizeof(Index), 1, VerseIndexFile);
	if (!Result) return (-1);  /* Read error */

	ReferenceLength = (Index >> 24) * 50;  /* Estimated reference length */

	Index &= 0x00FFFFFFl;  /* reset the top byte */

		/* Read index for next verse */
	Result = fread(&NextIndex, sizeof(NextIndex), 1, VerseIndexFile);
	if (!Result) return (-1);  /* Read error */

	NextIndex &= 0x00FFFFFFl;  /* reset the top byte */

	CurrentIndex = Index;
	CurrentReferenceSize = NextIndex-Index;  /* Actual encoded ref length */
	CurrentReference = Reference;

  return 0;
}

int TEngine::GetCurrentReference(char *Buffer, unsigned& ReferenceSize) {
	unsigned char Result;
  char *CompressedBuffer;
  unsigned long BytesRead;

  CompressedBuffer = new char[CurrentReferenceSize];

  if (CompressedBuffer != NULL) {

  		/* Read verse into buffer */
	  if (fseek(VerseFile,CurrentIndex,SEEK_SET) != 0) return(-1);
  	Result = fread(CompressedBuffer, CurrentReferenceSize, 1, VerseFile);

	  if (!Result) return (-1);  /* Read error */

	  DeCompressBuffer(CompressedBuffer,CurrentReferenceSize,Buffer,ReferenceSize);

    delete[] CompressedBuffer;
  }
 	else return(-1);  /* Insufficient memory */

	return(0);
}

/**************************************************************************
	Function  : GetRecord
	Purpose   : Reads the given record from the WordFile
	Date		  : August, 1991
	Procedure : Seek record, read buffer, unpack into Node
	Parameters: long RecordNum - file record number to get
							Node& NodeBuf - Resulting data from read
	Returns   : 0 if successful, -1 if not
***************************************************************************/
int TEngine::GetRecord(long RecordNum, Node& NodeBuf) {
	long Index;
	unsigned char Result;
	char RecordSize;
	short int StrPos;
	char Buffer[sizeof(Node)];
	short int Loop;
	short int Branch;
	short int BackLink;
	unsigned short Label;
	char *KeyPtr;
  unsigned long BytesRead;

	fseek(WordNdxFile,RecordNum*4,SEEK_SET);
	Result = fread(&Index, sizeof(Index), 1, WordNdxFile);

	if (!Result) return(-1);

	RecordSize = (char) Index;  /* rightmost byte is record size */

	Index = Index >> 8;

		/* Read entire record into buffer */
	fseek(WordFile,Index,SEEK_SET);
	Result = fread(&Buffer, RecordSize, 1, WordFile);

	if (!Result) return(-1);

		/* Get KeyCount - # of keys in node */
	NodeBuf.KeyCount = Buffer[0];

		/* Get BackLink from buffer */
	memmove(&BackLink,&Buffer[1],2);
	NodeBuf.BackLink = BackLink;

		/* Get Branch[0] from buffer */
	memmove(&Branch,&Buffer[3],2);
	NodeBuf.Branch[0] = Branch;

	StrPos = 5;

  NodeBuf.Keys[0][0] = 0;

	for (Loop = 1; Loop <= NodeBuf.KeyCount; Loop++) {
		memmove(&Branch,&Buffer[StrPos],2);
		NodeBuf.Branch[Loop] = Branch;
		StrPos += 2;

		memmove(&Label,Buffer+StrPos, 2);
		NodeBuf.Label[Loop] = Label;
		StrPos += 2;

		KeyPtr = NodeBuf.Keys[Loop];

			/* Read in key until end of string character ('\0') */
		do {
			*KeyPtr = Buffer[StrPos++];
		} while (*KeyPtr++ != 0);
	}

	return(0);
}

/**************************************************************************
	Function  : SearchNode
	Purpose   : Searches a given node for the given key
	Date		  : 1-14-91
	Procedure : Sequential search from 1 to MaxKeysInNode
	Parameters: char *Target - Key to search for
							long Start - Node to search in
							short int& - Position of key in node if found
	Returns   : TRUE if found, FALSE if not
***************************************************************************/
long TEngine::SearchNode(Node NodeBuf, char *Target, short int& TargetPos) {

	if (strcmp(Target,NodeBuf.Keys[1]) < 0) {  /* Target not in this node */
		TargetPos = 0;  /* Next path to explore */
		return(0);
	}

		/* Sequentially search through all keys in this node */
	TargetPos = NodeBuf.KeyCount;

	while (strcmp(Target,NodeBuf.Keys[TargetPos]) < 0 && TargetPos > 1)
		TargetPos--;

	if (strcmp(Target,NodeBuf.Keys[TargetPos]) == 0) return(NodeBuf.Label[TargetPos]);
		else return(0);
}

/**************************************************************************
	Function  : SearchTree
	Purpose   : Searches a B-Tree starting at a given node
	Date		  : 1-14-91
	Procedure :
	Parameters: char *Target - Key to search for
							long Start - Starting node in tree
							short int& - Position of key in node if found
	Returns   : Label of key or NULL if not found
***************************************************************************/
long TEngine::SearchTree(char *Target, long Start, short int& TargetPos, long& TargetNode) {
	long Found;
	Node NodeBuf;

	if (Start == -1) return(0);

	GetRecord(Start, NodeBuf);

	if ((Found = SearchNode(NodeBuf,Target,TargetPos)) > 0) return(Found);  /* Target found at root */
			else
	{
		if (NodeBuf.Branch[TargetPos] != -1) TargetNode = NodeBuf.Branch[TargetPos];
		return(SearchTree(Target,NodeBuf.Branch[TargetPos],TargetPos,TargetNode));
	}
}


/**************************************************************************
	Function  : Finunsigned long
	Purpose   : Searches the word B-Tree for a word
	Date		  : 1-14-91
	Procedure :
	Parameters: char *Target - String to search for in tree
	Returns   : Label of word if found, 0 if not
***************************************************************************/
unsigned TEngine::FindWord(const char *Target) {
	short int TargetPos;
	long TargetNode;
	long Start;
  char cTarget[MaxKeySize+1];

	Start = Stats->Root;
  for (unsigned i = 0; i < (unsigned) strlen(Target)+1; i++)
    cTarget[i] = (char) Target[i];

	return(SearchTree(cTarget,Start,TargetPos,TargetNode));
}

/**************************************************************************
	Function  : FindFirstWord
	Purpose   : Searches the word B-Tree for a word and stores information
								for FindNextWord and FindPreviousWord functions
	Date		  : August, 14, 1991
	Procedure :
	Parameters: char *Target - String to search for in tree
	Returns   : 0 if found, -1 if not
***************************************************************************/
int TEngine::FindFirstWord( char *Target, long& Label) {
	char NextWord[MaxKeySize+1];
  long NextLabel;
  char cTarget[MaxKeySize+1];

	EndOfList = FALSE;
	BeginningOfList = FALSE;

  for (unsigned i = 0; i < (unsigned) strlen(Target)+1; i++)
    cTarget[i] = (char) Target[i];

	Label = SearchTree(cTarget,Stats->Root,TargetPos,TargetNode);

	GetRecord(TargetNode,*NodeBuf);

	if (Label == 0) {   /* Word not found so find next word */
//		if (TargetPos == 0) TargetPos = 1;
		FindNextWord(NextWord, NextLabel);

		if (strcmp(Target, NextWord) < 0) {
			strcpy(Target, NextWord);
			Label = NextLabel;
		}
    	else
		tstrcpy(Target,NodeBuf->Keys[TargetPos]);

		return(-1);
	}
	else return(0);
}

/**************************************************************************
	Function  : FindNextWord
	Purpose   : Given the information provided in FindFirstWord this function
							returns the next word in the B-Tree alphabetically
	Date		  : August, 14, 1991
	Procedure :
	Parameters: char *Target - String to search for in tree
	Returns   : 0 if Ok, -1 if end of list
***************************************************************************/
int TEngine::FindNextWord(char *Target, long& Label) {
	long TempNode;

	BeginningOfList = FALSE;

	if (EndOfList) return(-1);

	if (NodeBuf->Branch[TargetPos] == -1) {
		if (TargetPos < NodeBuf->KeyCount) tstrcpy(Target,NodeBuf->Keys[++TargetPos]);
			else  /* Follow back links until next word is found */
		{

			while (TargetPos == NodeBuf->KeyCount) {

				if (NodeBuf->BackLink == -1) {
					EndOfList = TRUE;
					return(-1);
				}

				TempNode = TargetNode;
				TargetNode = NodeBuf->BackLink;

				GetRecord(TargetNode,*NodeBuf);

				for (TargetPos = 0; TargetPos <= NodeBuf->KeyCount; TargetPos++)
					if (NodeBuf->Branch[TargetPos] == TempNode) break;
			}

			tstrcpy(Target,NodeBuf->Keys[TargetPos+1]);
			TargetPos++;
		}
	}
		else   /* Search down left branches until -1 found */
	{
		TargetNode = NodeBuf->Branch[TargetPos];

		GetRecord(TargetNode,*NodeBuf);

		while (NodeBuf->Branch[0] != -1) {
			TargetNode = NodeBuf->Branch[0];
			GetRecord(TargetNode,*NodeBuf);
		}

		tstrcpy(Target,NodeBuf->Keys[1]);
		TargetPos = 1;
	}

	Label = NodeBuf->Label[TargetPos];

	return(0);
}

/**************************************************************************
	Function  : FindPreviousWord
	Purpose   : Given the information provided in FindFirstWord this function
							returns the previous word in the B-Tree alphabetically
	Date		  : August, 14, 1991
	Procedure :
	Parameters: char *Target - String to search for in tree
	Returns   : 0 if Ok, -1 if beginning of list, Label of word
***************************************************************************/
int TEngine::FindPreviousWord(char *Target, long& Label) {
	long TempNode;

	if (EndOfList) {
		EndOfList = FALSE;
		tstrcpy(Target,NodeBuf->Keys[TargetPos]);
		Label = NodeBuf->Label[TargetPos];
		return(0);
	}

	if (BeginningOfList) return(-1);

	if (NodeBuf->Branch[TargetPos-1] == -1) {
		if (TargetPos > 1) tstrcpy(Target,NodeBuf->Keys[--TargetPos]);
			else  /* Follow back links until previous word is found */
		{

			TargetPos = 0;

			while (TargetPos == 0) {

				if (NodeBuf->BackLink == -1) {
					BeginningOfList = TRUE;
					return(-1);
				}

				TempNode = TargetNode;
				TargetNode = NodeBuf->BackLink;

				GetRecord(TargetNode,*NodeBuf);

				for (TargetPos = 0; TargetPos <= NodeBuf->KeyCount; TargetPos++)
					if (NodeBuf->Branch[TargetPos] == TempNode) break;
			}

			tstrcpy(Target,NodeBuf->Keys[TargetPos]);
		}
	}
		else   /* Search down right branches until -1 found */
	{
		TargetNode = NodeBuf->Branch[TargetPos-1];

		GetRecord(TargetNode,*NodeBuf);

		while (NodeBuf->Branch[NodeBuf->KeyCount] != -1) {
			TargetNode = NodeBuf->Branch[NodeBuf->KeyCount];
			GetRecord(TargetNode,*NodeBuf);
		}

		tstrcpy(Target,NodeBuf->Keys[NodeBuf->KeyCount]);


		TargetPos = NodeBuf->KeyCount;
	}

	Label = NodeBuf->Label[TargetPos];

	return(0);
}

/**************************************************************************
	Function  : int GetOccurences
	Purpose   : Returns the number of verses the given word occurs in
	Date		  : August, 1991
	Procedure : Read starting index for this word and the next one and subtract
	Parameters: unsigned long WordNum - Number of word from unique word list
	Returns   : Number of occurences if found, -1 if error
***************************************************************************/
int TEngine::GetOccurences(unsigned long WordNum) {
	unsigned char Result;
	long StartIndex;
	long StopIndex;
  unsigned long BytesRead;

	if (WordNum == 0) return(0);

	if (fseek(CIndexFile,WordNum*4,SEEK_SET) != 0) return -1;

	Result = fread(&StartIndex, 4, 1, CIndexFile);
	if (!Result) return(-1);

	Result = fread(&StopIndex, 4, 1, CIndexFile);
	if (!Result) return(-1);

	return(StopIndex-StartIndex);
}

/**************************************************************************
	Function  : GetWordBitMap
	Purpose   : Reads the given word's concordance info into bit map
	Date		  : August, 1991
	Procedure : Seek index to concordance, read concordance info and put in map
	Parameters: unsigned long WordNum - Index to desired word info
							char *BitMap - resulting bit map, 1 bit per verse/page
	Returns   : 0 if successful, -1 if not
  Note			: Make sure BitMap is zeroed out before calling this function
							if desired
***************************************************************************/
int TEngine::GetWordBitMap(unsigned long WordNum, char *BitMap, int& Occurences) {
  unsigned char Result;
	long StartIndex;
	long StopIndex;
	long Loop;
	unsigned short Verse;
	short int Byte;
	short int Bit;
  unsigned long BytesRead;

	if (WordNum <= 0) return(-1);

	if (fseek(CIndexFile,WordNum*4,SEEK_SET) != 0) return -1;
	
		/* Read Starting and Ending indexes from CIndex file */
	Result = fread(&StartIndex,4, 1, CIndexFile);
	if (Result == 0) return(-1);
	Result = fread(&StopIndex,4,1, CIndexFile);
	if (Result == 0) return(-1);

		/* Find start of concordance info in Concord file */
	Result = fseek(ConcordFile,StartIndex*sizeof(unsigned short),SEEK_SET);
	if (Result != 0) return(-1);

		/* Read each occurrence and create bit map */
	for (Loop = StartIndex; Loop < StopIndex; Loop++) {
		Result = fread(&Verse, sizeof(unsigned short),1, ConcordFile);

		Byte = (Verse-1) / 8;
		Bit = (Verse-1) % 8;

		*(BitMap+Byte) |= (0x80 >> Bit);
	}

  Occurences = StopIndex-StartIndex;

	return(0);
}

/**************************************************************************
	Function  : GetStrongsWordBitMap
	Purpose   : Reads the given word's concordance info into bit map
	Date		  : August, 1991
	Procedure : Seek index to concordance, read concordance info and put in map
	Parameters: unsigned long WordNum - Index to desired word info
							char *BitMap - resulting bit map, 1 bit per verse/page
	Returns   : 0 if successful, -1 if not
  Note			: Make sure BitMap is zeroed out before calling this function
							if desired
***************************************************************************/
int TEngine::GetStrongsWordBitMap(unsigned char fNewTestament, unsigned long WordNum, char *BitMap, int& Occurences) {
	unsigned char Result;
	long Loop;
	unsigned short Verse;
	short int Byte;
	short int Bit;
	unsigned long BytesRead;
	SrchStrongsRec NdxRec;

	if (WordNum <= 0) return(-1);

	if (CSrchStrongsIndexFile == NULL) return -1;
	if (CSrchStrongsFile == NULL) return -1;

	if (fNewTestament) {
		if (fseek(CSrchStrongsIndexFile,(WordNum+NUM_STRONGS_OLDT)*sizeof(NdxRec),SEEK_SET) != 0) return -1;
	}
	else
	{
		if (fseek(CSrchStrongsIndexFile,WordNum * sizeof(NdxRec),SEEK_SET) != 0) return -1;
	}
	
	Result = fread(&NdxRec, sizeof(NdxRec), 1, CSrchStrongsIndexFile);
	if (Result == 0) return(-1);

		/* Find start of concordance info in Concord file */
	Result = fseek(CSrchStrongsFile,NdxRec.dwStart,SEEK_SET);
	if (Result != 0) return(-1);

		/* Read each occurrence and create bit map */
	for (Loop = 0; Loop < NdxRec.uNumEntries; Loop++) {
		Result = fread(&Verse, sizeof(unsigned short),1, CSrchStrongsFile);

		Byte = (Verse-1) / 8;
		Bit = (Verse-1) % 8;

		*(BitMap+Byte) |= (0x80 >> Bit);
	}

	Occurences = NdxRec.uNumEntries;

	return(0);
}

/**************************************************************************
	Function  : GetBitMapOccurences
	Purpose   : Given a starting reference, count the number of set bits to the end
	Date		  : September, 1991
	Procedure :
	Parameters: unsigned StartVerse - Verse to start from
							char *BitMap - Current word bit map
	Returns   : next reference. if none found returns 0
***************************************************************************/
int TEngine::GetBitMapOccurences(unsigned StartVerse, char *BitMap) {
	int Byte;
  char Bit;
	char MapByte;
  int Occurences = 0;

  if (StartVerse >= Stats->TotalVerses) return(-1);

	if (StartVerse == 0) StartVerse = 1;

	Byte = (StartVerse-1) / 8;
	Bit = (StartVerse) % 8;
	if (Bit == 0) Bit = 8;
	MapByte = *(BitMap+Byte) << (Bit-1);  /* Remove bits before start verse */

  do {
		if (MapByte != 0) {   /* refs present */
			if ((MapByte & 0x80) != 0) Occurences++;
			if ((MapByte & 0x40) != 0) Occurences++;
			if ((MapByte & 0x20) != 0) Occurences++;
			if ((MapByte & 0x10) != 0) Occurences++;
			if ((MapByte & 0x08) != 0) Occurences++;
			if ((MapByte & 0x04) != 0) Occurences++;
			if ((MapByte & 0x02) != 0) Occurences++;
			if ((MapByte & 0x01) != 0) Occurences++;
		}

    Byte++;
		MapByte = *(BitMap+Byte);
  } while (Byte < (Stats->TotalVerses / 8+1));

	return(Occurences);
}

/**************************************************************************
	Function  : GetNextConcordanceReference
	Purpose   : Given the current reference, get next non-zero in bitmap
	Date		  : August, 1991
	Procedure :
	Parameters: unsigned CurrentVerse - Verse to start from
							char *BitMap - Current word bit map
	Returns   : next reference. if none found returns 0
***************************************************************************/
unsigned TEngine::GetNextConcordanceReference(unsigned CurrentVerse, char *BitMap) {
	unsigned NextVerse;
	int Byte;
	char Bit;
	char MapByte;

  if (CurrentVerse >= Stats->TotalVerses || !BitMap) return(CurrentVerse);

	if (CurrentVerse == 0) {
		if ((*BitMap & 0x80) != 0) return(1);
			else CurrentVerse = 1;
	}

	NextVerse = 0;   /* Unless we succeed in finding a next one */

	Byte = (CurrentVerse-1) / 8;
	Bit = (CurrentVerse) % 8;
	if (Bit == 0) Bit = 8;

		/* Check to see if there are any other refs in this byte */
	MapByte = *(BitMap+Byte) << Bit;
	if (MapByte != 0) {   /* Other refs present */

		if ((MapByte & 0x80) != 0) NextVerse = CurrentVerse+1;
			else
		if ((MapByte & 0x40) != 0) NextVerse = CurrentVerse+2;
			else
		if ((MapByte & 0x20) != 0) NextVerse = CurrentVerse+3;
			else
		if ((MapByte & 0x10) != 0) NextVerse = CurrentVerse+4;
			else
		if ((MapByte & 0x08) != 0) NextVerse = CurrentVerse+5;
			else
		if ((MapByte & 0x04) != 0) NextVerse = CurrentVerse+6;
			else
		if ((MapByte & 0x02) != 0) NextVerse = CurrentVerse+7;
	}
		else   /* Seek other byte that is non-zero for next ref */
	{
		Byte++;

		while (Byte < (Stats->TotalVerses / 8 + 1) && *(BitMap+Byte) == 0)
			Byte ++;

		if (Byte < (Stats->TotalVerses / 8 + 1)) {
			MapByte = *(BitMap+Byte);
			if (MapByte != 0) {

				NextVerse = Byte * 8 + 1;
				while ((MapByte & 0x80) == 0) {
					MapByte <<= 1;
					NextVerse++;
				}
			}
    		else NextVerse = 0;
		}
			else NextVerse = 0;
	}

  if (NextVerse <= 0 || NextVerse >= Stats->TotalVerses) return(0);
		else return(NextVerse);
}

/**************************************************************************
	Function  : GetPreviousConcordanceReference
	Purpose   : Given the current reference, get previous non-zero in bitmap
	Date		  : August, 1991
	Procedure :
	Parameters: unsigned CurrentVerse - Verse to start from
							char *BitMap - Current word bit map
	Returns   : previous reference. If none found returns 0
***************************************************************************/
unsigned TEngine::GetPreviousConcordanceReference(unsigned CurrentVerse, char *BitMap) {
	unsigned PrevVerse;
	int Byte;
	char Bit;
	char MapByte;

	if (CurrentVerse == 0 || CurrentVerse == 1 || !BitMap) return(CurrentVerse);

	PrevVerse = 0;   /* Unless we succeed in finding a next one */

	Byte = (CurrentVerse-1) / 8;
	Bit = (CurrentVerse) % 8;
	if (Bit == 0) Bit = 8;

		/* Check to see if there are any other refs in this byte */
  if (Bit == 1) MapByte = 0;
  	else MapByte = *(BitMap+Byte) >> (9-Bit);
	if (MapByte != 0) {   /* Other refs present */

		if ((MapByte & 0x01) != 0) PrevVerse = CurrentVerse-1;
			else
		if ((MapByte & 0x02) != 0) PrevVerse = CurrentVerse-2;
			else
		if ((MapByte & 0x04) != 0) PrevVerse = CurrentVerse-3;
			else
		if ((MapByte & 0x08) != 0) PrevVerse = CurrentVerse-4;
			else
		if ((MapByte & 0x10) != 0) PrevVerse = CurrentVerse-5;
			else
		if ((MapByte & 0x20) != 0) PrevVerse = CurrentVerse-6;
			else
		if ((MapByte & 0x40) != 0) PrevVerse = CurrentVerse-7;
	}
		else   /* Seek other byte that is non-zero for next ref */
	{
		if (Byte > 0) {
			Byte--;

			while (Byte >= 0 && *(BitMap+Byte) == 0)
				Byte --;

			MapByte = *(BitMap+Byte);
			if (MapByte != 0) {

				PrevVerse = (Byte+1) * 8;
				while ((MapByte & 0x01) == 0) {
					MapByte >>= 1;
					PrevVerse--;
				}
			}
		}
	}

	if (PrevVerse < 0) PrevVerse = CurrentVerse;

	return(PrevVerse);
}



// JNI ***************************************
JNIEXPORT jlong JNICALL Java_edu_southern_CBibleEngine_StartEngine
  (JNIEnv *env, jobject, jstring Path, jstring BookCode)
{
	if (pEngine == NULL) pEngine = new TEngine;

	jboolean isCopy;
	const char * szPath = env->GetStringUTFChars(Path, &isCopy);   
	const char * szBookCode = env->GetStringUTFChars(BookCode, &isCopy);   
 
	loga("StartEngine call: %s %s\n", szPath, szBookCode);

	return pEngine->StartEngine(szPath, szBookCode);
}

JNIEXPORT jint JNICALL Java_edu_southern_CBibleEngine_StopEngine
  (JNIEnv *env, jobject)
{
	jint Result = pEngine->StopEngine();
	
	if (pEngine) delete pEngine;
	pEngine = NULL;

	loga("**** StopEngine");

	return Result;
}


JNIEXPORT jint JNICALL Java_edu_southern_CBibleEngine_GetTotalVerses
  (JNIEnv *env, jobject)
{
	return pEngine->GetTotalVerses();
}

JNIEXPORT jint JNICALL Java_edu_southern_CBibleEngine_GetTotalWords
  (JNIEnv *env, jobject)
{
	return pEngine->GetTotalWords();
}


JNIEXPORT jstring JNICALL Java_edu_southern_CBibleEngine_GetReference
  (JNIEnv *env, jobject, jlong nReference)
{
	jstring result = env->NewStringUTF(pEngine->GetReference(nReference));
}

JNIEXPORT jlong JNICALL Java_edu_southern_CBibleEngine_ConvertStringToReference
  (JNIEnv *env, jobject, jstring RefStr)
  {
	const char * szRefStr = env->GetStringUTFChars(RefStr, NULL);   
	long nResult =  pEngine->ConvertStringToReference((char *)szRefStr);
	
	return nResult;
  }

JNIEXPORT jlong JNICALL Java_edu_southern_CBibleEngine_ConvertStringToReferenceLen
  (JNIEnv *env, jobject, jstring RefStr)
{
	unsigned short uRefStart;
	unsigned short uRefLen;
	const char * szRefStr = env->GetStringUTFChars(RefStr, NULL); 
	  
	pEngine->ConvertStringToReference((char *)szRefStr, &uRefStart, &uRefLen);
	
	return uRefLen;
}


JNIEXPORT jstring JNICALL Java_edu_southern_CBibleEngine_ConvertReferenceToString
  (JNIEnv *env, jobject, jlong Ref)
{
	jstring result = env->NewStringUTF(pEngine->ConvertReferenceToString(Ref));	
}

JNIEXPORT jint JNICALL Java_edu_southern_CBibleEngine_FindWord
  (JNIEnv *env, jobject, jstring WordStr)
{
	const char * szWordStr = env->GetStringUTFChars(WordStr, NULL); 

	return pEngine->FindWord(szWordStr);
}

JNIEXPORT jstring JNICALL Java_edu_southern_CBibleEngine_FindFirstWord
  (JNIEnv *env, jobject, jstring WordStr)
{
	const char * szWordStr = env->GetStringUTFChars(WordStr, NULL); 
	char LargeWordStr[40];
	long Label;

	strcpy(LargeWordStr, szWordStr);

	pEngine->FindFirstWord(LargeWordStr, Label);

	return env->NewStringUTF(LargeWordStr);	
}

JNIEXPORT jstring JNICALL Java_edu_southern_CBibleEngine_FindNextWord
  (JNIEnv *env, jobject, jstring WordStr)
{
	const char * szWordStr = env->GetStringUTFChars(WordStr, NULL); 
	char LargeWordStr[60];
	long Label;

	strcpy(LargeWordStr, szWordStr);

	pEngine->FindNextWord(LargeWordStr, Label);

	return env->NewStringUTF(LargeWordStr);	
	
}

JNIEXPORT jstring JNICALL Java_edu_southern_CBibleEngine_FindPreviousWord
  (JNIEnv *env, jobject, jstring WordStr)
{
	const char * szWordStr = env->GetStringUTFChars(WordStr, NULL); 
	char LargeWordStr[60];
	long Label;

	strcpy(LargeWordStr, szWordStr);

	pEngine->FindPreviousWord(LargeWordStr, Label);

	return env->NewStringUTF(LargeWordStr);	
}


JNIEXPORT jint JNICALL Java_edu_southern_CBibleEngine_GetOccurences
  (JNIEnv *env, jobject, jlong WordNum)
{
	return pEngine->GetOccurences(WordNum);
}


JNIEXPORT jint JNICALL Java_edu_southern_CBibleEngine_GetBitMapOccurences
  (JNIEnv *env, jobject, jlong StartVerse, jbyteArray WordBitMap)
{
	int BitMapSize = (pEngine->GetTotalVerses() / 8) + 10;
	long lResult = 0;

	jbyte *jBitMap = env->GetByteArrayElements( WordBitMap, 0);

	if (jBitMap)
	{
		char *pBitMap = new char [BitMapSize];
		if (pBitMap)
		{
				// Copy from Java bitmap to C bitmap
			for (unsigned i = 0; i < BitMapSize; i++)
				pBitMap[i] = jBitMap[i];

			lResult = pEngine->GetBitMapOccurences(StartVerse, pBitMap);
		
			delete[] pBitMap;
		}

 		env-> ReleaseByteArrayElements(WordBitMap, (jbyte *)jBitMap, 0);
	}
	
	return lResult;
}


JNIEXPORT jbyteArray JNICALL Java_edu_southern_CBibleEngine_GetWordBitMap
  (JNIEnv *env, jobject, jlong WordNum)
{
	int Occurences;
	int BitMapSize = (pEngine->GetTotalVerses() / 8) + 10;
	jbyteArray bArray;

	char *pBitMap = new char [BitMapSize];

	if (pBitMap)
	{
		memset(pBitMap, 0, BitMapSize);
		
		if (0 == pEngine->GetWordBitMap(WordNum, pBitMap, Occurences))
		{
			bArray = env->NewByteArray(BitMapSize);
		
			jbyte *jBytes = env->GetByteArrayElements( bArray, 0);

			for (unsigned i = 0; i < BitMapSize; i++)
				jBytes[i] = pBitMap[i];


	 		env-> ReleaseByteArrayElements(bArray, (jbyte *)jBytes, 0);

		}

		delete[] pBitMap;
	}
	
	return bArray;
}


JNIEXPORT jint JNICALL Java_edu_southern_CBibleEngine_GetNextConcordanceReference
  (JNIEnv *env, jobject, jlong CurrentVerse, jbyteArray WordBitMap)
{
	int BitMapSize = (pEngine->GetTotalVerses() / 8) + 10;
	long lResult = 0;

	jbyte *jBitMap = env->GetByteArrayElements( WordBitMap, 0);

	if (jBitMap)
	{
		char *pBitMap = new char [BitMapSize];
		if (pBitMap)
		{
				// Copy from Java bitmap to C bitmap
			for (unsigned i = 0; i < BitMapSize; i++)
				pBitMap[i] = jBitMap[i];

			lResult = pEngine->GetNextConcordanceReference(CurrentVerse, pBitMap);
		
			delete[] pBitMap;
		}

 		env-> ReleaseByteArrayElements(WordBitMap, (jbyte *)jBitMap, 0);
	}
	
	return lResult;
}

JNIEXPORT jint JNICALL Java_edu_southern_CBibleEngine_GetPreviousConcordanceReference
  (JNIEnv *env, jobject, jlong CurrentVerse, jbyteArray WordBitMap)
{
	int BitMapSize = (pEngine->GetTotalVerses() / 8) + 10;
	long lResult = 0;

	jbyte *jBitMap = env->GetByteArrayElements( WordBitMap, 0);

	if (jBitMap)
	{
		char *pBitMap = new char [BitMapSize];
		if (pBitMap)
		{
				// Copy from Java bitmap to C bitmap
			for (unsigned i = 0; i < BitMapSize; i++)
				pBitMap[i] = jBitMap[i];

			lResult = pEngine->GetPreviousConcordanceReference(CurrentVerse, pBitMap);
		
			delete[] pBitMap;
		}

 		env-> ReleaseByteArrayElements(WordBitMap, (jbyte *)jBitMap, 0);
	}
	
	return lResult;
}

JNIEXPORT jbyteArray JNICALL Java_edu_southern_CBibleEngine_GetStrongsWordBitMap
  (JNIEnv *env, jobject, jchar fNewTestament, jlong WordNum)
{
	int Occurences;
	int BitMapSize = (pEngine->GetTotalVerses() / 8) + 10;
	jbyteArray bArray;

	char *pBitMap = new char [BitMapSize];

	if (pBitMap)
	{
		memset(pBitMap, 0, BitMapSize);
		
		if (0 == pEngine->GetStrongsWordBitMap(fNewTestament, WordNum, pBitMap, Occurences))
		{
			bArray = env->NewByteArray(BitMapSize);
		
			jbyte *jBytes = env->GetByteArrayElements( bArray, 0);

			for (unsigned i = 0; i < BitMapSize; i++)
				jBytes[i] = pBitMap[i];


	 		env-> ReleaseByteArrayElements(bArray, (jbyte *)jBytes, 0);

		}

		delete[] pBitMap;
	}
	
	return bArray;

}


#include <stdafx.h>
#include <tchar.h>
#include <string.h>
#include <stdlib.h>
#include <ctype.h>
#include <Winbase.h>
#include "CeMargin.h"

#define TRUE 1
#define FALSE 0
#define RED 0x000000FF

#define CHARWIDTH 7
#define LINEWIDTH 15
#define DefaultWindowWidth 315
#define DefaultWindowHeight 215
#define DefaultMarginColor RED

TMargin::TMargin(void) {

	CurrentNode = NULL;
	FirstNode = NULL;

  CurrentVerse = 0;
	CurrentReference = 0;
	NumVerses = 0;

  NdxFile = NULL;
  DatFile = NULL;

  SavePath[0] = 0;
}

TMargin::~TMargin(void) {

	FlushList();

}

char TMargin::StartEngine(WCHAR *Path) {
	WCHAR FileName[80];
  char Flag = TRUE;

  lstrcpy(SavePath, Path);

	lstrcpy(FileName,Path);
	lstrcat(FileName,_T("XREF.DAT"));

  DatFile = CreateFile(FileName, GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_ALWAYS, FILE_ATTRIBUTE_NORMAL, NULL);

  if (DatFile == INVALID_HANDLE_VALUE) Flag = FALSE;

	lstrcpy(FileName,Path);
	lstrcat(FileName,_T("XREF.NDX"));

  NdxFile = CreateFile(FileName, GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_ALWAYS, FILE_ATTRIBUTE_NORMAL, NULL);

  if (NdxFile == INVALID_HANDLE_VALUE) Flag = FALSE;

	return Flag;
}

void TMargin::StopEngine() {

  if (DatFile) CloseHandle(DatFile);
  if (NdxFile) CloseHandle(NdxFile);

  DatFile = NULL;
  NdxFile = NULL;
}

void TMargin::ReStartEngine() {
  if (SavePath[0] != 0) StartEngine(SavePath);
}

char TMargin::ReadXRef(long Reference) {
  BOOL Result;
	long StartPos;
	long StopPos;
	Node *MemPtr;
 	Node *MemPtr2;
	DWORD NumBytes;
	DWORD BytesRead;
  DWORD BytesResult;
	unsigned char Byte1, Byte2;
	unsigned Verse;
	int PreviousVerse = 0;
	int Count;  // Number of consecutive xrefs

  NumVerses = 0;

	if (NdxFile) {

			// Find starting pos in Margin.dat for this ref
    if (SetFilePointer(NdxFile, (unsigned long) Reference*4, 0,FILE_BEGIN) == -1) return(FALSE);
  	Result = ReadFile(NdxFile, &StartPos, sizeof(StartPos), &BytesResult, NULL);
  	if (!Result) return (FALSE);  /* Read error */
  	Result = ReadFile(NdxFile, &StopPos, sizeof(StopPos), &BytesResult, NULL);
  	if (!Result) return (FALSE);  /* Read error */

		NumBytes = StopPos-StartPos;

		if (DatFile) {

				// Find starting pos in Margin.dat for this ref
      if (SetFilePointer(DatFile, (unsigned long) StartPos, 0,FILE_BEGIN) == -1) return(FALSE);

      	// Allocate and initialize first node
			MemPtr = new Node;
			FirstNode = MemPtr;
			MemPtr->NumVerses = 0;
			MemPtr->NextNode = NULL;
			MemPtr->PrevNode = NULL;

			BytesRead = 0;
      Count = 0;

				// Read in entire list for this verse into linked list
			while (BytesRead < NumBytes || Count != 0) {
   	     if (MemPtr) {

         if (Count == 0) {					// No consecutive verses
  	      Result = ReadFile(DatFile, &Byte1, sizeof(Byte1), &BytesResult, NULL);
   	      BytesRead++;

					if ((Byte1 & 0x80) != 0) {
    	      Result = ReadFile(DatFile, &Byte2, sizeof(Byte2), &BytesResult, NULL);

   					BytesRead++;

						Verse = Byte1;
						Verse = ((Verse << 8) & 0xFF00) + Byte2;

						Verse ^= 0x8000;  // Reset flag bit
					}
						else  // Consecutive xrefs found
					{
						Count = Byte1;

						Verse = PreviousVerse + 1;

						Count--;
					}
				 }
						else  // get next consecutive verse
         {
				 		Verse = PreviousVerse + 1;

				 		Count--;

				 }

				 PreviousVerse = Verse;

				 MemPtr->Verses[MemPtr->NumVerses] = Verse;
				 MemPtr->NumVerses++;

         NumVerses++;

          	// Allocate new node if this one is full
					if (MemPtr->NumVerses > 19 && (BytesRead < NumBytes || Count != 0)) {

						MemPtr2 = new Node;
						MemPtr2->NumVerses = 0;
						MemPtr2->NextNode = NULL;
						MemPtr2->PrevNode = MemPtr;

		            	// Set up previous nodes next link
						MemPtr->NextNode = MemPtr2;

						MemPtr = MemPtr2;
					}

				}
			}

		}
    	else return FALSE;

		CurrentReference = Reference;

  	return TRUE;
	}

  	else return FALSE;
}

void TMargin::FlushList(void) {
	Node *TempHandle;
	Node *NextHandle;
	Node *MemPtr;

	TempHandle = FirstNode;

	while (TempHandle) {
		MemPtr = TempHandle;

    NextHandle = MemPtr->NextNode;

		delete TempHandle;

		TempHandle = NextHandle;
	}

	FirstNode = NULL;
	CurrentNode = NULL;
	CurrentReference = 0;
  NumVerses = 0;
}


long TMargin::GetFirst(long Reference) {
	Node *MemPtr;
  long Verse;

  	// Read list into memory if not already there for this ref
	if (!FirstNode || Reference != CurrentReference) if (!ReadXRef(Reference)) return 0;

	if (NumVerses == 0) return 0;   // No xrefs for this ref

	CurrentVerse = 0;
	CurrentNode = FirstNode;

	MemPtr = FirstNode;

	Verse = MemPtr->Verses[0];

  return Verse;
}

long TMargin::GetNext(void) {
	Node *TempNode;
	Node *MemPtr;
	long Verse = 0;
  char Done = FALSE;
	
	if (!FirstNode) return 0;  // List not read in for this ref

	if (NumVerses == 0) return 0;   // No xrefs for this ref

	if (CurrentNode) MemPtr = CurrentNode;

	CurrentVerse++;

	if (CurrentVerse == MemPtr->NumVerses) {	// Get next node in list

		TempNode = MemPtr->NextNode;

		if (TempNode) {
			CurrentNode = TempNode;
			CurrentVerse = 0;
		}
    	else { CurrentVerse--; Done = TRUE; }

    if (CurrentNode) MemPtr = CurrentNode;
	}

  if (!Done) {
		Verse = MemPtr->Verses[CurrentVerse];

	}

	return Verse;
}

long TMargin::GetPrevious(void) {
	Node *TempNode;
	Node *MemPtr;
	long Verse = 0;
  char Done = FALSE;

	if (!FirstNode) return 0;  // List not read in for this ref

	if (NumVerses == 0) return 0;   // No xrefs for this ref

	MemPtr = CurrentNode;

	CurrentVerse--;

	if (CurrentVerse < 0) {	// Get previous node in list

		TempNode = MemPtr->PrevNode;

		if (TempNode) {
			CurrentNode = TempNode;

	    MemPtr = CurrentNode;

			CurrentVerse = MemPtr->NumVerses-1;
		}
    	else {CurrentVerse = 0; Done = TRUE; }
	}

  if (!Done) {
		Verse = MemPtr->Verses[CurrentVerse];

	}

  return Verse;
}

long TMargin::GetMargin(long Reference, int MarginNumber) {
	long Verse = 0;
	int Loop;


	Verse = GetFirst(Reference);

	if (MarginNumber > NumVerses) return 0;

	for (Loop = 0; Loop < MarginNumber-1; Loop++)
  	Verse = GetNext();

	return Verse;
}



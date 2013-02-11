#include <edu_southern_CBibleEngine.h>
#include <jni.h>
#include <android/log.h>
#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include <ctype.h>
#include "JCMargin.h"

#define TRUE 1
#define FALSE 0

TMargin *pMarginEngine = NULL;

void loga(const char *format, ...);


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

char TMargin::StartEngine(const char *Path) {
	char FileName[80];
  char Flag = TRUE;

  strcpy(SavePath, Path);

	strcpy(FileName,Path);
	strcat(FileName,("XREF.DAT"));

  DatFile = fopen(FileName, "rb"); // GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_ALWAYS, FILE_ATTRIBUTE_NORMAL, NULL);

  if (DatFile == NULL) Flag = FALSE;

	if (Flag)
	{
		strcpy(FileName,Path);
		strcat(FileName,("XREF.NDX"));

	  NdxFile = fopen(FileName, "rb"); // GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_ALWAYS, FILE_ATTRIBUTE_NORMAL, NULL);

	  if (NdxFile == NULL) Flag = FALSE;
	}

	if (Flag) loga("Margin Engine started");

	return Flag;
}

void TMargin::StopEngine() {

  if (DatFile) fclose(DatFile);
  if (NdxFile) fclose(NdxFile);

  DatFile = NULL;
  NdxFile = NULL;
}

void TMargin::ReStartEngine() {
  if (SavePath[0] != 0) StartEngine(SavePath);
}

char TMargin::ReadXRef(long Reference) {
  unsigned char Result;
	long StartPos;
	long StopPos;
	Node *MemPtr;
 	Node *MemPtr2;
	long NumBytes;
	long BytesRead;
  long BytesResult;
	unsigned char Byte1, Byte2;
	unsigned Verse;
	int PreviousVerse = 0;
	int Count;  // Number of consecutive xrefs

  NumVerses = 0;

	if (NdxFile) {

			// Find starting pos in Margin.dat for this ref
    if (fseek(NdxFile, (unsigned long) Reference*4,SEEK_SET) != 0) return(FALSE);
  	Result = fread(&StartPos, sizeof(StartPos), 1, NdxFile);
  	if (!Result) return (FALSE);  /* Read error */
  	Result = fread(&StopPos, sizeof(StopPos), 1, NdxFile);
  	if (!Result) return (FALSE);  /* Read error */

		NumBytes = StopPos-StartPos;

		if (DatFile) {

				// Find starting pos in Margin.dat for this ref
      if (fseek(DatFile, (unsigned long) StartPos, SEEK_SET) != 0) return(FALSE);

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
  	      Result = fread(&Byte1, sizeof(Byte1), 1, DatFile);
   	      BytesRead++;

					if ((Byte1 & 0x80) != 0) {
    	      Result = fread(&Byte2, sizeof(Byte2), 1, DatFile);

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

JNIEXPORT jbyte JNICALL Java_edu_southern_CBibleEngine_StartMarginEngine
  (JNIEnv *env, jobject, jstring Path)
{
	const char * szPath = env->GetStringUTFChars(Path, NULL);   

	if (!pMarginEngine) pMarginEngine = new TMargin;

	char fResult = pMarginEngine->StartEngine(szPath);
	
	if (fResult == 0) loga("Margin Engine failed to start");
	
	return fResult;
}

JNIEXPORT void JNICALL Java_edu_southern_CBibleEngine_StopMarginEngine
  (JNIEnv *env, jobject)
{
	if (pMarginEngine) pMarginEngine->StopEngine();
	
	delete pMarginEngine;
	pMarginEngine = NULL;
}

JNIEXPORT void JNICALL Java_edu_southern_CBibleEngine_ReStartMarginEngine
  (JNIEnv *env, jobject)
{
	if (pMarginEngine) pMarginEngine->ReStartEngine();
}

JNIEXPORT jint JNICALL Java_edu_southern_CBibleEngine_GetTotalMarginVerses
  (JNIEnv *env, jobject)
{
	if (pMarginEngine) 
		return pMarginEngine->GetTotalVerses();
	else
		return 0;

}

JNIEXPORT jlong JNICALL Java_edu_southern_CBibleEngine_GetFirstMargin
  (JNIEnv *env, jobject, jlong Reference)
{
	loga("GetFirstMargin");

	if (pMarginEngine) 
		return pMarginEngine->GetFirst(Reference);
	else
		return 0;
	
}

JNIEXPORT jlong JNICALL Java_edu_southern_CBibleEngine_GetNextMargin
  (JNIEnv *env, jobject)
{
	if (pMarginEngine) 
		return pMarginEngine->GetNext();
	else
		return 0;
}

JNIEXPORT jlong JNICALL Java_edu_southern_CBibleEngine_GetPreviousMargin
  (JNIEnv *env, jobject)
{
	if (pMarginEngine) 
		return pMarginEngine->GetPrevious();
	else
		return 0;
}

JNIEXPORT jlong JNICALL Java_edu_southern_CBibleEngine_GetMargin
  (JNIEnv *env, jobject, jlong Reference, jint MarginNdx)
{
	if (pMarginEngine) 
		return pMarginEngine->GetMargin(Reference, MarginNdx);
	else
		return 0;
}



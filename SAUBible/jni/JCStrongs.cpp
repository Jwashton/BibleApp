#include <edu_southern_CBibleEngine.h>
#include <jni.h>
#include <android/log.h>
#include <string.h>
#include "JCStrongs.h"

void loga(const char *format, ...);

#define TRUE 1
#define FALSE 0

TStrongs *pStrongsEngine = NULL;

TStrongs::TStrongs(void) {

	CurrentNode = NULL;
	FirstNode = NULL;
	DefBuf = NULL;
	WDefBuffer = NULL;

	CurrentReference = 0;
	NumStrongs = 0;

}

TStrongs::~TStrongs(void) {

	FlushList();

  if (DefBuf) delete[] DefBuf;
  if (WDefBuffer) delete[] WDefBuffer;
}

char TStrongs::ReadLzwTable(const char *Path) {
	char FileName[80];
  long BytesRead;
  FILE * LzwFile;

	strcpy(FileName,Path);
	strcat(FileName,("KJVLEX.LZW"));

  LzwFile = fopen(FileName, "rb"); // GENERIC_READ, 0, NULL, OPEN_ALWAYS, FILE_ATTRIBUTE_NORMAL, NULL);

  if (LzwFile != NULL) {

    fread(StringTable, sizeof(StringTable), 1, LzwFile);

    fclose(LzwFile);

    return TRUE;
	}
  	else return FALSE;

}

char TStrongs::ReadStrongs(long Reference) {
	long StartPos;
	long StartPos2;
	Node *MemPtr;
	Node *NextHandle;
	int Loop;
	char WordNum;
	short int StrongsNum;
	unsigned char Result;
	long BytesRead;

	if (StrongsNdx != NULL) {

	if (fseek(StrongsNdx, (unsigned long) Reference*4, SEEK_SET) != 0) return FALSE;
  	Result = fread(&StartPos, sizeof(StartPos), 1, StrongsNdx);
	
	if (!Result) return FALSE;  /* Read error */
  	Result = fread(&StartPos2, sizeof(StartPos2), 1, StrongsNdx);
	
	if (!Result) return FALSE;  /* Read error */

    NumStrongs = (StartPos2-StartPos) / 3;

	if (NumStrongs && StrongsDat != NULL) {

				// Find starting pos in strongs.dat for this ref
		fseek(StrongsDat, StartPos, SEEK_SET);

      	// Read in entire list for this verse into linked list
		MemPtr = new Node;
		FirstNode = MemPtr;

		for (Loop = 0; Loop < NumStrongs; Loop++) {
        	// Get next memory block in advance so links can be set
				NextHandle = new Node;

			if (MemPtr) {
        		fread(&WordNum, 1, 1, StrongsDat);
        		fread(&StrongsNum, 2, 1, StrongsDat);

				MemPtr->WordNum = WordNum;
				MemPtr->StrongsNum = StrongsNum;
				if (Loop +1 == NumStrongs)
			  		MemPtr->NextNode = NULL;
				else
					MemPtr->NextNode = NextHandle;

							// This will be the next word's Handle		
				MemPtr = NextHandle;
			}

		}

      	// Dispose of last unused memory block
		delete MemPtr;

	}
    	else return FALSE;

		CurrentReference = Reference;

  	return TRUE;
	}

  	else return FALSE;
}

void TStrongs::FlushList(void) {
	Node *TempNode;
	Node *NextNode;
	Node *MemPtr;

	TempNode = FirstNode;

	while (TempNode) {
		MemPtr = TempNode;

		NextNode = MemPtr->NextNode;

		delete TempNode;

		TempNode = NextNode;
	}

	FirstNode = NULL;
	CurrentNode = NULL;
	CurrentReference = 0;
	NumStrongs = 0;
}

char TStrongs::StartEngine(const char *Path) {
	char FileName[80];
  char Flag = TRUE;

	strcpy(FileName,Path);
	strcat(FileName,("STRONGS.DAT"));

  StrongsDat = fopen(FileName, "rb"); // GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_ALWAYS, FILE_ATTRIBUTE_NORMAL, NULL);

  if (StrongsDat == NULL) Flag = FALSE;

	strcpy(FileName,Path);
	strcat(FileName,("STRONGS.NDX"));

  StrongsNdx = fopen(FileName, "rb"); 
  if (StrongsNdx == NULL) Flag = FALSE;

	strcpy(FileName,Path);
	strcat(FileName,("KJVLEX.DAT"));

  LexDat = fopen(FileName, "rb"); 
  if (LexDat == NULL) Flag = FALSE;

	strcpy(FileName,Path);
	strcat(FileName,("KJVLEX.NDX"));

  LexNdx = fopen(FileName, "rb"); 
  if (LexNdx == NULL) Flag = FALSE;

	if (Flag) Flag = ReadLzwTable(Path);

	return Flag;

}

void TStrongs::StopEngine() {
	fclose(StrongsDat);
	fclose(StrongsNdx);
	fclose(LexDat);
	fclose(LexNdx);
}

int TStrongs::GetStrongs(long Reference, int WordNum) {
	int Ok = TRUE;
	Node *TempPtr;
	Node *NextPtr;
	Node *MemPtr;
	int StrongsNum = 0;

	if (Reference > 0) {
		if (Reference != CurrentReference) {
			FlushList();
			Ok = ReadStrongs(Reference);  // Read in list for this ref
		}

		if (!Ok) return FALSE;

  		// Search for word num in strongs list
		TempPtr = FirstNode;

		while (TempPtr != NULL) {
			MemPtr = TempPtr;

			if (MemPtr->WordNum >= WordNum) {
				if (MemPtr->WordNum == WordNum)
					StrongsNum = MemPtr->StrongsNum;
				break;
		}

			NextPtr = MemPtr->NextNode;

			TempPtr = NextPtr;
		}
	}

	return StrongsNum;
}

void TStrongs::DeCompressBuffer(char *InBuffer, unsigned NumBytes, char *OutBuffer, unsigned& OutBytes) {

	short int Index;
	short int BufPos;
	char TempBuf;
  char Alternate = FALSE;
	char OutStr[30];
	short int OutStrPos;
  short int ITemp;

	BufPos = 0;
	TempBuf = 0;

  OutBytes = 0;

	while (BufPos < (short int) NumBytes) {
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


char *TStrongs::GetDef(long Reference, int StrongsNum, unsigned& BufferSize) {

	char *Buffer = NULL;
	char *CompBuffer = NULL;
	unsigned CompBufferSize;
	long StartPos, EndPos;
	long BytesRead;

	if (StrongsNum > 0) {
			// Add 8674 if this is a greek strongs num
      // 23145 is the last verse in old testament
		if (Reference > 23145) StrongsNum += 8674;

		if (LexNdx) {
			if (DefBuf) delete[] DefBuf;
			if (WDefBuffer) delete[] WDefBuffer;
			DefBuf = NULL;
			WDefBuffer = NULL;

//			if (0xFFFFFFFFF != SetFilePointer(LexNdx, (unsigned long) StrongsNum*4, 0,SEEK_SET)) {
			if (0 == fseek(LexNdx, (unsigned long) StrongsNum*4, SEEK_SET)) {

				BytesRead = fread(&StartPos, sizeof(StartPos), 1, LexNdx);
				
				if (BytesRead) {
					fread(&EndPos, sizeof(EndPos), 1, LexNdx);

					CompBufferSize = EndPos-StartPos;

					CompBuffer = new char[CompBufferSize+5];
					Buffer = new char[5000];

					// Read in compressed buffer
					fseek(LexDat, StartPos, SEEK_SET);
					fread(CompBuffer, CompBufferSize, 1, LexDat);

					DeCompressBuffer(CompBuffer,CompBufferSize,Buffer,BufferSize);

					delete[] CompBuffer;

					// Copy buffer to char buffer
					WDefBuffer = new char[BufferSize+50];

					unsigned i;

					for (i = 0; i < BufferSize+1; i++) {
						char t = (unsigned short) Buffer[i];

						if (t == 10) {	// Replace character with newline chars
							WDefBuffer[i] = '\r';
							i++;
							t = '\n';
						}

						WDefBuffer[i] = t;
					}

					WDefBuffer[i] = 0;

					delete[] Buffer;
				}
			}
		}

		if (Buffer) {
			DefBuf = new char[BufferSize+10];
			memcpy(DefBuf, Buffer, BufferSize+1);
		}
	}

	return WDefBuffer;
}


JNIEXPORT jchar JNICALL Java_edu_southern_CBibleEngine_StartLexiconEngine
  (JNIEnv *env, jobject, jstring Path)
{
	if (pStrongsEngine == NULL) pStrongsEngine = new TStrongs;

	const char * szPath = env->GetStringUTFChars(Path, NULL);   
 
	char fFlag = pStrongsEngine->StartEngine(szPath);
 
	if (fFlag) loga("Lexicon engine started");

	return fFlag;

}

JNIEXPORT void JNICALL Java_edu_southern_CBibleEngine_StopLexiconEngine
  (JNIEnv *env, jobject)
{
	if (pStrongsEngine) pStrongsEngine->StopEngine();
	
	delete pStrongsEngine;
	pStrongsEngine = NULL;

}

JNIEXPORT jint JNICALL Java_edu_southern_CBibleEngine_GetStrongs
  (JNIEnv *env, jobject, jlong Reference, jchar WordNdx)
{
	if (pStrongsEngine)
		return pStrongsEngine->GetStrongs(Reference, WordNdx);
	else
		return 0;
}

JNIEXPORT jint JNICALL Java_edu_southern_CBibleEngine_GetNumStrongs
  (JNIEnv *env, jobject, jlong Reference)
{
	if (pStrongsEngine)
		return pStrongsEngine->GetNumStrongs(Reference);
	else
		return 0;
}

JNIEXPORT jstring JNICALL Java_edu_southern_CBibleEngine_GetDef
  (JNIEnv *env, jobject, jlong Reference, jint StrongsNum)
{
	unsigned BufferSize;
	jstring result;
	 
	if (pStrongsEngine)
	{
		char *StrongsStr = pStrongsEngine->GetDef(Reference, StrongsNum, BufferSize);
	 
		result = env->NewStringUTF(StrongsStr);	
	}

	return result;
}




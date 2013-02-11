#include <stdafx.h>
#include <string.h>
#include <tchar.h>

#include "CeStrongs.h"

#define TRUE 1
#define FALSE 0

#define BLACK 0x00000000

#define LINEWIDTH 15
#define CHARWIDTH 7
#define DefaultWindowWidth 315
#define DefaultWindowHeight 215
#define DefaultStrongsColor BLACK

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

char TStrongs::ReadLzwTable(WCHAR *Path) {
	WCHAR FileName[80];
  DWORD BytesRead;
  HANDLE LzwFile;

	lstrcpy(FileName,Path);
	lstrcat(FileName,_T("KjvLex.Lzw"));

  LzwFile = CreateFile(FileName, GENERIC_READ, 0, NULL, OPEN_ALWAYS, FILE_ATTRIBUTE_NORMAL, NULL);

  if (LzwFile != INVALID_HANDLE_VALUE) {

    ReadFile(LzwFile, StringTable, sizeof(StringTable), &BytesRead, NULL);

    CloseHandle(LzwFile);

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
	BOOL Result;
	DWORD BytesRead;

	if (StrongsNdx != INVALID_HANDLE_VALUE) {

	if (SetFilePointer(StrongsNdx, (unsigned long) Reference*4, 0,FILE_BEGIN) == -1) return FALSE;
  	Result = ReadFile(StrongsNdx, &StartPos, sizeof(StartPos), &BytesRead, NULL);
	
	if (!Result) return FALSE;  /* Read error */
  	Result = ReadFile(StrongsNdx, &StartPos2, sizeof(StartPos2), &BytesRead, NULL);
	
	if (!Result) return FALSE;  /* Read error */

    NumStrongs = (StartPos2-StartPos) / 3;

	if (NumStrongs && StrongsDat != INVALID_HANDLE_VALUE) {

				// Find starting pos in strongs.dat for this ref
		SetFilePointer(StrongsDat, StartPos, 0,FILE_BEGIN);

      	// Read in entire list for this verse into linked list
		MemPtr = new Node;
		FirstNode = MemPtr;

		for (Loop = 0; Loop < NumStrongs; Loop++) {
        	// Get next memory block in advance so links can be set
				NextHandle = new Node;

			if (MemPtr) {
        		ReadFile(StrongsDat, &WordNum, 1, &BytesRead, NULL);
        		ReadFile(StrongsDat, &StrongsNum, 2, &BytesRead, NULL);

				MemPtr->WordNum = WordNum;
				MemPtr->StrongsNum = StrongsNum;
				if (Loop +1 == NumStrongs)
			  		MemPtr->NextNode = NULL;
				else
					MemPtr->NextNode = NextHandle;

							// This will be the next word's handle
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
	NumStrongs = 0;
}

char TStrongs::StartEngine(WCHAR *Path) {
	WCHAR FileName[80];
  char Flag = TRUE;

	lstrcpy(FileName,Path);
	lstrcat(FileName,_T("Strongs.Dat"));

  StrongsDat = CreateFile(FileName, GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_ALWAYS, FILE_ATTRIBUTE_NORMAL, NULL);

  if (StrongsDat == INVALID_HANDLE_VALUE) Flag = FALSE;

	lstrcpy(FileName,Path);
	lstrcat(FileName,_T("Strongs.Ndx"));

  StrongsNdx = CreateFile(FileName, GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_ALWAYS, FILE_ATTRIBUTE_NORMAL, NULL);
  if (StrongsNdx == INVALID_HANDLE_VALUE) Flag = FALSE;

	lstrcpy(FileName,Path);
	lstrcat(FileName,_T("KjvLex.Dat"));

  LexDat = CreateFile(FileName, GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_ALWAYS, FILE_ATTRIBUTE_NORMAL, NULL);
  if (LexDat == INVALID_HANDLE_VALUE) Flag = FALSE;

	lstrcpy(FileName,Path);
	lstrcat(FileName,_T("KjvLex.Ndx"));

  LexNdx = CreateFile(FileName, GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_ALWAYS, FILE_ATTRIBUTE_NORMAL, NULL);
  if (LexNdx == INVALID_HANDLE_VALUE) Flag = FALSE;

	if (Flag) Flag = ReadLzwTable(Path);

	return Flag;

}

void TStrongs::StopEngine() {
	CloseHandle(StrongsDat);
	CloseHandle(StrongsNdx);
	CloseHandle(LexDat);
	CloseHandle(LexNdx);
}

int TStrongs::GetStrongs(long Reference, char WordNum) {
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


WCHAR *TStrongs::GetDef(long Reference, int StrongsNum, unsigned& BufferSize) {

	char *Buffer = NULL;
	char *CompBuffer = NULL;
	unsigned CompBufferSize;
	long StartPos, EndPos;
	DWORD BytesRead;

	if (StrongsNum > 0) {
			// Add 8674 if this is a greek strongs num
      // 23145 is the last verse in old testament
		if (Reference > 23145) StrongsNum += 8674;

		if (LexNdx) {
			if (DefBuf) delete[] DefBuf;
			if (WDefBuffer) delete[] WDefBuffer;
			DefBuf = NULL;
			WDefBuffer = NULL;

			if (0xFFFFFFFFF != SetFilePointer(LexNdx, (unsigned long) StrongsNum*4, 0,FILE_BEGIN)) {

				ReadFile(LexNdx, &StartPos, sizeof(StartPos), &BytesRead, NULL);
				
				if (BytesRead) {
					ReadFile(LexNdx, &EndPos, sizeof(EndPos), &BytesRead, NULL);

					CompBufferSize = EndPos-StartPos;

					CompBuffer = new char[CompBufferSize+5];
					Buffer = new char[5000];

					// Read in compressed buffer
					SetFilePointer(LexDat, StartPos, 0,FILE_BEGIN);
					ReadFile(LexDat, CompBuffer, CompBufferSize, &BytesRead, NULL);

					DeCompressBuffer(CompBuffer,CompBufferSize,Buffer,BufferSize);

					delete[] CompBuffer;

					// Copy buffer to WCHAR buffer
					WDefBuffer = new TCHAR[BufferSize+50];

					for (unsigned i = 0; i < BufferSize+1; i++) {
						TCHAR t = (unsigned short) Buffer[i];

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






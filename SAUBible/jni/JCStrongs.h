#ifndef __STRONGS_H
#define __STRONGS_H

#include <stdio.h>


class TStrongs {

// The old testament has numbers up to 8674
// The new testament has numbers up to 5624

	typedef struct NodeTag {
		char WordNum;
		short int StrongsNum;
		NodeTag *NextNode;
	} Node;

		/* Lzw compression table */
	struct {
		short int PrevChar;
		short int FollChar;
	} StringTable[4096];

	FILE * StrongsDat;
	FILE * StrongsNdx;
	FILE * LexDat;
	FILE * LexNdx;

	Node *CurrentNode; // Points to current word in strongs list
	Node *FirstNode;		// Points to first word in strongs list
	char *DefBuf;			// Stores current definition text
	char *WDefBuffer;

	long CurrentReference;
  int NumStrongs;		// Number of strongs numbers for this reference

	char ReadLzwTable(const char *Path);
	char ReadStrongs(long Reference);
	void FlushList(void);
	void DeCompressBuffer(char *InBuffer, unsigned NumBytes, char *OutBuffer, unsigned& OutBytes);

public:
	TStrongs(void);
  ~TStrongs(void);

	char StartEngine(const char *Path);
	void StopEngine();
	long GetCurrentReference(void) { return CurrentReference; }
	int GetStrongs(long Reference, int WordNum);
	int GetNumStrongs(long Reference) { return NumStrongs; };
	char *GetDef(long Reference, int StrongsNum, unsigned& BufferSize);
//	int GetFirst(long Reference, char& WordNum);
//	int GetNext(char& WordNum);
};

#endif

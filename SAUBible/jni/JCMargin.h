#ifndef __Margin_H
#define __Margin_H

class TMargin {

	typedef struct NodeTag {
		short int Verses[20];  // 20 xref verses per node
		char NumVerses;  // Number of xref verses in this node
		NodeTag *NextNode;
	   NodeTag *PrevNode;
	} Node;

	Node *CurrentNode; // Points to current node in xref list
	Node *FirstNode;		// Points to first node in xref list

  FILE *NdxFile;
  FILE *DatFile;

  int CurrentVerse;  // Index into Verses array for current node
  long CurrentReference;
  int NumVerses;		// Number of cross reference verses for this reference

  char SavePath[100];

	char ReadXRef(long Reference);
	void FlushList(void);

public:
	TMargin(void);
  ~TMargin(void);

	char StartEngine(const char *Path);
  void StopEngine();
  void ReStartEngine();
	int GetTotalVerses(void) { return NumVerses; };
	long GetFirst(long Reference);
	long GetNext(void);
	long GetPrevious(void);
	long GetMargin(long Reference, int MarginNumber);
};



#endif

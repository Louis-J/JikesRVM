/*
 * (C) Copyright IBM Corp. 2001
 */
//$Id$

/**
 * @author Perry Cheng
 */
class VM_Heap 
  implements VM_Constants, VM_GCConstants, VM_Uninterruptible {

    final String name;  // Name of the heap

    int verbose = 0;    // Amount of chattering during operation

    static private VM_BootRecord bootRecord;
    static public int MAX_HEAPS = 10;
    static VM_Heap [] allHeaps = new VM_Heap[MAX_HEAPS];
    static private int heapCount = 0;
    private int id;          // instance's position within the allHeaps array
    static VM_Heap bootHeap;

    protected VM_Address start;         // INCLUSIVE-EXCLUSIVE range of memory belonging to this heap
    protected VM_Address end;
    protected int size;                 // end - start
    protected VM_Address minRef;        // INCLUSIVE range for legal object reference values
    protected VM_Address maxRef;


    // For initial creation when no information is known at boot-image build time 
    //
    public VM_Heap(String name) {        
	this.name = name;
	start = end = VM_Address.fromInt(0);
	minRef = maxRef = VM_Address.fromInt(0);
	size = 0;
	id = heapCount++;
	allHeaps[id] = this;
    }

    // Set minRef, maxRef, size and update bootRecord heap ranges
    //
    void setAuxiliary() {     
	if (VM.VerifyAssertions) VM.assert(id < bootRecord.heapRanges.length - 2); 
	bootRecord.heapRanges[2 * id] = start.toInt();
	bootRecord.heapRanges[2 * id + 1] = end.toInt();
	minRef = VM_ObjectModel.minimumObjectRef(start);
	maxRef = VM_ObjectModel.maximumObjectRef(end);
	size = end.diff(start);
    }
    
    static void boot(VM_Heap bh, VM_BootRecord br) {       // Should be called from VM_Allocator.boot 
	bootHeap = bh;
	bootRecord = br;
	bootHeap.start = bootRecord.bootImageStart;
	bootHeap.end = bootRecord.bootImageEnd;
	bootHeap.setAuxiliary();
	if (VM.VerifyAssertions) VM.assert(bootHeap.refInHeap(VM_Magic.objectAsAddress(bootHeap)));
    }

    public void setRegion(VM_Address s, VM_Address e) {
	start = s;
	end = e;
	setAuxiliary();
    }

    public int getSize() {
	return size;
    }

    // Zero the entire heap
    //
    public void zero() {
	if (VM.VerifyAssertions) VM.assert(VM_Memory.roundDownPage(size) == size);
	VM_Memory.zeroPages(start, size);
    }

    // Zero the portion of the s ..e range of this heap that is assigned to the given processor
    //
    public void zeroParallel(VM_Address s, VM_Address e) {

	if (VM.VerifyAssertions) VM.assert(s.GE(start));
	if (VM.VerifyAssertions) VM.assert(e.LE(end));
	int np = VM_CollectorThread.numCollectors();
	VM_CollectorThread self = VM_Magic.threadAsCollectorThread(VM_Thread.getCurrentThread());
	int which = self.gcOrdinal - 1;  // gcOrdinal starts at 1
	int chunk = VM_Memory.roundUpPage(e.diff(s) / np);
	VM_Address zeroBegin = s.add(which * chunk);
	VM_Address zeroEnd = zeroBegin.add(chunk);
	if (zeroEnd.GT(end)) zeroEnd = end;
	int size = zeroEnd.diff(zeroBegin);  // actual size to zero
	if (VM.VerifyAssertions) VM.assert(VM_Memory.roundUpPage(size) == size);
	VM_Memory.zeroPages(zeroBegin, size);

    }

    // size is specified in bytes and must be positive - automatically rounded up to the next page
    //
    public void attach(int size) {
	if (VM.VerifyAssertions) VM.assert(bootRecord != null);
	if (size < 0) 
	    VM.sysFail("VM_Heap.attach given negative size\n");
	if (this.size != 0)
	    VM.sysFail("VM_Heap.attach called on already attached heap\n");
	size = VM_Memory.roundUpPage(size);
	// Let OS place region for now
	start = VM_Memory.mmap(VM_Address.fromInt(0), 
			       size, 
			       VM_Memory.PROT_READ | VM_Memory.PROT_WRITE | VM_Memory.PROT_EXEC,
			       VM_Memory.MAP_PRIVATE | VM_Memory.MAP_ANONYMOUS);
	if (start.GE(VM_Address.fromInt(0)) && 
	    start.LT(VM_Address.fromInt(128))) {  // errno range
	    VM.sysWrite("VM_Heap failed to mmap ", size / 1024);
	    VM.sysWrite(" Kbytes with errno = "); VM.sysWrite(start); VM.sysWriteln();
	    if (VM.VerifyAssertions) VM.assert(false);
	}
	end = start.add(size);
	if (verbose >= 1) {
	    VM.sysWrite("VM_Heap sucessfully mmap ", size / 1024);
	    VM.sysWrite(" Kbytes from ");
	    VM.sysWrite(start); VM.sysWrite(" to " ); VM.sysWrite(end); VM.sysWrite("\n");
	}
	setAuxiliary();
    }

    public void grow(int sz) {
	if (sz < size)
	    VM.sysFail("VM_Heap.grow given smaller size than current size\n");
	sz = VM_Memory.roundUpPage(sz);
	VM_Address result = VM_Memory.mmap(end, sz - size,
					   VM_Memory.PROT_READ | VM_Memory.PROT_WRITE | VM_Memory.PROT_EXEC,
					   VM_Memory.MAP_PRIVATE | VM_Memory.MAP_ANONYMOUS | VM_Memory.MAP_FIXED);
	int status = result.toInt();
	if (status >= 0 && status < 128) {
	    VM.sysWrite("VM_Heap.grow failed to mmap additional ", (sz - size) / 1024);
	    VM.sysWrite(" Kbytes at ");
	    VM.sysWrite(end);
	    VM.sysWriteln(" with errno = ", status);
	    if (VM.VerifyAssertions) VM.assert(false);
	}
	if (verbose >= 1) {
	    VM.sysWrite("VM_Heap.grow successfully mmap additional ", (sz - size) / 1024);
	    VM.sysWrite(" Kbytes at ");  VM.sysWrite(end); VM.sysWrite("\n");
	}
	// start not modified
	end = start.add(sz);
	setAuxiliary();
    }

    public void detach() {
	if (this.size == 0)
	    VM.sysFail("VM_Heap.detach called on unattached heap\n");
	int status = VM_Memory.munmap(start, size);
	if (status != 0) {
	    VM.sysWriteln("VM_Heap.detach failed with errno = ", status);
	    VM.sysFail("VM_Heap.detach failed\n");
	}
	if (verbose >= 1) {
	    VM.sysWrite("VM_Heap successfully detached ", size / 1024); 
	    VM.sysWrite(" Kbytes starting at ");
	    VM.sysWrite(start); VM.sysWriteln();
	}
	start = end = VM_Address.fromInt(0);
	setAuxiliary();
    }

    public void protect() {
	VM_Memory.mprotect(start, size, VM_Memory.PROT_NONE);
    }
    
    public void unprotect() {
	VM_Memory.mprotect(start, size, VM_Memory.PROT_READ | VM_Memory.PROT_WRITE | VM_Memory.PROT_EXEC);
    }
    
    public boolean refInHeap(VM_Address ref) {
	return ref.GE(minRef) && ref.LE(maxRef);
    }

    public boolean addrInHeap(VM_Address addr) {
	return addr.GE(start) && addr.LT(end);
    }

    static public boolean refInAnyHeap(VM_Address ref) {
	for (int i=0; i<heapCount; i++)
	    if (allHeaps[i].refInHeap(ref))
		return true;
	return false;
    }

    static public void showAllHeaps() {
	for (int i=0; i<heapCount; i++) {
	    VM.sysWrite("Heap ", i, ": "); 
	    allHeaps[i].show(); 
	}
    }


    static public boolean addrInAnyHeap(VM_Address addr) {
	for (int i=0; i<heapCount; i++)
	    if (allHeaps[i].addrInHeap(addr))
		return true;
	return false;
    }

    public void showRange() {
	VM.sysWrite(start); VM.sysWrite(" .. "); VM.sysWrite(end);
    }


    public void show() {
	int tab = 25 - name.length();
	for (int i=0; i<tab; i++) VM.sysWrite(" ");
	VM.sysWrite(name, ": ");
	VM.sysWriteField(6, size / 1024); VM.sysWrite(" Kb  at  "); 
	showRange();
	VM.sysWriteln();
    }

    public void touchPages() {
	int ps = VM_Memory.getPagesize();
	for (int i = size - ps; i >= 0; i -= ps)
	    VM_Magic.setMemoryWord(start.add(i), 0);
    }

    static public void clobber(VM_Address start, VM_Address end) {
	VM.sysWrite("Zapping region ", start);
	VM.sysWrite(" .. ", end);
	VM.sysWriteln(" with 0xff****ff: ");
	int size = end.diff(start);
	for (int i=0; i<size; i+=4) {
	    int pattern = 0xff0000ff;
	    pattern |= i & 0x00ffff00;
	    VM_Magic.setMemoryWord(start.add(i), pattern);
	}
    }

    public void clobber() { clobber(start, end); }

    // Scan this heap for references to the target heap and report them
    // This is approximate since the scan is done without type information.
    //
    public int paranoidScan(VM_Heap target, boolean show) {
	int count = 0;
	VM.sysWrite("Checking heap "); showRange(); 
	VM.sysWrite(" for references to "); target.showRange(); VM.sysWriteln();
	for (VM_Address loc = start; loc.LT(end); loc = loc.add(4)) {
	    VM_Address value = VM_Magic.getMemoryAddress(loc);
	    int value2 = value.toInt();
	    if (((value2 & 3) == 0) && target.refInHeap(value)) {
		count++;
		if (show) {
		    int oldVal = VM_Magic.getMemoryWord(value.sub(12));
		    VM.sysWrite("Warning:  GC ", VM_Allocator.gcCount);
		    VM.sysWrite(" # ", count);
		    VM.sysWrite("  loc ", loc); 
		    VM.sysWrite(" holds poss ref ", value);
		    VM.sysWriteln(" with value ", oldVal);
		}
	    }
	}
	VM.sysWrite("\nThere were ", count, " suspicious references to ");
	target.showRange();
	VM.sysWriteln();
	return count;
    }


}

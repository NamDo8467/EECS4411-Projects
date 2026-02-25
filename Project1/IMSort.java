// ===========================================================================
// IMSort
//     External Sort routine implemented in the "iterative merge"
//     approach to sort a multi-page file representing a sequence of
//     integers.
// ---------------------------------------------------------------------------
//  author: ?
// created: ?
//  latest: ?
// ---------------------------------------------------------------------------
// TEMPLATE
// ---------------------------------------------------------------------------
//  author: parke godfrey
// version: 2025-10-08
// ===========================================================================

import java.io.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.lang.Math;
import intfile.IntFileUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static intfile.IntFileUtils.readIntPage;
import static intfile.IntFileUtils.writeIntPage;
import static intfile.IntFileUtils.filePageSize;
import static intfile.IntFileUtils.PAGE_SIZE;

public class IMSort {
    // -----------------------------------------------------------------------
    // MAIN
    //     fileName: name for the file to be sorted
    //     frames:   the number of "buffer frames" allocated for the job
    // 
    //     sorts a large binary, multi-page length file representing a
    //     sequence of integers
    //     implements external sort 
    // -----------------------------------------------------------------------

    public static void main(String[] args)
            throws FileNotFoundException, IOException, ArithmeticException {
        // -------------------------------------------------------------------
        // PROCESS COMMAND-LINE ARGUMENTS

        if (args.length < 2) {
            System.out.println("please provide name of integer"
                             + "file to be sorted1111");
            System.out.println("and number of buffer frames allocated");
            System.exit(0);
        }
 
        String  intFile = args[0];
        int     frames   = Integer.parseInt(args[1]);

        if (frames < 3) {
            System.out.println("must allocate at least three frames");
            System.exit(-1);
        }

        // -------------------------------------------------------------------
        // ALLOCATE THE BUFFER, ETC.

        // 1,024 4-byte ints fit on a "page" in our buffer
        // we emulate this by an array of int of length 1,024
        // (PAGE_SIZE / 4) * number of FRAMES requested
        int[] intBuf   = new int[(PAGE_SIZE / 4) * frames];
        int[] frameOff = new int[frames + 1]; // last is for sentinel!
        for (int i = 0; i < frameOff.length; i++) {
            frameOff[i] = (PAGE_SIZE / 4) * i;
        }
        int outOff = frameOff[frames - 1]; // offset to last frame

        int runNum      = 0; // used to sequentially number runs
        int firstUnused = 0; // first of the runs still unused
        int ioCount     = 0; // used to count IOs

        // -------------------------------------------------------------------
        // BLOCK SORT OPERATIONS
        //   * read in frames number of pages into the intBuf,
        //     sort across these,
        //     write intBuf out as a temporary run file.
        //   * NAMING CONVENTION: name run file intFile + "-RUN-" + runNum
        //   * repeat until input file has been consumed

        // YOUR CODE HERE ...
        //   * NOTE: already imported Arrays above as you likely want to use
        //     its sort method instead of implementing it from scratch
        int totalPages = filePageSize(intFile);

        for (int pageStart = 0; pageStart < totalPages; pageStart += frames) {
            // how many pages to read this iteration (last block may be smaller)
            int pagesToRead = Math.min(frames, totalPages - pageStart);

            // read in pagesToRead pages into intBuf
            for (int i = 0; i < pagesToRead; i++) {
                readIntPage(intFile, pageStart + i, frameOff[i], intBuf);
                ioCount++;
            }

            // sort only the integers we read in
            Arrays.sort(intBuf, 0, pagesToRead * (PAGE_SIZE / 4));

            // write sorted block out as a run file
            String runFile = intFile + "-RUN-" + runNum;
            for (int i = 0; i < pagesToRead; i++) {
                writeIntPage(runFile, frameOff[i], intBuf, i == 0);
                ioCount++;
            }

            runNum++;
        }

        // -------------------------------------------------------------------
        // MERGE OPERATIONS
        //   * pick up earliest (frame - 1) unused runs and merge to make a
        //     new output run
        //   * if fewer than (frame - 1) unused runs remain, merge them;
        //     this is the final run!
        //   * name the final run as intFile + "-SORTED"
        //   * be sure to remove runs once they have been used!
        //   * note that the last frame of the buffer is being used to
        //     accumulate the sorted results, writing out to the output run
        //     every time it becomes full
        //   * we also are careful not to be CPU bound, in case the number
        //     of allocated frames is large!

        // YOUR CODE HERE ...
        //   * NOTE: already imported PriorityQueue above as you likely
        //     want to use it in your merge procedure to be more
        //     efficient, especially when the fan-in is large
        //   * oh, and you really, really will want to use PriorityQueue 
        //     for a second reason: it make the coding easier!

        // while there are still input runs to merge:
        //     consume frames - 1 unused input runs
        //     produce a new, merged output run
        //     runNum++
        while(runNum - firstUnused > 1) {
        	// get the number of runs to merge each iteration
        	int runsToMerge = Math.min(frames - 1, runNum - firstUnused); 
        	
        	// isFinal = true if runsToMerge = # of runs left
            boolean isFinal = (runsToMerge == runNum - firstUnused);
            String outputRun = isFinal ? intFile + "-SORTED" : intFile + "-RUN-" + runNum;

            // load first page of each run into its assigned frame
            int[] runPages = new int[runsToMerge];
            int[] runSizes = new int[runsToMerge];
            int[] framePosn = new int[runsToMerge];

            for (int i = 0; i < runsToMerge; i++) {
                String runFile = intFile + "-RUN-" + (firstUnused + i);
                runSizes[i] = filePageSize(runFile);
                
                // i is the i'th page of this run.
                // 0 means the first 1024 integers.
                runPages[i] = 0; 
                
                // i is the i'th frame.
                // 0 means the first position in this frame (index 0 to 1023)
                // in the intBuf. The frame also contains 1024 integers.
                framePosn[i] = 0;
                
                
                // Read the first page (the first 1024 integers) from 
                // the current runFile to intBuf
                readIntPage(runFile, 0, frameOff[i], intBuf);
                ioCount++;
            }
            
            // set up priority queue
            PriorityQueue<int[]> pq = new PriorityQueue<>(runsToMerge,
                (a, b) -> Integer.compare(a[0], b[0]));
            
            
            for (int i = 0; i < runsToMerge; i++) {
            	
            	// frameOff[i] is the starting index of the i'th frame,
            	// each frame contains exactly 1024 integers
            	// For example: 
            	// frameOff[0] = 1024 * 0 = 0 (start from 0 to 1023)
            	// frameOff[1] = 1024 * 1 = 1024 (start from 1024 to 2047)
            	// frameOff[2] = 1024 * 2 = 2048 (start from 2048 to 4095),
            	// so on
            	
            	// This code pushes the first number of the i'th frame and
            	// frame number i to the queue
            	// Since the all numbers are sorted, it's guaranteed
            	// that the smallest number of the frame is being pushed to the queue.
                pq.offer(new int[]{intBuf[frameOff[i]], i});

                
            }
         // tracks the current position within the output frame
            // It starts at 0 and increments every time we place an integer into the output frame
            // When it reaches 1024 (PAGE_SIZE / 4), the output frame is full 
            // then reset outPos = 0 and we write it out,
            int outPos = 0;
            
            // tracks how many pages we have written out to the output run file so far.
            // It starts at 0 and increments every time we write a full page.
            // It is used to determine whether to create the file or append to the current one.
            // outPage == 0 means first = true
            // outPage > 0 means first = false
            int outPage = 0;
            
            
            while (!pq.isEmpty()) {
            	int[] entry = pq.poll();
                int value = entry[0];
                int frameIdx = entry[1];
                
                // places the smallest integer polled from the PQ into the output frame in intBuf.
                // outOff is the starting index of the output frame (e.g. with frames=3, outOff = 2048)
                // because the first two frames are (0 to 1023 and 1024 to 2047)
                // the 3rd frame is from 2048 to 3071
                // outPos is the current position within the output frame
                // So for example if outOff = 2048 and outPos = 0, we place the value at intBuf[2048]
                // So essentially we are just filling up the output frame one integer 
                // at a time with the smallest integers from the PQ.
                intBuf[outOff + outPos] = value;
                
                // moves to the next position in the output frame, ready for the next integer
                outPos++;
                
                
                // If outPos is at the last position of the frame (outPos == PAGE_SIZE / 4)
                // then create an output run if outPage == 0 and write the first page
                // that contains "PAGE_SIZE/4" integers to this file.
                // OR append the page to the current output run if outPage != 0                  
                if (outPos == PAGE_SIZE / 4) {
                    writeIntPage(outputRun, outOff, intBuf, outPage == 0);
                    ioCount++;
                    outPos = 0; // reset outPos to 0
                    outPage++; // increase the outPage to the next page
                }
                
                
                // After we popped a value from the PQ and placed it in the output frame,
                // we move to the next position in that same frame
                framePosn[frameIdx]++;
                

                // If framePosn reached 1024, the frame is exhausted (no more integers left in it)
                if (framePosn[frameIdx] == PAGE_SIZE / 4) {
                	
                	// framePosn[frameIdx] = 0 —> reset position back to start of frame
                    framePosn[frameIdx] = 0;
                    
                    // runPages[frameIdx]++ —> move to next page of that run
                    runPages[frameIdx]++;

                    
                    // check if that run still has more pages
                    // If yes then load next page into that frame with readIntPage and add its first integer to PQ
                    // If no → the run is exhausted, don't add anything to PQ
                    if (runPages[frameIdx] < runSizes[frameIdx]) {
                        String runFile = intFile + "-RUN-" + (firstUnused + frameIdx);
                        readIntPage(runFile, runPages[frameIdx], frameOff[frameIdx], intBuf);
                        ioCount++;
                        pq.offer(new int[]{intBuf[frameOff[frameIdx]], frameIdx});
                    }
                } else { // If the frame is NOT exhausted yet (still has integers left)
                	
                	// Add the next integer from that same frame to the PQ
                    pq.offer(new int[]{intBuf[frameOff[frameIdx] + framePosn[frameIdx]], frameIdx});
                }
				
			}
            
            // write out any remaining integers in output frame
            if (outPos > 0) {
                writeIntPage(outputRun, outOff, intBuf, outPage == 0);
                ioCount++;
            }
            
            // delete input runs
            for (int i = 0; i < runsToMerge; i++) {
            	Path path = Paths.get(intFile + "-RUN-" + (firstUnused + i));
            	 try {
                     Files.delete(path);
                     System.out.println("File deleted successfully.");
                 } catch (java.nio.file.NoSuchFileException e) {
                     System.out.println("Oops! The file doesn't exist: " + e.getFile());
                 } catch (java.nio.file.DirectoryNotEmptyException e) {
                     System.out.println("Hold up! That's a folder with stuff in it: " + e.getFile());
                 } catch (java.io.IOException e) {
                     System.out.println("Something else went wrong: " + e.getMessage());
                     e.printStackTrace();
                 }

            }

            firstUnused += runsToMerge;
            runNum++;
        }

        System.out.println("sorted with " 
                         + runNum
                         + " output runs in "
                         + ioCount
                         + " IOs");
    }
}

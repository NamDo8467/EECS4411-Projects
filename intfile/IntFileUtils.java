// ===========================================================================
// IntFileUtils
//     a collection of utilities to read and write binary files
//     with a length that is a multiple of a PAGE size
//     that represent integer data.
// ---------------------------------------------------------------------------
//  author: parke godfrey
// created: 2023-10-24
//  latest: 2023-10-25
// ===========================================================================

package intfile;

import java.io.*;
import java.util.Random;
import java.lang.Math;

public class IntFileUtils {
    // declare our page size
    public static final int PAGE_SIZE = 4096; // 4KB

    // the default seed to use to seed the random number generator
    private static long defaultSeed = 48988659276962496L; // taxicab(5)!

    // -----------------------------------------------------------------------
    // int2bytes
    //     x:       an int(eger)
    //     RETTURN: an array of four bytes ("represents" an int)
    // 
    //     convert an int into an array of four bytes
    // -----------------------------------------------------------------------

    public static byte[] int2bytes(int x) {
        byte[] bytes = new byte[4];

        bytes[0] = (byte) (x >> 24);
        bytes[1] = (byte) (x >> 16);
        bytes[2] = (byte) (x >>  8);
        bytes[3] = (byte) (x /*>> 0*/);

        return bytes;
    }

    // -----------------------------------------------------------------------
    // intPage2bytePage
    //     ints:   a page (array of length 1024) integers
    //     RETURN: a page (array of length 4096) of bytes
    // 
    //     convert a page of ints into a page of bytes
    // -----------------------------------------------------------------------

    public static byte[] intPage2bytePage(int[] ints)
            throws IOException {
        return intPage2bytePage(ints, 0);
    }

    // -----------------------------------------------------------------------
    // intPage2bytePage
    //     ints:   array of integers of at least length 1024 (a page worth)
    //     offset: where in the array to start reading a page worth of ints
    //     RETURN: a page (array of length 4096) of bytes
    // 
    //     convert a page of ints into a page of bytes
    // -----------------------------------------------------------------------

    public static byte[] intPage2bytePage(int[] ints,
                                          int offset)
            throws IOException {
        byte[] bytes = new byte[PAGE_SIZE];

        if (ints.length - offset < PAGE_SIZE / 4)
            throw new IOException("not a PAGE worth of ints");

        for (int i = 0; i < PAGE_SIZE / 4; i++) {
            bytes[4*i    ] = (byte) (ints[i + offset] >> 24);
            bytes[4*i + 1] = (byte) (ints[i + offset] >> 16);
            bytes[4*i + 2] = (byte) (ints[i + offset] >>  8);
            bytes[4*i + 3] = (byte) (ints[i + offset] /*>> 0*/);
        }

        return bytes;
    }

    // -----------------------------------------------------------------------
    // bytes2int
    //     bytes:  an array of four bytes ("represents" an int)
    //     RETURN: an int(eger)
    // 
    //     convert an array of four bytes into an int
    // -----------------------------------------------------------------------

    public static int bytes2int(byte[] bytes) {
        int x = 0;
        for (int i = 0; i < 4; i++) {
            x <<= 8;
            x |= (int) bytes[i] & 0xFF;
        }

        return x;
    }

    // -----------------------------------------------------------------------
    // bytePage2intPage
    //     bytes:  a page (array of length 4096) of bytes
    //     RETURN: a page (array of length 1024) integers
    // 
    //     convert a page of bytes into a page of ints
    // -----------------------------------------------------------------------

    public static int[] bytePage2intPage(byte[] bytes)
            throws IOException {
        int[] ints = new int[PAGE_SIZE / 4];

        if (bytes.length != PAGE_SIZE)
            throw new IOException("not a PAGE of bytes");

        for (int i = 0; i < PAGE_SIZE / 4; i++) {
            ints[i] = 0;
            for (int j = 0; j < 4; j++) {
                ints[i] <<= 8;
                ints[i] |= (int) bytes[4*i + j] & 0xFF;
            }
        }

        return ints;
    }

    // -----------------------------------------------------------------------
    // generateIntFile (fileName, pages)
    // -----------------------------------------------------------------------

    public static void generateIntFile(String intFile, int pages)
            throws FileNotFoundException, IOException {
        generateIntFile(intFile, pages, false, defaultSeed);
    }

    // -----------------------------------------------------------------------
    // generateIntFile (fileName, pages, show)
    // -----------------------------------------------------------------------

    public static void generateIntFile(String intFile,
                                       int pages,
                                       boolean show)
            throws FileNotFoundException, IOException {
        generateIntFile(intFile, pages, show, defaultSeed);
    }

    // -----------------------------------------------------------------------
    // generateIntFile
    //     fileName: name for the file to be written
    //     pages:    number of pages the file should be
    //     show:     whether to spill to STDOUT
    //     seed:     a seed (long) for the random number generator
    // -----------------------------------------------------------------------

    public static void generateIntFile(String intFile,
                                       int pages,
                                       boolean show,
                                       long mySeed)
            throws FileNotFoundException, IOException {
        Random  gen     = new Random(mySeed);
        int[]  intPage  = new int[PAGE_SIZE / 4];
        OutputStream intStream = new FileOutputStream(intFile);

        if (show) System.out.println("writing integers into file");
		for (int p = 0; p < pages; p++) {
			for (int i = 0; i < PAGE_SIZE / 4; i++) {
				intPage[i] = gen.nextInt();
				if (show) System.out.println("" + intPage[i]);
			}
			intStream.write(intPage2bytePage(intPage));
		}
    }

    // -----------------------------------------------------------------------
    // readIntPage
    //     fileName: name for the file to read from
	//     page:     which page of the file to read
    //     offset:   where in int array to start writing a page worth
    //               of int(eger)s
    //     ints:     array of ints
    // -----------------------------------------------------------------------

    public static void readIntPage(String  intFile,
								   int     page,
                                   int     offset,
                                   int[]   ints)
			throws FileNotFoundException, IOException {
        byte[] bytes = new byte[PAGE_SIZE];

		if (page < 0) {
            throw new IOException("illegal page-read request: page = "
								+ page);
		}

		RandomAccessFile inHandle = new RandomAccessFile(intFile, "r");
		inHandle.seek(page * PAGE_SIZE);
		int amount = inHandle.read(bytes); // read in PAGE of bytes
		if (amount < PAGE_SIZE) {
            throw new IOException("not a PAGE worth of bytes returned"
								+ " when reading from file "
								+ intFile
								+ " from page "
								+ page);
		}
		// translate bytes into ints and place in ite array provided
		for (int i = 0; i < PAGE_SIZE / 4; i++) {
            ints[i + offset] = 0;
            for (int j = 0; j < 4; j++) {
                ints[i + offset] <<= 8;
                ints[i + offset] |= (int) bytes[4*i + j] & 0xFF;
            }
		}
	}

    // -----------------------------------------------------------------------
    // writeIntPage
    //     fileName: name for the file to write into
    //     offset:   where in int array to start pulling a page worth
    //               of int(eger)s to write
    //     ints:     array of ints
    //     first:    whether initializing file or appending to it
    // -----------------------------------------------------------------------

    public static void writeIntPage(String  intFile,
                                    int     offset,
                                    int[]   ints,
                                    boolean first)
            throws FileNotFoundException, IOException {
		OutputStream intStream = new FileOutputStream(intFile, !first);
		intStream.write(intPage2bytePage(ints, offset));
    }

    // -----------------------------------------------------------------------
    // printIntFile
    //     intFile: "integer" file to read and print out
    // 
    //     print out the integers from our "integer" binary file
    // -----------------------------------------------------------------------

    public static void printIntFile(String intFile)
            throws FileNotFoundException, IOException {
        InputStream intStream = new FileInputStream(intFile);
        byte[]      bytePage  = new byte[PAGE_SIZE];
        int[]       intPage;

		int    bytesRead = -1;
		while ((bytesRead = intStream.read(bytePage)) != -1) {
			if (bytesRead != PAGE_SIZE) {
				System.out.println("file not a multiple of page size!");
				System.exit(-1);
			}
			intPage = bytePage2intPage(bytePage);
			for (int i = 0; i < intPage.length; i++)
				System.out.println("" + intPage[i]);
		}
    }

    // -----------------------------------------------------------------------
	public static int filePageSize(String intFile)
			throws FileNotFoundException, IOException, ArithmeticException {
		// get the file's size
		File file = new File(intFile);
		long fileLen = file.length();
		if (fileLen % IntFileUtils.PAGE_SIZE != 0)
            throw new IOException("file "
								+ intFile
								+ "is not a multiple of page size!");

		return Math.toIntExact(fileLen / PAGE_SIZE);
	}

    // -----------------------------------------------------------------------
    // MAIN
    //     calls generateIntFile to create a binary file
    //     of a multiple of page size filled with consecutive integers
    // -----------------------------------------------------------------------

    public static void main(String[] args)
            throws FileNotFoundException, IOException {
        // process command-line arguments
        //     fileName: name for the file to be written
        //     pages:    number of pages file should be
        //     show ["true" | "false"]:
        //         spills integers written to STDOUT
        //         and then reads and spills the file
        //     [seed]:   a seed (long) for the random number generator
        if (args.length < 2) {
            System.out.println("please provide output file name");
            System.out.println("and number of pages to write");
            System.exit(0);
        }
 
        String  intFile = args[0];
        int     pages   = Integer.parseInt(args[1]);
        // System.out.println("writing out " + pages + " pages");

        boolean show = false;
        if (args.length >= 3) {
            if (args[2].equals("true")) {
                show = true;
            }
        }

        long mySeed = defaultSeed;
        if (args.length >= 4) {
            mySeed = Long.parseLong(args[3]);
        }

        // generate the file
        generateIntFile(intFile, pages, show, mySeed);

        // read and spill the file, if requested
        if (args.length >= 3 && show) {
            System.out.println("reading integers out of file");
            printIntFile(intFile);
        }
    }
}


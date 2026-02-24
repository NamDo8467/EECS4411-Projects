// ===========================================================================
// SpillIntFile
//     reads binary file of multi-page length representing a sequence of
//     integers and writes the integers out to STDOUT
// ---------------------------------------------------------------------------
//  author: parke godfrey
// created: 2023-10-25
// ===========================================================================

package intfile;

import java.io.*;
import intfile.*;

public class SpillIntFile {
    // -----------------------------------------------------------------------
    // MAIN
	//     fileName: name for the file to be spilled
    // -----------------------------------------------------------------------

    public static void main(String[] args)
            throws FileNotFoundException, IOException {
		// process command-line arguments
        if (args.length < 1) {
            System.out.println("please provide name of integer"
							 + "int file to be spilled");
            System.exit(0);
        }
 
        String  intFile = args[0];
		IntFileUtils.printIntFile(intFile);
    }
}


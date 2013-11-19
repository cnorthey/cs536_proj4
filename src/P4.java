/*  This is the driver of the compilation process.
 *  The CSX Lite program is scanned and parsed, and an AST is built.
 *  The AST is then type checked.
 *   You'll need to update this to handle ASTs rooted by classNodes
 * 
 */
import java.io.*;

import java_cup.runtime.*;

public class P4 {

  public static void
  main(String args[]) throws java.io.IOException,  Exception {
	  
	  String fileName;
		fileName = ArgsProcessor.getArg(args);
		
	    java.io.FileInputStream yyin = null;
	    if (ArgsProcessor.openedFile != null) {
	    	// file chooser GUI already opened a file; create a 
	    	//  FileInputStream from it
	    	yyin = new FileInputStream(ArgsProcessor.openedFile);
	    } else { // open file using name provided
	    	try {
	    		yyin = new FileInputStream(fileName);  
	    	} catch (FileNotFoundException notFound){
	    		System.out.println ("Error: unable to open file: "+fileName);
	    		System.exit(-1);
	    	}
	    }

    Scanner.init(yyin); // Initialize Scanner class for parser

    parser csxParser = new parser(); 

    System.out.println ("\n\n" + "Begin CSX compilation of " +
			fileName + ".\n");
    Symbol root=null;
    try {
        root = csxParser.parse(); // do the parse
        System.out.println ("CSX program parsed correctly.");
	
    } catch (SyntaxErrorException e){  
        System.out.println ("Compilation terminated due to syntax errors.");
        System.exit(0);
    }

    boolean    ok;
    TypeChecking typeCheck = new TypeChecking();
    
    ok = typeCheck.isTypeCorrect((classNode) root.value);
    
    if (ok) 
            System.out.println("No CSX type errors detected.");
     else   System.out.println("\nCSX compilation halted due to type errors.");
	

    return;
    }
}
	

package helpers;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class bundles all the Space Invaders hex files into one big hex file
 * @author Shabbir
 *
 */
public class Bundler {
	
	public static void main(String args[]) throws IOException{
		
		String outFile ="SpaceInvaders.mc"; //space invades machine code
		PrintStream out = new PrintStream(outFile);
		
		String inFile1 = "invaders.h";
		String inFile2 = "invaders.g";
		String inFile3 = "invaders.f";
		String inFile4 = "invaders.e";
		
		AppendtoFile(inFile1,out);
		AppendtoFile(inFile2,out);
		AppendtoFile(inFile3,out);
		AppendtoFile(inFile4,out);
		
	}
	
	public static void AppendtoFile(String inuptFilename, PrintStream out) throws IOException{
		
		Path path = Paths.get(inuptFilename);
		byte[] data = Files.readAllBytes(path);
		
		out.write(data);
	}

}

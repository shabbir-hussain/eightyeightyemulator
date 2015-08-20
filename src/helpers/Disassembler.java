package helpers;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Disassembler {

	//Note some bytes are missing because they are set to data rather than opcode
	public static void main(String args[]) throws IOException{
		
		String outFile ="SpaceInvadersASM.txt";
		PrintStream out = new PrintStream(outFile);
		
		String inFile = "SpaceInvaders.mc";
		
		AppendtoFile(inFile,out);
		
	}
	
	public static void AppendtoFile(String inuptFilename, PrintStream out) throws IOException{
		
		Path path = Paths.get(inuptFilename);
		byte[] data = Files.readAllBytes(path);
		int [] idata = new int[data.length]; //data array casted to int
		
		//convert bytes to int for easy calculations
		for(int i=0;i<data.length;i++){
			idata[i] = data[i] &0xFF;
		}
				
		//disassembly loop
		for(int i=0;i<idata.length;){
			i+= Disassemble8080Op(idata,i,out);
			out.println();
		}
	}
	
	public static int Disassemble8080Op( int[] codebuffer, int pc,PrintStream out)
	{		
		int opbytes = 1;
		int[] code = codebuffer;
		out.printf("%04x ", pc);
		switch (codebuffer[pc])
		{
			case 0x00: out.printf("NOP"); break;
			case 0x01: out.printf("LXI    B,#$%02x%02x", code[pc+2], code[pc+1]); opbytes=3; break;
			case 0x02: out.printf("STAX   B"); break;
			case 0x03: out.printf("INX    B"); break;
			case 0x04: out.printf("INR    B"); break;
			case 0x05: out.printf("DCR    B"); break;
			case 0x06: out.printf("MVI    B,#$%02x", code[pc+1]); opbytes=2; break;
			case 0x07: out.printf("RLC"); break;
			case 0x08: out.printf("NOP"); break;
			case 0x09: out.printf("DAD    B"); break;
			case 0x0a: out.printf("LDAX   B"); break;
			case 0x0b: out.printf("DCX    B"); break;
			case 0x0c: out.printf("INR    C"); break;
			case 0x0d: out.printf("DCR    C"); break;
			case 0x0e: out.printf("MVI    C,#$%02x", code[pc+1]); opbytes = 2;	break;
			case 0x0f: out.printf("RRC"); break;
				
			case 0x10: out.printf("NOP"); break;
			case 0x11: out.printf("LXI    D,#$%02x%02x", code[pc+2], code[pc+1]); opbytes=3; break;
			case 0x12: out.printf("STAX   D"); break;
			case 0x13: out.printf("INX    D"); break;
			case 0x14: out.printf("INR    D"); break;
			case 0x15: out.printf("DCR    D"); break;
			case 0x16: out.printf("MVI    D,#$%02x", code[pc+1]); opbytes=2; break;
			case 0x17: out.printf("RAL"); break;
			case 0x18: out.printf("NOP"); break;
			case 0x19: out.printf("DAD    D"); break;
			case 0x1a: out.printf("LDAX   D"); break;
			case 0x1b: out.printf("DCX    D"); break;
			case 0x1c: out.printf("INR    E"); break;
			case 0x1d: out.printf("DCR    E"); break;
			case 0x1e: out.printf("MVI    E,#$%02x", code[pc+1]); opbytes = 2; break;
			case 0x1f: out.printf("RAR"); break;
				
			case 0x20: out.printf("NOP"); break;
			case 0x21: out.printf("LXI    H,#$%02x%02x", code[pc+2], code[pc+1]); opbytes=3; break;
			case 0x22: out.printf("SHLD   $%02x%02x", code[pc+2], code[pc+1]); opbytes=3; break;
			case 0x23: out.printf("INX    H"); break;
			case 0x24: out.printf("INR    H"); break;
			case 0x25: out.printf("DCR    H"); break;
			case 0x26: out.printf("MVI    H,#$%02x", code[pc+1]); opbytes=2; break;
			case 0x27: out.printf("DAA"); break;
			case 0x28: out.printf("NOP"); break;
			case 0x29: out.printf("DAD    H"); break;
			case 0x2a: out.printf("LHLD   $%02x%02x", code[pc+2], code[pc+1]); opbytes=3; break;
			case 0x2b: out.printf("DCX    H"); break;
			case 0x2c: out.printf("INR    L"); break;
			case 0x2d: out.printf("DCR    L"); break;
			case 0x2e: out.printf("MVI    L,#$%02x", code[pc+1]); opbytes = 2; break;
			case 0x2f: out.printf("CMA"); break;
				
			case 0x30: out.printf("NOP"); break;
			case 0x31: out.printf("LXI    SP,#$%02x%02x", code[pc+2], code[pc+1]); opbytes=3; break;
			case 0x32: out.printf("STA    $%02x%02x", code[pc+2], code[pc+1]); opbytes=3; break;
			case 0x33: out.printf("INX    SP"); break;
			case 0x34: out.printf("INR    M"); break;
			case 0x35: out.printf("DCR    M"); break;
			case 0x36: out.printf("MVI    M,#$%02x", code[pc+1]); opbytes=2; break;
			case 0x37: out.printf("STC"); break;
			case 0x38: out.printf("NOP"); break;
			case 0x39: out.printf("DAD    SP"); break;
			case 0x3a: out.printf("LDA    $%02x%02x", code[pc+2], code[pc+1]); opbytes=3; break;
			case 0x3b: out.printf("DCX    SP"); break;
			case 0x3c: out.printf("INR    A"); break;
			case 0x3d: out.printf("DCR    A"); break;
			case 0x3e: out.printf("MVI    A,#$%02x", code[pc+1]); opbytes = 2; break;
			case 0x3f: out.printf("CMC"); break;
				
			case 0x40: out.printf("MOV    B,B"); break;
			case 0x41: out.printf("MOV    B,C"); break;
			case 0x42: out.printf("MOV    B,D"); break;
			case 0x43: out.printf("MOV    B,E"); break;
			case 0x44: out.printf("MOV    B,H"); break;
			case 0x45: out.printf("MOV    B,L"); break;
			case 0x46: out.printf("MOV    B,M"); break;
			case 0x47: out.printf("MOV    B,A"); break;
			case 0x48: out.printf("MOV    C,B"); break;
			case 0x49: out.printf("MOV    C,C"); break;
			case 0x4a: out.printf("MOV    C,D"); break;
			case 0x4b: out.printf("MOV    C,E"); break;
			case 0x4c: out.printf("MOV    C,H"); break;
			case 0x4d: out.printf("MOV    C,L"); break;
			case 0x4e: out.printf("MOV    C,M"); break;
			case 0x4f: out.printf("MOV    C,A"); break;
				
			case 0x50: out.printf("MOV    D,B"); break;
			case 0x51: out.printf("MOV    D,C"); break;
			case 0x52: out.printf("MOV    D,D"); break;
			case 0x53: out.printf("MOV    D.E"); break;
			case 0x54: out.printf("MOV    D,H"); break;
			case 0x55: out.printf("MOV    D,L"); break;
			case 0x56: out.printf("MOV    D,M"); break;
			case 0x57: out.printf("MOV    D,A"); break;
			case 0x58: out.printf("MOV    E,B"); break;
			case 0x59: out.printf("MOV    E,C"); break;
			case 0x5a: out.printf("MOV    E,D"); break;
			case 0x5b: out.printf("MOV    E,E"); break;
			case 0x5c: out.printf("MOV    E,H"); break;
			case 0x5d: out.printf("MOV    E,L"); break;
			case 0x5e: out.printf("MOV    E,M"); break;
			case 0x5f: out.printf("MOV    E,A"); break;

			case 0x60: out.printf("MOV    H,B"); break;
			case 0x61: out.printf("MOV    H,C"); break;
			case 0x62: out.printf("MOV    H,D"); break;
			case 0x63: out.printf("MOV    H.E"); break;
			case 0x64: out.printf("MOV    H,H"); break;
			case 0x65: out.printf("MOV    H,L"); break;
			case 0x66: out.printf("MOV    H,M"); break;
			case 0x67: out.printf("MOV    H,A"); break;
			case 0x68: out.printf("MOV    L,B"); break;
			case 0x69: out.printf("MOV    L,C"); break;
			case 0x6a: out.printf("MOV    L,D"); break;
			case 0x6b: out.printf("MOV    L,E"); break;
			case 0x6c: out.printf("MOV    L,H"); break;
			case 0x6d: out.printf("MOV    L,L"); break;
			case 0x6e: out.printf("MOV    L,M"); break;
			case 0x6f: out.printf("MOV    L,A"); break;

			case 0x70: out.printf("MOV    M,B"); break;
			case 0x71: out.printf("MOV    M,C"); break;
			case 0x72: out.printf("MOV    M,D"); break;
			case 0x73: out.printf("MOV    M.E"); break;
			case 0x74: out.printf("MOV    M,H"); break;
			case 0x75: out.printf("MOV    M,L"); break;
			case 0x76: out.printf("HLT");        break;
			case 0x77: out.printf("MOV    M,A"); break;
			case 0x78: out.printf("MOV    A,B"); break;
			case 0x79: out.printf("MOV    A,C"); break;
			case 0x7a: out.printf("MOV    A,D"); break;
			case 0x7b: out.printf("MOV    A,E"); break;
			case 0x7c: out.printf("MOV    A,H"); break;
			case 0x7d: out.printf("MOV    A,L"); break;
			case 0x7e: out.printf("MOV    A,M"); break;
			case 0x7f: out.printf("MOV    A,A"); break;

			case 0x80: out.printf("ADD    B"); break;
			case 0x81: out.printf("ADD    C"); break;
			case 0x82: out.printf("ADD    D"); break;
			case 0x83: out.printf("ADD    E"); break;
			case 0x84: out.printf("ADD    H"); break;
			case 0x85: out.printf("ADD    L"); break;
			case 0x86: out.printf("ADD    M"); break;
			case 0x87: out.printf("ADD    A"); break;
			case 0x88: out.printf("ADC    B"); break;
			case 0x89: out.printf("ADC    C"); break;
			case 0x8a: out.printf("ADC    D"); break;
			case 0x8b: out.printf("ADC    E"); break;
			case 0x8c: out.printf("ADC    H"); break;
			case 0x8d: out.printf("ADC    L"); break;
			case 0x8e: out.printf("ADC    M"); break;
			case 0x8f: out.printf("ADC    A"); break;

			case 0x90: out.printf("SUB    B"); break;
			case 0x91: out.printf("SUB    C"); break;
			case 0x92: out.printf("SUB    D"); break;
			case 0x93: out.printf("SUB    E"); break;
			case 0x94: out.printf("SUB    H"); break;
			case 0x95: out.printf("SUB    L"); break;
			case 0x96: out.printf("SUB    M"); break;
			case 0x97: out.printf("SUB    A"); break;
			case 0x98: out.printf("SBB    B"); break;
			case 0x99: out.printf("SBB    C"); break;
			case 0x9a: out.printf("SBB    D"); break;
			case 0x9b: out.printf("SBB    E"); break;
			case 0x9c: out.printf("SBB    H"); break;
			case 0x9d: out.printf("SBB    L"); break;
			case 0x9e: out.printf("SBB    M"); break;
			case 0x9f: out.printf("SBB    A"); break;

			case 0xa0: out.printf("ANA    B"); break;
			case 0xa1: out.printf("ANA    C"); break;
			case 0xa2: out.printf("ANA    D"); break;
			case 0xa3: out.printf("ANA    E"); break;
			case 0xa4: out.printf("ANA    H"); break;
			case 0xa5: out.printf("ANA    L"); break;
			case 0xa6: out.printf("ANA    M"); break;
			case 0xa7: out.printf("ANA    A"); break;
			case 0xa8: out.printf("XRA    B"); break;
			case 0xa9: out.printf("XRA    C"); break;
			case 0xaa: out.printf("XRA    D"); break;
			case 0xab: out.printf("XRA    E"); break;
			case 0xac: out.printf("XRA    H"); break;
			case 0xad: out.printf("XRA    L"); break;
			case 0xae: out.printf("XRA    M"); break;
			case 0xaf: out.printf("XRA    A"); break;

			case 0xb0: out.printf("ORA    B"); break;
			case 0xb1: out.printf("ORA    C"); break;
			case 0xb2: out.printf("ORA    D"); break;
			case 0xb3: out.printf("ORA    E"); break;
			case 0xb4: out.printf("ORA    H"); break;
			case 0xb5: out.printf("ORA    L"); break;
			case 0xb6: out.printf("ORA    M"); break;
			case 0xb7: out.printf("ORA    A"); break;
			case 0xb8: out.printf("CMP    B"); break;
			case 0xb9: out.printf("CMP    C"); break;
			case 0xba: out.printf("CMP    D"); break;
			case 0xbb: out.printf("CMP    E"); break;
			case 0xbc: out.printf("CMP    H"); break;
			case 0xbd: out.printf("CMP    L"); break;
			case 0xbe: out.printf("CMP    M"); break;
			case 0xbf: out.printf("CMP    A"); break;

			case 0xc0: out.printf("RNZ"); break;
			case 0xc1: out.printf("POP    B"); break;
			case 0xc2: out.printf("JNZ    $%02x%02x",code[pc+2],code[pc+1]); opbytes = 3; break;
			case 0xc3: out.printf("JMP    $%02x%02x",code[pc+2],code[pc+1]); opbytes = 3; break;
			case 0xc4: out.printf("CNZ    $%02x%02x",code[pc+2],code[pc+1]); opbytes = 3; break;
			case 0xc5: out.printf("PUSH   B"); break;
			case 0xc6: out.printf("ADI    #$%02x",code[pc+1]); opbytes = 2; break;
			case 0xc7: out.printf("RST    0"); break;
			case 0xc8: out.printf("RZ"); break;
			case 0xc9: out.printf("RET"); break;
			case 0xca: out.printf("JZ     $%02x%02x",code[pc+2],code[pc+1]); opbytes = 3; break;
			case 0xcb: out.printf("JMP    $%02x%02x",code[pc+2],code[pc+1]); opbytes = 3; break;
			case 0xcc: out.printf("CZ     $%02x%02x",code[pc+2],code[pc+1]); opbytes = 3; break;
			case 0xcd: out.printf("CALL   $%02x%02x",code[pc+2],code[pc+1]); opbytes = 3; break;
			case 0xce: out.printf("ACI    #$%02x",code[pc+1]); opbytes = 2; break;
			case 0xcf: out.printf("RST    1"); break;

			case 0xd0: out.printf("RNC"); break;
			case 0xd1: out.printf("POP    D"); break;
			case 0xd2: out.printf("JNC    $%02x%02x",code[pc+2],code[pc+1]); opbytes = 3; break;
			case 0xd3: out.printf("OUT    #$%02x",code[pc+1]); opbytes = 2; break;
			case 0xd4: out.printf("CNC    $%02x%02x",code[pc+2],code[pc+1]); opbytes = 3; break;
			case 0xd5: out.printf("PUSH   D"); break;
			case 0xd6: out.printf("SUI    #$%02x",code[pc+1]); opbytes = 2; break;
			case 0xd7: out.printf("RST    2"); break;
			case 0xd8: out.printf("RC");  break;
			case 0xd9: out.printf("RET"); break;
			case 0xda: out.printf("JC     $%02x%02x",code[pc+2],code[pc+1]); opbytes = 3; break;
			case 0xdb: out.printf("IN     #$%02x",code[pc+1]); opbytes = 2; break;
			case 0xdc: out.printf("CC     $%02x%02x",code[pc+2],code[pc+1]); opbytes = 3; break;
			case 0xdd: out.printf("CALL   $%02x%02x",code[pc+2],code[pc+1]); opbytes = 3; break;
			case 0xde: out.printf("SBI    #$%02x",code[pc+1]); opbytes = 2; break;
			case 0xdf: out.printf("RST    3"); break;

			case 0xe0: out.printf("RPO"); break;
			case 0xe1: out.printf("POP    H"); break;
			case 0xe2: out.printf("JPO    $%02x%02x",code[pc+2],code[pc+1]); opbytes = 3; break;
			case 0xe3: out.printf("XTHL");break;
			case 0xe4: out.printf("CPO    $%02x%02x",code[pc+2],code[pc+1]); opbytes = 3; break;
			case 0xe5: out.printf("PUSH   H"); break;
			case 0xe6: out.printf("ANI    #$%02x",code[pc+1]); opbytes = 2; break;
			case 0xe7: out.printf("RST    4"); break;
			case 0xe8: out.printf("RPE"); break;
			case 0xe9: out.printf("PCHL");break;
			case 0xea: out.printf("JPE    $%02x%02x",code[pc+2],code[pc+1]); opbytes = 3; break;
			case 0xeb: out.printf("XCHG"); break;
			case 0xec: out.printf("CPE     $%02x%02x",code[pc+2],code[pc+1]); opbytes = 3; break;
			case 0xed: out.printf("CALL   $%02x%02x",code[pc+2],code[pc+1]); opbytes = 3; break;
			case 0xee: out.printf("XRI    #$%02x",code[pc+1]); opbytes = 2; break;
			case 0xef: out.printf("RST    5"); break;

			case 0xf0: out.printf("RP");  break;
			case 0xf1: out.printf("POP    PSW"); break;
			case 0xf2: out.printf("JP     $%02x%02x",code[pc+2],code[pc+1]); opbytes = 3; break;
			case 0xf3: out.printf("DI");  break;
			case 0xf4: out.printf("CP     $%02x%02x",code[pc+2],code[pc+1]); opbytes = 3; break;
			case 0xf5: out.printf("PUSH   PSW"); break;
			case 0xf6: out.printf("ORI    #$%02x",code[pc+1]); opbytes = 2; break;
			case 0xf7: out.printf("RST    6"); break;
			case 0xf8: out.printf("RM");  break;
			case 0xf9: out.printf("SPHL");break;
			case 0xfa: out.printf("JM     $%02x%02x",code[pc+2],code[pc+1]); opbytes = 3; break;
			case 0xfb: out.printf("EI");  break;
			case 0xfc: out.printf("CM     $%02x%02x",code[pc+2],code[pc+1]); opbytes = 3; break;
			case 0xfd: out.printf("CALL   $%02x%02x",code[pc+2],code[pc+1]); opbytes = 3; break;
			case 0xfe: out.printf("CPI    #$%02x",code[pc+1]); opbytes = 2; break;
			case 0xff: out.printf("RST    7"); break;
		}
		
		return opbytes;
	}
}

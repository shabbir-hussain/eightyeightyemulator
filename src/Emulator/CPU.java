package Emulator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class emulates the 8080 microprocessor
 * @author Shabbir
 *
 */
public class CPU {

	String codeFile = "SpaceInvaders.mc";

	//Registers
	int a;
	int b;
	int c;
	int d;
	int e;
	int h;
	int l;
	int sp;
	int pc;
	int int_enable;
	
	//flags
	boolean zero;
	boolean sign;
	boolean parity;
	boolean carry;
	boolean auxiliaryCarry;
		
	//main memory
	Memory mmry;

	public CPU() throws IOException{
		//init registers and memory
		mmry = new Memory();

		//load code to memory
		loadCodeToMmry();

		//goto execution loop
	}

	/**
	 * loads romfile to memory
	 * @throws IOException
	 */
	void loadCodeToMmry() throws IOException{
		Path path = Paths.get(codeFile);
		byte[] data = Files.readAllBytes(path);

		mmry.copyTo(data, 0);
	}
	
	/**
	 * checks for parity of byte
	 * @param x
	 * @param size
	 * @return
	 */
	boolean parity(int x, int size)
	{
		int i;
		int p = 0;
		x = (x & ((1<<size)-1));
		for (i=0; i<size; i++)
		{
			if ((x & 0x1)!=0) p++;
			x = x >> 1;
		}
		return (0 == (p & 0x1));
	}

	int ExecuteNextInstruction()
	{
		int cycles = 4;
		int opcode = mmry.read(pc); //get opCode


		switch (opcode)
		{
		case 0x00: break;	//NOP
		case 0x01: 			//LXI	B,word
			c = mmry.read(pc+1);
			b = mmry.read(pc+2);
			pc+=2; 

			break;
		case 0x02: throw new UnimplementedInstruction(); break;
		case 0x03: throw new UnimplementedInstruction(); break;
		case 0x04: throw new UnimplementedInstruction(); break;
		case 0x05: 			//DCR    B
		{
			int res = (b - 1);
			zero = (res == 0);
			sign = (0x80 == (res & 0x80));
			parity = parity(res, 8);
			b = res;
		}
		break;
		case 0x06: 							//MVI B,byte
			b = mmry.read(pc+1);
			pc++;
			break;
		case 0x07: throw new UnimplementedInstruction(); break;
		case 0x08: throw new UnimplementedInstruction(); break;
		case 0x09: 							//DAD B
		{
			int hl = (h << 8) | (l&0xFF);
			int bc = (b << 8) | (c&0xFF);
			int res = hl + bc;
			h = (res & 0xff00) >> 8;
			l = res & 0xff;
			carry = ((res & 0xffff0000) > 0);
		}
		break;
		case 0x0a: throw new UnimplementedInstruction(); break;
		case 0x0b: throw new UnimplementedInstruction(); break;
		case 0x0c: throw new UnimplementedInstruction(); break;
		case 0x0d: 							//DCR    C
		{
			int res = c - 1;
			zero = (res == 0);
			sign = (0x80 == (res & 0x80));
			parity = parity(res, 8);
			c = res;
		}
		break;
		case 0x0e: 							//MVI C,byte
			c = mmry.read(pc+1);
			pc++;
			break;
		case 0x0f: 							//RRC
		{
			//rotate accumulator right
			int x = (byte)(a&0xFF);
			a = ((x & 1) << 7) | (x >> 1);
			carry = (1 == (x&1));
		}
		break;
		case 0x10: throw new UnimplementedInstruction(); break;
		case 0x11: 							//LXI	D,word
			e = mmry.read(pc+ 1);
			d = mmry.read(pc+2);
			pc += 2;
			break;
		case 0x12: throw new UnimplementedInstruction(); break;
		case 0x13: 							//INX    D
			e++;
			if (e > 0xFF){
				d++;
				e =0;
			}
				break;		
		case 0x14: throw new UnimplementedInstruction(); break;
		case 0x15: throw new UnimplementedInstruction(); break;
		case 0x16: throw new UnimplementedInstruction(); break;
		case 0x17: throw new UnimplementedInstruction(); break;
		case 0x18: throw new UnimplementedInstruction(); break;
		case 0x19: 							//DAD    D
		{
			int hl = (h << 8) | (l&0xFF);
			int de = (d << 8) | (e&0xFF);
			int res = hl + de;
			h = (res & 0xff00) >> 8;
			l = res & 0xff;
			carry = ((res & 0xffff0000) != 0);
		}
		break;
		case 0x1a: 							//LDAX	D
		{
			//load value to accumulator
			a = mmry.read(mmry.BytesToShort(d, e));
		}
		break;
		case 0x1b: throw new UnimplementedInstruction(); break;
		case 0x1c: throw new UnimplementedInstruction(); break;
		case 0x1d: throw new UnimplementedInstruction(); break;
		case 0x1e: throw new UnimplementedInstruction(); break;
		case 0x1f: throw new UnimplementedInstruction(); break;
		case 0x20: throw new UnimplementedInstruction(); break;
		case 0x21: 							//LXI	H,word
			state->l = opcode[1];
			state->h = opcode[2];
			state->pc += 2;
			break;
		case 0x22: throw new UnimplementedInstruction(); break;
		case 0x23: 							//INX    H
			state->l++;
			if (state->l == 0)
				state->h++;
				break;		
		case 0x24: throw new UnimplementedInstruction(); break;
		case 0x25: throw new UnimplementedInstruction(); break;
		case 0x26:  							//MVI H,byte
			state->h = opcode[1];
			state->pc++;
			break;
		case 0x27: throw new UnimplementedInstruction(); break;
		case 0x28: throw new UnimplementedInstruction(); break;
		case 0x29: 								//DAD    H
		{
			uint32_t hl = (state->h << 8) | state->l;
			uint32_t res = hl + hl;
			state->h = (res & 0xff00) >> 8;
			state->l = res & 0xff;
			state->cc.cy = ((res & 0xffff0000) != 0);
		}
		break;
		case 0x2a: throw new UnimplementedInstruction(); break;
		case 0x2b: throw new UnimplementedInstruction(); break;
		case 0x2c: throw new UnimplementedInstruction(); break;
		case 0x2d: throw new UnimplementedInstruction(); break;
		case 0x2e: throw new UnimplementedInstruction(); break;
		case 0x2f: throw new UnimplementedInstruction(); break;
		case 0x30: throw new UnimplementedInstruction(); break;
		case 0x31: 							//LXI	SP,word
			state->sp = (opcode[2]<<8) | opcode[1];
			state->pc += 2;
			break;
		case 0x32: 							//STA    (word)
		{
			uint16_t offset = (opcode[2]<<8) | (opcode[1]);
			state->memory[offset] = state->a;
			state->pc += 2;
		}
		break;
		case 0x33: throw new UnimplementedInstruction(); break;
		case 0x34: throw new UnimplementedInstruction(); break;
		case 0x35: throw new UnimplementedInstruction(); break;
		case 0x36: 							//MVI	M,byte
		{					
			//AC set if lower nibble of h was zero prior to dec
			uint16_t offset = (state->h<<8) | state->l;
			state->memory[offset] = opcode[1];
			state->pc++;
		}
		break;
		case 0x37: throw new UnimplementedInstruction(); break;
		case 0x38: throw new UnimplementedInstruction(); break;
		case 0x39: throw new UnimplementedInstruction(); break;
		case 0x3a: 							//LDA    (word)
		{
			uint16_t offset = (opcode[2]<<8) | (opcode[1]);
			state->a = state->memory[offset];
			state->pc+=2;
		}
		break;
		case 0x3b: throw new UnimplementedInstruction(); break;
		case 0x3c: throw new UnimplementedInstruction(); break;
		case 0x3d: throw new UnimplementedInstruction(); break;
		case 0x3e: 							//MVI    A,byte
			state->a = opcode[1];
			state->pc++;
			break;
		case 0x3f: throw new UnimplementedInstruction(); break;
		case 0x40: throw new UnimplementedInstruction(); break;
		case 0x41: throw new UnimplementedInstruction(); break;
		case 0x42: throw new UnimplementedInstruction(); break;
		case 0x43: throw new UnimplementedInstruction(); break;
		case 0x44: throw new UnimplementedInstruction(); break;
		case 0x45: throw new UnimplementedInstruction(); break;
		case 0x46: throw new UnimplementedInstruction(); break;
		case 0x47: throw new UnimplementedInstruction(); break;
		case 0x48: throw new UnimplementedInstruction(); break;
		case 0x49: throw new UnimplementedInstruction(); break;
		case 0x4a: throw new UnimplementedInstruction(); break;
		case 0x4b: throw new UnimplementedInstruction(); break;
		case 0x4c: throw new UnimplementedInstruction(); break;
		case 0x4d: throw new UnimplementedInstruction(); break;
		case 0x4e: throw new UnimplementedInstruction(); break;
		case 0x4f: throw new UnimplementedInstruction(); break;
		case 0x50: throw new UnimplementedInstruction(); break;
		case 0x51: throw new UnimplementedInstruction(); break;
		case 0x52: throw new UnimplementedInstruction(); break;
		case 0x53: throw new UnimplementedInstruction(); break;
		case 0x54: throw new UnimplementedInstruction(); break;
		case 0x55: throw new UnimplementedInstruction(); break;
		case 0x56: 							//MOV D,M
		{
			uint16_t offset = (state->h<<8) | (state->l);
			state->d = state->memory[offset];
		}
		break;
		case 0x57: throw new UnimplementedInstruction(); break;
		case 0x58: throw new UnimplementedInstruction(); break;
		case 0x59: throw new UnimplementedInstruction(); break;
		case 0x5a: throw new UnimplementedInstruction(); break;
		case 0x5b: throw new UnimplementedInstruction(); break;
		case 0x5c: throw new UnimplementedInstruction(); break;
		case 0x5d: throw new UnimplementedInstruction(); break;
		case 0x5e: 							//MOV E,M
		{
			uint16_t offset = (state->h<<8) | (state->l);
			state->e = state->memory[offset];
		}
		break;
		case 0x5f: throw new UnimplementedInstruction(); break;
		case 0x60: throw new UnimplementedInstruction(); break;
		case 0x61: throw new UnimplementedInstruction(); break;
		case 0x62: throw new UnimplementedInstruction(); break;
		case 0x63: throw new UnimplementedInstruction(); break;
		case 0x64: throw new UnimplementedInstruction(); break;
		case 0x65: throw new UnimplementedInstruction(); break;
		case 0x66: 							//MOV H,M
		{
			uint16_t offset = (state->h<<8) | (state->l);
			state->h = state->memory[offset];
		}
		break;
		case 0x67: throw new UnimplementedInstruction(); break;
		case 0x68: throw new UnimplementedInstruction(); break;
		case 0x69: throw new UnimplementedInstruction(); break;
		case 0x6a: throw new UnimplementedInstruction(); break;
		case 0x6b: throw new UnimplementedInstruction(); break;
		case 0x6c: throw new UnimplementedInstruction(); break;
		case 0x6d: throw new UnimplementedInstruction(); break;
		case 0x6e: throw new UnimplementedInstruction(); break;
		case 0x6f: state->l = state->a; break; //MOV L,A
		case 0x70: throw new UnimplementedInstruction(); break;
		case 0x71: throw new UnimplementedInstruction(); break;
		case 0x72: throw new UnimplementedInstruction(); break;
		case 0x73: throw new UnimplementedInstruction(); break;
		case 0x74: throw new UnimplementedInstruction(); break;
		case 0x75: throw new UnimplementedInstruction(); break;
		case 0x76: throw new UnimplementedInstruction(); break;
		case 0x77: 							//MOV    M,A
		{
			uint16_t offset = (state->h<<8) | (state->l);
			state->memory[offset] = state->a;
		}
		break;
		case 0x78: throw new UnimplementedInstruction(); break;
		case 0x79: throw new UnimplementedInstruction(); break;
		case 0x7a: state->a  = state->d;  break;	//MOV D,A
		case 0x7b: state->a  = state->e;  break;	//MOV E,A
		case 0x7c: state->a  = state->h;  break;	//MOV H,A
		case 0x7d: throw new UnimplementedInstruction(); break;
		case 0x7e: 							//MOV A,M
		{
			uint16_t offset = (state->h<<8) | (state->l);
			state->a = state->memory[offset];
		}
		break;
		case 0x7f: throw new UnimplementedInstruction(); break;
		case 0x80: throw new UnimplementedInstruction(); break;
		case 0x81: throw new UnimplementedInstruction(); break;
		case 0x82: throw new UnimplementedInstruction(); break;
		case 0x83: throw new UnimplementedInstruction(); break;
		case 0x84: throw new UnimplementedInstruction(); break;
		case 0x85: throw new UnimplementedInstruction(); break;
		case 0x86: throw new UnimplementedInstruction(); break;
		case 0x87: throw new UnimplementedInstruction(); break;
		case 0x88: throw new UnimplementedInstruction(); break;
		case 0x89: throw new UnimplementedInstruction(); break;
		case 0x8a: throw new UnimplementedInstruction(); break;
		case 0x8b: throw new UnimplementedInstruction(); break;
		case 0x8c: throw new UnimplementedInstruction(); break;
		case 0x8d: throw new UnimplementedInstruction(); break;
		case 0x8e: throw new UnimplementedInstruction(); break;
		case 0x8f: throw new UnimplementedInstruction(); break;
		case 0x90: throw new UnimplementedInstruction(); break;
		case 0x91: throw new UnimplementedInstruction(); break;
		case 0x92: throw new UnimplementedInstruction(); break;
		case 0x93: throw new UnimplementedInstruction(); break;
		case 0x94: throw new UnimplementedInstruction(); break;
		case 0x95: throw new UnimplementedInstruction(); break;
		case 0x96: throw new UnimplementedInstruction(); break;
		case 0x97: throw new UnimplementedInstruction(); break;
		case 0x98: throw new UnimplementedInstruction(); break;
		case 0x99: throw new UnimplementedInstruction(); break;
		case 0x9a: throw new UnimplementedInstruction(); break;
		case 0x9b: throw new UnimplementedInstruction(); break;
		case 0x9c: throw new UnimplementedInstruction(); break;
		case 0x9d: throw new UnimplementedInstruction(); break;
		case 0x9e: throw new UnimplementedInstruction(); break;
		case 0x9f: throw new UnimplementedInstruction(); break;
		case 0xa0: throw new UnimplementedInstruction(); break;
		case 0xa1: throw new UnimplementedInstruction(); break;
		case 0xa2: throw new UnimplementedInstruction(); break;
		case 0xa3: throw new UnimplementedInstruction(); break;
		case 0xa4: throw new UnimplementedInstruction(); break;
		case 0xa5: throw new UnimplementedInstruction(); break;
		case 0xa6: throw new UnimplementedInstruction(); break;
		case 0xa7: state->a = state->a & state->a; LogicFlagsA(state);	break; //ANA A
		case 0xa8: throw new UnimplementedInstruction(); break;
		case 0xa9: throw new UnimplementedInstruction(); break;
		case 0xaa: throw new UnimplementedInstruction(); break;
		case 0xab: throw new UnimplementedInstruction(); break;
		case 0xac: throw new UnimplementedInstruction(); break;
		case 0xad: throw new UnimplementedInstruction(); break;
		case 0xae: throw new UnimplementedInstruction(); break;
		case 0xaf: state->a = state->a ^ state->a; LogicFlagsA(state);	break; //XRA A
		case 0xb0: throw new UnimplementedInstruction(); break;
		case 0xb1: throw new UnimplementedInstruction(); break;
		case 0xb2: throw new UnimplementedInstruction(); break;
		case 0xb3: throw new UnimplementedInstruction(); break;
		case 0xb4: throw new UnimplementedInstruction(); break;
		case 0xb5: throw new UnimplementedInstruction(); break;
		case 0xb6: throw new UnimplementedInstruction(); break;
		case 0xb7: throw new UnimplementedInstruction(); break;
		case 0xb8: throw new UnimplementedInstruction(); break;
		case 0xb9: throw new UnimplementedInstruction(); break;
		case 0xba: throw new UnimplementedInstruction(); break;
		case 0xbb: throw new UnimplementedInstruction(); break;
		case 0xbc: throw new UnimplementedInstruction(); break;
		case 0xbd: throw new UnimplementedInstruction(); break;
		case 0xbe: throw new UnimplementedInstruction(); break;
		case 0xbf: throw new UnimplementedInstruction(); break;
		case 0xc0: throw new UnimplementedInstruction(); break;
		case 0xc1: 						//POP    B
		{
			state->c = state->memory[state->sp];
			state->b = state->memory[state->sp+1];
			state->sp += 2;
		}
		break;
		case 0xc2: 						//JNZ address
			if (0 == state->cc.z)
				state->pc = (opcode[2] << 8) | opcode[1];
				else
					state->pc += 2;
					break;
		case 0xc3:						//JMP address
			state->pc = (opcode[2] << 8) | opcode[1];
			break;
		case 0xc4: throw new UnimplementedInstruction(); break;
		case 0xc5: 						//PUSH   B
		{
			state->memory[state->sp-1] = state->b;
			state->memory[state->sp-2] = state->c;
			state->sp = state->sp - 2;
		}
		break;
		case 0xc6: 						//ADI    byte
		{
			uint16_t x = (uint16_t) state->a + (uint16_t) opcode[1];
			state->cc.z = ((x & 0xff) == 0);
			state->cc.s = (0x80 == (x & 0x80));
			state->cc.p = parity((x&0xff), 8);
			state->cc.cy = (x > 0xff);
			state->a = (uint8_t) x;
			state->pc++;
		}
		break;
		case 0xc7: throw new UnimplementedInstruction(); break;
		case 0xc8: throw new UnimplementedInstruction(); break;
		case 0xc9: 						//RET
			state->pc = state->memory[state->sp] | (state->memory[state->sp+1] << 8);
			state->sp += 2;
			break;
		case 0xca: throw new UnimplementedInstruction(); break;
		case 0xcb: throw new UnimplementedInstruction(); break;
		case 0xcc: throw new UnimplementedInstruction(); break;
		case 0xcd: 						//CALL adr
		{
			uint16_t	ret = state->pc+2;
			state->memory[state->sp-1] = (ret >> 8) & 0xff;
			state->memory[state->sp-2] = (ret & 0xff);
			state->sp = state->sp - 2;
			state->pc = (opcode[2] << 8) | opcode[1];
		}
		break;
		case 0xce: throw new UnimplementedInstruction(); break;
		case 0xcf: throw new UnimplementedInstruction(); break;
		case 0xd0: throw new UnimplementedInstruction(); break;
		case 0xd1: 						//POP    D
		{
			state->e = state->memory[state->sp];
			state->d = state->memory[state->sp+1];
			state->sp += 2;
		}
		break;
		case 0xd2: throw new UnimplementedInstruction(); break;
		case 0xd3: 
			//Don't know what to do here (yet)
			state->pc++;
			break;
		case 0xd4: throw new UnimplementedInstruction(); break;
		case 0xd5: 						//PUSH   D
		{
			state->memory[state->sp-1] = state->d;
			state->memory[state->sp-2] = state->e;
			state->sp = state->sp - 2;
		}
		break;
		case 0xd6: throw new UnimplementedInstruction(); break;
		case 0xd7: throw new UnimplementedInstruction(); break;
		case 0xd8: throw new UnimplementedInstruction(); break;
		case 0xd9: throw new UnimplementedInstruction(); break;
		case 0xda: throw new UnimplementedInstruction(); break;
		case 0xdb: throw new UnimplementedInstruction(); break;
		case 0xdc: throw new UnimplementedInstruction(); break;
		case 0xdd: throw new UnimplementedInstruction(); break;
		case 0xde: throw new UnimplementedInstruction(); break;
		case 0xdf: throw new UnimplementedInstruction(); break;
		case 0xe0: throw new UnimplementedInstruction(); break;
		case 0xe1: 					//POP    H
		{
			state->l = state->memory[state->sp];
			state->h = state->memory[state->sp+1];
			state->sp += 2;
		}
		break;
		case 0xe2: throw new UnimplementedInstruction(); break;
		case 0xe3: throw new UnimplementedInstruction(); break;
		case 0xe4: throw new UnimplementedInstruction(); break;
		case 0xe5: 						//PUSH   H
		{
			state->memory[state->sp-1] = state->h;
			state->memory[state->sp-2] = state->l;
			state->sp = state->sp - 2;
		}
		break;
		case 0xe6: 						//ANI    byte
		{
			state->a = state->a & opcode[1];
			LogicFlagsA(state);
			state->pc++;
		}
		break;
		case 0xe7: throw new UnimplementedInstruction(); break;
		case 0xe8: throw new UnimplementedInstruction(); break;
		case 0xe9: throw new UnimplementedInstruction(); break;
		case 0xea: throw new UnimplementedInstruction(); break;
		case 0xeb: 					//XCHG
		{
			uint8_t save1 = state->d;
			uint8_t save2 = state->e;
			state->d = state->h;
			state->e = state->l;
			state->h = save1;
			state->l = save2;
		}
		break;
		case 0xec: throw new UnimplementedInstruction(); break;
		case 0xed: throw new UnimplementedInstruction(); break;
		case 0xee: throw new UnimplementedInstruction(); break;
		case 0xef: throw new UnimplementedInstruction(); break;
		case 0xf0: throw new UnimplementedInstruction(); break;
		case 0xf1: 					//POP PSW
		{
			state->a = state->memory[state->sp+1];
			uint8_t psw = state->memory[state->sp];
			state->cc.z  = (0x01 == (psw & 0x01));
			state->cc.s  = (0x02 == (psw & 0x02));
			state->cc.p  = (0x04 == (psw & 0x04));
			state->cc.cy = (0x05 == (psw & 0x08));
			state->cc.ac = (0x10 == (psw & 0x10));
			state->sp += 2;
		}
		break;
		case 0xf2: throw new UnimplementedInstruction(); break;
		case 0xf3: throw new UnimplementedInstruction(); break;
		case 0xf4: throw new UnimplementedInstruction(); break;
		case 0xf5: 						//PUSH   PSW
		{
			state->memory[state->sp-1] = state->a;
			uint8_t psw = (state->cc.z |
					state->cc.s << 1 |
					state->cc.p << 2 |
					state->cc.cy << 3 |
					state->cc.ac << 4 );
			state->memory[state->sp-2] = psw;
			state->sp = state->sp - 2;
		}
		break;
		case 0xf6: throw new UnimplementedInstruction(); break;
		case 0xf7: throw new UnimplementedInstruction(); break;
		case 0xf8: throw new UnimplementedInstruction(); break;
		case 0xf9: throw new UnimplementedInstruction(); break;
		case 0xfa: throw new UnimplementedInstruction(); break;
		case 0xfb: state->int_enable = 1;  break;	//EI
		case 0xfc: throw new UnimplementedInstruction(); break;
		case 0xfd: throw new UnimplementedInstruction(); break;
		case 0xfe: 						//CPI  byte
		{
			uint8_t x = state->a - opcode[1];
			state->cc.z = (x == 0);
			state->cc.s = (0x80 == (x & 0x80));
			state->cc.p = parity(x, 8);
			state->cc.cy = (state->a < opcode[1]);
			state->pc++;
		}
		break;
		case 0xff: throw new UnimplementedInstruction(); break;
		}


		pc++; //increment program Counter

		printf("\t");
		printf("%c", state->cc.z ? 'z' : '.');
		printf("%c", state->cc.s ? 's' : '.');
		printf("%c", state->cc.p ? 'p' : '.');
		printf("%c", state->cc.cy ? 'c' : '.');
		printf("%c  ", state->cc.ac ? 'a' : '.');
		printf("A $%02x B $%02x C $%02x D $%02x E $%02x H $%02x L $%02x SP %04x\n", state->a, state->b, state->c,
				state->d, state->e, state->h, state->l, state->sp);
		return 0;
	}

}

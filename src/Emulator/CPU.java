package Emulator;


import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Dictionary;
import java.util.HashMap;

/**
 * This class emulates the 8080 microprocessor
 * @author Shabbir
 *
 * notes: next step is to make all instructions generic
 */
public class CPU {

	String codeFile = "SpaceInvaders.mc";

	//used to make all functions generic
	public enum Register{
		a,b,c,d,e,h,l,sp,pc,int_enable,int_code
	}
	HashMap<Register,Integer> registers;
	
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
	int int_code;
	
	int port1 = 0;
	int port2i = 0;
	int port2o = 0;
	int port3o = 0;
	int port4lo = 0;
	int port4h = 0;
	int port5o = 0;

	//flags
	boolean zero;
	boolean sign;
	boolean parity;
	boolean carry;
	boolean auxiliaryCarry;

	//main memory
	public Memory mmry;

	public CPU() throws IOException{
		//init registers and memory
		mmry = new Memory();

		//load code to memory
		loadCodeToMmry();
		
		//set sp
		sp = 0xf000;
		
		//init registers;
		
		registers = new HashMap<Register,Integer>();
		for(Register r:Register.values()){
			writeToRegister(r,0);
		}
		writeToRegister(Register.sp, 0xf000);
	}
	
	/**
	 * Writes to the register dictionary consistently
	 * @param r
	 * @param value
	 */
	void writeToRegister(Register r, int value){
		registers.put(r, truncateTo8Bit(value));
	}
	
	int truncateTo8Bit(int value){
		return value&0xff;
	}
	
	/**
	 * Returns int value from register
	 * @param r
	 * @return
	 */
	int readFromRegister(Register r){
		return registers.get(r).intValue();
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
	 * True means even parity
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

	void LogicFlagsA()
	{
		carry = false;
		auxiliaryCarry = false;
		zero = (a == 0);
		sign = (0x80 == (a & 0x80));
		parity = parity(a, 8);
	}

	void ArithFlags(int res)
	{
		carry = (res > 0xff);
		ArithFlagsWithoutCarry(res);
	}
	
	void ArithFlagsWithoutCarry(int res)
	{
		zero = ((res&0xff) == 0);
		sign = (0x80 == (res & 0x80));
		parity = parity(res&0xff, 8);
		auxiliaryCarry = 0x10 == (res & 0x10);
	}
	
	///////////////////////////////////////INSTRUCTION DEFINNITIONS///////////////////////////////////////
	/**
	 * moves from var2 to var1
	 * @param var1 dest
	 * @param var2 src
	 */
	void MOV(Register var1, Register var2){
		this.writeToRegister(var1, (this.readFromRegister(var2)&0xFF));
	}
	
	/**
	 * Adds to the accumulator
	 * @param r
	 */
	void ADD(Register r){
		int value = this.readFromRegister(r);
		int sum = value + this.readFromRegister(Register.a);
		this.writeToRegister(Register.a, sum);
		ArithFlags(sum);
	}
	void ADD(int val){
		int value = val;
		int sum = value + this.readFromRegister(Register.a);
		this.writeToRegister(Register.a, sum);
		ArithFlags(sum);
	}
	
	/**
	 * Adds to the accumulator with carry
	 * @param r
	 */
	void ADC(Register r){
		int value = this.readFromRegister(r);
		int sum = value + this.readFromRegister(Register.a);
		sum += carry ? 1 : 0;
		this.writeToRegister(Register.a, sum);
		ArithFlags(sum);
	}
	
	/**
	 * Subtracts from the accumulator
	 * @param r
	 */
	void SUB(Register r){
		int value = this.readFromRegister(r);
		int diff = this.readFromRegister(Register.a) - value;
		this.writeToRegister(Register.a, diff);
		LogicFlagsA();
	}
	void SUB(int val){
		int value = val;
		int diff = this.readFromRegister(Register.a) - value;
		this.writeToRegister(Register.a, diff);
		LogicFlagsA();
	}
	
	/**
	 * Subtracts from the accumulator with carry
	 * @param r
	 */
	void SBB(Register r ){
		int value = this.readFromRegister(r);
		int diff = this.readFromRegister(Register.a) - value;
		diff -= carry ? 1 : 0;
		this.writeToRegister(Register.a, diff);
		LogicFlagsA();
	}
	
	/**
	 * And with the accumulator
	 * @param r
	 */
	void ANA(Register r){
		int value = this.readFromRegister(r);
		int and = this.readFromRegister(Register.a) & value;
		this.writeToRegister(Register.a, and);
		ArithFlags(and);
	}
	void ANA(int val){
		int value = val;
		int and = this.readFromRegister(Register.a) & value;
		this.writeToRegister(Register.a, and);
		ArithFlags(and);
	}
	
	/**
	 * Or with the accumulator
	 * @param r
	 */
	void ORA(Register r){
		int value = this.readFromRegister(r);
		int or = this.readFromRegister(Register.a) | value;
		this.writeToRegister(Register.a, or);
		ArithFlags(or);
	}
	void ORA(int val){
		int value = val;
		int or = this.readFromRegister(Register.a) | value;
		this.writeToRegister(Register.a, or);
		ArithFlags(or);
	}
	
	/**
	 * Cmp with the accumulator
	 * @param r
	 */
	void CMP(Register r){
		int value = this.readFromRegister(r);
		int diff = this.readFromRegister(Register.a) - value;
		LogicFlagsA();
	}
	
	/** 
	 * 16 bit load operation
	 * @param rmsb
	 * @param rlsb
	 * @param vmsb
	 * @param vlsb
	 */
	void LXI(Register rmsb, Register rlsb, int vmsb, int vlsb){
		writeToRegister(rmsb,vmsb);
		writeToRegister(rlsb,vlsb);
	}
	
	/**
	 * Stores the value of the accumulator to memory
	 * @param rmsb
	 * @param rlsb
	 */
	void STAX(Register rmsb, Register rlsb){
		int vmsb = this.readFromRegister(rmsb);
		int vlsb = this.readFromRegister(rlsb);
		int value = this.readFromRegister(Register.a);
		WriteToMemoryLocation(vmsb,vlsb,value);
	}
	

	/**
	 * Writes Register H and L to memory. this is a 3 byte operation
	 * @param msb of memory offset
	 * @param lsb of memory offset
	 */
	void SHLD(int msb, int lsb){
		int offset = To16Bit(msb,lsb);
		this.mmry.write(offset, this.readFromRegister(Register.l));
		offset++;
		this.mmry.write(offset, this.readFromRegister(Register.h));
	}
	
	/**
	 * 16 increment
	 * @param rmsb
	 * @param rlsb
	 */
	void INX(Register rmsb, Register rlsb){
		int value = this.To16Bit(this.readFromRegister(rmsb), this.readFromRegister(rlsb));
		value++;
		this.writeToRegister(rmsb, this.GetMSB(value));
		this.writeToRegister(rlsb, this.GetLSB(value));
	}
	
	/**
	 * 8 bit increment
	 * @param r
	 */
	void INR(Register r){
		int value = this.readFromRegister(r);
		value++;
		this.writeToRegister(r, GetLSB(value));
		ArithFlagsWithoutCarry(value);
	}
	/**
	 * increment an 8bit value in memory
	 */
	void INRM(){
		int vlsb = this.readFromRegister(Register.l);
		int vmsb = this.readFromRegister(Register.h);
		
		int value = GetMemLocation(vmsb,vlsb);
		value++;
		WriteToMemoryLocation(vmsb,vlsb,GetLSB(value));
		ArithFlagsWithoutCarry(value);
	}
	/**
	 * Decrement a register
	 * @param r
	 */
	void DCR(Register r){
		int value = this.readFromRegister(r);
		value--;
		this.writeToRegister(r, GetLSB(value));
		ArithFlagsWithoutCarry(value);
	}
	/**
	 * decrement a 8bit value in memory
	 */
	void DCRM(){
		int vlsb = this.readFromRegister(Register.l);
		int vmsb = this.readFromRegister(Register.h);
		
		int value = GetMemLocation(vmsb,vlsb);
		value--;
		WriteToMemoryLocation(vmsb,vlsb,GetLSB(value));
		ArithFlagsWithoutCarry(value);
	}
	
	/**
	 * Move immediate into register
	 * @param r
	 * @param value
	 */
	void MVI(Register r, int value){
		this.writeToRegister(r, value);
	}
	/**
	 * Move immediate into memory
	 * @param value
	 */
	void MVIM(int value){
		int vlsb = this.readFromRegister(Register.l);
		int vmsb = this.readFromRegister(Register.h);
		
		WriteToMemoryLocation(vmsb,vlsb,GetLSB(value));
	}
	
	/**
	 * Rotate Accumulator left
	 */
	void RLC(){
		int value = this.readFromRegister(Register.a);
		carry = (value & 0x80) == 0x80 ?  true: false;
		
		value = value << 1; //shift left
		value = value |0x01; //set incoming bit
		
		this.writeToRegister(Register.a, GetLSB(value));
	}
	/**
	 * Rotate Accumulator right
	 */
	void RRC(){
		int value = this.readFromRegister(Register.a);
		
		carry = value %2 == 1 ?  true: false;		
		value = value >> 1; //shift right
		value = value &0x7F; //set incoming bit to 0
		
		this.writeToRegister(Register.a, GetLSB(value));
	}
	
	/**
	 * Rotate Accumulator Left Through Carry
	 */
	void RAL(){
		boolean leading = carry;
		int value = this.readFromRegister(Register.a);
		carry = (value & 0x80) == 0x80 ?  true: false;
		
		value = value << 1; //shift left
		if(leading){//set incoming bit to 1 or 0
			value = value | 0x01;
		}else{
			value = value & 0xFE;
		}
		
		this.writeToRegister(Register.a, GetLSB(value));
	}
	
	/**
	 * Rotate Accumulator Right	 Through Carry
	 */
	void RAR(){
		boolean leading = carry;
		int value = this.readFromRegister(Register.a);
		
		carry = value %2 == 1 ?  true: false;		
		value = value >> 1; //shift right
		if(leading){ //set incoming bit to 1 or 0
			value = value | 0x80;
		}else{

			value = value &0x7F; 
		}
		
		this.writeToRegister(Register.a, GetLSB(value));
	}
	
	/**
	 * special decimal adjust instruction
	 */
	void DAA(){
		int registerA = this.readFromRegister(Register.a);
		int lsb = registerA & 0x0F;
		if(lsb>9 || this.auxiliaryCarry){
			registerA+=6;
			this.ArithFlags(registerA);
		}
		
		int msb = registerA & 0xF0;
		if(msb>0x90 || this.carry){
			registerA+= 0x60;
			this.ArithFlags(registerA);
		}		
		this.writeToRegister(Register.a, registerA);		
	}
	
	/**
	 * set carry to true
	 */
	void STC(){
		this.carry = true;
	}
	
	/**
	 * Decimal add
	 * @param rmsb
	 * @param rlsb
	 */
	void DAD(Register rmsb, Register rlsb){
		int value = this.To16Bit(this.readFromRegister(rmsb), this.readFromRegister(rlsb));
		int hl = this.To16Bit(this.readFromRegister(Register.h), this.readFromRegister(Register.l));
		int sum = value+hl;
		
		this.writeToRegister(Register.h, GetMSB(sum));
		this.writeToRegister(Register.l, GetLSB(sum));
		
		this.carry=false;
	}
	
	/**
	 * Load a value to A from memory
	 * @param rmsb
	 * @param rlsb
	 */
	void LDAX(Register rmsb, Register rlsb){
		int vmsb = this.readFromRegister(rmsb);
		int vlsb = this.readFromRegister(rlsb);
		int value = GetMemLocation(vmsb,vlsb);
		
		this.writeToRegister(Register.a, value);
	}
	
	/**
	 * Load to H and L direct
	 * @param msb
	 * @param lsb
	 */
	void LHLD(int msb, int lsb){
		int offset = To16Bit(msb,lsb);
		int lValue = this.mmry.read(offset);
		offset++;
		int hValue = this.mmry.read(offset);
		
		this.writeToRegister(Register.l, lValue);
		this.writeToRegister(Register.h, hValue);
	}
	
	/**
	 * 16 decrement
	 * @param rmsb
	 * @param rlsb
	 */
	void DCX(Register rmsb, Register rlsb){
		int value = this.To16Bit(this.readFromRegister(rmsb), this.readFromRegister(rlsb));
		value--;
		this.writeToRegister(rmsb, this.GetMSB(value));
		this.writeToRegister(rlsb, this.GetLSB(value));
	}
	
	/**
	 * One'a complement of Register A
	 */
	void CMA(){
		int value = this.readFromRegister(Register.a);
		value = ~value;
		this.writeToRegister(Register.a, value);
	}
	
	/**
	 * Complement Carry
	 */
	void CMC(){
		carry = carry ? false:true;
	}
	
	/**
	 * 
	 * @throws InterruptedException
	 */
	void HLT() throws InterruptedException{
		//TODO: lookup thread waiting
		//this.wait();
	}
	
	/**
	 * Return from call
	 */
	void RET(){
		this.writeToRegister(Register.pc, (mmry.read(this.readFromRegister(Register.sp))&0xFF) | (mmry.read(this.readFromRegister(Register.sp)+1) << 8));
		this.writeToRegister(Register.sp, this.readFromRegister(Register.sp)+2);
	}
	
	/**
	 * Return not zero
	 */
	void RNZ(){
		if(!this.zero){
			RET();
		}
	}
	
	/**
	 * Return if zero
	 */
	void RZ(){
		if(this.zero){
			RET();
		}
	}
	
	/**
	 * Return if Carry
	 */
	void RC(){
		if(this.carry){
			RET();
		}
	}
	
	/**
	 * Return not Carry
	 */
	void RNC(){
		if(!this.carry){
			RET();
		}
	}
	
	/**
	 * Return if minus (negative)
	 */
	void RM(){
		if(this.sign){
			RET();
		}
	}
	
	/**
	 * Return if positive
	 */
	void RP(){
		if(!this.sign){
			RET();
		}
	}
	
	/**
	 * Return if parity even
	 */
	void RPE(){
		if(this.parity){
			RET();
		}
	}
	
	void RPO(){
		if(!this.parity){
			RET();
		}
	}
	
	/**
	 * Pop from stack 
	 * @param rmsb
	 * @param rlsb
	 */
	void POP(Register rmsb, Register rlsb){
		int spValue = mmry.read(this.readFromRegister(Register.sp));
		this.writeToRegister(rlsb,mmry.read(spValue));
		this.writeToRegister(rmsb,mmry.read(spValue+1));
		
		//move stack pointer
		this.writeToRegister(Register.sp, spValue+2);
	}
	
	/**
	 * Push to Stack
	 * @param rmsb
	 * @param rlsb
	 */
	void PUSH(Register rmsb, Register rlsb){
		int spValue = mmry.read(this.readFromRegister(Register.sp));
		mmry.write(spValue-1,this.readFromRegister(rmsb));
		mmry.write(spValue-2, this.readFromRegister(rlsb));
		
		//move stack pointer
		this.writeToRegister(Register.sp, spValue-2);
	}
	
	/**
	 * Special Case of Pop
	 * Pop values into accumulator and flags
	 * flags are encoded into a byte as follows:
	 * s|z|0|ac|0|p|1|c
	 */
	void POPPSW(){
		int spVal = this.readFromRegister(Register.sp);
		this.writeToRegister(Register.a, mmry.read(spVal+1));
		
		int psw = mmry.read(spVal);
		carry = (0x01 == (psw & 0x01));
		parity  = (0x04 == (psw & 0x04));
		auxiliaryCarry = (0x10 == (psw & 0x10));
		zero  = (0x40 == (psw & 0x40));
		sign  = (0x80 == (psw & 0x80));
		
		this.writeToRegister(Register.sp, spVal+2);
	}
	
	/**
	 * Special Case of PUSH
	 * Push values from accumulator and flags
	 * flags are encoded into a byte as follows:
	 * s|z|0|ac|0|p|1|c
	 */
	void PUSHPSW(){
		int spVal = this.readFromRegister(Register.sp);
		mmry.write(spVal-1, this.readFromRegister(Register.a));
		
		int psw =  2;
		psw |= carry ? 0x01:0x00;
		psw |= parity ? 0x04:0x00;
		psw |= auxiliaryCarry ? 0x10:0x00;		
		psw |= zero ? 0x40:0x00;		
		psw |= sign ? 0x80:0x00;
		
		mmry.write(spVal-2, psw);
		
		this.writeToRegister(Register.sp, spVal-2);
	}
	
	/**
	 * Jump to address 
	 */
	void JMP(){
		int pcVal = this.readFromRegister(Register.pc);
		
		int pcJmp = (mmry.read(pcVal+2) << 8) | (mmry.read(pcVal+1)&0xFF);
		pcJmp--;//fix auto increment
		
		this.writeToRegister(Register.pc, pcJmp);
	}
	
	void JNZ(){
		if(!zero){
			JMP();
		}
	}
	
	void JNC(){
		if(!carry){
			JMP();
		}
	}
	
	void JPO(){
		if(!parity){
			JMP();
		}
	}
	
	void JP(){
		if(parity){
			JMP();
		}
	}
	
	/**
	 * Exchange stack with HL
	 */
	void XTHL(){
		int spVal = this.readFromRegister(Register.sp);
		int spVal1 = mmry.read(sp);
		int spVal2 = mmry.read(sp+1);
		
		int hVal = this.readFromRegister(Register.h);
		int lVal = this.readFromRegister(Register.l);
		
		this.writeToRegister(Register.l, spVal1);
		this.writeToRegister(Register.h, spVal2);
		
		mmry.write(sp, lVal);
		mmry.write(sp+1, hVal);
		
	}
	
	/**
	 * Call subroutine
	 */
	void CALL(){
		int pcVal = this.readFromRegister(Register.pc);
		int	ret = pcVal+3; //return address is the next instruction
		
		int spVal = this.readFromRegister(Register.sp);
		mmry.write(spVal-1, ((ret >> 8) & 0xff));
		mmry.write(spVal-2, (ret & 0xff));
		
		this.writeToRegister(Register.sp, spVal-2);
		
		pcVal = (mmry.read(pcVal+2) << 8) | mmry.read(pcVal+1);
		this.writeToRegister(Register.pc, pcVal);
	}
	void CALL(int addr){
		int pcVal = this.readFromRegister(Register.pc);
		int	ret = pcVal+1; //return address is the next instruction
		
		int spVal = this.readFromRegister(Register.sp);
		mmry.write(spVal-1, ((ret >> 8) & 0xff));
		mmry.write(spVal-2, (ret & 0xff));
		
		this.writeToRegister(Register.sp, spVal-2);
		
		pcVal =addr;
		this.writeToRegister(Register.pc, pcVal);
	}
	
	void CNZ(){
		if(!zero){
			CALL();
		}
	}
	
	void CNC(){
		if(!carry){
			CALL();
		}
	}
	
	void CPO(){
		if(!parity){
			CALL();
		}
	}
	
	void CP(){
		if(parity){
			CALL();
		}
	}
	
	/**
	 * Call an ISR based on the opcode
	 * @param rstOpCode
	 */
	void RST(int rstOpCode){
		int addr = (rstOpCode & 0x38)>>3;
		CALL(addr);
	}
	
	/**
	 * Load PC
	 */
	void PCHL(){
		int hVal = this.readFromRegister(Register.h);
		int lVal = this.readFromRegister(Register.l);
		int pcVal = this.To16Bit(hVal, lVal);
		
		this.writeToRegister(Register.pc, pcVal);
	}
	
	void SPHL(){
		int hVal = this.readFromRegister(Register.h);
		int lVal = this.readFromRegister(Register.l);
		int spVal = this.To16Bit(hVal, lVal);
		
		this.writeToRegister(Register.sp, spVal);
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////
	//get memory offset
	int GetMemLocation(int msb, int lsb){
		int offset = (msb<<8)|(lsb&0xFF);
		return this.mmry.read(offset);
	}
	void WriteToMemoryLocation(int msb, int lsb, int value){
		int offset = (msb<<8)|(lsb&0xFF);
		this.mmry.write(offset, value);
	}
	
	//concat two bytes
	int To16Bit(int msb,int lsb){
		int offset = (msb<<8)|(lsb&0xFF);
		return offset;
	}
	
	//getters for conversion 16->8 bit
	int GetMSB(int largeByte){
		return ((largeByte&0xFF00)>>8);
	}
	int GetLSB(int largeByte){
		return largeByte&0xFF;
	}
	
	//increment 16 bit returns 8 bit number
	int Inx(int var1, int var2){
		var2++;
		if(var2>0xFF){
			var2=0;
			var1++;
			if(var1>0xFF){
				var1=0;
			}
		}
		
		return To16Bit(var1,var2);
	}
	
	//decrement 8bit
	int Dcr(int var1){
		int res = var1==0? 0xFF:(var1 - 1);
		zero = (res == 0);
		sign = (0x80 == (res & 0x80));
		parity = parity(res, 8);
		return res;
	}

	public int ExecuteNextInstruction() throws UnimplementedInstruction, ProgramCounterOutOfBounds{
		int cycles = 4;
		
		//debug statement
		//System.out.println(this.ToString());
		
		if(pc>0x1fff){
			//pc out of bounds
			throw new ProgramCounterOutOfBounds();
		}
		
		int opcode = mmry.read(pc); //get opCode
		return ExecuteInstruction(opcode);
	}
	
	public int ExecuteInstruction(int opcode) throws UnimplementedInstruction
	{
		switch (opcode)
		{
		case 0x00: break;	//NOP
		case 0x01: 			//LXI	B,word
			c = mmry.read(pc+1);
			b = mmry.read(pc+2);
			pc+=2; 
			break;
		case 0x02: throw new UnimplementedInstruction();
		case 0x03:
		{//inx B
			int incremented = Inx(b,c);
			b = this.GetMSB(incremented);
			c = this.GetLSB(incremented);
			break;
		}
		case 0x04: throw new UnimplementedInstruction();
		case 0x05: 			//DCR    B
		{
			b= Dcr(b);
		}
		break;
		case 0x06: 							//MVI B,byte
			b = mmry.read(pc+1);
			pc++;
			break;
		case 0x07: throw new UnimplementedInstruction();
		case 0x08: throw new UnimplementedInstruction();
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
		case 0x0a: 
		{ //ldax A, (BC)
			int offset = ( b<<8 ) | ( c&0xFF );
			int value = this.mmry.read(offset);	
		}
		break;
		case 0x0b: throw new UnimplementedInstruction();
		case 0x0c: throw new UnimplementedInstruction();
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
		case 0x10: throw new UnimplementedInstruction();
		case 0x11: 							//LXI	D,word
			e = mmry.read(pc+ 1);
			d = mmry.read(pc+2);
			pc += 2;
			break;
		case 0x12: throw new UnimplementedInstruction();
		case 0x13: 							//INX    D
			e++;
			if (e > 0xFF){
				d++;
				if(d>0xFF){
					d=0;
				}
				e =0;
			}
			break;		
		case 0x14: throw new UnimplementedInstruction();
		case 0x15: throw new UnimplementedInstruction();
		case 0x16: throw new UnimplementedInstruction();
		case 0x17: throw new UnimplementedInstruction();
		case 0x18: throw new UnimplementedInstruction();
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
		case 0x1b: throw new UnimplementedInstruction();
		case 0x1c: throw new UnimplementedInstruction();
		case 0x1d: throw new UnimplementedInstruction();
		case 0x1e: throw new UnimplementedInstruction();
		case 0x1f: throw new UnimplementedInstruction();
		case 0x20: throw new UnimplementedInstruction();
		case 0x21: 							//LXI	H,word
			l = mmry.read(pc+1);
			h = mmry.read(pc+2);
			pc += 2;
			break;
		case 0x22: throw new UnimplementedInstruction();
		case 0x23: 							//INX    H
			l++;
			if (l > 0xFF){ //if overflow
				h++;
				l=0;
			}
			break;		
		case 0x24: throw new UnimplementedInstruction();
		case 0x25: throw new UnimplementedInstruction();
		case 0x26:  							//MVI H,byte
			h = mmry.read(pc+1);
			pc++;
			break;
		case 0x27: throw new UnimplementedInstruction();
		case 0x28: throw new UnimplementedInstruction();
		case 0x29: 								//DAD    H
		{
			int hl = (h << 8) | (l&0xFF);
			int res = hl + hl;
			h = (res & 0xff00) >> 8;
			l = res & 0xff;
			carry = ((res & 0xffff0000) != 0);
		}
		break;
		case 0x2a: throw new UnimplementedInstruction();
		case 0x2b: throw new UnimplementedInstruction();
		case 0x2c: throw new UnimplementedInstruction();
		case 0x2d: throw new UnimplementedInstruction();
		case 0x2e: throw new UnimplementedInstruction();
		case 0x2f: throw new UnimplementedInstruction();
		case 0x30: throw new UnimplementedInstruction();
		case 0x31: 							//LXI	SP,word
			sp = (mmry.read(pc+2)<<8) | (mmry.read(pc+1)&0xFF);
			pc += 2;
			break;
		case 0x32: 							//STA    (word)
		{
			int offset = (mmry.read(pc+2)<<8) | (mmry.read(pc+1)&0xFF);
			mmry.write(offset, a);
			pc += 2;
		}
		break;
		case 0x33: throw new UnimplementedInstruction();
		case 0x34: throw new UnimplementedInstruction();
		case 0x35: 
		{
			int offset = (h<<8) | (l&0xFF);
			int value = mmry.read(offset);
			int res = value==0? 0xFF:value-1;
			
			zero = (res == 0);
			sign = (0x80 == (res & 0x80));
			parity = parity(res, 8);
			mmry.write(offset, res);
		}
		break;
		case 0x36: 							//MVI	M,byte
		{					
			//AC set if lower nibble of h was zero prior to dec
			int offset = (h<<8) | (l&0xFF);
			mmry.write(offset, mmry.read(pc+1));
			pc++;
		}
		break;
		case 0x37: 
		{ //stc set carry
			carry = true;
		}
		break;
		case 0x38: throw new UnimplementedInstruction();
		case 0x39: throw new UnimplementedInstruction();
		case 0x3a: 							//LDA    (word)
		{
			int offset = (mmry.read(pc+2)<<8) | (mmry.read(pc+1)&0xFF);
			a = mmry.read(offset);
			pc+=2;
		}
		break;
		case 0x3b: throw new UnimplementedInstruction();
		case 0x3c: throw new UnimplementedInstruction();
		case 0x3d:
			//DCR A 
			int res = a==0? 0xFF:(a - 1);
			zero = (res == 0);
			sign = (0x80 == (res & 0x80));
			parity = parity(res, 8);
			a = res;
			break;
		case 0x3e: 							//MVI    A,byte
			a = mmry.read(pc+1);
			pc++;
			break;
		case 0x3f: throw new UnimplementedInstruction();
		case 0x40: throw new UnimplementedInstruction();
		case 0x41: //MOV B,B
		{
			this.b=this.b;
		}
		break;
		case 0x42: throw new UnimplementedInstruction();
		case 0x43: throw new UnimplementedInstruction();
		case 0x44: throw new UnimplementedInstruction();
		case 0x45: //MOV B, L
			{
				this.b = this.l;
			}
			break;
		case 0x46: 
			{//mov b,m
				int offset = (h<<8) | (l&0xFF);
				b = mmry.read(offset);
				break;
			}
		case 0x47: throw new UnimplementedInstruction();
		case 0x48: throw new UnimplementedInstruction();
		case 0x49: 
			{ //MOV C,C ie nop
				this.c = this.c;		
			}
			break;
		case 0x4a: throw new UnimplementedInstruction();
		case 0x4b: throw new UnimplementedInstruction();
		case 0x4c: throw new UnimplementedInstruction();
		case 0x4d: throw new UnimplementedInstruction();
		case 0x4e: throw new UnimplementedInstruction();
		case 0x4f: 
		{//mov c,a
			c=a;
			break;
		}
		case 0x50: throw new UnimplementedInstruction();
		case 0x51: 
		{ // MOV D, B
			this.d = this.b;
		}
		break;
		case 0x52: throw new UnimplementedInstruction();
		case 0x53: throw new UnimplementedInstruction();
		case 0x54: throw new UnimplementedInstruction();
		case 0x55: throw new UnimplementedInstruction();
		case 0x56: 							//MOV D,M
		{
			int offset = (h<<8) | (l&0xFF);
			d = mmry.read(offset);
		}
		break;
		case 0x57: 
			{//mov d,a
				d=a;
				break;			
			}
		case 0x58: throw new UnimplementedInstruction();
		case 0x59: throw new UnimplementedInstruction();
		case 0x5a: throw new UnimplementedInstruction();
		case 0x5b: throw new UnimplementedInstruction();
		case 0x5c: throw new UnimplementedInstruction();
		case 0x5d: throw new UnimplementedInstruction();
		case 0x5e: 							//MOV E,M
		{
			int offset = (h<<8) | (l&0xFF);
			e = mmry.read(offset);
		}
		break;
		case 0x5f: 
		{ //mov e,a
			e=a;
			break;
		}
		case 0x60: throw new UnimplementedInstruction();
		case 0x61: throw new UnimplementedInstruction();
		case 0x62: throw new UnimplementedInstruction();
		case 0x63: throw new UnimplementedInstruction();
		case 0x64: throw new UnimplementedInstruction();
		case 0x65: throw new UnimplementedInstruction();
		case 0x66: 							//MOV H,M
		{
			int offset = (h<<8) | (l&0xFF);
			h = mmry.read(offset);
		}
		break;
		case 0x67: 
			{ //mov h,a
				h=a;	
				break;
			}
		case 0x68: throw new UnimplementedInstruction();
		case 0x69: throw new UnimplementedInstruction();
		case 0x6a: throw new UnimplementedInstruction();
		case 0x6b: throw new UnimplementedInstruction();
		case 0x6c: throw new UnimplementedInstruction();
		case 0x6d: throw new UnimplementedInstruction();
		case 0x6e: throw new UnimplementedInstruction();
		case 0x6f: l = a; break; //MOV L,A
		case 0x70: throw new UnimplementedInstruction();
		case 0x71: throw new UnimplementedInstruction();
		case 0x72: //MOV M,D
		{
			int offset =  (h<<8) | (l&0xFF);
			this.mmry.write(offset, this.d);
		}
		break;
		case 0x73: throw new UnimplementedInstruction();
		case 0x74: throw new UnimplementedInstruction();
		case 0x75: throw new UnimplementedInstruction();
		case 0x76: throw new UnimplementedInstruction();
		case 0x77: 							//MOV    M,A
		{
			int offset = (h<<8) | (l&0xFF);
			mmry.write(offset, a);
		}
		break;
		case 0x78: throw new UnimplementedInstruction();
		case 0x79: 
			{//mov a,c
				a=(c&0xFF);
				break;
				//throw new UnimplementedInstruction();
			}
		case 0x7a: a  = d;  break;	//MOV A,D
		case 0x7b: a  = e;  break;	//MOV A,E
		case 0x7c: a  = h;  break;	//MOV A,H
		case 0x7d: throw new UnimplementedInstruction();
		case 0x7e: 							//MOV A,M
		{
			int offset = (h<<8) | (l&0xFF);
			a = mmry.read(offset);
		}
		break;
		case 0x7f: throw new UnimplementedInstruction();
		case 0x80: throw new UnimplementedInstruction();
		case 0x81: throw new UnimplementedInstruction();
		case 0x82: throw new UnimplementedInstruction();
		case 0x83: throw new UnimplementedInstruction();
		case 0x84: throw new UnimplementedInstruction();
		case 0x85: throw new UnimplementedInstruction();
		case 0x86: throw new UnimplementedInstruction();
		case 0x87: throw new UnimplementedInstruction();
		case 0x88: throw new UnimplementedInstruction();
		case 0x89: throw new UnimplementedInstruction();
		case 0x8a: throw new UnimplementedInstruction();
		case 0x8b: throw new UnimplementedInstruction();
		case 0x8c: throw new UnimplementedInstruction();
		case 0x8d: throw new UnimplementedInstruction();
		case 0x8e: throw new UnimplementedInstruction();
		case 0x8f: throw new UnimplementedInstruction();
		case 0x90: throw new UnimplementedInstruction();
		case 0x91: throw new UnimplementedInstruction();
		case 0x92: throw new UnimplementedInstruction();
		case 0x93: throw new UnimplementedInstruction();
		case 0x94: throw new UnimplementedInstruction();
		case 0x95: throw new UnimplementedInstruction();
		case 0x96: throw new UnimplementedInstruction();
		case 0x97: throw new UnimplementedInstruction();
		case 0x98: throw new UnimplementedInstruction();
		case 0x99: throw new UnimplementedInstruction();
		case 0x9a: throw new UnimplementedInstruction();
		case 0x9b: throw new UnimplementedInstruction();
		case 0x9c: throw new UnimplementedInstruction();
		case 0x9d: throw new UnimplementedInstruction();
		case 0x9e: throw new UnimplementedInstruction();
		case 0x9f: throw new UnimplementedInstruction();
		case 0xa0: throw new UnimplementedInstruction();
		case 0xa1: throw new UnimplementedInstruction();
		case 0xa2: throw new UnimplementedInstruction();
		case 0xa3: throw new UnimplementedInstruction();
		case 0xa4: throw new UnimplementedInstruction();
		case 0xa5: throw new UnimplementedInstruction();
		case 0xa6: throw new UnimplementedInstruction();
		case 0xa7: a = a & a; LogicFlagsA();	break; //ANA A
		case 0xa8: throw new UnimplementedInstruction();
		case 0xa9: throw new UnimplementedInstruction();
		case 0xaa: throw new UnimplementedInstruction();
		case 0xab: throw new UnimplementedInstruction();
		case 0xac: throw new UnimplementedInstruction();
		case 0xad: throw new UnimplementedInstruction();
		case 0xae: throw new UnimplementedInstruction();
		case 0xaf: a = a ^ a; LogicFlagsA();	break; //XRA A
		case 0xb0: 
			{ //ORA B
				int ora = (b&0xFF)|a;
				LogicFlagsA();
				break;
			}
		case 0xb1: throw new UnimplementedInstruction();
		case 0xb2: throw new UnimplementedInstruction();
		case 0xb3: throw new UnimplementedInstruction();
		case 0xb4: throw new UnimplementedInstruction();
		case 0xb5: throw new UnimplementedInstruction();
		case 0xb6: throw new UnimplementedInstruction();
		case 0xb7: throw new UnimplementedInstruction();
		case 0xb8: throw new UnimplementedInstruction();
		case 0xb9: throw new UnimplementedInstruction();
		case 0xba: throw new UnimplementedInstruction();
		case 0xbb: throw new UnimplementedInstruction();
		case 0xbc: throw new UnimplementedInstruction();
		case 0xbd: throw new UnimplementedInstruction();
		case 0xbe: throw new UnimplementedInstruction();
		case 0xbf: throw new UnimplementedInstruction();
		case 0xc0: throw new UnimplementedInstruction();
		case 0xc1: 						//POP    B
		{
			c = mmry.read(sp);
			b = mmry.read(sp+1);
			sp += 2;
		}
		break;
		case 0xc2: 						//JNZ address
			if (!zero){
				pc = (mmry.read(pc+2) << 8) | (mmry.read(pc+1)&0xFF);
				pc--;//fix auto inr
			}
			else
				pc += 2;
			break;
		case 0xc3:						//JMP address
			pc = (mmry.read(pc+2) << 8) | (mmry.read(pc+1)&0xFF);
			pc--;//fix auto increment
			break;
		case 0xc4: throw new UnimplementedInstruction();
		case 0xc5: 						//PUSH   B
		{
			mmry.write(sp-1,b);
			mmry.write(sp-2,c);
			sp = sp - 2;
		}
		break;
		case 0xc6: 						//ADI    byte
		{
			int x =  a +  mmry.read(pc+1);
			zero = ((x & 0xff) == 0);
			sign = (0x80 == (x & 0x80));
			parity = parity((x&0xff), 8);
			carry = (x > 0xff);
			a = x&(0xFF);
			pc++;
		}
		break;
		case 0xc7: throw new UnimplementedInstruction();
		case 0xc8: 
			//RZ
			if(zero)
			{		
				pc = (mmry.read(sp)&0xFF) | (mmry.read(sp+1) << 8);
				sp += 2;
				return 0;
			}else{
				//do nothing
				break;
			}
		case 0xc9: 						//RET
			pc = (mmry.read(sp)&0xFF) | (mmry.read(sp+1) << 8);
			sp += 2;
			return 0;
		case 0xca: 
		if(this.zero)
		{
			pc = (mmry.read(pc+2) << 8) | (mmry.read(pc+1)&0xFF);
			pc--;//fix auto increment
			
		}
		else{
			pc+=2;
		}
		break;
		case 0xcb: throw new UnimplementedInstruction();
		case 0xcc: throw new UnimplementedInstruction();
		case 0xcd: 						//CALL adr
		{
			int	ret = pc+3;
			mmry.write(sp-1, ((ret >> 8) & 0xff));
			mmry.write(sp-2, (ret & 0xff));
			sp = sp - 2;
			pc = (mmry.read(pc+2) << 8) | mmry.read(pc+1);
			return 0;
		}
		case 0xce: throw new UnimplementedInstruction();
		case 0xcf: throw new UnimplementedInstruction();
		case 0xd0: throw new UnimplementedInstruction();
		case 0xd1: 						//POP    D
		{
			e = mmry.read(sp);
			d = mmry.read(sp+1);
			sp += 2;
		}
		break;
		case 0xd2: //JNCarry
			if (!carry){
				pc = (mmry.read(pc+2) << 8) | (mmry.read(pc+1)&0xFF);
				pc--;//fix auto inr
			}
			else
				pc += 2;
			break;
		case 0xd3: 
			//Don't know what to do here (yet)
			pc++;
			break;
		case 0xd4: throw new UnimplementedInstruction();
		case 0xd5: 						//PUSH   D
		{
			mmry.write(sp-1, d);
			mmry.write(sp-2, e);
			sp = sp - 2;
		}
		break;
		case 0xd6: throw new UnimplementedInstruction();
		case 0xd7: throw new UnimplementedInstruction();
		case 0xd8: 
			{
				throw new UnimplementedInstruction();
			}
		case 0xd9: throw new UnimplementedInstruction();
		case 0xda: 
			//JC
			if(carry)
			{		
				pc = (mmry.read(pc+2) << 8) | (mmry.read(pc+1)&0xFF);
				pc--;//fix auto inr
			}
			else{
				pc+=2;
			}
			break;
		case 0xdb: //input keys			
			a = readPort(pc+1);
			pc++;
			break;
		case 0xdc: throw new UnimplementedInstruction();
		case 0xdd: throw new UnimplementedInstruction();
		case 0xde: throw new UnimplementedInstruction();
		case 0xdf: throw new UnimplementedInstruction();
		case 0xe0: throw new UnimplementedInstruction();
		case 0xe1: 					//POP    H
		{
			l=mmry.read(sp);
			h=mmry.read(sp+1);
			sp += 2;
		}
		break;
		case 0xe2: throw new UnimplementedInstruction();
		case 0xe3: throw new UnimplementedInstruction();
		case 0xe4: throw new UnimplementedInstruction();
		case 0xe5: 						//PUSH   H
		{
			mmry.write(sp-1, h);
			mmry.write(sp-2, l);
			sp = sp - 2;
		}
		break;
		case 0xe6: 						//ANI    byte
		{
			a = a & mmry.read(pc+1);
			LogicFlagsA();
			pc++;
		}
		break;
		case 0xe7: throw new UnimplementedInstruction();
		case 0xe8: throw new UnimplementedInstruction();
		case 0xe9: throw new UnimplementedInstruction();
		case 0xea: throw new UnimplementedInstruction();
		case 0xeb: 					//XCHG
		{
			int save1 = d;
			int save2 = e;
			d = h;
			e = l;
			h = save1;
			l = save2;
		}
		break;
		case 0xec: throw new UnimplementedInstruction();
		case 0xed: throw new UnimplementedInstruction();
		case 0xee: throw new UnimplementedInstruction();
		case 0xef: throw new UnimplementedInstruction();
		case 0xf0: throw new UnimplementedInstruction();
		case 0xf1: 					//POP PSW
		{
			a = mmry.read(sp+1);
			int psw = mmry.read(sp);
			zero  = (0x01 == (psw & 0x01));
			sign  = (0x02 == (psw & 0x02));
			parity  = (0x04 == (psw & 0x04));
			carry = (0x05 == (psw & 0x08));
			auxiliaryCarry = (0x10 == (psw & 0x10));
			sp += 2;
		}
		break;
		case 0xf2: throw new UnimplementedInstruction();
		case 0xf3: throw new UnimplementedInstruction();
		case 0xf4: throw new UnimplementedInstruction();
		case 0xf5: 						//PUSH   PSW
		{
			mmry.write(sp-1, a);
			int psw = (zero?1:0) | (sign? (0x1<< 1):0) |(parity?(0x1 << 2):0 )|(carry? (0x1<< 3):0 )|(auxiliaryCarry? (0x1<< 4):0 );
			mmry.write(sp-1, psw);
			sp = sp - 2;
		}
		break;
		case 0xf6: throw new UnimplementedInstruction();
		case 0xf7: throw new UnimplementedInstruction();
		case 0xf8: throw new UnimplementedInstruction();
		case 0xf9: throw new UnimplementedInstruction();
		case 0xfa: throw new UnimplementedInstruction();
		case 0xfb: int_enable = 1;  break;	//EI
		case 0xfc: throw new UnimplementedInstruction();
		case 0xfd: throw new UnimplementedInstruction();
		case 0xfe: 						//CPI  byte
		{
			int x = a - mmry.read(pc+1);
			zero = (x == 0);
			sign = (0x80 == (x & 0x80));
			parity = parity(x, 8);
			carry = (a < mmry.read(pc+1));
			pc++;
		}
		break;
		case 0xff: throw new UnimplementedInstruction();
		}


		pc++; //increment program Counter

		return 0;
	}

	private int readPort(int port) {
		// Port 1 maps the keys for space invaders
		  // Bit 0 = coin slot
		  // Bit 1 = two players button
		  // Bit 2 = one player button
		  // Bit 4 = player one fire
		  // Bit 5 = player one left
		  // Bit 6 = player one right
		  // Port 2 maps player 2 controls and dip switches
		  // Bit 0,1 = number of ships
		  // Bit 2   = mode (1=easy, 0=hard)
		  // Bit 4   = player two fire
		  // Bit 5   = player two left
		  // Bit 6   = player two right
		  // Bit 7   = show or hide coin info
		switch(port) {
		  case 1:
		    {
		      int r = this.port1;
		      this.port1 &= 0xFE;
		      return r;
		    }
		  case 2:
		    {
		      return (this.port2i & 0x8f) | (this.port1 & 0x70);
		    }
		    
		  case 3:
		    {
			    //TODO: Figure out port 3
		    	break;
		    	//return ((((this.port4hi << 8) | (this.port4lo)) << this.port2o) >> 8) & 0xFF;
		    }
		  default:
		    break;
		  }
		  return 0;
				
	}
	
	private void writePort (int port, int value) {
//		  switch(port) {
//		  case 2:
//		    {
//		      this.port2o = value;
//		      return;
//		    }
//		    break;
//		  case 3:
//		    {
//		      // Connected to the sound hardware
//		      // Bit 1 = spaceship sound (looped)
//		      // Bit 2 = Shot
//		      // Bit 3 = Your ship hit
//		      // Bit 4 = Invader hit
//		      // Bit 5 = Extended play sound
//		      if (this.sound) {
//			for(int i=0; i< 5; ++i) {
//			  int b = 1 << i;
//			  if(!(this.port3o & b) && (v & b))
//			    sound.apply(this, [i+1]);
//			}
//		      }
//
//		      this.port3o = v;
//
//		    }
//		    break;
//		  case 4:
//		    {
//		      this.port4lo = this.port4hi;
//		      this.port4hi = v;
//		    }
//		    break;
//		  case 5:
//		    {
//		      // Plays sounds
//		      // Bit 0 = invaders sound 1
//		      // Bit 1 = invaders sound 2
//		      // Bit 2 = invaders sound 3
//		      // Bit 3 = invaders sound 4
//		      // Bit 4 = spaceship hit
//		      // Bit 5 = amplifier enabled/disabled
//		      if (this.sound) {
//			for(var i=0; i< 5; ++i) {
//			  var b = 1 << i;
//			  if(!(this.port5o & b) && (v & b))
//			    sound.apply(this, [i+11]);
//			}
//		      }
//		      this.port5o = v;
//		    }
//		    break;
//		  }
		}


	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(int bytes) {
	    char[] hexChars = new char[2];
	    
	        int v = bytes & 0xFF;
	        hexChars[0] = hexArray[v >>> 4];
	        hexChars[1] = hexArray[v & 0x0F];
	    
	    return new String(hexChars);
	}
	public String ToString(){

		int psw = (zero?1:0) | (sign? (0x1<< 1):0) |(parity?(0x1 << 2):0 )|(carry? (0x1<< 3):0 )|(auxiliaryCarry? (0x1<< 4):0 );
		
		String res = "af\tbc\tde\thl\tsp\tpc\n";
		res += bytesToHex(a)+ bytesToHex(psw)+"\t"+bytesToHex(b)+
				bytesToHex(c)+"\t"+ bytesToHex(d)+bytesToHex(e)+ "\t"+
				bytesToHex(h)+bytesToHex(l)+"\t"+
				bytesToHex((sp&0xFF00) >> 8)+bytesToHex(sp)+"\t"
				+ bytesToHex((pc&0xFF00) >> 8)+ bytesToHex(pc);
		
		return res;
	}
	
	//cpu execution loop
	public void  executionLoop() throws UnimplementedInstruction, ProgramCounterOutOfBounds{

		for(int i=0;i<16667;i++){
			//do instruction
			ExecuteNextInstruction();
		}
		
		//check for interrupt
		handleInterrpts();
	}
	
	private void handleInterrpts() {
		if(int_code!=0){
			//perform "PUSH PC"
			int ret = pc;
			mmry.write(sp-1, ((ret >> 8) & 0xff));
			mmry.write(sp-2, (ret & 0xff));
			sp = sp - 2;
		  
		  //Set the PC to the low memory vector.
		  //This is identical to an "RST interrupt_num" instruction.
		  pc = 8 * int_code;
		}
		int_code=0;//reset interrupt code
	}

	//////////////////////////GETTERS AND SETTERS////////////////////////////////
	public boolean intEnabled(){
		if(this.int_enable!=0){
			return true;
		}
		return false;
	}
	
	public void setInterruptEnable(int value){
		this.int_enable = value;
	}
	public void setInterruptCode(int code){
		this.int_code = code;
	}
	
	public static void main(String args[]) throws IOException, UnimplementedInstruction, ProgramCounterOutOfBounds{
		CPU c = new CPU();
		String outFile ="SpaceInvadersCPUState.txt";
		PrintStream out = new PrintStream(outFile);
		
		int i=1;
		while(true){
			out.println("Step "+i);
			c.ExecuteNextInstruction();
			out.println(c.ToString());
			i++;
		}
	}
	
}

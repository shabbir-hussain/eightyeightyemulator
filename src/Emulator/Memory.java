package Emulator;

/**
 * @author Shabbir
 * This class simulates the  main memory
 *
 */
public class Memory {
	
	public static int MemorySize = 0x10000; //16k memory
	int[] memBuffer = new int[MemorySize];
	int[] bBuffer = new int[MemorySize];

	/**
	 * This method writes data to the memory
	 * @param address location of memory
	 * @param data to be written
	 */
	public void write(int addr,int data){
		memBuffer[addr]=(data);
	}
	
	/**
	 * 
	 * @param address
	 * @return the value in memory
	 */
	public int read(int addr){
		return memBuffer[addr];
	}
	
	/**
	 * Copies an array into memory
	 * @param src
	 * @param offset
	 */
	public void copyTo(byte[] src, int offset){
		System.arraycopy( src, 0, bBuffer, offset, src.length );
		
		//copy to int buffer
		for(int i=0;i<src.length;i++){
			memBuffer[offset+i] = (bBuffer[offset+i] &0xFF);
		}
	}
	
	
	/**
	 * Converts two bytes into a short by appending them
	 * in reality everything is in integers
	 * @param LSB
	 * @param MSB
	 * @return
	 */
	public static int BytesToShort(int MSB, int LSB){
		
		int  result = ((MSB& 0xFF) << 8)|(LSB &0xFF);
		return result;
	}
	
	public static void main(String args[]){
		int a = (byte) 0xFF;
		int b = (byte) 0x1;
		int ab = BytesToShort(a,b);
		System.out.println( (int)(ab));
	}
	
}

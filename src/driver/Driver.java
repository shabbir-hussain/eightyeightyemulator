package driver;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintStream;

import javax.swing.JFrame;
import javax.swing.Timer;

import video.VideoFrame;
import Emulator.CPU;
import Emulator.ProgramCounterOutOfBounds;
import Emulator.UnimplementedInstruction;


/**
 * This class runs the cpu and simulates interupts
 * @author Shabbir
 *
 */
public class Driver implements ActionListener {

	//screen is 224W,256H
	int W = 224;
	int H = 256;
	VideoFrame frame = new VideoFrame(W,H);

	public static final int TIMEOUT = 16; //16MS ~60HZ

	String outFile; // create log file
	PrintStream out;
	CPU cpu; // create cpu obj

	public Driver() throws IOException{
		//init
		cpu = new CPU();
		outFile ="SpaceInvadersCPUState.txt";
		out = new PrintStream(outFile);
	}


	public void executionLoop() throws UnimplementedInstruction, ProgramCounterOutOfBounds, IOException {

		//setup timer for interupts
		Timer t = new Timer(TIMEOUT, this);
		t.start();

		try{
		int i=1;
			while(true){
				out.println("Step "+i);
				cpu.executionLoop();
				//cpu.ExecuteNextInstruction();
				out.println(cpu.ToString());
				i++;
			}
		}catch(UnimplementedInstruction e ){
			out.println(cpu.ToString());
			out.println("Memory Dump:");
			byte memory[] = cpu.mmry.memDump();
			out.write(memory);
			out.println();
			throw e;
		}catch(ProgramCounterOutOfBounds e){
			out.println(cpu.ToString());
			out.println("Memory Dump:");
			byte memory[] = cpu.mmry.memDump();
			out.write(memory);
			out.println();			
			throw e;
		}
		
	}



	public void fireInterrupt(int i){

		if (cpu.intEnabled()) {
			cpu.setInterruptEnable(0);
			cpu.setInterruptCode(i);
		}


	}

	public void vblank()
	{
		/* draw screen */
		// Update video memory
		//video memory goes from 0x2400 to 0x3fff
		for(int mPtr=0x2400;mPtr<0x3FFF;mPtr++){
			int base = mPtr - 0x2400;
			int y = ~(((base & 0x1f) * 8) & 0xFF) & 0xFF;
			int x = base >> 5;

			int value = cpu.mmry.read(mPtr);
			for(int i=0; i<8; ++i) {
				frame.plotData( x, y, value, i);
			}
		}


		//		/* read input */
		//		if (window!=GetForegroundWindow()) return;
		//		#define KEY(x) GetAsyncKeyState(x)
		//		key=(~KEY(51)>>15&1)|(KEY(50)>>14&2)|(KEY(49)>>13&4)|(KEY(90)>>11&0x10)|(KEY(VK_RIGHT)>>9&0x40)|(KEY(84)>>8&0x80);
		//		key|=((key>>1^(KEY(VK_LEFT)>>10))&0x20);
		//		
		//		if (KEY(82)&0x8000) { PC=0; memset(mem+0x2000,0x0,0x02000); } /* 'reset' */
	}



	@Override
	public void actionPerformed(ActionEvent arg0) {
		//timed event fired
		fireInterrupt(1);
		this.vblank();
		fireInterrupt(2);

	}

	public static void main(String args[]) throws IOException, UnimplementedInstruction, ProgramCounterOutOfBounds{

		//create obj
		Driver drvr = new Driver();
		
		//run
		drvr.executionLoop();
	}

}

package Emulator;

public class UnimplementedInstruction extends Exception {

	public UnimplementedInstruction(){
		super("Instruction not Implemented");
	}
}

/*
 * File: VMFrame.java
 * Date: Spring 2022
 * Auth: S. Bowers
 * Desc: Class to hold info about MyPL VM Stack Frames. 
 */


import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;



/**
 * Each VMFrame corresponds to an executable MyPL function. A frame
 *  consists of function name, an argument count, the frame's
 *  instructions, the frame's local variables, a stack (for operation
 *  operands), and a program counter.
 */ 
public class VMFrame {

  // name of the function corresponding to the frame (used to identify
  // the frame by the VM)
  private String functionName; 

  private int argCount;
  
  // the program instructions 
  public List<VMInstr> instructions = new ArrayList<>();

  // @TODO Should this be a HashMap? 
  public List<Object> variables = new ArrayList<>();

  // the operand stack
  public Deque<Object> operandStack = new ArrayDeque<>();

  // the program counter (pc) for an active stack frame
  public int pc = 0;


  // basic constructor
  public VMFrame(String functionName, int argCount) {
    this.functionName = functionName;
    this.argCount = argCount;
  }

  // get the frame function's name
  public String functionName() {
    return functionName;
  }

  // get the frame function's argument count
  public int argCount() {
    return argCount;
  }
  
  /**
   * Creates a new frame based on the current frame (for the purpose
   * of adding a new function call to the frame stack). The new frame
   * is created so that it has the same name, argument count, and
   * instructions as the current frame. However, the new frame has an
   * empty operand stack, and empty variable store, and the program
   * counter set to the first instruction.
   * @return a new version of the current frame to add to the frame stack
   */
  public VMFrame instantiate() {
    VMFrame newFrame = new VMFrame(functionName, argCount);
    for (VMInstr instr : instructions) 
      newFrame.instructions.add(instr);
    return newFrame;
  }
  
}

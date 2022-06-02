/*
 * File: VM.java
 * Date: Spring 2022
 * Auth: Ahmad Moltafet
 * Desc: A bare-bones MyPL Virtual Machine. The architecture is based
 *       loosely on the architecture of the Java Virtual Machine
 *       (JVM).  Minimal error checking is done except for runtime
 *       program errors, which include: out of bound indexes,
 *       dereferencing a nil reference, and invalid value conversion
 *       (to int and double).
 */


import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.Scanner;

import javax.print.event.PrintEvent;
import javax.swing.DebugGraphics;


/*----------------------------------------------------------------------

  TODO: Your main job for HW-6 is to finish the VM implementation
        below by finishing the handling of each instruction.

        Note that PUSH, NOT, JMP, READ, FREE, and NOP (trivially) are
        completed already to help get you started. 

        Be sure to look through OpCode.java to get a basic idea of
        what each instruction should do as well as the unit tests for
        additional details regarding the instructions.

        Note that you only need to perform error checking if the
        result would lead to a MyPL runtime error (where all
        compile-time errors are assumed to be found already). This
        includes things like bad indexes (in GETCHR), dereferencing
        and/or using a NIL_OBJ (see the ensureNotNil() helper
        function), and converting from strings to ints and doubles. An
        error() function is provided to help generate a MyPLException
        for such cases.

----------------------------------------------------------------------*/ 


class VM {

  // set to true to print debugging information
  private boolean DEBUG = false;
  
  // the VM's heap (free store) accessible via object-id
  private Map<Integer,Map<String,Object>> heap = new HashMap<>();
  
  // next available object-id
  private int objectId = 1111;
  
  // the frames for the program (one frame per function)
  private Map<String,VMFrame> frames = new HashMap<>();

  // the VM call stack
  private Deque<VMFrame> frameStack = new ArrayDeque<>();

  
  /**
   * For representing "nil" as a value
   */
  public static String NIL_OBJ = new String("nil");
  

  /** 
   * Add a frame to the VM's list of known frames
   * @param frame the frame to add
   */
  public void add(VMFrame frame) {
    frames.put(frame.functionName(), frame);
  }

  /**
   * Turn on/off debugging, which prints out the state of the VM prior
   * to each instruction. 
   * @param debug set to true to turn on debugging (by default false)
   */
  public void setDebug(boolean debug) {
    DEBUG = debug;
  }

  /**
   * Run the virtual machine
   */
  public void run() throws MyPLException {

    // grab the main stack frame
    if (!frames.containsKey("main"))
      throw MyPLException.VMError("No 'main' function");
    VMFrame frame = frames.get("main").instantiate();
    frameStack.push(frame);
    
    // run loop (keep going until we run out of frames or
    // instructions) note that we assume each function returns a
    // value, and so the second check below should never occur (but is
    // useful for testing, etc).
    while (frame != null && frame.pc < frame.instructions.size()) {
      // get next instruction
      VMInstr instr = frame.instructions.get(frame.pc);
      // increment instruction pointer
      ++frame.pc;

      // For debugging: to turn on the following, call setDebug(true)
      // on the VM.
      if (DEBUG) {
        System.out.println();
        System.out.println("\t FRAME........: " + frame.functionName());
        System.out.println("\t PC...........: " + (frame.pc - 1));
        System.out.println("\t INSTRUCTION..: " + instr);
        System.out.println("\t OPERAND STACK: " + frame.operandStack);
        System.out.println("\t HEAP ........: " + heap);
      }

      
      //------------------------------------------------------------
      // Consts/Vars
      //------------------------------------------------------------

      if (instr.opcode() == OpCode.PUSH) {
        frame.operandStack.push(instr.operand());
      }
      
      else if (instr.opcode() == OpCode.POP) {
        frame.operandStack.pop();
      }
      
      else if (instr.opcode() == OpCode.LOAD) {
        frame.operandStack.push(frame.variables.get((int)instr.operand()));
      }
      
      else if (instr.opcode() == OpCode.STORE) {
        if ((int)instr.operand() >= frame.variables.size()) {
          frame.variables.add(frame.operandStack.pop());
        } else {
          frame.variables.set((int)instr.operand(), frame.operandStack.pop());
        }
      }
      
      //------------------------------------------------------------
      // Ops
      //------------------------------------------------------------
        
      else if (instr.opcode() == OpCode.ADD) {
        // TODO
        Object op1 = frame.operandStack.pop();
        Object op2 = frame.operandStack.pop();
        // ensure that the top two values are of type double
        if (op1 instanceof Double && op2 instanceof Double) {
          frame.operandStack.push((double)op1 + (double)op2);
        }
        else if(op1 instanceof Integer && op2 instanceof Integer) {
          frame.operandStack.push((int)op1 + (int)op2);
        }
        else if(op1 instanceof String && op2 instanceof String) {
          frame.operandStack.push((String)op2 + (String)op1);
        } // string char add
        else if(op1 instanceof Character && op2 instanceof Character) {
          frame.operandStack.push((char)op1 + (char)op2);
        } // string char add
        
        else {
          throw MyPLException.VMError("Invalid operands for ADD");
        }        
      }

      else if (instr.opcode() == OpCode.SUB) {
        // TODO
        // pop the top two values off the stack
        Object op1 = frame.operandStack.pop();
        Object op2 = frame.operandStack.pop();
        // push the difference of the two values back onto the stack
        // ensure that the top two values are of type double
        if (op1 instanceof Double && op2 instanceof Double) {
          double s1 = (double)op1;
          double s2 = (double)op2;
          if(s1 < s2) {
            frame.operandStack.push((s1 - s2)*-1);
          } else {
            frame.operandStack.push(s1 - s2);
          }
        }
        else if(op1 instanceof Integer && op2 instanceof Integer) {
          int s1 = (int)op1;
          int s2 = (int)op2;
          if(s1 < s2) {
            frame.operandStack.push((s1 - s2)*-1);
          } else {
            frame.operandStack.push(s1 - s2);
          }
        }
        else {
          throw MyPLException.VMError("Operands must be of type int or double");
        }  
      }

      else if (instr.opcode() == OpCode.MUL) {
        // TODO
        // ensure that there are at least 2 values on the stack
        if (frame.operandStack.size() < 2) {
          throw MyPLException.VMError("Not enough operands on the stack");
        }
        Object op1 = frame.operandStack.pop();
        Object op2 = frame.operandStack.pop();
        // ensure that the top two values are of type double
        if (op1 instanceof Double && op2 instanceof Double) {
          frame.operandStack.push((Double)op1 * (Double)op2);
        }
        else if(op1 instanceof Integer && op2 instanceof Integer) {
          frame.operandStack.push((Integer)op1 * (Integer)op2);
        }
        else {
          throw MyPLException.VMError("Operands must be of type int or double");
        }        
      }

      else if (instr.opcode() == OpCode.DIV) {
        // TODO
        // ensure that there are at least 2 values on the stack
        if (frame.operandStack.size() < 2) {
          throw MyPLException.VMError("Not enough operands on the stack");
        }
        Object op1 = frame.operandStack.pop();
        Object op2 = frame.operandStack.pop();
        // ensure that the top two values are of type double
        if (op1 instanceof Double && op2 instanceof Double) {
          frame.operandStack.push((Double)op2 / (Double)op1);
        }
        else if(op1 instanceof Integer && op2 instanceof Integer) {
          frame.operandStack.push((Integer)op2 / (Integer)op1);
        }
        else {
          throw MyPLException.VMError("Operands must be of type int or double");
        } 
      }

      else if (instr.opcode() == OpCode.MOD) {
        // TODO
        // ensure that there are at least 2 values on the stack
        if (frame.operandStack.size() < 2) {
          throw MyPLException.VMError("Not enough operands on the stack");
        }
        Object op1 = frame.operandStack.pop();
        Object op2 = frame.operandStack.pop();
        // ensure that the top two values are of type int
        if(op1 instanceof Integer && op2 instanceof Integer) {
          frame.operandStack.push((Integer)op2 % (Integer)op1);
        }
        else {
          throw MyPLException.VMError("Operands must be of type int or double");
        } 
      }

      else if (instr.opcode() == OpCode.AND) {
        // TODO
        if (frame.operandStack.size() < 2) {
          throw MyPLException.VMError("Not enough operands on the stack");
        }
        Object op1 = frame.operandStack.pop();
        Object op2 = frame.operandStack.pop();

        if(op1 instanceof Boolean && op2 instanceof Boolean) {
          frame.operandStack.push((Boolean)op1 & (Boolean)op2);
        }
        else {
          throw MyPLException.VMError("Operands must be of type boolean");
        }
      }

      else if (instr.opcode() == OpCode.OR) {
        // TODO
        if (frame.operandStack.size() < 2) {
          throw MyPLException.VMError("Not enough operands on the stack");
        }
        Object op1 = frame.operandStack.pop();
        Object op2 = frame.operandStack.pop();

        if(op1 instanceof Boolean && op2 instanceof Boolean) {
          frame.operandStack.push((Boolean)op1 | (Boolean)op2);
        }
        else {
          throw MyPLException.VMError("Operands must be of type boolean");
        }

      }

      else if (instr.opcode() == OpCode.NOT) {
        Object operand = frame.operandStack.pop();
        ensureNotNil(frame, operand);
        frame.operandStack.push(!(boolean)operand);
      }

      else if (instr.opcode() == OpCode.CMPLT) {
        // TODO 1 > 2
        Object op1 = frame.operandStack.pop();
        Object op2 = frame.operandStack.pop();
        if(op1 instanceof Double && op2 instanceof Double) {
          frame.operandStack.push((double)op1 > (double)op2);
        }
        else if(op1 instanceof Integer && op2 instanceof Integer) {
          frame.operandStack.push((int)op1 > (int)op2);
          
        }
        else if(op1 instanceof String && op2 instanceof String) {
          String s1 = (String)op1;
          String s2 = (String)op2;
          frame.operandStack.push(s1.compareTo(s2) > 0);
        }
        else {
          throw MyPLException.VMError("Operands must be of type int or double");
        }
      }

      else if (instr.opcode() == OpCode.CMPLE) {
        // TODO
        Object op1 = frame.operandStack.pop();
        Object op2 = frame.operandStack.pop();
        if(op1 instanceof Double && op2 instanceof Double) {
          frame.operandStack.push((double)op1 >= (double)op2);
        }
        else if(op1 instanceof Integer && op2 instanceof Integer) {
          frame.operandStack.push((int)op1 >= (int)op2);
        }
        else if(op1 instanceof String && op2 instanceof String) {
          if(((String)op1).compareTo((String)op2) >= 0) {
            frame.operandStack.push(true);
          }
          else {
            frame.operandStack.push(false);
          }
        }
        else {
          throw MyPLException.VMError("Operands must be of type int or double");
        }
        
      }

      else if (instr.opcode() == OpCode.CMPGT) {
        // TODO
        Object op1 = frame.operandStack.pop();
        Object op2 = frame.operandStack.pop();
        if(op1 instanceof Double && op2 instanceof Double) {
          frame.operandStack.push((double)op1 < (double)op2);
        }
        else if(op1 instanceof Integer && op2 instanceof Integer) {
          frame.operandStack.push((int)op1 < (int)op2);
        }
        else if(op1 instanceof String && op2 instanceof String) {
          if(((String)op1).compareTo((String)op2) < 0) {
            frame.operandStack.push(true);
          }
          else {
            frame.operandStack.push(false);
          }
        }
        else {
          throw MyPLException.VMError("Operands must be of type int or double");
        }
        
      }

      else if (instr.opcode() == OpCode.CMPGE) {
        // TODO
        Object op1 = frame.operandStack.pop();
        Object op2 = frame.operandStack.pop();

        if(op1 instanceof Double && op2 instanceof Double) {
          frame.operandStack.push((double)op1 <= (double)op2);
        }
        else if(op1 instanceof Integer && op2 instanceof Integer) {
          frame.operandStack.push((int)op1 <= (int)op2);
        }
        else if(op1 instanceof String && op2 instanceof String) {
          if(((String)op1).compareTo((String)op2) <= 0) {
            frame.operandStack.push(true);
          }
          else {
            frame.operandStack.push(false);
          }
        }
        else {
          throw MyPLException.VMError("Operands must be of type int or double");
        }
        

      }

      else if (instr.opcode() == OpCode.CMPEQ) {
        // TODO
        Object op1 = frame.operandStack.pop();
        Object op2 = frame.operandStack.pop();
        if(op1 instanceof Double && op2 instanceof Double) {
          frame.operandStack.push((double)op1 == ((double)op2));
        }
        else if(op1 instanceof Integer && op2 instanceof Integer) {
          frame.operandStack.push((int)op1 == (int)op2);
        }
        else if(op1 instanceof String && op2 instanceof String) {
          frame.operandStack.push((String)op1 == (String)op2);
        }
        // check for null objects
        else if(op1 == null && op2 == null) {
          frame.operandStack.push(true);
        }
        else {
          frame.operandStack.push(false);
        }
        
      }

      else if (instr.opcode() == OpCode.CMPNE) {
        // TODO
        Object op1 = frame.operandStack.pop();
        Object op2 = frame.operandStack.pop();
        if(op1 instanceof Double && op2 instanceof Double) {
          double d1 = (double)op1;
          double d2 = (double)op2;
          if(d1 == d2) {
            frame.operandStack.push(false);
          }
          else {
            frame.operandStack.push(true);
          }
        }
        else if(op1 instanceof Integer && op2 instanceof Integer) {
          frame.operandStack.push((int)op1 != (int)op2);
        }
        else if(op1 instanceof String && op2 instanceof String) {
          frame.operandStack.push((String)op1 != (String)op2);
        }
        // check for null objects
        else if(op1 == null && op2 == null) {
          frame.operandStack.push(false);
        }
        else {
          frame.operandStack.push(true);
        }
      }

      else if (instr.opcode() == OpCode.NEG) {
        // TODO
        Object op1 = frame.operandStack.pop();
        if(op1 instanceof Double) {
          frame.operandStack.push(-1 * (Double)op1);
        }
        else if(op1 instanceof Integer) {
          frame.operandStack.push(-1 * (Integer)op1);
        }
        else {
          throw MyPLException.VMError("Operand must be of type int or double");
        }
      }

      
      //------------------------------------------------------------
      // Jumps
      //------------------------------------------------------------
        
      else if (instr.opcode() == OpCode.JMP) {
        frame.pc = (int)instr.operand();
      }

      else if (instr.opcode() == OpCode.JMPF) {
        // TODO
        Object op1 = frame.operandStack.pop();
        if(op1 instanceof Boolean) {
          if((Boolean)op1 == false) {
            frame.pc = (int)instr.operand();
          }
        }
        else {
          throw MyPLException.VMError("Operand must be of type boolean");
        }
      }
        
      //------------------------------------------------------------
      // Functions
      //------------------------------------------------------------

      else if (instr.opcode() == OpCode.CALL) {
        String funName = (String)instr.operand();
        VMFrame new_frame = frames.get(funName).instantiate();
        frameStack.push(new_frame);
        for(int i = 0; i < new_frame.argCount(); i++) {
          new_frame.operandStack.push(frame.operandStack.pop());
        }
        frame = new_frame;
    }
        
      else if (instr.opcode() == OpCode.VRET) {
        // TODO:
        Object ret = frame.operandStack.pop();
        frameStack.pop();        
        frame = frameStack.peek();
        if (frame != null) {
          frame.operandStack.push(ret);
        }
      }
        
      //------------------------------------------------------------
      // Built-ins
      //------------------------------------------------------------
        else if (instr.opcode() == OpCode.WRITE) {
        System.out.print(frame.operandStack.pop());
      }

      else if (instr.opcode() == OpCode.READ) {
        Scanner s = new Scanner(System.in);
        frame.operandStack.push(s.nextLine());
      }

      else if (instr.opcode() == OpCode.LEN) {
        frame.operandStack.push(((String)(frame.operandStack.pop())).length());
      }

      else if (instr.opcode() == OpCode.GETCHR) {
        String op_string = (String)frame.operandStack.pop();
        Integer op_index = (Integer)frame.operandStack.pop();
        if (op_index >= op_string.length() || op_index < 0) {
          error("index out of range", frame);
        }
        frame.operandStack.push(op_string.charAt(op_index));
      }

      else if (instr.opcode() == OpCode.TOINT) {
        Object op = frame.operandStack.pop();
        if (op instanceof String) {
          try {
            frame.operandStack.push(Integer.parseInt((String)op));
          }
          catch(Exception e) {
            error("error converting string to int", frame);
          }
        }
        else {
          frame.operandStack.push((int)((double)op));
        }
      }

      else if (instr.opcode() == OpCode.TODBL) {
        Object op = frame.operandStack.pop();
        if (op instanceof String)
          try {
            frame.operandStack.push(Double.parseDouble((String)op));
          }
          catch(Exception e) {
            error("error converting string to double", frame);
          }
        else {
          frame.operandStack.push((double)((int)op));
        }
      }

      else if (instr.opcode() == OpCode.TOSTR) {
        Object op = frame.operandStack.pop();
        if (op instanceof Integer) {
          frame.operandStack.push(Integer.toString((Integer)op));
        } else {
          frame.operandStack.push(Double.toString((Double)op));
        }
      }
      //------------------------------------------------------------
      // Heap related
      //------------------------------------------------------------

      else if (instr.opcode() == OpCode.ALLOC) {      
        // TODO
        List<String> op = (List<String>)instr.operand();
        int oid = objectId++;
        Map<String, Object> heapObj = new HashMap<>();
        for(String field : op) {
          heapObj.put(field, null);
        }
        heap.put(oid, heapObj);
        frame.operandStack.push(oid);
      }

      else if (instr.opcode() == OpCode.FREE) {
        // pop the oid to 
        Object oid = frame.operandStack.pop();
        ensureNotNil(frame, oid);
        // remove the object with oid from the heap
        heap.remove((int)oid);
      }
      else if (instr.opcode() == OpCode.SETFLD) {
        String op_string = (String)instr.operand();
        Object op0 = frame.operandStack.pop();
        Object id = frame.operandStack.pop();
        Integer location = -1;
        if (id instanceof Integer) {
          location = (Integer)id;
        } else {
          error("cant", frame);
        }
        Map<String, Object> obj = heap.get(location);
        obj.put(op_string, op0);
        heap.put(location, obj);
      }

      else if (instr.opcode() == OpCode.GETFLD) {
        Object id = frame.operandStack.pop();
        int location = -1;
        if (id instanceof Integer) {
          location = (int)id;
        } else {
          error("cant do nil", frame);
        }
        ensureNotNil(frame, location);
        Map<String, Object> obj = heap.get(location);
        Object value = obj.get((String)instr.operand());
        frame.operandStack.push(value);
        if(obj.get((String)instr.operand()) == null) {
          throw MyPLException.VMError("field does not exist");
        }
      }

      //------------------------------------------------------------
      // Special instructions
      //------------------------------------------------------------
        
      else if (instr.opcode() == OpCode.DUP) {
        Object op = frame.operandStack.pop();
        frame.operandStack.push(op);
        frame.operandStack.push(op);
      }

      else if (instr.opcode() == OpCode.SWAP) {
        Object op0 = frame.operandStack.pop();
        Object op1 = frame.operandStack.pop();
        frame.operandStack.push(op0);
        frame.operandStack.push(op1);
      }

      else if (instr.opcode() == OpCode.NOP) {
        // do nothing
      }
    }
  }

  
  // to print the lists of instructions for each VM Frame
  @Override
  public String toString() {
    String s = "";
    for (Map.Entry<String,VMFrame> e : frames.entrySet()) {
      String funName = e.getKey();
      s += "Frame '" + funName + "'\n";
      List<VMInstr> instructions = e.getValue().instructions;      
      for (int i = 0; i < instructions.size(); ++i) {
        VMInstr instr = instructions.get(i);
        s += "  " + i + ": " + instr + "\n";
      }
      // s += "\n";
    }
    return s;
  }

  
  //----------------------------------------------------------------------
  // HELPER FUNCTIONS
  //----------------------------------------------------------------------

  // error
  private void error(String m, VMFrame f) throws MyPLException {
    int pc = f.pc - 1;
    VMInstr i = f.instructions.get(pc);
    String name = f.functionName();
    m += " (in " + name + " at " + pc + ": " + i + ")";
    throw MyPLException.VMError(m);
  }

  // error if given value is nil
  private void ensureNotNil(VMFrame f, Object v) throws MyPLException {
    if (v == NIL_OBJ)
      error("Nil reference", f);
  }
  
  
}

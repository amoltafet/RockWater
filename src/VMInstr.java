/*
 * File: Instr.java
 * Date: Spring 2022
 * Auth: S. Bowers
 * Desc: Class to represent a MyPL instruction. Includes static helper
 *       functions for creating instructions. Instructions can also
 *       consist of comments (via addComment)
 */

import java.util.List;


public class VMInstr {

  private OpCode opcode = null;
  private Object operand = null;
  private String comment = null;
  
  
  public VMInstr(OpCode opcode) {
    this.opcode = opcode;
    this.operand = null;
  }
  
  public VMInstr(OpCode opcode, Object operand) {
    this.opcode = opcode;
    this.operand = operand;
  }

  public void addComment(String comment) {
    this.comment = comment;
  }
  
  public Object operand() {
    return operand;
  }

  public void updateOperand(Object operand) {
    this.operand = operand;
  }

  public OpCode opcode() {
    return opcode;
  }

  public String comment() {
    return comment;
  }
  
  public String toString() {
    String str = opcode.toString();
    // print the operand
    if (operand != null && operand != VM.NIL_OBJ) {
      str += " " + operand.toString();
      // pretty print special chars:
      str = str.replace("\n", "\\n");
      str = str.replace("\r", "\\r");
      str = str.replace("\t", "\\t");
    }
    else if (operand == VM.NIL_OBJ) {
      str += " NIL_OBJ";
    }
    // print the comment
    if (comment != null)
      str += "  // " + comment;
    return str;
  }

  public static VMInstr PUSH(Object x) {
    return new VMInstr(OpCode.PUSH, x);
  }

  public static VMInstr POP() {
    return new VMInstr(OpCode.POP);
  }

  public static VMInstr LOAD(int address) {
    return new VMInstr(OpCode.LOAD, address);    
  }

  public static VMInstr STORE(int address) {
    return new VMInstr(OpCode.STORE, address);    
  }

  public static VMInstr ADD() {
    return new VMInstr(OpCode.ADD);
  }

  public static VMInstr SUB() {
    return new VMInstr(OpCode.SUB);
  }

  public static VMInstr MUL() {
    return new VMInstr(OpCode.MUL);
  }

  public static VMInstr DIV() {
    return new VMInstr(OpCode.DIV);
  }

  public static VMInstr MOD() {
    return new VMInstr(OpCode.MOD);
  }

  public static VMInstr AND() {
    return new VMInstr(OpCode.AND);
  }

  public static VMInstr OR() {
    return new VMInstr(OpCode.OR);
  }
  
  public static VMInstr NOT() {
    return new VMInstr(OpCode.NOT);
  }

  public static VMInstr CMPLT() {
    return new VMInstr(OpCode.CMPLT);
  }

  public static VMInstr CMPLE() {
    return new VMInstr(OpCode.CMPLE);
  }

  public static VMInstr CMPGT() {
    return new VMInstr(OpCode.CMPGT);
  }

  public static VMInstr CMPGE() {
    return new VMInstr(OpCode.CMPGE);
  }

  public static VMInstr CMPEQ() {
    return new VMInstr(OpCode.CMPEQ);
  }

  public static VMInstr CMPNE() {
    return new VMInstr(OpCode.CMPNE);
  }

  public static VMInstr NEG() {
    return new VMInstr(OpCode.NEG);
  }
  
  public static VMInstr JMP(int address) {
    return new VMInstr(OpCode.JMP, address);
  }

  public static VMInstr JMPF(int address) {
    return new VMInstr(OpCode.JMPF, address);
  }

  public static VMInstr CALL(String funName) {
    return new VMInstr(OpCode.CALL, funName);    
  }

  public static VMInstr VRET() {
    return new VMInstr(OpCode.VRET);
  }
  
  public static VMInstr WRITE() {
    return new VMInstr(OpCode.WRITE);
  }

  public static VMInstr READ() {
    return new VMInstr(OpCode.READ);
  }

  public static VMInstr LEN() {
    return new VMInstr(OpCode.LEN);
  }

  public static VMInstr GETCHR() {
    return new VMInstr(OpCode.GETCHR);
  }

  public static VMInstr TOINT() {
    return new VMInstr(OpCode.TOINT);
  }

  public static VMInstr TODBL() {
    return new VMInstr(OpCode.TODBL);
  }

  public static VMInstr TOSTR() {
    return new VMInstr(OpCode.TOSTR);
  }
  
  public static VMInstr ALLOC(List<String> fields) {
    return new VMInstr(OpCode.ALLOC, fields);
  }

  public static VMInstr FREE() {
    return new VMInstr(OpCode.FREE);
  }

  public static VMInstr SETFLD(String field) {
    return new VMInstr(OpCode.SETFLD, field);
  }

  public static VMInstr GETFLD(String field) {
    return new VMInstr(OpCode.GETFLD, field);
  }

  public static VMInstr DUP() {
    return new VMInstr(OpCode.DUP);
  }

  public static VMInstr SWAP() {
    return new VMInstr(OpCode.SWAP);
  }
  
  public static VMInstr NOP() {
    return new VMInstr(OpCode.NOP);
  }


  
}
  

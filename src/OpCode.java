/*
 * File: OpCode.java
 * Date: Spring 2022
 * Auth: S. Bowers
 * Desc: List of opcodes and their semantics.
 */


public enum OpCode {

  // consts/vars
  PUSH,         // push operand onto stack
  POP,          // pop value off of stack
  LOAD,         // push value at memory address onto stack
  STORE,        // pop x off stack, store x at memory address

  // ops
  ADD,          // pop x and y off stack, push (y + x) onto stack
  SUB,          // pop x and y off stack, push (y - x) onto stack
  MUL,          // pop x and y off stack, push (y * x) onto stack
  DIV,          // pop x and y off stack, push (y / x) onto stack
  MOD,          // pop x and y off stack, push (y % x) onto stack
  AND,          // pop bools x and y, push (y and x)
  OR,           // pop bools x and y, push (y or x)
  NOT,          // pop bool x, push (not x)
  CMPLT,        // pop x and y off stack, push (y < x)
  CMPLE,        // pop x and y off stack, push (y <= x)
  CMPGT,        // pop x and y off stack, push (y > x)
  CMPGE,        // pop x and y off stack, push (y >= x)
  CMPEQ,        // pop x and y off stack, push (y == x)  
  CMPNE,        // pop x and y off stack, push (y != x)
  NEG,          // pop x, push (-x)

  // jump
  JMP,          // jump to given instruction
  JMPF,         // pop x off stack, if x is false jump to instruction

  // functions
  CALL,         // calls the function f
  VRET,         // pop x, exit from function passing back x

  // built-ins
  WRITE,        // pop x, write to stdout
  READ,         // read stdin, push on stack
  LEN,          // pop (string) x, push x.length()
  GETCHR,       // pop (string) x, pop y, push x.substring(y, y+1)
  TOINT,        // pop x, push x as an integer
  TODBL,        // pop x, push x as a double
  TOSTR,        // pop x, push x.toString()
  
  // heap 
  ALLOC,        // allocate obj w/ atts-list, push y (oid)
  FREE,         // pop x, deallocate object with oid x
  SETFLD,       // set field f: pop x and y, set obj(y).f = x
  GETFLD,       // get field f: pop x, push obj(x).f value
  
  // special
  DUP,          // pop x, push x, push x
  SWAP,         // pop x, pop y, push x, push y
  NOP           // has no effect (for jumping over code segments)
  ;
}

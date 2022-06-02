/*
 * File: HW6.java
 * Date: Spring 2022
 * Auth: Ahmad Moltafet
 * Desc: Example program to test the MyPL VM
 */


/*----------------------------------------------------------------------
   Your job for this part of the assignment is to imlement the
   following as a set of MyPL VM instructions and the VM. Note that
   you must implement the is_prime function and generally folow the
   approach laid out below. You can view the following as pseudocode
   (which could have been written in any procedural programming
   language). Note that since we don't have a square root function in
   MyPL, our naive primality tester is not very efficient.

    fun bool is_prime(int n) {
      var m = n / 2
      var v = 2
      while v <= m {
        var r = n / v
        var p = r * v
        if p == n {
          return false
        }
        v = v + 1
      }
      return true
    }

    fun void main() {
      print("Please enter integer values to sum (prime number to quit)\n")
      var sum = 0
      while true {
        print(">> Enter an int: ")
        var val = stoi(read())
        if is_prime(val) {
          print("The sum is: " + itos(sum) + "\n")
          print("Goodbye!\n")
          return
        }
        sum = sum + val
      }
    }
----------------------------------------------------------------------*/  

public class HW6 {

  public static void main(String[] args) throws Exception {
    VM vm = new VM();
    // TODO
    VMFrame is_prime = new VMFrame("is_prime", 1);
    is_prime.instructions.add(VMInstr.PUSH(0));
    is_prime.instructions.add(VMInstr.STORE(0)); 
    is_prime.instructions.add(VMInstr.PUSH(0));
    is_prime.instructions.add(VMInstr.STORE(1)); 
    is_prime.instructions.add(VMInstr.LOAD(0)); 
    is_prime.instructions.add(VMInstr.PUSH(3));
    is_prime.instructions.add(VMInstr.DIV()); 
    is_prime.instructions.add(VMInstr.STORE(1)); 
    is_prime.instructions.add(VMInstr.PUSH(2)); 
    is_prime.instructions.add(VMInstr.STORE(2)); 
    // while loop
    is_prime.instructions.add(VMInstr.LOAD(2)); 
    is_prime.instructions.add(VMInstr.LOAD(1)); 
    is_prime.instructions.add(VMInstr.CMPLE()); 
    is_prime.instructions.add(VMInstr.JMPF(31)); 
    // var r = n / v
    is_prime.instructions.add(VMInstr.LOAD(0)); 
    is_prime.instructions.add(VMInstr.LOAD(2)); 
    is_prime.instructions.add(VMInstr.DIV()); 
    is_prime.instructions.add(VMInstr.STORE(3)); 
    // var p = r * v
    is_prime.instructions.add(VMInstr.LOAD(3)); 
    is_prime.instructions.add(VMInstr.LOAD(2));
    is_prime.instructions.add(VMInstr.MUL()); 
    is_prime.instructions.add(VMInstr.STORE(4));
    // if p == n {
    is_prime.instructions.add(VMInstr.LOAD(4)); 
    is_prime.instructions.add(VMInstr.LOAD(0)); 
    is_prime.instructions.add(VMInstr.CMPEQ()); 
    is_prime.instructions.add(VMInstr.JMPF(27)); 
    // return false
    is_prime.instructions.add(VMInstr.VRET()); 
    is_prime.instructions.add(VMInstr.JMP(33)); 
    // v = v + 1
    is_prime.instructions.add(VMInstr.LOAD(2)); 
    is_prime.instructions.add(VMInstr.PUSH(1));
    is_prime.instructions.add(VMInstr.ADD()); 
    is_prime.instructions.add(VMInstr.STORE(2)); 
    // return true
    is_prime.instructions.add(VMInstr.VRET()); 
    is_prime.instructions.add(VMInstr.NOP());
    vm.add(is_prime);
    // main 
    VMFrame main = new VMFrame("main", 1);
    vm.add(main);
    // print()
    main.instructions.add(VMInstr.PUSH("Please enter integer values to sum (prime number to quit)\n")); 
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.PUSH(0)); 
    main.instructions.add(VMInstr.STORE(0)); 

    main.instructions.add(VMInstr.PUSH(">> Enter an int: ")); 
    main.instructions.add(VMInstr.WRITE());

    main.instructions.add(VMInstr.PUSH(0)); 
    main.instructions.add(VMInstr.STORE(1));            

    main.instructions.add(VMInstr.CALL("read"));
    main.instructions.add(VMInstr.TOSTR());
    main.instructions.add(VMInstr.STORE(2));

    main.instructions.add(VMInstr.PUSH(2));
    main.instructions.add(VMInstr.CALL("is_prime"));
    main.instructions.add(VMInstr.PUSH(true)); 
    main.instructions.add(VMInstr.CMPEQ());
    main.instructions.add(VMInstr.JMPF(24)); 
    main.instructions.add(VMInstr.LOAD(0)); 
    main.instructions.add(VMInstr.TOINT()); 
    main.instructions.add(VMInstr.STORE(3)); 
    main.instructions.add(VMInstr.PUSH("The sum is")); 
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.LOAD(3));
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.PUSH("\n Goodbye!"));  
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.VRET()); 
    main.instructions.add(VMInstr.LOAD(0)); 
    main.instructions.add(VMInstr.LOAD(1)); 
    main.instructions.add(VMInstr.ADD()); 
    main.instructions.add(VMInstr.NOP());
    vm.run();
  }
}

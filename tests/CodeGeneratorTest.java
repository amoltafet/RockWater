/*
 * File: CodeGeneratorTest.java
 * Date: Spring 2022
 * Auth: S. Bowres
 * Desc: Basic unit tests for the MyPL code generator class. Note that
 *       these tests use the VM to test the code generator.
 */


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.Before;
import org.junit.After;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;


public class CodeGeneratorTest {

  private PrintStream stdout = System.out;
  private ByteArrayOutputStream output = new ByteArrayOutputStream(); 

  @Before
  public void changeSystemOut() {
    // redirect System.out to output
    System.setOut(new PrintStream(output));
  }

  @After
  public void restoreSystemOut() {
    // reset System.out to standard out
    System.setOut(stdout);
  }

  //------------------------------------------------------------
  // HELPER FUNCTIONS
  //------------------------------------------------------------
  
  private static VM buildVM(String s) throws Exception {
    InputStream in = new ByteArrayInputStream(s.getBytes("UTF-8"));
    ASTParser parser = new ASTParser(new Lexer(in));
    Program program = parser.parse();
    TypeInfo  typeInfo = new TypeInfo();
    program.accept(new StaticChecker(typeInfo));
    VM vm = new VM();
    CodeGenerator genVisitor = new CodeGenerator(typeInfo, vm);
    program.accept(genVisitor);
    return vm;
  }

  private static String buildString(String... args) {
    String str = "";
    for (String s : args)
      str += s + "\n";
    return str;
  }


  //------------------------------------------------------------
  // Basics
  //------------------------------------------------------------

   @Test
  public void emptyProgram() throws Exception {
    String s = buildString
      ("fun void main() {",
       "}");
    VM vm = buildVM(s);
    vm.run();
    assertEquals("", output.toString());
  }

   @Test
  public void simplePrint() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  print(\"Hello World!\")",
       "}");
    VM vm = buildVM(s);
    vm.run();
    assertEquals("Hello World!", output.toString());
  } 

  //------------------------------------------------------------
  // Basic Variables and Assignement
  //------------------------------------------------------------

   @Test
  public void simpleVariable() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var x1 = 3",
       "  var x2 = true",
       "  var x3 = 'a'",
       "  var x4 = 2.7",
       "  print(x1)",
       "  print(x2)",
       "  print(x3)",
       "  print(x4)",
       "}");
    VM vm = buildVM(s);
    vm.run();
    assertEquals("3truea2.7", output.toString());
  } 

   @Test
  public void simpleVariableAssignment() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var x = 3",
       "  print(x)", 
       "  x = 4",
       "  print(x)",
       "}");
    VM vm = buildVM(s);
    vm.run();
    assertEquals("34", output.toString());
  } 

  //------------------------------------------------------------
  // Arithmetic Expressions
  //------------------------------------------------------------

   @Test
  public void simpleAdd() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var x = 3 + 4",
       "  var y = 3.25 + 4.5",
       "  print(x)", 
       "  print(' ')", 
       "  print(y)",
       "}");
    VM vm = buildVM(s);
    vm.run();
    assertEquals("7 7.75", output.toString());
  } 

   @Test
  public void stringCharAdd() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var x = \"abc\" + 'd'",
       "  var y = 'a' + \"bcd\"", 
       "  var z = \"ab\" + \"cd\"", 
       "  print(x)", 
       "  print(' ')", 
       "  print(y)",
       "  print(' ')",
       "  print(z)",
       "}");
    VM vm = buildVM(s);
    vm.run();
    assertEquals("abcd abcd abcd", output.toString());
  } 

   @Test
  public void longerIntExpr() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var x = 3 + (6 - 5) + (5 * 2) + (2 / 2)",
       "  var y = x % 2",
       "  print(x)", 
       "  print(' ')", 
       "  print(y)",
       "}");
    VM vm = buildVM(s);
    vm.run();
    assertEquals("15 1", output.toString());
  } 

   @Test
  public void negExpr() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var x = neg 1",
       "  var y = neg (3 * 4)", 
       "  var z = neg (3.0 - 1.75)", 
       "  print(x)", 
       "  print(' ')", 
       "  print(y)",
       "  print(' ')",
       "  print(z)",
       "}");
    VM vm = buildVM(s);
    vm.run();
    assertEquals("-1 -12 -1.25", output.toString());
  } 
    
  //------------------------------------------------------------
  // Basic Function Calls
  //------------------------------------------------------------

   @Test
  public void noArgCall() throws Exception {
    String s = buildString
      ("fun void f() {}",
       "fun void main() {",
       "  print(f())", 
       "}");
    VM vm = buildVM(s);
    vm.run();
    assertEquals("nil", output.toString());
  } 

   @Test
  public void oneArgCall() throws Exception {
    String s = buildString
      ("fun int f(int x) {return x}",
       "fun void main() {",
       "  print(f(3))", 
       "}");
    VM vm = buildVM(s);
    vm.run();
    assertEquals("3", output.toString());
  } 

   @Test
  public void twoArgCall() throws Exception {
    String s = buildString
      ("fun int f(int x, int y) {return x / y}",
       "fun void main() {",
       "  print(f(4, 2))", 
       "}");
    VM vm = buildVM(s);
    vm.run();
    assertEquals("2", output.toString());
  } 

   @Test
  public void threeArgCall() throws Exception {
    String s = buildString
      ("fun int f(int x, int y, int z) {return (x - y) - z}",
       "fun void main() {",
       "  print(f(6, 4, 2))", 
       "}");
    VM vm = buildVM(s);
    vm.run();
    assertEquals("0", output.toString());
  }
  
   @Test
  public void multiLevelCall() throws Exception {
    String s = buildString
      ("fun string f(string s) {",
       "  return s",
       "}",
       "fun string g(string s1, string s2) {",
       "  return f(s1) + s2",
       "}",
       "fun string h(string s1, string s2, string s3) {",
       "  return g(s1, s2) + s3",
       "}",
       "fun void main() {",
       "  print(h(\"go\", \"gon\", \"zaga\"))",
       "}");
    VM vm = buildVM(s);
    vm.run();
    assertEquals("gogonzaga", output.toString());
  }
  
  //------------------------------------------------------------
  // Built-In Functions
  //------------------------------------------------------------

   @Test
  public void builtInFunctions() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var c = get(1, \"zags\")",
       "  print(c) print(' ')",
       "  var n = length(\"bulldogs\")",
       "  print(n) print(' ')",
       "  var x = stoi(\"3\")",
       "  print(x) print(' ')",
       "  var y = stod(\"2.5\")",
       "  print(y) print(' ')",
       "  var s = itos(4)",
       "  print(s) print(' ')",
       "  var t = dtos(1.25)",
       "  print(t)",
       "}");
    VM vm = buildVM(s);
    vm.run();
    assertEquals("a 8 3 2.5 4 1.25", output.toString());
  }

  //------------------------------------------------------------
  // Boolean Expressions
  //------------------------------------------------------------

   @Test
  public void andOperator() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var x1 = true and true",
       "  var x2 = true and false",
       "  var x3 = false and true",
       "  var x4 = false and false",
       "  print(x1) print(' ')",
       "  print(x2) print(' ')",
       "  print(x3) print(' ')",
       "  print(x4)",
       "}");
    VM vm = buildVM(s);
    vm.run();
    assertEquals("true false false false", output.toString());
  }

   @Test
  public void orOperator() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var x1 = true or true",
       "  var x2 = true or false",
       "  var x3 = false or true",
       "  var x4 = false or false",
       "  print(x1) print(' ')",
       "  print(x2) print(' ')",
       "  print(x3) print(' ')",
       "  print(x4)",
       "}");
    VM vm = buildVM(s);
    vm.run();
    assertEquals("true true true false", output.toString());
  }

   @Test
  public void notOperator() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var x1 = not true", 
       "  var x2 = not false", 
       "  print(x1) print(' ')",
       "  print(x2)",
       "}");
    VM vm = buildVM(s);
    vm.run();
    assertEquals("false true", output.toString());
  }

   @Test
  public void booleanOperatorExpression() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var x = (true or false) and ((not true or false) or true)", 
       "  print(x)",
       "}");
    VM vm = buildVM(s);
    vm.run();
    assertEquals("true", output.toString());
  }

   @Test
  public void trueNumberRelationalOps() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var x1 = 3 < 4",
       "  var x2 = 3 <= 4",
       "  var x3 = 3 <= 3",
       "  var x4 = 4 > 3",
       "  var x5 = 4 >= 3",
       "  var x6 = 3 >= 3",
       "  var x7 = 3 == 3",
       "  var x8 = 3 != 4",
       "  print(x1 and x2 and x3 and x4 and x5 and x6 and x7 and x8)",
       "  var y1 = 3 < 4",
       "  var y2 = 3 <= 4",
       "  var y3 = 3 <= 3",
       "  var y4 = 4 > 3",
       "  var y5 = 4 >= 3",
       "  var y6 = 3 >= 3",
       "  var y7 = 3 == 3",
       "  var y8 = 3 != 4",
       "  print(y1 and y2 and y3 and y4 and y5 and y6 and y7 and y8)",
       "}");
    VM vm = buildVM(s);
    vm.run();
    assertEquals("truetrue", output.toString());
  }

   @Test
  public void falseNumberRelationalOps() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var x1 = 4 < 3",
       "  var x2 = 4 <= 3",
       "  var x3 = 3 > 4",
       "  var x4 = 3 >= 4",
       "  var x5 = 3 == 4",
       "  var x6 = 3 != 3",
       "  print(x1 or x2 or x3 or x4 or x5 or x6)",
       "  var y1 = 4.25 < 3.25",
       "  var y2 = 4.25 <= 3.25",
       "  var y3 = 3.25 > 4.25",
       "  var y4 = 3.25 >= 4.25",
       "  var y5 = 3.25 == 4.25",
       "  var y6 = 3.25 != 3.25",
       "  print(y1 or y2 or y3 or y4 or y5 or y6)",
       "}");
    VM vm = buildVM(s);
    vm.run();
    assertEquals("falsefalse", output.toString());
  }

   @Test
  public void trueAlphaRelationalOps() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var c1 = 'a'",
       "  var c2 = 'b'",
       "  var x1 = c1 < c2",
       "  var x2 = c1 <= c2",
       "  var x3 = c1 <= c1",
       "  var x4 = c2 > c1",
       "  var x5 = c2 >= c1",
       "  var x6 = c1 >= c1",
       "  var x7 = c1 == c1",
       "  var x8 = c1 != c2",
       "  print(x1 and x2 and x3 and x4 and x5 and x6 and x7 and x8)",
       "  var s1 = \"aa\"",
       "  var s2 = \"ab\"",
       "  var y1 = s1 < s2",
       "  var y2 = s1 <= s2",
       "  var y3 = s1 <= s1",
       "  var y4 = s2 > s1",
       "  var y5 = s2 >= s1",
       "  var y6 = s1 >= s1",
       "  var y7 = s1 == s1",
       "  var y8 = s1 != s2",
       "  print(y1 and y2 and y3 and y4 and y5 and y6 and y7 and y8)",
       "}");
    VM vm = buildVM(s);
    vm.run();
    assertEquals("truetrue", output.toString());
  }
  
   @Test
  public void falseAlphaRelationalOps() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var c1 = 'a'",
       "  var c2 = 'b'", 
       "  var x1 = c2 < c1",
       "  var x2 = c2 <= c1",
       "  var x3 = c1 > c2",
       "  var x4 = c1 >= c2",
       "  var x5 = c1 == c2",
       "  var x6 = c1 != c1",
       "  print(x1 or x2 or x3 or x4 or x5 or x6)",
       "  var s1 = \"aa\"",
       "  var s2 = \"ab\"", 
       "  var y1 = s2 < s1",
       "  var y2 = s2 <= s1",
       "  var y3 = s1 > s2",
       "  var y4 = s1 >= s2",
       "  var y5 = s1 == s2",
       "  var y6 = s1 != s1",
       "  print(y1 or y2 or y3 or y4 or y5 or y6)",
       "}");
    VM vm = buildVM(s);
    vm.run();
    assertEquals("falsefalse", output.toString());
  }

   @Test
  public void nilComparisons() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var i = 3",
       "  var d = 2.75",
       "  var c = 'a'", 
       "  var s = \"abc\"",
       "  var b = false", 
       "  var x1 = nil != nil", 
       "  var x2 = (i == nil) or (d == nil) or (c == nil) or (s == nil) or ",
       "           (b == nil)",
       "  var x3 = (nil == i) or (nil == d) or (nil == c) or (nil == s) or ",
       "           (nil == b)",
       "  print(x1 or x2 or x3)", 
       "  var x4 = nil == nil",
       "  var x5 = (i != nil) or (d != nil) or (c != nil) or (s != nil) or ",
       "           (b != nil)",
       "  var x6 = (nil != i) or (nil != d) or (nil != c) or (nil != s) or ",
       "           (nil != b)",
       "  print(x4 and x5 and x6)",
       "}");
    VM vm = buildVM(s);
    vm.run();
    assertEquals("falsetrue", output.toString());
  }
  
  //------------------------------------------------------------
  // While Loops
  //------------------------------------------------------------
  
   @Test
  public void basicWhile() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var i = 0",
       "  while i < 5 {",
       "    i = i + 1",
       "  }",
       "  print(i)", 
       "}");
    VM vm = buildVM(s);
    vm.run();
    assertEquals("5", output.toString());
  }

   @Test
  public void moreInvolvedWhile() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var i = 0",
       "  while i < 5 {",
       "    var j = i * 2",
       "    print(j)",
       "    i = i + 1",
       "  }",
       "}");
    VM vm = buildVM(s);
    vm.run();
    assertEquals("02468", output.toString());
  }

   @Test
  public void nestedWhile() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var i = 0",
       "  while i < 5 {",
       "    print(i)",
       "    print(' ')",
       "    var j = 0",
       "    while j < i {",
       "      print(j)", 
       "      j = j + 1",
       "    }",
       "    i = i + 1", 
       "  }",
       "}");
    VM vm = buildVM(s);
    vm.run();
    assertEquals("0 1 02 013 0124 0123", output.toString());
  }
  
  //------------------------------------------------------------
  // For Loops
  //------------------------------------------------------------

   @Test
  public void basicFor() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  for i from 1 upto 5 {",
       "    print(i)",
       "  }",
       "  print(' ')",
       "  for i from 5 downto 1 {",
       "    print(i)",
       "  }",
       "}");
    VM vm = buildVM(s);
    vm.run();
    assertEquals("12345 54321", output.toString());
  }

   @Test
  public void conditionalFor() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var start = 1",
       "  var end = 10",
       "  for i from start upto end {",
       "    print(i)",
       "    end = end - 1",
       "  }",
       "}");
    VM vm = buildVM(s);
    vm.run();
    assertEquals("12345", output.toString());
  }

   @Test
  public void nestedFor() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var x = 0",
       "  for i from 1 upto 5 {", 
       "    for j from 1 upto 4 {",
       "      x = x + i*j",
       "    }",
       "    i = i + 1",
       "  }",
       "  print(x)", 
       "}");
    VM vm = buildVM(s);
    vm.run();
    assertEquals("90", output.toString());
  }
  
  //------------------------------------------------------------
  // If-Then-Else Statements
  //------------------------------------------------------------

   @Test
  public void basicIf() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  print(0)",
       "  if 3 < 4 {", 
       "    print(' ')",
       "    print(1)",
       "  }",
       "  print(' ')", 
       "  print(0)",
       "}");
    VM vm = buildVM(s);
    vm.run();
    assertEquals("0 1 0", output.toString());
  }

   @Test
  public void consecutiveIfs() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  print(0)",
       "  if 3 < 4 {", 
       "    print(' ')",
       "    print(1)",
       "  }",
       "  if true {", 
       "    print(' ')",
       "    print(2)",
       "  }",
       "  if 4 < 3 {", 
       "    print(' ')",
       "    print(3)",
       "  }",
       "  print(' ')", 
       "  print(0)",
       "}");
    VM vm = buildVM(s);
    vm.run();
    assertEquals("0 1 2 0", output.toString());
  }

   @Test
  public void basicElif() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  print(0)",
       "  if 3 < 4 {", 
       "    print(' ')",
       "    print(1)",
       "  }",
       "  elif 4 > 3 {",
       "    print(' ')",
       "    print(2)",
       "  }",
       "  else {",
       "    print(' ')",
       "    print(3)",
       "  }",
       "  if 4 < 3 {", 
       "    print(' ')",
       "    print(1)",
       "  }",
       "  elif 3 < 4 {",
       "    print(' ')",
       "    print(2)",
       "  }",
       "  else {",
       "    print(' ')",
       "    print(3)",
       "  }",
       "  if 4 < 3 {", 
       "    print(' ')",
       "    print(1)",
       "  }",
       "  elif 3 != 3 {",
       "    print(' ')",
       "    print(2)",
       "  }",
       "  else {",
       "    print(' ')",
       "    print(3)",
       "  }",
       "  print(' ')", 
       "  print(0)",
       "}");
    VM vm = buildVM(s);
    vm.run();
    assertEquals("0 1 2 3 0", output.toString());
  }
  
  //------------------------------------------------------------
  // Recursive Functions
  //------------------------------------------------------------

   @Test
  public void basicRecursion() throws Exception {
    String s = buildString
      ("fun int non_negative_sum(int x) {",
       "  if x <= 0 {",
       "    return 0",
       "  }", 
       "  return x + non_negative_sum(x - 1)",
       "}",
       "fun void main() {",
       "  print(non_negative_sum(0))",
       "  print(' ')",
       "  print(non_negative_sum(1))",
       "  print(' ')",
       "  print(non_negative_sum(10))",
       "}");
    VM vm = buildVM(s);
    vm.run();
    assertEquals("0 1 55", output.toString());
  }
  
  //------------------------------------------------------------
  // User-Defined Types
  //------------------------------------------------------------

   @Test
  public void basicUDT() throws Exception {
    String s = buildString
      ("type T {", 
       "  var x = 0", 
       "  var y = true",
       "}", 
       "fun void main() {",
       "  var t1 = new T",
       "  var t2 = new T",
       "  print(t1)",
       "  print(' ')",
       "  print(t2)",
       "}");
    VM vm = buildVM(s);
    vm.run();
    assertEquals("1111 1112", output.toString());
  }

   @Test
  public void basicPathsUDT() throws Exception {
    String s = buildString
      ("type T {", 
       "  var x = 0", 
       "  var y = true",
       "}", 
       "fun void main() {",
       "  var t = new T",
       "  t.x = 3",
       "  var x = t.x",
       "  print(x)",
       "  print(' ')", 
       "  t.y = false", 
       "  var y = t.y", 
       "  print(y)",
       "}");
    VM vm = buildVM(s);
    vm.run();
    assertEquals("3 false", output.toString());
  }

   @Test
  public void basicTwoLevelUDT() throws Exception {
    String s = buildString
      ("type T {", 
       "  var x = 0", 
       "  var y = true",
       "}",
       "type S {",
       "  var T t = new T",
       "}",
       "fun void main() {",
       "  var s = new S",
       "  s.t.x = 3",
       "  var x = s.t.x",
       "  print(x)",
       "  print(' ')", 
       "  print(s.t.y)",
       "}");
    VM vm = buildVM(s);
    vm.run();
    assertEquals("3 true", output.toString());
  }
  
   @Test
  public void basicRecursiveUDT() throws Exception {
    String s = buildString
      ("type T {", 
       "  var x = 0", 
       "  var y = true",
       "  var T t = nil",
       "}",
       "fun void main() {",
       "  var t1 = new T",
       "  print(t1.x)",
       "  print(t1.y)",
       "  print(t1.t)",
       "  var t2 = new T",
       "  t2.x = 3",
       "  t1.t = t2",
       "  print(' ')",
       "  print(t1.t)",
       "  print(t1.t.x)",
       "  print(t1.t.y)",
       "}");
    VM vm = buildVM(s);
    vm.run();
    assertEquals("0truenil 11123true", output.toString());
  }
  
  //------------------------------------------------------------
  // Runtime errors
  //------------------------------------------------------------
  
   @Test
  public void nilVarDerefence() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var int x = nil",
       "  var y = 3 + x",
       "}");
    VM vm = buildVM(s);
    try {
      vm.run();
      fail("runtime error not detected");
    } catch(MyPLException e) {
      // to test message: assertEquals("...", e.getMessage());
      // to print message: stdout.println(e.toString());
    }
  }

   @Test
  public void invalidIndex() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var s = \"foobar\"",
       "  var y = get(6, s)",
       "}");
    VM vm = buildVM(s);
    try {
      vm.run();
      fail("runtime error not detected");
    } catch(MyPLException e) {
      // to test message: assertEquals("...", e.getMessage());
      // to print message: stdout.println(e.toString());
    }
  }

   @Test
  public void invalidIntConversion() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var s = \"bar\"",
       "  var y = stoi(s)",
       "}");
    VM vm = buildVM(s);
    try {
      vm.run();
      fail("runtime error not detected");
    } catch(MyPLException e) {
      // to test message: assertEquals("...", e.getMessage());
      // to print message: stdout.println(e.toString());
    }
  }

   @Test
  public void invalidDoubleConversion() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var s = \"bar\"",
       "  var y = stod(s)",
       "}");
    VM vm = buildVM(s);
    try {
      vm.run();
      fail("runtime error not detected");
    } catch(MyPLException e) {
      // to test message: assertEquals("...", e.getMessage());
      // to print message: stdout.println(e.toString());
    }
  }

   @Test
  public void invalidHeapAccess() throws Exception {
    String s = buildString
      ("type T {",
       "  var x = 0",
       "}",       
       "fun void main() {",
       "  var t = new T", 
       "  delete t",
       "  print(t.x)",
       "}");
    VM vm = buildVM(s);
    try {
      vm.run();
      fail("runtime error not detected");
    } catch(MyPLException e) {
      // to test message: assertEquals("...", e.getMessage());
      // to print message: stdout.println(e.toString());
    }
  }
  
  
}

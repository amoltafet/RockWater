/*
 * File: StatiCheckerTest.java
 * Date: Spring 2022
 * Auth: S. Bowers
 * Desc: Various static analysis tests for HW-5
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Ignore;
import org.junit.Test;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.ArrayList;


public class StaticCheckerTest {

  //------------------------------------------------------------
  // HELPER FUNCTIONS
  //------------------------------------------------------------

  private static ASTParser buildParser(String s) throws Exception {
    InputStream in = new ByteArrayInputStream(s.getBytes("UTF-8"));
    ASTParser parser = new ASTParser(new Lexer(in));
    return parser;
  }
  
  private static StaticChecker buildChecker() throws Exception {
    TypeInfo types = new TypeInfo();
    StaticChecker checker = new StaticChecker(types);
    return checker;
  }

  private static String buildString(String... args) {
    String str = "";
    for (String s : args)
      str += s + "\n";
    return str;
  }

  //------------------------------------------------------------
  // BASIC FUNCTION AND TYPE DEFINITION CASES
  //------------------------------------------------------------

   @Test
  public void simpleProgramCheck() throws Exception {
    String s = buildString("fun void main() {}");
    buildParser(s).parse().accept(buildChecker());
  }

   @Test
  public void missingMain() throws Exception {
    String s = buildString("");
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

   @Test
  public void mainWithBadArgs() throws Exception {
    String s = buildString("fun void main(int x) {}");
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

   @Test
  public void mainWithBadReturn() throws Exception {
    String s = buildString("fun int main() {}");
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

   @Test
  public void redefineBuiltInFunction() throws Exception {
    String s = buildString
      ("fun void read(string msg) {}",
       "fun void main() {}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }
  
   @Test
  public void duplicateFunctionName() throws Exception {
    String s = buildString
      ("fun void f(int x) {}",
       "fun void f() {}",
       "fun void main() {}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

   @Test
  public void duplicateParams() throws Exception {
    String s = buildString
      ("fun void f(int x, int y, bool x) {}",
       "fun void main() {}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

   @Test
  public void duplicateTypeAndFunction() throws Exception {
    String s = buildString
      ("type T1 {}",
       "fun void T1() {}",
       "fun void main() {}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

   @Test
  public void duplicateTypes() throws Exception {
    String s = buildString
      ("type T1 {}",
       "type T2 {}",
       "type T1 {}",
       "fun void main() {}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  //------------------------------------------------------------
  // VARIABLE DECLARATIONS
  //------------------------------------------------------------
  
   @Test
  public void duplicateFields() throws Exception {
    String s = buildString
      ("type T1 {",
       "  var x = 0",
       "  var y = 0",
       "  var x = 0",
       "}",
       "fun void main() {}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

   @Test
  public void duplicateVarNames() throws Exception {
    // shadowing in current environment
    String s = buildString
      ("fun void main() {",
       "  var x = 0", 
       "  var y = 0",
       "  var x = 0",
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

   @Test 
  public void goodShadowingExample() throws Exception {
    // shadowing in current environment
    String s = buildString
      ("fun void main() {",
       "  var x = 0",
       "  var y = 0",
       "  while true {",
       "    var x = 0",
       "    var y = 1.0",
       "  }",
       "}"
       );
    buildParser(s).parse().accept(buildChecker());
  }
  
  @Test
  public void badImplicitVarType() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var x = nil", 
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void mismatchedVarTypes() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var string x = 0", 
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  //------------------------------------------------------------
  // EXPRESSIONS
  //------------------------------------------------------------
  
  @Test
  public void badLogicalNegation() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var x = not true",
       "  var y = not 1",
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void basicExpressionTypeMismatch() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var x = 3 + 4.1",
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void invalidNilComparison() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var x = 3 < nil",
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }
  
  @Test
  public void invalidVarFromNilComparison() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var int x = nil != 3.1 == nil",
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void invalidModExpression() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var int x = 12 % 3.1", 
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void invalidIntExpression() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var int x = 5 + \"6\"", 
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void invalidDoubleExpression() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var int x = 3.14 + 6", 
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void invalidDoubleModExpression() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var x = 3.14 % 1.2", 
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void invalidBoolArithmeticExpression() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var x = true + false", 
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

   @Test
  public void invalidBoolComparisonExpression() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var x = true <= false", 
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void invalidStringArithmeticExpression() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var x = \"x\" - \"y\"", 
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void invalidStringConcatExpression() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var x = \"6\" + 3", 
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void invalidStringComparisonExpression() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var x = (\"6\" + \"3\") == 63", 
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void invalidCharExpression() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var x = \'6\' - \'3\'", 
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void invalidCharPlusExpression() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var x = \'6\' + \'3\'", 
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void invalidCharComparisonExpression() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var x = \'6\' != 3", 
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void invalidTypeExpression() throws Exception {
    String s = buildString
      ("type T1 {}",
       "type T2 {}",
       "fun void main() {",
       "  var x = new T1",
       "  var y = new T2",
       "  var z = x < y", 
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void invalidTypeComparisonExpression() throws Exception {
    String s = buildString
      ("type T1 {}",
       "fun void main() {",
       "  var x = new T1",
       "  var z = x <= x", 
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void invalidLogicalNegationExpression() throws Exception {
    String s = buildString
      ("type T1 {}",
       "fun void main() {",
       "  var x = not (5 + 10)", 
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  //------------------------------------------------------------
  // IF, WHILE, FOR CASES
  //------------------------------------------------------------

  @Test
  public void nonBoolWhileCondition() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  while 3 + 4 {}",
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void invalidForStartExpression() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  for i from 1.0 upto 10 {}",
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void invalidForEndExpression() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  for i from 1 upto true {}",
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void invalidForLoopVarStatement() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  for i from 1 upto 10 {",
       "    var x = i + 3.0",
       "  }",
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void invalidIfExpression() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  if 1 + 2 {}",
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void invalidElseIfExpression() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  if false {}",
       "  elif true {}",
       "  elif 1 + 2 {}", 
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }
  
  //------------------------------------------------------------
  // NEW AND DELETE TESTS
  //------------------------------------------------------------

  @Test
  public void undefinedNewTypeExpression() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var x = new T1",
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void invalidNewTypeExpression() throws Exception {
    String s = buildString
      ("fun void f() {}",
       "fun void main() {",
       "  var x = new f",
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }
  
  @Test
  public void invalidPrimitiveDeleteType() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var x = 3",
       "  delete x",
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void invalidFunctionDeleteType() throws Exception {
    String s = buildString
      ("fun void f() {}",
       "fun void main() {",
       "  delete f",
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }
  
  //------------------------------------------------------------
  // FUNCTION CALL TESTS
  //------------------------------------------------------------

   @Test
  public void validPrintCalls() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  print(0)",
       "  print(1.0)",
       "  print(true)",
       "  print(\'a\')",
       "  print(\"\")",
       "  var int x = print(0)",
       "}"
       );
    buildParser(s).parse().accept(buildChecker());
  }

   @Test
  public void invalidPrintCall() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  print(0, 1)",
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void invalidPrintCallAssignment() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var x = print(0)",
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void invalidReadCall() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  read(0)", 
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void invalidReadCallAssignment() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var int x = read()", 
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void invalidGetCallArgCount() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  get(0)", 
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void invalidGetCallArgs() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  get(\"abc\", 0)", 
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void invalidGetAssignment() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var int x = get(0, \"abc\")", 
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void invalidStoiCall() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  stoi(0)", 
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void invalidStoiAssignment() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var char x = stoi(\"7\")", 
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void invalidStodCall() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  stod(0)", 
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void invalidStodAssignment() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var char x = stod(\"7.0\")", 
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void invalidItosCall() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  itos(3.1)", 
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void invalidItosAssignment() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var char x = itos(7)", 
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void invalidItodCall() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  itod(3.1)", 
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void invalidItodAssignment() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var char x = itod(7)", 
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void invalidDtosCall() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  dtos(0)", 
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void invalidDtosAssignment() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var char x = dtos(7.0)", 
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void invalidDtoiCall() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  dtoi(0)", 
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void invalidDtoiAssignment() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var char x = dtoi(7.0)", 
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }
  
  @Test
  public void undefinedFunctionCall() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  f()", 
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

   @Test
  public void invalidNumberOfArgsInFunctionCall() throws Exception {
    String s = buildString
      ("fun int f(char a, int c, double d, int b) {}",
       "fun void main() {",
       "  f('0', 1, 2.0)", 
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void mismatchedArgTypeInFunctionCall() throws Exception {
    String s = buildString
      ("fun int f(char a, int c, double d, int b) {}",
       "fun void main() {",
       "  f('0', 1, 2.0, true)", 
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void mismatchedReturnTypeInFunctionCall() throws Exception {
    String s = buildString
      ("fun int f(char a, int c, double d, int b) {}",
       "fun void main() {",
       "  var double x = f('0', 1, 2.0, 3)", 
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void mismatchedArgTypeInRecursiveFunctionCall() throws Exception {
    String s = buildString
      ("fun int f(char a, int c, double d, int b) {",
       "  f('0', 1, 2.0, true)",
       "}",
       "fun void main() {}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }
  
  //------------------------------------------------------------
  // FUNCTION RETURN TESTS
  //------------------------------------------------------------

  @Test
  public void mismatchReturnType() throws Exception {
    String s = buildString
      ("fun int f() {return 3.14}",
       "fun void main() {}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void mismatchVoidReturnType() throws Exception {
    String s = buildString
      ("fun void main() {return true}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void validReturnNilCases() throws Exception {
    String s = buildString
      ("fun int f() {return}",
       "fun int g() {}",
       "fun int h() {return nil}",
       "fun void i() {return}",
       "fun void j() {return nil}",
       "fun void main() {}"
       );
    buildParser(s).parse().accept(buildChecker());
  }

  //------------------------------------------------------------
  // ASSIGNMENT STATEMENTS
  //------------------------------------------------------------

  @Test
  public void useBeforeDef() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var x = 1 + y",
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void assignmentTypeError() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var int x = nil",
       "  x = 3.14",
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  //------------------------------------------------------------
  // PATH EXPRESSIONS AND ASSIGNMENTS
  //------------------------------------------------------------

  @Test
  public void typeInsteadOfVariableInAssignment() throws Exception {
    String s = buildString
      ("type T {var x = 0}",
       "fun void main() {",
       "  var t = T", 
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void funInsteadOfVariableInAssignment() throws Exception {
    String s = buildString
      ("fun int f() {}",
       "fun void main() {",
       "  var x = f", 
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }
  
   @Test
  public void goodSimplePathExpressionType() throws Exception {
    String s = buildString
      ("type T {var x = 0}",
       "fun void main() {",
       "  var t = new T", 
       "  var int v = t.x", 
       "}"
       );
    buildParser(s).parse().accept(buildChecker());
  }

  @Test
  public void badSimplePathExpressionType() throws Exception {
    String s = buildString
      ("type T {var x = 0}",
       "fun void main() {",
       "  var t = new T", 
       "  var bool v = t.x", 
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void goodComplexPathExpressionType() throws Exception {
    String s = buildString
      ("type T {",
       "  var x = 0",
       "  var T next = nil",
       "}",
       "fun void main() {",
       "  var t = new T", 
       "  var int v = t.next.next.next.x", 
       "}"
       );
    buildParser(s).parse().accept(buildChecker());
  }

  @Test
  public void badComplexPathExpressionType() throws Exception {
    String s = buildString
      ("type T {",
       "  var x = 0",
       "  var T next = nil",
       "}",
       "fun void main() {",
       "  var t = new T", 
       "  var bool v = t.next.next.next.x", 
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }
  
  @Test
  public void missingFieldNameInPathExpression() throws Exception {
    String s = buildString
      ("type T {var x = 0}",
       "fun void main() {",
       "  var t = new T", 
       "  var bool v = t.y", 
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void goodSimplePathAssignmentType() throws Exception {
    String s = buildString
      ("type T {var x = 0}",
       "fun void main() {",
       "  var t = new T", 
       "  t.x = 42", 
       "}"
       );
    buildParser(s).parse().accept(buildChecker());
  }

  @Test
  public void badSimplePathAssignmentType() throws Exception {
    String s = buildString
      ("type T {var x = 0}",
       "fun void main() {",
       "  var t = new T", 
       "  t.x = true", 
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void goodComplexPathAssignmentType() throws Exception {
    String s = buildString
      ("type T {",
       "  var x = 0",
       "  var T next = nil",
       "}",
       "fun void main() {",
       "  var t = new T", 
       "  t.next.next.x = 42",
       "}"
       );
    buildParser(s).parse().accept(buildChecker());
  }

  @Test
  public void badComplexPathAssignmentType() throws Exception {
    String s = buildString
      ("type T {",
       "  var x = 0",
       "  var T next = nil",
       "}",
       "fun void main() {",
       "  var t = new T", 
       "  t.next.next.x = true",
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }
  
  @Test
  public void missingFieldNameInPathAssignment() throws Exception {
    String s = buildString
      ("type T {",
       "  var x = 0",
       "  var T next = nil",
       "}",
       "fun void main() {",
       "  var t = new T", 
       "  t.next.next.y = nil", 
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
      fail("error not detected");
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  //------------------------------------------------------------
  // TYPE INFO TESTS
  //------------------------------------------------------------
  
  @Test
  public void typeAddedToTypeInfo() throws Exception {
    String s = buildString
      ("type T1 {}",
       "type T2 {",
       "  var x = 0",
       "  var string y = nil",
       "  var T2 z = nil",
       "}",
       "fun void main() {}"
       );
    TypeInfo typeInfo = new TypeInfo();
    StaticChecker checker = new StaticChecker(typeInfo);
    buildParser(s).parse().accept(checker);
    assertTrue(typeInfo.types().contains("T1"));
    assertTrue(typeInfo.types().contains("T2"));
    assertEquals(0, typeInfo.components("T1").size());
    assertEquals(3, typeInfo.components("T2").size());
    List<String> components = new ArrayList(typeInfo.components("T2"));
    assertEquals("x", components.get(0));
    assertEquals("int", typeInfo.get("T2", "x"));
    assertEquals("y", components.get(1));    
    assertEquals("string", typeInfo.get("T2", "y"));    
    assertEquals("z", components.get(2));
    assertEquals("T2", typeInfo.get("T2", "z"));    
  }

  @Test
  public void functionAddedToTypeInfo() throws Exception {
    String s = buildString
      ("fun void main() {}",
       "fun int f(int x, string y) {}"
       );
    TypeInfo typeInfo = new TypeInfo();
    StaticChecker checker = new StaticChecker(typeInfo);
    buildParser(s).parse().accept(checker);
    assertTrue(typeInfo.types().contains("main"));
    assertTrue(typeInfo.types().contains("f"));
    assertEquals(1, typeInfo.components("main").size());
    assertEquals("void", typeInfo.get("main", "return"));
    assertEquals(3, typeInfo.components("f").size());
    List<String> components = new ArrayList(typeInfo.components("f"));
    assertEquals("x", components.get(0));
    assertEquals("int", typeInfo.get("f", "x"));
    assertEquals("y", components.get(1));
    assertEquals("string", typeInfo.get("f", "y"));    
    assertEquals("return", components.get(2));
    assertEquals("int", typeInfo.get("f", "return"));    

  }
  
  
}

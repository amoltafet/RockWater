/*
 * File: ParserTest.java
 * Date: Spring 2022
 * Auth: Ahmad Moltafet
 * Desc: Basic unit tests for the MyPL parser class.
 */


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.annotation.Target;


public class ParserTest {

  //------------------------------------------------------------
  // HELPER FUNCTIONS
  //------------------------------------------------------------
  
  private static Parser buildParser(String s) throws Exception {
    InputStream in = new ByteArrayInputStream(s.getBytes("UTF-8"));
    Parser parser = new Parser(new Lexer(in));
    return parser;
  }

  private static String buildString(String... args) {
    String str = "";
    for (String s : args)
      str += s + "\n";
    return str;
  }


  //------------------------------------------------------------
  // POSITIVE TEST CASES
  //------------------------------------------------------------

  @Test
  public void emptyParse() throws Exception {
    Parser parser = buildParser("");
    parser.parse();
  }

  @Test
  public void implicitVariableDecls() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var v1 = 0",
       "  var v2 = 0.0",
       "  var v3 = false",
       "  var v4 = 'a'",
       "  var v5 = \"abc\"",
       "  var v6 = new Node",
       "}");
    Parser parser = buildParser(s);
    parser.parse();
  }

  @Test
  public void explicitVariableDecls() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var int v1 = 0",
       "  var double v2 = 0.0",
       "  var bool v3 = false",
       "  var char v4 = 'a'",
       "  var string v5 = \"abc\"",
       "  var Node v6 = new Node",
       "}");
    Parser parser = buildParser(s);
    parser.parse();
  }

  /* TODO: 

       (1). Add your own test cases below as you create your recursive
            descent functions. By the end you should have a full suite
            of positive test cases that "pass" the tests. 

       (2). Ensure your program (bazel-bin/mypl --parse) works for the
            example file (examples/parser.txt). 
 
       (3). For the parser, the "negative" tests below are just as
            important as the "positive" test cases. Like in (1), be
            sure to add negative test cases as you build out your
            parser. By the end you should also have a full set of
            negative cases as well.
  */

  @Test
  public void forLoop() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var int i = 0",
       "  for i from 1 upto 5 {",
       "    i = i + 1",
       "  }",
       "}");
    Parser parser = buildParser(s);
    parser.parse();
  }

  @Test
  public void whileLoop() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  while (true) {",
       "    print(\"hello\")",
       "  }",
       "}");
    Parser parser = buildParser(s);
    parser.parse();
  }

  @Test
  public void ifStatement() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  if (true) {",
       "    print(\"hello\")",
       "  }",
       "}");
    Parser parser = buildParser(s);
    parser.parse();
  }

  @Test
  public void ifElseStatement() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  if (true) {",
       "    print(\"hello\")",
       "  } else {",
       "    print(\"goodbye\")",
       "  }",
       "}");
    Parser parser = buildParser(s);
    parser.parse();
  }

  @Test
  public void returnStatement() throws Exception {
    String s = buildString
      ("fun int main() {",
       "  return 0",
       "}");
    Parser parser = buildParser(s);
    parser.parse();
    s = buildString
    ("fun int main() {",
     "  return 0 + 1",
     "}");
     parser = buildParser(s);
     parser.parse();
  }

  @Test
  public void declarationStatement() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var int i = 0",
       "  i = i - 1",
       "}");
    Parser parser = buildParser(s);
    parser.parse();
  }
  
  @Test
  public void multipleParameterDeclarations() throws Exception {
    String s = buildString
      ("fun void main(int i, double d) {",
       "  i = i - 1",
       "  d = d - 1.0",
       "}");
    Parser parser = buildParser(s);
    parser.parse();
  }

  @Test
  public void multipleStatementBlock() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var int i = 0",
       "  i = i - 1",
       "  var double d = 0.0",
       "  d = d - 1.0",
       "}");
    Parser parser = buildParser(s);
    parser.parse();
  }

  @Test
  public void deleteVariable() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  delete v1",
       "}");
    Parser parser = buildParser(s);
    parser.parse();
    
  }
  //------------------------------------------------------------
  // NEGATIVE TEST CASES
  //------------------------------------------------------------
  
  @Test
  public void statementOutsideOfFunction() throws Exception {
    String s = "var v1 = 0";
    Parser parser = buildParser(s);
    try {
      parser.parse();
      fail("syntax error not detected");
    } catch(MyPLException e) {
      // can check message here if desired
      // e.g., assertEquals("...", e.getMessage());
    }
  }

  @Test
  public void functionWithoutReturnType() throws Exception {
    String s = "fun main() {}";
    Parser parser = buildParser(s);
    try {
      parser.parse();
      fail("syntax error not detected");
    } catch(MyPLException e) {
      // can check message here if desired
      // e.g., assertEquals("...", e.getMessage());
    }
  }

  @Test
  public void functionWithoutClosingBrace() throws Exception {
    String s = "fun void main() {";
    Parser parser = buildParser(s);
    try {
      parser.parse();
      fail("syntax error not detected");
    } catch(MyPLException e) {
      // can check message here if desired
      // e.g., assertEquals("...", e.getMessage());
    }
  }
  
  /* add additional negative test cases here */ 
 
  @Test
  public void functionWithoutOpeningBrace() throws Exception {
    String s = "fun void main)";
    Parser parser = buildParser(s);
    try {
      parser.parse();
      fail("syntax error not detected");
    } catch(MyPLException e) {
      // can check message here if desired
      // e.g., assertEquals("...", e.getMessage());
    }
  }

  @Test
  public void functionWithoutName() throws Exception {
    String s = "fun void() {}";
    Parser parser = buildParser(s);
    try {
      parser.parse();
      fail("syntax error not detected");
    } catch(MyPLException e) {
      // can check message here if desired
      // e.g., assertEquals("...", e.getMessage());
    }
  }

  @Test
  public void functionWithoutParameter() throws Exception {
    String s = "fun void main {}";
    Parser parser = buildParser(s);
    try {
      parser.parse();
      fail("syntax error not detected");
    } catch(MyPLException e) {
      // can check message here if desired
      // e.g., assertEquals("...", e.getMessage());
    }
  }

  @Test
  public void functionWithoutParameterType() throws Exception {
    String s = "fun void main(int) {}";
    Parser parser = buildParser(s);
    try {
      parser.parse();
      fail("syntax error not detected");
    } catch(MyPLException e) {
      // can check message here if desired
      // e.g., assertEquals("...", e.getMessage());
    }
  }

  @Test
  public void invalidAssignment() throws Exception {
    String s = "fun void main() { var int v1 = var 2 }";
    Parser parser = buildParser(s);
    try {
      parser.parse();
      fail("syntax error not detected");
    } catch(MyPLException e) {
      // can check message here if desired
      // e.g., assertEquals("...", e.getMessage());
    }
  }


}

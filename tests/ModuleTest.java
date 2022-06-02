/*
 * File: ModuleTest.java
 * Date: Spring 2022
 * Auth: Ahmad Moltafet
 * Desc: Basic unit tests for the MyPL ast-based parser class.
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


public class ModuleTest {

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
  public void basicImportStatement() throws Exception {
    String s = buildString
      ("import myModule",
       "type T1 {",
       "  var x = 0",
       "  var y = 0",
       "}",
       "fun void main() {}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void basicUsage() throws Exception {
    String s = buildString
      (
      "fun void myModule@printThis() {",
      "  print(x)",
      "}",
       "import myModule",
       "fun void main() {",
       "  var x = 0",
       "  myModule@printThis()",
       "}"
       );
    try {
      buildParser(s).parse().accept(buildChecker());
    } catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
    }
  }

  @Test
  public void createImportToken() throws Exception {
    Token t = new Token(TokenType.IMPORT, "import", 10, 11);
    assertEquals(TokenType.IMPORT, t.type());
    assertEquals("import", t.lexeme());
    assertEquals(10, t.line());
    assertEquals(11, t.column());
    assertEquals("IMPORT 'import' line 10 column 11", t.toString());
  }

  @Test
  public void noImportString() throws Exception {
    String s = buildString
      ("import",
      "fun void main() {",
       "}");
      try {
        buildParser(s).parse().accept(buildChecker());
        fail("error not detected");
      } catch(MyPLException ex) {
        assertTrue(ex.getMessage().startsWith("PARSE_ERROR:"));
      }
  }
  
  @Test
  public void importStatementInsideMain() throws Exception {
    String s = buildString
      (
      "fun void main() {",
      "  import myModule",
       "}");
      try {
        buildParser(s).parse().accept(buildChecker());
        fail("error not detected");
      } catch(MyPLException ex) {
        assertTrue(ex.getMessage().startsWith("PARSE_ERROR:"));
      }
  }


}
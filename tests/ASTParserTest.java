/*
 * File: ASTParserTest.java
 * Date: Spring 2022
 * Auth: Ahmad Moltafet
 * Desc: Basic unit tests for the MyPL ast-based parser class.
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Ignore;
import org.junit.Test;

import java.beans.Transient;
import java.io.ByteArrayInputStream;
import java.io.InputStream;


public class ASTParserTest {

  //------------------------------------------------------------
  // HELPER FUNCTIONS
  //------------------------------------------------------------
  
  private static ASTParser buildParser(String s) throws Exception {
    InputStream in = new ByteArrayInputStream(s.getBytes("UTF-8"));
    ASTParser parser = new ASTParser(new Lexer(in));
    return parser;
  }

  private static String buildString(String... args) {
    String str = "";
    for (String s : args)
      str += s + "\n";
    return str;
  }

  //------------------------------------------------------------
  // TEST CASES
  //------------------------------------------------------------

  @Test
  public void emptyParse() throws Exception {
    ASTParser parser = buildParser("");
    Program p = parser.parse();
    assertEquals(0, p.tdecls.size());
    assertEquals(0, p.fdecls.size());
  }

  @Test
  public void oneTypeDeclInProgram() throws Exception {
    String s = buildString
      ("type Node {",
       "}");
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    assertEquals(1, p.tdecls.size());
    assertEquals(0, p.fdecls.size());
  }
  
  @Test
  public void oneFunDeclInProgram() throws Exception {
    String s = buildString
      ("fun void main() {",
       "}"
       );
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    assertEquals(0, p.tdecls.size());
    assertEquals(1, p.fdecls.size());
  }

  @Test
  public void funWithParams() throws Exception {
    String s = buildString
      ("fun void main(int x, int y) {",
       "}"
       );
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    assertEquals(0, p.tdecls.size());
    assertEquals(1, p.fdecls.size());
    FunDecl f = p.fdecls.get(0);
    assertEquals("main", f.funName.lexeme());
    assertEquals(2, f.params.size());
    assertEquals("x", f.params.get(0).paramName.lexeme());
    assertEquals("y", f.params.get(1).paramName.lexeme());
  }

  @Test
  public void funWithReturnType() throws Exception {
    String s = buildString
      ("fun int main() {",
       "return 0",
       "}"
      );
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    assertEquals(0, p.tdecls.size());
    assertEquals(1, p.fdecls.size());
    FunDecl f = p.fdecls.get(0);
    assertEquals("main", f.funName.lexeme());
    assertEquals(0, f.params.size());
    assertEquals("int", f.returnType.lexeme());
  }

  @Test
  public void funWithStmts() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var int x = 0",
       "}"
      );
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    assertEquals(0, p.tdecls.size());
    assertEquals(1, p.fdecls.size());
    FunDecl f = p.fdecls.get(0);
    assertEquals(0, f.params.size());
    assertEquals("void", f.returnType.lexeme());
    assertEquals(1, f.stmts.size());
    VarDeclStmt vds = (VarDeclStmt) f.stmts.get(0);
    assertEquals("x", vds.varName.lexeme());
    assertEquals("int", vds.typeName.lexeme());
  }

  @Test
  public void funWithExpr() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  a = b + c",
       "}"
       );
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    assertEquals(0, p.tdecls.size());
    assertEquals(1, p.fdecls.size());
    FunDecl f = p.fdecls.get(0);
    assertEquals("main", f.funName.lexeme());
    assertEquals(0, f.params.size());
    assertEquals(1, f.stmts.size());
    AssignStmt s1 = (AssignStmt) f.stmts.get(0);
    assertEquals(1, s1.lvalue.size());
    assertEquals("a", s1.lvalue.get(0).lexeme());
  }

  @Test
  public void typeDeclaration() throws Exception {
    String s = buildString
      ("type Node {",
       "  var int x = 2",
       "  var int y = 1",
       "}"
       );
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    assertEquals(1, p.tdecls.size());
    assertEquals(0, p.fdecls.size());
    TypeDecl t = p.tdecls.get(0);
    assertEquals("Node", t.typeName.lexeme());
    assertEquals(2, t.vdecls.size());
    assertEquals("x", t.vdecls.get(0).varName.lexeme());
    assertEquals("y", t.vdecls.get(1).varName.lexeme());
  }

  @Test
  public void funWithIfElse() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  if (x) {",
       "    y = z",
       "  } else {",
       "    w = v",
       "  }",
       "}"
       );
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    assertEquals(0, p.tdecls.size());
    assertEquals(1, p.fdecls.size());
    FunDecl f = p.fdecls.get(0);
    assertEquals("main", f.funName.lexeme());
    assertEquals(0, f.params.size());
    assertEquals(1, f.stmts.size());
  }

}

/*
 * File: TokenTest.java
 * Date: Spring 2022
 * Auth: S. Bowers
 * Desc: Basic unit tests for the MyPL Token class
 */


import static org.junit.Assert.assertEquals;
import org.junit.Test;


public class TokenTest {

  @Test
  public void createAndGetIDTokenInfo() throws Exception {
    Token t = new Token(TokenType.ID, "x_val", 10, 11);
    assertEquals(TokenType.ID, t.type());
    assertEquals("x_val", t.lexeme());
    assertEquals(10, t.line());
    assertEquals(11, t.column());
    assertEquals("ID 'x_val' line 10 column 11", t.toString());
  }

  @Test
  public void createAndGetAllTokenTypes() throws Exception {
    int line = 1;
    String id = "var";
    for (TokenType type : TokenType.values()) {
      id = id + line;
      Token t = new Token(type, id, line, 1);
      assertEquals(type, t.type());
      assertEquals(id, t.lexeme());
      assertEquals(line, t.line());
      assertEquals(1, t.column());
      assertEquals(type + " " + "'" + id + "' line " + line + " column 1", t.toString());
      ++line;
    }
  }
  
}

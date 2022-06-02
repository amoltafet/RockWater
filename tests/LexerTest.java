/*
 * File: TokenTest.java
 * Date: Spring 2022
 * Auth: SMB
 * Desc: Basic unit tests for the MyPL Lexer class
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Ignore;
import org.junit.Test;

import java.beans.Transient;
import java.io.ByteArrayInputStream;
import java.io.InputStream;


public class LexerTest {


  //------------------------------------------------------------
  // POSITIVE TEST CASES
  //------------------------------------------------------------
 
  @Test
  public void checkEmptyInput() throws Exception {
    String s = "";
    InputStream in = new ByteArrayInputStream(s.getBytes("UTF-8"));
    Lexer lexer = new Lexer(in);
    assertEquals(TokenType.EOS, lexer.nextToken().type());
  }
  
  @Test
  public void checkOneCharacterSymbols() throws Exception {
    String s = ",.+-*/%{}()=<>";
    InputStream in = new ByteArrayInputStream(s.getBytes("UTF-8"));
    Lexer lexer = new Lexer(in);
    TokenType[] types = {
      TokenType.COMMA, TokenType.DOT, TokenType.PLUS, TokenType.MINUS,
      TokenType.MULTIPLY, TokenType.DIVIDE, TokenType.MODULO,
      TokenType.LBRACE, TokenType.RBRACE, TokenType.LPAREN,
      TokenType.RPAREN, TokenType.ASSIGN, TokenType.LESS_THAN,
      TokenType.GREATER_THAN, TokenType.EOS
    };
    for (int i = 0; i < types.length; ++i) {
      Token t = lexer.nextToken();
      assertEquals(types[i], t.type());
      assertEquals(1, t.line());
      assertEquals(1 + i, t.column());
    }
  }

  @Test
  public void checkTwoCharacterSymbols() throws Exception {
    String s = "!=>=<=";
    InputStream in = new ByteArrayInputStream(s.getBytes("UTF-8"));
    Lexer lexer = new Lexer(in);
    TokenType[] types = {
      TokenType.NOT_EQUAL, TokenType.GREATER_THAN_EQUAL,
      TokenType.LESS_THAN_EQUAL, TokenType.EOS
    };
    for (int i = 0; i < types.length; ++i) {
      Token t = lexer.nextToken();
      assertEquals(types[i], t.type());
      assertEquals(1, t.line());
      assertEquals(1 + 2*i, t.column());
    }
  }

  @Test
  public void checkCharacters() throws Exception {
    String s = "'a' '?' '<' '\\n' '\\t'";
    InputStream in = new ByteArrayInputStream(s.getBytes("UTF-8"));
    Lexer lexer = new Lexer(in);
    Token t = lexer.nextToken();
    assertEquals(TokenType.CHAR_VAL, t.type());
    assertEquals("a", t.lexeme());
    assertEquals(1, t.line());
    assertEquals(1, t.column());
    t = lexer.nextToken();
    assertEquals(TokenType.CHAR_VAL, t.type());
    assertEquals("?", t.lexeme());
    assertEquals(1, t.line());
    assertEquals(5, t.column());
    t = lexer.nextToken();
    assertEquals(TokenType.CHAR_VAL, t.type());
    assertEquals("<", t.lexeme());
    assertEquals(1, t.line());
    assertEquals(9, t.column());
    t = lexer.nextToken();
    assertEquals(TokenType.CHAR_VAL, t.type());
    assertEquals("\\n", t.lexeme());
    assertEquals(1, t.line());
    assertEquals(13, t.column());
    t = lexer.nextToken();
    assertEquals(TokenType.CHAR_VAL, t.type());
    assertEquals("\\t", t.lexeme());
    assertEquals(1, t.line());
    assertEquals(18, t.column());
    t = lexer.nextToken();
    assertEquals(TokenType.EOS, t.type());
  }

  @Test
  public void checkStrings() throws Exception {
    String s = "\"abc\" \"><!=\"";
    InputStream in = new ByteArrayInputStream(s.getBytes("UTF-8"));
    Lexer lexer = new Lexer(in);
    Token t = lexer.nextToken();
    assertEquals(TokenType.STRING_VAL, t.type());
    assertEquals("abc", t.lexeme());
    assertEquals(1, t.line());
    assertEquals(1, t.column());
    t = lexer.nextToken();
    assertEquals(TokenType.STRING_VAL, t.type());
    assertEquals("><!=", t.lexeme());
    assertEquals(1, t.line());
    assertEquals(7, t.column());
    t = lexer.nextToken();
    assertEquals(TokenType.EOS, t.type());
  }

  @Test
  public void checkNumbers() throws Exception {
    String s = "0 42 0.0 0.00 3.14 2.0";
    InputStream in = new ByteArrayInputStream(s.getBytes("UTF-8"));
    Lexer lexer = new Lexer(in);
    Token t = lexer.nextToken();
    assertEquals(TokenType.INT_VAL, t.type());
    assertEquals("0", t.lexeme());
    assertEquals(1, t.line());
    assertEquals(1, t.column());
    t = lexer.nextToken();
    assertEquals(TokenType.INT_VAL, t.type());
    assertEquals("42", t.lexeme());
    assertEquals(1, t.line());
    assertEquals(3, t.column());
    t = lexer.nextToken();
    assertEquals(TokenType.DOUBLE_VAL, t.type());
    assertEquals("0.0", t.lexeme());
    assertEquals(1, t.line());
    assertEquals(6, t.column());
    t = lexer.nextToken();
    assertEquals(TokenType.DOUBLE_VAL, t.type());
    assertEquals("0.00", t.lexeme());
    assertEquals(1, t.line());
    assertEquals(10, t.column());
    t = lexer.nextToken();
    assertEquals(TokenType.DOUBLE_VAL, t.type());
    assertEquals("3.14", t.lexeme());
    assertEquals(1, t.line());
    assertEquals(15, t.column());
    t = lexer.nextToken();
    assertEquals(TokenType.DOUBLE_VAL, t.type());
    assertEquals("2.0", t.lexeme());
    assertEquals(1, t.line());
    assertEquals(20, t.column());
    t = lexer.nextToken();
    assertEquals(TokenType.EOS, t.type());
  }

  
  @Test
  public void checkReservedWords() throws Exception {
    TokenType[] types = {
      TokenType.AND, TokenType.OR, TokenType.NOT, TokenType.NEG,
      TokenType.INT_TYPE, TokenType.DOUBLE_TYPE, TokenType.CHAR_TYPE,
      TokenType.STRING_TYPE, TokenType.BOOL_TYPE, TokenType.VOID_TYPE,
      TokenType.VAR, TokenType.TYPE, TokenType.WHILE, TokenType.FOR,
      TokenType.FROM, TokenType.UPTO, TokenType.DOWNTO,
      TokenType.IF, TokenType.ELIF, TokenType.ELSE, TokenType.FUN,
      TokenType.NEW, TokenType.DELETE, TokenType.RETURN,
      TokenType.NIL};
    String[] words = {
      "and", "or", "not", "neg", "int", "double", "char", "string",
      "bool", "void", "var", "type", "while", "for", "from", "upto",
      "downto", "if", "elif", "else", "fun", "new", "delete",
      "return", "nil"};
    String s = "";
    for (String word : words)
      s += word + " ";
    InputStream in = new ByteArrayInputStream(s.getBytes("UTF-8"));
    Lexer lexer = new Lexer(in);
    int distance = 1;           // for current position in s
    for (int i = 0; i < words.length; ++i) {
      Token t = lexer.nextToken();
      assertEquals(types[i], t.type());
      assertEquals(words[i], t.lexeme());
      assertEquals(1, t.line());
      assertEquals(distance, t.column());
      distance += words[i].length() + 1;
    }
    assertEquals(TokenType.EOS, lexer.nextToken().type());
  }
  
  @Test
  public void checkIdentifiers() throws Exception {
    String s = "x xs foo_bar foo_bar_baz quix__";
    InputStream in = new ByteArrayInputStream(s.getBytes("UTF-8"));
    Lexer lexer = new Lexer(in);
    Token t = lexer.nextToken();
    assertEquals(TokenType.ID, t.type());
    assertEquals("x", t.lexeme());
    assertEquals(1, t.line());
    assertEquals(1, t.column());
    t = lexer.nextToken();
    assertEquals(TokenType.ID, t.type());
    assertEquals("xs", t.lexeme());
    assertEquals(1, t.line());
    assertEquals(3, t.column());
    t = lexer.nextToken();
    assertEquals(TokenType.ID, t.type());
    assertEquals("foo_bar", t.lexeme());
    assertEquals(1, t.line());
    assertEquals(6, t.column());
    t = lexer.nextToken();
    assertEquals(TokenType.ID, t.type());
    assertEquals("foo_bar_baz", t.lexeme());
    assertEquals(1, t.line());
    assertEquals(14, t.column());
    t = lexer.nextToken();
    assertEquals(TokenType.ID, t.type());
    assertEquals("quix__", t.lexeme());
    assertEquals(1, t.line());
    assertEquals(26, t.column());
    t = lexer.nextToken();
    assertEquals(TokenType.EOS, t.type());
  }
  

  //------------------------------------------------------------
  // Negative Test Cases
  //------------------------------------------------------------

  @Test
  public void multilineStringTest() throws Exception {
    String s = "";
    s += "\"\n";
    s += "\"\n";
    InputStream in = new ByteArrayInputStream(s.getBytes("UTF-8"));
    Lexer lexer = new Lexer(in);
    try {
      lexer.nextToken();
      fail("no error reported");
    }
    catch(MyPLException e) {
      String m;
      m = "LEXER_ERROR: found newline within string at line 1, column 2";
      assertEquals(m, e.getMessage());
    }
  }

  @Test
  public void nonTerminatedStringTest() throws Exception {
    String s = "\" foo bar ";
    InputStream in = new ByteArrayInputStream(s.getBytes("UTF-8"));
    Lexer lexer = new Lexer(in);
    try {
      lexer.nextToken();
      fail("no error reported");
    }
    catch(MyPLException e) {
      String m;
      m = "LEXER_ERROR: found end-of-file in string at line 1, column 10";
      assertEquals(m, e.getMessage());
    }
  }

  @Test
  public void invalidNotEqual() throws Exception {
    String s = "!>";
    InputStream in = new ByteArrayInputStream(s.getBytes("UTF-8"));
    Lexer lexer = new Lexer(in);
    try {
      lexer.nextToken();
      fail("no error reported");
    }
    catch(MyPLException e) {
      String m;
      m = "LEXER_ERROR: expecting '=', found '>' at line 1, column 2";
      assertEquals(m, e.getMessage());
    }
  }

  @Test
  public void emptyCharacter() throws Exception {
    String s = "''";
    InputStream in = new ByteArrayInputStream(s.getBytes("UTF-8"));
    Lexer lexer = new Lexer(in);
    try {
      lexer.nextToken();
      fail("no error reported");
    }
    catch(MyPLException e) {
      String m;
      m = "LEXER_ERROR: empty character at line 1, column 1";
      assertEquals(m, e.getMessage());
    }
  }

  @Test
  public void newlineCharacter() throws Exception {
    String s = "'\n'";
    InputStream in = new ByteArrayInputStream(s.getBytes("UTF-8"));
    Lexer lexer = new Lexer(in);
    try {
      lexer.nextToken();
      fail("no error reported");
    }
    catch(MyPLException e) {
      String m;
      m = "LEXER_ERROR: found newline in character at line 1, column 2";
      assertEquals(m, e.getMessage());
    }
  }

  @Test
  public void singleQuoteCharacter() throws Exception {
    String s = "'";
    InputStream in = new ByteArrayInputStream(s.getBytes("UTF-8"));
    Lexer lexer = new Lexer(in);
    try {
      lexer.nextToken();
      fail("no error reported");
    }
    catch(MyPLException e) {
      String m;
      m = "LEXER_ERROR: empty character at line 1, column 1";
      assertEquals(m, e.getMessage());
    }
  }
  
  
  @Test
  public void nonTerminatedCharacter() throws Exception {
    String s = "'ab'";
    InputStream in = new ByteArrayInputStream(s.getBytes("UTF-8"));
    Lexer lexer = new Lexer(in);
    try {
      lexer.nextToken();
      fail("no error reported");
    }
    catch(MyPLException e) {
      String m;
      m = "LEXER_ERROR: expecting ' found, 'b' at line 1, column 2";
      assertEquals(m, e.getMessage());
    }
  }


  
  @Test
  public void missingDecimalDigit() throws Exception {
    String s = "32.a";
    InputStream in = new ByteArrayInputStream(s.getBytes("UTF-8"));
    Lexer lexer = new Lexer(in);
    try {
      lexer.nextToken();
      fail("no error reported");
    }
    catch(MyPLException e) {
      String m;
      m = "LEXER_ERROR: missing decimal digit in double value '32.' at line 1, column 1";
      assertEquals(m, e.getMessage());
    }
  }

  @Test
  public void tooManyDecimalPoints() throws Exception {
    String s = "32.112.05";
    InputStream in = new ByteArrayInputStream(s.getBytes("UTF-8"));
    Lexer lexer = new Lexer(in);
    try {
      lexer.nextToken();
      fail("no error reported");
    }
    catch(MyPLException e) {
      String m;
      m = "LEXER_ERROR: too many decimal points in double value '32.112' at line 1, column 1";
      assertEquals(m, e.getMessage());
    }
  }

  @Test
  public void intWithLeadingZero() throws Exception {
    String s = "0 1 02"; // first two ints are okay, third is bad
    InputStream in = new ByteArrayInputStream(s.getBytes("UTF-8"));
    Lexer lexer = new Lexer(in);
    try {
      lexer.nextToken();
      lexer.nextToken();
      lexer.nextToken();
      fail("no error reported");
    }
    catch(MyPLException e) {
      String m;
      m = "LEXER_ERROR: leading zero in '02' at line 1, column 5";
      assertEquals(m, e.getMessage());
    }
  }

  @Test
  public void doubleWithLeadingZero() throws Exception {
    String s = "0.0 1.00 02.3"; // first two doubles are okay, third is bad
    InputStream in = new ByteArrayInputStream(s.getBytes("UTF-8"));
    Lexer lexer = new Lexer(in);
    try {
      lexer.nextToken();
      lexer.nextToken();
      lexer.nextToken();
      fail("no error reported");
    }
    catch(MyPLException e) {
      String m;
      m = "LEXER_ERROR: leading zero in '02.3' at line 1, column 10";
      assertEquals(m, e.getMessage());
    }
  }

  @Test
  public void invalidSymbol() throws Exception {
    String s = "?"; // this is just one of many possible bad symbols
    InputStream in = new ByteArrayInputStream(s.getBytes("UTF-8"));
    Lexer lexer = new Lexer(in);
    try {
      lexer.nextToken();
      fail("no error reported");
    }
    catch(MyPLException e) {
      String m;
      m = "LEXER_ERROR: invalid symbol '?' at line 1, column 1";
      assertEquals(m, e.getMessage());
    }
  }
  
  
  
  
}

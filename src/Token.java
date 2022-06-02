/*
 * File: Token.java
 * Date: Spring 2022
 * Auth:
 * Desc:
 */


public class Token {

  private TokenType type;
  private String lexeme;
  private int line;
  private int column;

  public Token(TokenType type, String lexeme, int line, int column) {
    this.type = type;
    this.lexeme = lexeme;
    this.line = line;
    this.column = column;
  }

  public TokenType type() {
    return type;
  }

  public String lexeme() {
    return lexeme;
  }

  public int line() {
    return line;
  }

  public int column() {
    return column;
  }

  @Override
  public String toString() {
    return type + " '" + lexeme + "' line " + line + " column " + column;
  }
    
}

/*
 * File: TokenType.java
 * Date: Spring 2022
 * Auth: SMB
 * Desc: The basic token types in MyPL
 */

public enum TokenType {
  // basic symbols
  COMMA, DOT, PLUS, MINUS, MULTIPLY, DIVIDE, MODULO,
  LBRACE, RBRACE, LPAREN, RPAREN,
  // comparators
  NOT_EQUAL, EQUAL, GREATER_THAN, GREATER_THAN_EQUAL, LESS_THAN,
  LESS_THAN_EQUAL,
  // assignment
  ASSIGN,
  // primitive values
  CHAR_VAL, STRING_VAL, INT_VAL, DOUBLE_VAL, BOOL_VAL,
  // boolean operators
  AND, OR, NOT, NEG,
  // data types
  INT_TYPE, DOUBLE_TYPE, CHAR_TYPE, STRING_TYPE, BOOL_TYPE, VOID_TYPE,  
  // reserved words
  VAR, TYPE, WHILE, FOR, FROM, UPTO, DOWNTO, 
  IF, ELIF, ELSE, 
  FUN, NEW, DELETE, RETURN, NIL,
  // identifiers
  ID,
  // end of stream
  EOS,
  // modules
  IMPORT
}

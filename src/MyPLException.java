/* 
 * File: MyPLException.java
 * Date: Spring 2022
 * Auth: S. Bowers
 * Desc: Exception class for MyPL errors
 */

public class MyPLException extends Exception {

  private enum ErrorType {
    LEXER_ERROR,
    PARSE_ERROR,
    STATIC_ERROR, 
    VM_ERROR
  };

  private String message;
  private ErrorType type;
  
  // constructor
  public MyPLException(String message, ErrorType type) {
    this.message = message;
    this.type = type;
  }

  @Override
  public String getMessage() {
    return type.toString() + ": " + message;
  }

  // helper functions to craete specific types of exceptions
  
  public static MyPLException LexerError(String message) {
    return new MyPLException(message, ErrorType.LEXER_ERROR);
  }

  public static MyPLException ParseError(String message) {
    return new MyPLException(message, ErrorType.PARSE_ERROR);
  }

  public static MyPLException StaticError(String message) {
    return new MyPLException(message, ErrorType.STATIC_ERROR);
  }

  public static MyPLException VMError(String message) {
    return new MyPLException(message, ErrorType.VM_ERROR);
  }


  
}

/*
 * File: Lexer.java
 * Date: Spring 2022
 * Auth: Ahmad Moltafet
 * Desc: The lexical analyzer for MyPL.
 */

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.CollationKey;
import java.util.regex.Pattern;
import java.io.IOException;


public class Lexer {

  private BufferedReader buffer; // handle to input stream
  private int line = 1;          // current line number
  private int column = 0;        // current column number
  String[] words = {
    "and", "or", "not", "neg", "int", "double", "char", "string",
    "bool", "void", "var", "type", "while", "for", "from", "upto",
    "downto", "if", "elif", "else", "fun", "new", "delete",
    "return", "nil", "true", "false"};

  TokenType[] types = {
      TokenType.AND, TokenType.OR, TokenType.NOT, TokenType.NEG,
      TokenType.INT_TYPE, TokenType.DOUBLE_TYPE, TokenType.CHAR_TYPE,
      TokenType.STRING_TYPE, TokenType.BOOL_TYPE, TokenType.VOID_TYPE,
      TokenType.VAR, TokenType.TYPE, TokenType.WHILE, TokenType.FOR,
      TokenType.FROM, TokenType.UPTO, TokenType.DOWNTO,
      TokenType.IF, TokenType.ELIF, TokenType.ELSE, TokenType.FUN,
      TokenType.NEW, TokenType.DELETE, TokenType.RETURN,
      TokenType.NIL};

  //--------------------------------------------------------------------
  // Constructor
  //--------------------------------------------------------------------
  
  public Lexer(InputStream instream) {
    buffer = new BufferedReader(new InputStreamReader(instream));
  }


  //--------------------------------------------------------------------
  // Private helper methods
  //--------------------------------------------------------------------

  // Returns next character in the stream. Returns -1 if end of file.
  private int read() throws MyPLException {
    try {
      return buffer.read();
    } catch(IOException e) {
      error("read error", line, column + 1);
    }
    return -1;
  }

  // Returns next character without removing it from the stream.
  private int peek() throws MyPLException {
    int ch = -1;
    try {
      buffer.mark(1);
      ch = read();
      buffer.reset();
    } catch(IOException e) {
      error("read error", line, column + 1);
    }
    return ch;
  }

  // Print an error message and exit the program.
  private void error(String msg, int line, int column) throws MyPLException {
    msg = msg + " at line " + line + ", column " + column;
    throw MyPLException.LexerError(msg);
  }

  // Checks for whitespace 
  public static boolean isWhitespace(int ch) {
    return Character.isWhitespace((char)ch);
  }

  // Checks for digit
  private static boolean isDigit(int ch) {
    return Character.isDigit((char)ch);
  }

  // Checks for letter
  private static boolean isLetter(int ch) {
    return Character.isLetter((char)ch);
  }

  // Checks if given symbol
  private static boolean isSymbol(int ch, char symbol) {
    return (char)ch == symbol;
  }

  // Checks if end-of-file
  private static boolean isEOF(int ch) {
    return ch == -1;
  }

  //--------------------------------------------------------------------
  // Public next_token function
  //--------------------------------------------------------------------
  
  // Returns next token in input stream
  public Token nextToken() throws MyPLException {
    int ch = peek();
    int starting_col = 1;
    while(isWhitespace(ch)) {
      if(ch == '\n') {
        line++;
        column = 0;
        ch = peek();
        if(isEOF(ch)) {
          return new Token(TokenType.EOS, "-1", line, column);
       }
        ch = read();
      } else {
        ch = read();
        column++;
        starting_col = column;
      }
      
      ch = peek();
    }

    if(ch == '#') {
      while(ch != '\n') {
        ch = read();
      }
      column = 0;
      line++;
      return nextToken();
    }

    if(ch == '/') {
      ch = read();
      column++;
      ch = peek();
      if(ch == '/') {
        do {
          ch = read();
          column++;
          ch = peek();
        } while(ch != '\n' && !isEOF(ch));
        return new Token(TokenType.MODULO, "/", line, starting_col);
      } else if(ch == '*') {
        do {
          ch = read();
          column++;
          ch = peek();
        } while(ch != '*' && !isEOF(ch));
        ch = read();
        column++;
        ch = peek();
        if(ch == '/') {
          return nextToken();
        } else {
          error("invalid comment", line, column);
        }
      } else {
        return new Token(TokenType.DIVIDE, "/", line, column);
      }
    }
    
    // read numbers
    if(isDigit(ch)) {
      boolean flag = false;
      // Reads digits
      StringBuilder sb = new StringBuilder();
      starting_col = column + 1;
      if(column == 0) {
        starting_col = 1;
      }
      do {
        ch = read();
        sb.append((char)ch);
        column++;
        if(sb.charAt(0) == '0' && sb.length()>1) {
          flag = true;
        }
        ch = peek();
      } while(isDigit(ch));
      if(ch == '.') {
        ch = read();
        sb.append((char)ch);
        ch = peek();
        if(!isDigit(ch)) {
          String m = "missing decimal digit in double value '" + sb.toString() + "'";
          error(m, line, (sb.length() - column));
        }
        else {
          column++;
          do {
            ch = read();
            sb.append((char)ch);
            column++;
            ch = peek();
          } while(isDigit(ch));
          if(ch == '.') {
            String m = "too many decimal points in double value '" + sb.toString() + "'";
            error(m, line, (sb.length() - column) + 1);
          }
          if(flag) {
            String m = "leading zero in '" + String.valueOf(sb) + "'";
            error(m, line, starting_col);
          }
          return new Token(TokenType.DOUBLE_VAL, String.valueOf(sb), line, starting_col);

        } 

      }
      if(flag) {
        String m = "leading zero in '" + String.valueOf(sb) + "'";
        error(m, line, column -1);
      }
      return new Token(TokenType.INT_VAL, String.valueOf(sb), line, starting_col);
    }
    
    // read end of file
    if(isEOF(ch)) {
      read();
      column++;
      return new Token(TokenType.EOS, "-1", line, column);
    }

    if(isSymbol(ch, '\"')) {
      StringBuilder sb = new StringBuilder();
      starting_col = column + 1;
      if(column == 0) {
        starting_col = 1;
      }
      ch = read();
      column++;
      ch = peek();
      while(ch != '\"' && ch != '\'' && ch != -1) {
        sb.append((char)ch);
        ch = read();
        column++;
        ch = peek();
      }
      
      if(isEOF(ch)) {
        error("found end-of-file in string", line, column);
      }
      String newline = System.getProperty("line.separator");
      boolean hasNewline = sb.toString().contains(newline);
      if(hasNewline) {
        error("found newline within string", line, column);
      }
      ch = read();
      column++;

      return new Token(TokenType.STRING_VAL, sb.toString(), line, starting_col);
    }
   
    if(isSymbol(ch, '_')) {
      ch = read();
      column++;
      return new Token(TokenType.ID, "_", line, column);
    }
    // read identifiers
    if(isSymbol(ch, '(')) {
      ch = read();
      column++;
      return new Token(TokenType.LPAREN, "(", line, column);
    }
    if(isSymbol(ch, ')')) {
      ch = read();
      column++;
      return new Token(TokenType.RPAREN, ")", line, column);
    }
    if(isSymbol(ch, ',')) {
      ch = read();
      column++;
      return new Token(TokenType.COMMA, ",", line, column);
    }
    if(isSymbol(ch, '.')) {
      ch = read();
      column++;
      return new Token(TokenType.DOT, ".", line, column);
    }
    if(isSymbol(ch, '+')) {
      ch = read();
      column++;
      return new Token(TokenType.PLUS, "+", line, column);
    }
    if(isSymbol(ch, '-')) {
      ch = read();
      column++;
      return new Token(TokenType.MINUS, "-", line, column);
    }
    if(isSymbol(ch, '*')) {
      ch = read();
      column++;
      return new Token(TokenType.MULTIPLY, "*", line, column);
    }
    if(isSymbol(ch, '=')) {
      starting_col = column;
      ch = read();
      column++;
      if(peek() == '=') {
        ch = read();
        column++;
        return new Token(TokenType.EQUAL, "==", line, starting_col + 1);
      }
      return new Token(TokenType.ASSIGN, "=", line, column);
    }
    if(isSymbol(ch, '<')) {
      starting_col = column;
      ch = read();
      column++;
      
      if(peek() == '=') {
        ch = read();
        column++;
        return new Token(TokenType.LESS_THAN_EQUAL, "<=", line, starting_col + 1);
      } else {
        return new Token(TokenType.LESS_THAN, "<", line, column);
      }
    }
    if(isSymbol(ch, '>')) {
      starting_col = column;
      ch = read();
      column++;
      if(peek() == '=') {
        ch = read();
        column++;
        return new Token(TokenType.GREATER_THAN_EQUAL, ">=", line, starting_col + 1);
      } else {
        return new Token(TokenType.GREATER_THAN, ">", line, column);
      }
    }
    if(isSymbol(ch, '&')) {
      ch = read();
      column++;
      return new Token(TokenType.AND, "&", line, column);
    }
    if(isSymbol(ch, '|')) {
      ch = read();
      column++;
      return new Token(TokenType.OR, "|", line, column);
    }
    if(isSymbol(ch, '{')) {
      ch = read();
      column++;
      return new Token(TokenType.LBRACE, "{", line, column);
    }
    if(isSymbol(ch, '}')) {
      ch = read();
      column++;
      return new Token(TokenType.RBRACE, "}", line, column);
    }
    if(isSymbol(ch, '"')) {
      // Read string
      StringBuffer str = new StringBuffer();
      ch = read();
      column++;
      while(ch != '"') {
        ch = read();
        str.append((char)ch);
        column++;
      }
      return new Token(TokenType.STRING_VAL, str.toString(), line, column);
    }
    if(isSymbol(ch, '%')) {
      ch = read();
      column++;
      return new Token(TokenType.MODULO, "%", line, column);
    }
    if(isSymbol(ch, '!')) {
        starting_col = column + 1;
        ch = read();
        column++;
        int ch2 = peek();
        if(isSymbol(ch2, '>')) {
          error("expecting '=', found '>'", line, starting_col + 1);
        }
        else if(isSymbol(ch2, '=')) {
          ch = read();
          column++;
         return new Token(TokenType.NOT_EQUAL, "!=", line, column - 1);
        } else {
          return new Token(TokenType.NOT, "!", line, column);
        }
              
        
    }
    
    // read letters
    if(isLetter(ch)) {
        starting_col = column + 1;
        if(column == 0) {
          starting_col = 1;
        }
        StringBuffer id = new StringBuffer();
        ch = read(); 
        id.append((char)ch);
        column++;
        ch = peek();
        while(isLetter(ch) || isDigit(ch) || isSymbol(ch, '_') || isSymbol(ch, '@')) {
          ch = read();
          id.append((char)ch);
          column++;
          ch = peek();
        }
        while(isLetter(ch) || isSymbol(ch, '_') || isDigit(ch) || isSymbol(ch, '@')) {
            ch = read();
            id.append((char)ch);
            column++;
            if(peek() == '_' || peek() == '@') {
                ch = read();
                id.append('_');
                column++;
            }
            if(isEOF(peek()) || !isLetter(peek()) && !isSymbol(peek(), '_') && !isSymbol(peek(), '@')) {
                break;
            }
        }
        String tmp = id.toString().replaceAll("\\s", "");

        if(tmp.equals("import")) {
          return new Token(TokenType.IMPORT, "import", line, starting_col);
        }
        
        for(int i = 0; i < words.length; i++) {
          if(words[i].equals(tmp)) {
            if(words[i].equals("true") || words[i].equals("false")) {
              return new Token(TokenType.BOOL_VAL, words[i], line, starting_col);
            }
            return new Token(types[i], words[i], line, column - words[i].length() + 1);
          }
        }
        return new Token(TokenType.ID, String.valueOf(tmp), line, starting_col);
    }
    // read characters
    if(isSymbol(ch, '\'')) {
        // Read char
        String c = "";
        starting_col = column + 1;
        if(column == 0) {
          starting_col = 1;
        }
        ch = read(); 
        
        if(isSymbol(peek(), '\'') || isEOF(peek())) {
          error("empty character", line, column + 1);
        }
        if(isSymbol(peek(), '\n')) {
          error("found newline in character", line, column + 2);
        }

        if(isSymbol(peek(), '\\') ) {
          ch = read();
          if(isSymbol(peek(), 'n')) {
            c = "\\";
          } else if(isSymbol(peek(), 't')) {
            column++;
            c = "\\";
          } else if(isSymbol(ch, '\\')) {
            c = "\\\\";
          } 
         
        }  
      
        ch = read();
        c += (char)ch;
        column++;
        ch = peek();
        
        if(!isSymbol(ch, '\'')) {
          error("expecting ' found, '" + (char)ch + "'", line, column + 1);
        }
        
        ch = read();
        return new Token(TokenType.CHAR_VAL, c, line, starting_col + column - 1);
    } else {
      error("invalid symbol \'" + (char)ch + "\'", line, column + 1);
    }
    
    return null;
  }

  


}



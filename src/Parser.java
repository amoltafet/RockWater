/* 
 * File: Parser.java
 * Date: Spring 2022
 * Auth: Ahmad Moltafet
 * Desc: Parses the input file and creates the AST
 */

 import java.util.ArrayList;

public class Parser {

  private Lexer lexer = null; 
  private Token currToken = null;

  // constructor
  public Parser(Lexer lexer) {
    this.lexer = lexer;
  }

  // do the parse
  public void parse() throws MyPLException
  {
    // <program> ::= (<tdecl> | <fdecl>)*
    advance();
    if(match(TokenType.IMPORT)) {
      advance();
      if(match(TokenType.ID)) {
        advance();
      }
      else {
        error("Expected a string value after import");
      }
    }
    while (!match(TokenType.EOS)) {
      if (match(TokenType.TYPE))
        tdecl();
      if (match(TokenType.IMPORT))
        advance();
      else
        fdecl();
    }
    advance(); 
  }

  
  //------------------------------------------------------------ 
  // Helper Functions
  //------------------------------------------------------------

  // get next token
  private void advance() throws MyPLException {
    currToken = lexer.nextToken();
  }

  // advance if current token is of given type, otherwise error
  private void eat(TokenType t, String msg) throws MyPLException {
    if (match(t))
      advance();
    else
      error(msg);
  }

  // true if current token is of type t
  private boolean match(TokenType t) {
    return currToken.type() == t;
  }
  
  // throw a formatted parser error
  private void error(String msg) throws MyPLException {
    String s = msg + ", found '" + currToken.lexeme() + "' ";
    s += "at line " + currToken.line();
    s += ", column " + currToken.column();
    throw MyPLException.ParseError(s);
  }

  // output a debug message (if DEBUG is set)
  private void debug(String msg) {
    if (DEBUG)
      System.out.println("[debug]: " + msg);
  }

  // return true if current token is a (non-id) primitive type
  private boolean isPrimitiveType() {
    return match(TokenType.INT_TYPE) || match(TokenType.DOUBLE_TYPE) ||
      match(TokenType.BOOL_TYPE) || match(TokenType.CHAR_TYPE) ||
      match(TokenType.STRING_TYPE);
  }

  // return true if current token is a (non-id) primitive value
  private boolean isPrimitiveValue() {
    return match(TokenType.INT_VAL) || match(TokenType.DOUBLE_VAL) ||
      match(TokenType.BOOL_VAL) || match(TokenType.CHAR_VAL) ||
      match(TokenType.STRING_VAL);
  }
    
  // return true if current token starts an expression
  private boolean isExpr() {
    return match(TokenType.NOT) || match(TokenType.LPAREN) ||
      match(TokenType.NIL) || match(TokenType.NEW) ||
      match(TokenType.ID) || match(TokenType.NEG) ||
      match(TokenType.INT_VAL) || match(TokenType.DOUBLE_VAL) ||
      match(TokenType.BOOL_VAL) || match(TokenType.CHAR_VAL) ||
      match(TokenType.STRING_VAL);
  }

  private boolean isOperator() {
    return match(TokenType.PLUS) || match(TokenType.MINUS) ||
      match(TokenType.DIVIDE) || match(TokenType.MULTIPLY) ||
      match(TokenType.MODULO) || match(TokenType.AND) ||
      match(TokenType.OR) || match(TokenType.EQUAL) ||
      match(TokenType.LESS_THAN) || match(TokenType.GREATER_THAN) ||
      match(TokenType.LESS_THAN_EQUAL) || match(TokenType.GREATER_THAN_EQUAL) ||
      match(TokenType.NOT_EQUAL);
  }

  //------------------------------------------------------------
  // Recursive Descent Functions 
  //------------------------------------------------------------

  /* TODO: Add the recursive descent functions below */
  
  private boolean DEBUG = false;

  // <tdecl> ::= TYPE ID LBRACE <vdecls> RBRACE
  private void tdecl() throws MyPLException {
    eat(TokenType.TYPE, "Expected 'TYPE'");
    String id = currToken.lexeme();
    eat(TokenType.ID, "Expected identifier" + id);
    eat(TokenType.LBRACE, "Expected '{'");
    vdecls();
    eat(TokenType.RBRACE, "Expected '}'");
    debug("tdecl");
  }

  //<fdecl> ::= FUN ( <dtype> | VOID ) ID LPAREN <params> RPAREN LBRACE <stmts> RBRACE
  private void fdecl() throws MyPLException {
    debug("started fdecl");
    eat(TokenType.FUN, "Expected 'fun'");
    // ( <dtype> | VOID )
    dtype();
    String id = currToken.lexeme();
    eat(TokenType.ID, "Expected identifier" + id);
    eat(TokenType.LPAREN, "Expected '('");
    // <params>
    if(!match(TokenType.RPAREN)) {
      params();
    }
    eat(TokenType.RPAREN, "Expected ')'");
    eat(TokenType.LBRACE, "Expected '{'");
    // <stmts>
    debug("called stmts");
    stmts();
    eat(TokenType.RBRACE, "Expected '}'");
    debug("finished fdecl");
  }

  // <stmts> ::= <stmt> | <stmt> <stmts>
  private void stmts() throws MyPLException {
    stmt();
    if (!match(TokenType.RBRACE)) {
      stmts();
    }
  }

  // <stmt> ::= <vdecl_stmt> | <assign_stmt> | <cond_stmt> | <while_stmt> | <for_stmt> |
  // <call_expr> | <ret_stmt> | <delete_stmt> 
  private void stmt() throws MyPLException {
    if (match(TokenType.VAR)) {
      vdecl_stmt();
    }
    else if (match(TokenType.IF)) {
      cond_stmt();
    }
    else if (match(TokenType.WHILE)) {
      while_stmt();
    }
    else if (match(TokenType.RETURN)) {
      ret_stmt();
    }
    else if (match(TokenType.FOR)) {
      for_stmt();
    }
    
    else if(match(TokenType.DELETE)) {
      delete_stmt();
    }
    else if(match(TokenType.ID)) {
      String id = currToken.lexeme();
      eat(TokenType.ID, "Expected identifier " + id);
      if(match(TokenType.LPAREN)) {
        call_expr();
      }
      else {
        assign_stmt();
      }
    }
    else {
      error("Expected statement");
    }
  }

  // <dtype> ID ( COMMA <dtype> ID )
  private void params() throws MyPLException {
    // <dtype>
    dtype();
    String id = currToken.lexeme();
    eat(TokenType.ID, "Expected identifier " + id);
    // ( COMMA <dtype> ID )
    while (match(TokenType.COMMA)) {
      eat(TokenType.COMMA, "Expected ','");
      dtype();
      eat(TokenType.ID, "Expected identifier " + id);
    }
    debug("params");
  }

  // <dtype> ::= <type> | VOID
  private void dtype() throws MyPLException {
    if (isPrimitiveType()) {
      advance();
    }
    else if (match(TokenType.VOID_TYPE)) {
      eat(TokenType.VOID_TYPE, "Expected 'void'");
    } else {
      advance();
    }
    debug("dtype");
  }

  // <expr> ::= ( <rvalue> | NOT <expr> | LPAREN <expr> RPAREN ) ( <operator> <expr> |  )
  private void expr() throws MyPLException {
    if (match(TokenType.NOT)) {
      eat(TokenType.NOT, "Expected '!'");
      expr();
    }
    else if(match(TokenType.LPAREN)) {
      eat(TokenType.LPAREN, "Expected '('");
      expr();
      eat(TokenType.RPAREN, "Expected ')'");
    }
    else {
      rvalue();
    }
    // ( <operator> <expr> |  )
    if (isOperator()) {
      operator();
      expr();
    } 
  }
  
  //<args> ::= <expr> ( COMMA <expr> )∗ | 
  private void args() throws MyPLException {
    if(isExpr()) {
      expr();
      while(match(TokenType.COMMA)) {
        eat(TokenType.COMMA, "Expected ','");
        expr();
      }
    }
  }
  
  // <condt> ::= ELIF <expr> LBRACE <stmts> RBRACE <condt> | ELSE LBRACE <stmts> RBRACE | 
  private void condt() throws MyPLException {
    if (match(TokenType.ELIF)) {
      eat(TokenType.ELIF, "Expected 'elif'");
      expr();
      eat(TokenType.LBRACE, "Expected '{'");
      stmts();
      eat(TokenType.RBRACE, "Expected '}'");
      condt();
    }
    else if (match(TokenType.ELSE)) {
      eat(TokenType.ELSE, "Expected 'else'");
      eat(TokenType.LBRACE, "Expected '{'");
      stmts();
      eat(TokenType.RBRACE, "Expected '}'");
    }
    debug("condt");
  }

  //----- stmt helpers ----- //

  // <vdecl_stmt> ::= VAR ( <dtype> |  ) ID ASSIGN <expr>
  private void vdecl_stmt() throws MyPLException {
    // VAR
    eat(TokenType.VAR, "Expected 'var'");
    // ( <dtype> |  )
    if(!match(TokenType.ID) ) {
      dtype();
    } else {
      advance();
    }  
    // ID
    if(match(TokenType.ID)) {
      String id = currToken.lexeme();
      eat(TokenType.ID, "Expected identifier " + id);
    } 
    // ASSIGN
    eat(TokenType.ASSIGN, "Expected '='");
    // <expr>
    expr();
    debug("vdecl_stmt ");
  }

  //<assign_stmt> ::= <lvalue> ASSIGN <expr>
  private void assign_stmt() throws MyPLException {
    // <lvalue>
    lvalue();
    // ASSIGN
    eat(TokenType.ASSIGN, "Expected '='");
    // <expr>
    expr();
  }

  //<cond_stmt> ::= IF <expr> LBRACE <stmts> RBRACE <condt>
  private void cond_stmt() throws MyPLException {
    // IF
    eat(TokenType.IF, "Expected 'if'");
    // <expr>
    expr();
    // LBRACE
    eat(TokenType.LBRACE, "Expected '{'");
    // <stmts>
    stmts();
    // RBRACE
    eat(TokenType.RBRACE, "Expected '}'");
    // <condt>
    condt();
    debug("cond_stmt");
  }

  // <while_stmt> ::= WHILE <expr> LBRACE <stmts> RBRACE
  private void while_stmt() throws MyPLException {
    // WHILE
    eat(TokenType.WHILE, "Expected 'while'");
    // <expr>
    expr();
    // LBRACE
    eat(TokenType.LBRACE, "Expected '{'");
    // <stmts>
    stmts();
    // RBRACE
    eat(TokenType.RBRACE, "Expected '}'");
    debug("while_stmt");
  }

  // <for_stmt> ::= FOR ID FROM <expr> ( UPTO | DOWNTO ) <expr> LBRACE <stmts> RBRACE
  private void for_stmt() throws MyPLException {
    // FOR
    eat(TokenType.FOR, "Expected 'for'");
    // ID
    String id = currToken.lexeme();
    eat(TokenType.ID, "Expected identifier " + id);
    // FROM
    eat(TokenType.FROM, "Expected 'from'");
    // <expr>
    expr();
    // ( UPTO | DOWNTO )
    if (match(TokenType.UPTO)) {
      eat(TokenType.UPTO, "Expected 'upto'");
    }
    else {
      eat(TokenType.DOWNTO, "Expected 'downto'");
    }
    // <expr>
    expr();
    // LBRACE
    eat(TokenType.LBRACE, "Expected '{'");
    // <stmts>
    stmts();
    // RBRACE
    eat(TokenType.RBRACE, "Expected '}'");
  }

  // <call_expr> ::= ID LPAREN <args> RPAREN
  private void call_expr() throws MyPLException {
    // ID
    // LPAREN
    eat(TokenType.LPAREN, "Expected '('");
    // <args>
    if(!match(TokenType.RPAREN)) {
      args();
    }
    // RPAREN
    eat(TokenType.RPAREN, "Expected ')'");
  }

  // <ret_stmt> ::= RETURN ( <expr> |  )
  private void ret_stmt() throws MyPLException {
    // RETURN
    eat(TokenType.RETURN, "Expected 'return'");
    // ( <expr> |  )
    if (match(TokenType.INT_VAL) || match(TokenType.DOUBLE_VAL) ||
        match(TokenType.BOOL_VAL) || match(TokenType.CHAR_VAL) ||
        match(TokenType.STRING_VAL) || match(TokenType.ID)) {
      expr();
    }
    debug("ret_stmt");
  }

  // <delete_stmt> ::= DELETE ID
  private void delete_stmt() throws MyPLException {
    // DELETE
    eat(TokenType.DELETE, "Expected 'delete'");
    // ID
    String id = currToken.lexeme();
    eat(TokenType.ID, "Expected identifier");
    debug("delete_stmt: " + id);
  }

  //----- expr helpers ----- //

  // <lvalue> ::= ID ( DOT ID )*
  private void lvalue() throws MyPLException {
    // ID
    if(match(TokenType.ID)) {
      String id = currToken.lexeme();
      eat(TokenType.ID, "Expected identifier");
    }
    
    // ( DOT ID )*
    while (match(TokenType.DOT)) {
      // DOT
      eat(TokenType.DOT, "Expected '.'");
      // ID
      eat(TokenType.ID, "Expected identifier");
    }
  }

  // <rvalue> ::= <pval> | NIL | NEW ID | <idrval> | <call_expr> | NEG <expr>
  private void rvalue() throws MyPLException {
    if (match(TokenType.INT_VAL) || match(TokenType.DOUBLE_VAL) ||
        match(TokenType.BOOL_VAL) || match(TokenType.CHAR_VAL) ||
        match(TokenType.STRING_VAL)) {
      pval();
    }
    else if (match(TokenType.NIL)) {
      eat(TokenType.NIL, "Expected 'nil'");
    }
    else if (match(TokenType.NEW)) {
      eat(TokenType.NEW, "Expected 'new'");
      eat(TokenType.ID, "Expected identifier");
    }
    else if (match(TokenType.ID)) {
      idrval();
    }
    else if (match(TokenType.LPAREN)) {
      call_expr();
    }
    else if (match(TokenType.NEG)) {
      eat(TokenType.NEG, "Expected '-'");
      expr();
    }
    else {
      error("Expected expression");
    }
    debug("rvalue");
  }

  // <pval> ::= INT_VAL | DOUBLE_VAL | BOOL_VAL | CHAR_VAL | STRING_VAL
  private void pval() throws MyPLException {
    if (match(TokenType.INT_VAL)) {
      eat(TokenType.INT_VAL, "Expected integer");
    }
    else if (match(TokenType.DOUBLE_VAL)) {
      eat(TokenType.DOUBLE_VAL, "Expected double");
    }
    else if (match(TokenType.BOOL_VAL)) {
      eat(TokenType.BOOL_VAL, "Expected boolean");
    }
    else if (match(TokenType.CHAR_VAL)) {
      eat(TokenType.CHAR_VAL, "Expected character");
    }
    else if (match(TokenType.STRING_VAL)) {
      eat(TokenType.STRING_VAL, "Expected string");
    }
    else {
      error("Expected expression value");
    }
    debug("pval");

  }

  // <idrval> ::= ID ( DOT ID )*
  private void idrval() throws MyPLException {
    eat(TokenType.ID, "Expected identifier name");
    while (match(TokenType.DOT)) {
      eat(TokenType.DOT, "Expected '.'");
      eat(TokenType.ID, "Expected identifier name");
    }
  }

  // <operator> ::= PLUS | MINUS | DIVIDE | MULTIPLY | MODULO | AND | OR | EQUAL | LESS_THAN |
  // GREATER_THAN | LESS_THAN_EQUAL | GREATER_THAN_EQUAL | NOT_EQUAL
  private void operator() throws MyPLException {
    if(match(TokenType.PLUS)) {
      eat(TokenType.PLUS, "Expected '+'");
    }
    else if(match(TokenType.MINUS)) {
      eat(TokenType.MINUS, "Expected '-'");
    }
    else if(match(TokenType.DIVIDE)) {
      eat(TokenType.DIVIDE, "Expected '/'");
    }
    else if(match(TokenType.MULTIPLY)) {
      eat(TokenType.MULTIPLY, "Expected '*'");
    }
    else if(match(TokenType.MODULO)) {
      eat(TokenType.MODULO, "Expected '%'");
    }
    else if(match(TokenType.AND)) {
      eat(TokenType.AND, "Expected '&&'");
    }
    else if(match(TokenType.OR)) {
      eat(TokenType.OR, "Expected '||'");
    }
    else if(match(TokenType.EQUAL)) {
      eat(TokenType.EQUAL, "Expected '=='");
    }
    else if(match(TokenType.LESS_THAN)) {
      eat(TokenType.LESS_THAN, "Expected '<'");
    }
    else if(match(TokenType.GREATER_THAN)) {
      eat(TokenType.GREATER_THAN, "Expected '>'");
    }
    else if(match(TokenType.LESS_THAN_EQUAL)) {
      eat(TokenType.LESS_THAN_EQUAL, "Expected '<='");
    }
    else if(match(TokenType.GREATER_THAN_EQUAL)) {
      eat(TokenType.GREATER_THAN_EQUAL, "Expected '>='");
    }
    else if(match(TokenType.NOT_EQUAL)) {
      eat(TokenType.NOT_EQUAL, "Expected '!='");
    }
    else {
      error("Expected operator");
    }
    debug("operator()");
  }

  // ---- td_stmt helpers ---- //

  // <vdecls> ::= ( <vdecl_stmt> )*
  private void vdecls() throws MyPLException {
    while(isPrimitiveValue()) {
      vdecl_stmt();
    }
  }

}

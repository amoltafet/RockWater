/* 
 * File: ASTParser.java
 * Date: Spring 2022
 * Auth: Ahmad Moltafet
 * Desc: This class is used to parse the input file and create the AST.
 */

import java.util.ArrayList;
import java.util.List;


public class ASTParser {

  private Lexer lexer = null; 
  private Token currToken = null;
  private final boolean DEBUG = false;

  /** 
   */
  public ASTParser(Lexer lexer) {
    this.lexer = lexer;
  }

  /**
   */
  public Program parse() throws MyPLException
  {
    // <program> ::= (<tdecl> | <fdecl>)*
    Program progNode = new Program();
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
      if (match(TokenType.TYPE)) {
        progNode.tdecls.add(tdecl());
      }
      else if (match(TokenType.IMPORT)) {
        advance();
        if(match(TokenType.ID)) {
          advance();
        }
        else {
          error("Expected a string value after import");
        }
      }
      else  {
        progNode.fdecls.add(fdecl());
      }
    }
    advance(); // eat the EOS token
    return progNode;
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


  // TODO: Add your recursive descent functions from HW-3
  // and extend them to build up the AST

  private TypeDecl tdecl() throws MyPLException {
    TypeDecl t = new TypeDecl();
    eat(TokenType.TYPE, "Expected 'type'");
    t.typeName = currToken;
    eat(TokenType.ID, "Expected identifier");
    eat(TokenType.LBRACE, "Expected '{'");
    t.vdecls = vdecls();
    eat(TokenType.RBRACE, "Expected '}'");
    return t;
  }

  private List<VarDeclStmt> vdecls() throws MyPLException {
    List<VarDeclStmt> vdecls = new ArrayList<>();
    while (match(TokenType.VAR)) {
      vdecls.add(vdecl_stmt());
    }
    return vdecls;
  }

  private FunDecl fdecl() throws MyPLException {
    FunDecl f = new FunDecl();
    eat(TokenType.FUN, "Expected 'fun'");
    f.returnType = currToken;
    if (match(TokenType.VOID_TYPE)) {
      eat(TokenType.VOID_TYPE, "Expected 'void'");
      
    } else {
      dtype();
    }
    f.funName = currToken;
    eat(TokenType.ID, "Expected identifier");
    eat(TokenType.LPAREN, "Expected '('");
    f.params = params();
    eat(TokenType.RPAREN, "Expected ')'");
    eat(TokenType.LBRACE, "Expected '{'");
    f.stmts = stmts();
    eat(TokenType.RBRACE, "Expected '}'");
    return f;
  }

  private List<FunParam> params() throws MyPLException {
    List<FunParam> paramList = new ArrayList<>();
    if (match(TokenType.RPAREN)) {
      return paramList;
    }
    FunParam funParam = new FunParam();
    funParam.paramType = currToken;
    dtype();
    funParam.paramName = currToken;
    eat(TokenType.ID, "Expected identifier");
    paramList.add(funParam);
    while (match(TokenType.COMMA)) {
      funParam = new FunParam();
      advance();

      funParam.paramType = currToken;
      dtype();
      funParam.paramName = currToken;
      eat(TokenType.ID, "Expected identifier");
      paramList.add(funParam);
    }
    return paramList;
  }

  private void dtype() throws MyPLException {
    if (!isPrimitiveType() && !match(TokenType.ID)) {
      error("Expected type" + currToken.lexeme());
    } else {
      advance();
    }
  }

  private List<Stmt> stmts() throws MyPLException {
    List<Stmt> stmts = new ArrayList<>();
    while (!match(TokenType.RBRACE)) {
      stmts.add(stmt());
    }
    return stmts;
  }

  private Stmt stmt() throws MyPLException {
    if (match(TokenType.VAR)) { 
      return vdecl_stmt();
    }
    else if (match(TokenType.ID)) { 
      Token n = currToken;
      eat(TokenType.ID, "Expected identifier");
      // -- add module case here 
      // if id is part of a module, then it is a module call
      if (match(TokenType.DOT)) {
        CallExpr mc = module_call(n);
        return mc;
      }
      // -- add module case here
      if (match(TokenType.LPAREN)) { 
        CallExpr stmt = new CallExpr();
        stmt.funName = n;
        stmt.args = call_expr();
        return stmt;
      } 
      else { 
        return assign_stmt(n);
      }
    } 
    else if (match(TokenType.IF)) { 
      return cond_stmt();
    }
    else if (match(TokenType.WHILE)) { 
      return while_stmt();
    } 
    else if (match(TokenType.FOR)) { 
      return for_stmt();
    } 
    else if (match(TokenType.RETURN)) { 
      return ret_stmt();
    } 
    else if (match(TokenType.DELETE)) { 
      return delete_stmt();
    } 
    else { 
      if (match(TokenType.LPAREN)) {
        CallExpr mc = module_call(currToken);
        return mc;
      }
      error("Expected statement");
      return new CondStmt();
    }
  }

  private CallExpr module_call(Token n) throws MyPLException {
    String moduleName = n.lexeme();
    CallExpr mc = new CallExpr();
    mc.args = call_expr();
    return mc;
  }

  private VarDeclStmt vdecl_stmt() throws MyPLException {
    VarDeclStmt stmt = new VarDeclStmt();
    eat(TokenType.VAR, "Expected 'variable'");
    if (isPrimitiveType()) {
      stmt.typeName = currToken;
      dtype();
    }
    Token id = currToken;
    eat(TokenType.ID, "Expected identifier");
    if (match(TokenType.ID)) {
      stmt.typeName = id;
      stmt.varName = currToken;
      advance();
    } else {
      stmt.varName = id;
    }
    eat(TokenType.ASSIGN, "Expected '='");
    stmt.expr = expr();
    return stmt;
  }

  private AssignStmt assign_stmt(Token name) throws MyPLException {
    AssignStmt stmt = new AssignStmt();
    stmt.lvalue.addAll(lvalue(name));
    eat(TokenType.ASSIGN, "Expected '='");
    stmt.expr = expr();
    return stmt;
  }

  private List<Token> lvalue(Token name) throws MyPLException {
    // this is a recursive function
    List<Token> leftList = new ArrayList<>();
    leftList.add(name);
    while (match(TokenType.DOT)) {
      eat(TokenType.DOT, "Expected '.'");
      leftList.add(currToken);
      eat(TokenType.ID, "Expected identifier");
    }
    return leftList;
  }

  private CondStmt cond_stmt() throws MyPLException {
    BasicIf basicIf = new BasicIf();
    eat(TokenType.IF, "Expected 'if'");
    basicIf.cond = expr();
    eat(TokenType.LBRACE, "Expected '{'");
    basicIf.stmts = stmts();
    eat(TokenType.RBRACE, "Expected '}'");
    CondStmt stmt = condt();
    stmt.ifPart = basicIf;
    return stmt;
  }

  private CondStmt condt() throws MyPLException {
    CondStmt stmt = new CondStmt();
    while (match(TokenType.ELIF)) { 
      BasicIf basicIf = new BasicIf();
      eat(TokenType.ELIF, "Expected 'elif'");
      basicIf.cond = expr();
      eat(TokenType.LBRACE, "Expected '{'");
      basicIf.stmts = stmts();
      eat(TokenType.RBRACE, "Expected '}'");
      stmt.elifs.add(basicIf);
    }
    // else part
    if (match(TokenType.ELSE)) { 
      eat(TokenType.ELSE, "Expected 'else'");
      eat(TokenType.LBRACE, "Expected '{'");
      stmt.elseStmts = stmts();
      eat(TokenType.RBRACE, "Expected '}'");
    }
    return stmt;
  }

  private WhileStmt while_stmt() throws MyPLException {
    WhileStmt w = new WhileStmt();
    eat(TokenType.WHILE, "Expected 'while'");
    w.cond = expr();
    eat(TokenType.LBRACE, "Expected '{'");
    w.stmts = stmts();
    eat(TokenType.RBRACE, "Expected '}'");
    return w;
  }

  private ForStmt for_stmt() throws MyPLException {
    ForStmt f = new ForStmt();
    eat(TokenType.FOR, "Expected 'for'");
    f.varName = currToken;
    eat(TokenType.ID, "Expected identifier");
    eat(TokenType.FROM, "Expected 'from'");
    f.start = expr();
    // TODO: check if expr is a constant
    if (match(TokenType.UPTO)) {
      f.upto = true;
      advance();
    } else {
      f.upto = false;
      eat(TokenType.DOWNTO, "Expecting 'upto' or 'downto'");
    }
    f.end = expr();
    eat(TokenType.LBRACE, "Expected '{'");
    f.stmts = stmts();
    eat(TokenType.RBRACE, "Expected '}'");
    return f;
  }

  private List<Expr> call_expr() throws MyPLException { 
    eat(TokenType.LPAREN, "Expected '('");
    List<Expr> args = args();
    eat(TokenType.RPAREN, "Expected ')'");
    return args;
  }

  private List<Expr> args() throws MyPLException {
    List<Expr> args = new ArrayList<>();
    if (match(TokenType.RPAREN)) { 
      return args;
    }
    args.add(expr());
    while (match(TokenType.COMMA)) {
      advance();
      args.add(expr());
    }
    return args;
  }

  private ReturnStmt ret_stmt() throws MyPLException {
    ReturnStmt returnStmt = new ReturnStmt();
    eat(TokenType.RETURN, "Expected 'return' statement");
    if (isExpr()) {
      returnStmt.expr = expr();
    }
    return returnStmt;
  }
  
  private DeleteStmt delete_stmt() throws MyPLException {
    DeleteStmt stmt = new DeleteStmt();
    eat(TokenType.DELETE, "Expected 'delete' statement");
    stmt.varName = currToken;
    eat(TokenType.ID, "Expected identifier");
    return stmt;
  }

  private Expr expr() throws MyPLException {
    Expr expr = new Expr();
    if (match(TokenType.LPAREN)) {
      advance();
      ComplexTerm term = new ComplexTerm();
      term.expr = expr();
      expr.first = term;
      eat(TokenType.RPAREN, "Expected ')'");
    } else if (match(TokenType.NOT)) {
      expr.logicallyNegated = true;
      advance();
      ComplexTerm term = new ComplexTerm();
      term.expr = expr();
      expr.first = term;
    } else {
      SimpleTerm term = new SimpleTerm();
      term.rvalue = rvalue();
      expr.first = term;
    }
    if (isOperator()) {
      expr.op = currToken;
      operator();
      expr.rest = expr();
    }
    return expr;
  }
  
  private void operator() throws MyPLException {
    if (isOperator()) {
      advance();
    } else {
      error("Expected operator");
    }
  }
  
  private RValue rvalue() throws MyPLException {
    if (isPrimitiveValue()) {
      SimpleRValue rValue = new SimpleRValue();
      rValue.value = pval();
      return rValue;
    } 
    else if (match(TokenType.NIL)) {
      SimpleRValue rValue = new SimpleRValue();
      rValue.value = currToken;
      advance();
      return rValue;
    } 
    else if (match(TokenType.NEW)) {
      NewRValue rValue = new NewRValue();
      advance();
      rValue.typeName = currToken;
      eat(TokenType.ID, "Expected identifier");
      return rValue;
    } 
    else if (match(TokenType.NEG)) {
      // negation
      NegatedRValue rValue = new NegatedRValue();
      advance();
      rValue.expr = expr();
      return rValue;
    } 
    else if (match(TokenType.ID)) {
      Token id = currToken;
      advance();
      // CHECK if this is a function call
      if (match(TokenType.LPAREN)) { 
        CallExpr callExpr = new CallExpr();
        callExpr.funName = id;
        eat(TokenType.LPAREN, "Expecting '('");
        callExpr.args = args();
        eat(TokenType.RPAREN, "Expecting ')'");
        return callExpr;
      } 
      else {
        IDRValue idrValue = new IDRValue();
        idrValue.path.add(id);
        while (match(TokenType.DOT)) {
          advance();
          idrValue.path.add(currToken);
          eat(TokenType.ID, "Expected identifier");
        }
        return idrValue;
      }
    } else {
      error("Expecting rvalue " + currToken);
      return null;
    }
  }
  
  private Token pval() throws MyPLException {
    Token t = currToken;
    if (isPrimitiveValue()) {
      advance();
    } else {
      error("Expected primitive value");
    }
    return t;
  }  
}
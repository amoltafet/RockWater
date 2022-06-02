/*
 * File: StaticChecker.java
 * Date: Spring 2022
 * Auth: Ahmad Moltafet
 * Desc: Static checker for MyPL
 */

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class StaticChecker implements Visitor {

  // the symbol table
  private SymbolTable symbolTable = new SymbolTable();
  // the current expression type
  private String currType = null;
  // the program's user-defined (record) types and function signatures
  private TypeInfo typeInfo = null;
  // bool return value for visit methods
  private boolean returnFlag = false;

  //--------------------------------------------------------------------
  // helper functions:
  //--------------------------------------------------------------------
  
  // generate an error
  private void error(String msg, Token token) throws MyPLException {
    String s = msg;
    if (token != null)
      s += " near line " + token.line() + ", column " + token.column();
    throw MyPLException.StaticError(s);
  }

  // return all valid types
  // assumes user-defined types already added to symbol table
  private List<String> getValidTypes() {
    List<String> types = new ArrayList<>();
    types.addAll(Arrays.asList("int", "double", "bool", "char", "string",
                               "void"));
    for (String type : typeInfo.types())
      if (symbolTable.get(type).equals("type"))
        types.add(type);
    return types;
  }

  // return the build in function names
  private List<String> getBuiltinFunctions() {
    return Arrays.asList("print", "read", "length", "get", "stoi",
                         "stod", "itos", "itod", "dtos", "dtoi");
  }
  
  // check if given token is a valid function signature return type
  private void checkReturnType(Token typeToken) throws MyPLException {
    if (!getValidTypes().contains(typeToken.lexeme())) {
      String msg = "'" + typeToken.lexeme() + "' is an invalid return type";
      error(msg, typeToken);
    }
  }

  // helper to check if the given token is a valid parameter type
  private void checkParamType(Token typeToken) throws MyPLException {
    if (typeToken.equals("void"))
      error("'void' is an invalid parameter type", typeToken);
    else if (!getValidTypes().contains(typeToken.lexeme())) {
      String msg = "'" + typeToken.lexeme() + "' is an invalid return type";
      error(msg, typeToken);
    }
  }
  
  // helpers to get first token from an expression for calls to error
  
  private Token getFirstToken(Expr expr) {
    return getFirstToken(expr.first);
  }

  private Token getFirstToken(ExprTerm term) {
    if (term instanceof SimpleTerm)
      return getFirstToken(((SimpleTerm)term).rvalue);
    else
      return getFirstToken(((ComplexTerm)term).expr);
  }

  private Token getFirstToken(RValue rvalue) {
    if (rvalue instanceof SimpleRValue)
      return ((SimpleRValue)rvalue).value;
    else if (rvalue instanceof NewRValue)
      return ((NewRValue)rvalue).typeName;
    else if (rvalue instanceof IDRValue)
      return ((IDRValue)rvalue).path.get(0);
    else if (rvalue instanceof CallExpr)
      return ((CallExpr)rvalue).funName;
    else 
      return getFirstToken(((NegatedRValue)rvalue).expr);
  }

  
  //---------------------------------------------------------------------
  // constructor
  //--------------------------------------------------------------------
  
  public StaticChecker(TypeInfo typeInfo) {
    this.typeInfo = typeInfo;
  }
  
  //--------------------------------------------------------------------
  // top-level nodes
  //--------------------------------------------------------------------
  
  public void visit(Program node) throws MyPLException {
    // push the "global" environment
    symbolTable.pushEnvironment();
    // (1) add each user-defined type name to the symbol table and to
    // the list of rec types, check for duplicate names
    for (TypeDecl tdecl : node.tdecls) {
      String t = tdecl.typeName.lexeme();
      if (symbolTable.nameExists(t))
        error("type '" + t + "' already defined", tdecl.typeName);
      // add as a record type to the symbol table
      symbolTable.add(t, "type");
      typeInfo.add(t);
    }

    // TODO: (2) add each function name and signature to the symbol
    // table check for duplicate names
    for (FunDecl fdecl : node.fdecls) {
      String funName = fdecl.funName.lexeme();
      // make sure not redefining built-in functions
      if (getBuiltinFunctions().contains(funName)) {
        String m = "cannot redefine built in function " + funName;
        error(m, fdecl.funName);
      }
      // check if function already exists
      if (symbolTable.nameExists(funName))
        error("function '" + funName + "' already defined", fdecl.funName);
     
      checkReturnType(fdecl.returnType);
      symbolTable.add(funName, "fun");
      typeInfo.add(funName);

      // TODO: add each formal parameter as a component type
      // ...
      List<String> paramNames = new ArrayList<>(typeInfo.components(funName));
      for (FunParam pdecl : fdecl.params) {
        paramNames = new ArrayList<>(typeInfo.components(funName));
        String p = pdecl.paramName.lexeme();
        // check for duplicate parameter names
        if(paramNames.contains(p))
          error("parameter '" + p + "' already defined", pdecl.paramName);
        // add to the symbol table as a parameter
        symbolTable.add(pdecl.paramName.lexeme(), pdecl.paramType.lexeme());
        // add to typeInfo
        typeInfo.add(funName, p, pdecl.paramType.lexeme());
        if(funName.equals("main")) {
          error("main contains param", null);
        }
      }
      // add the return type
      typeInfo.add(funName, "return", fdecl.returnType.lexeme());
    }
    // TODO: (3) ensure "void main()" defined and it has correct
    // signature
    // ...
    if (!symbolTable.nameExists("main"))
      error("function 'main' not defined ", null);
   
    // check each type and function
    for (TypeDecl tdecl : node.tdecls) 
      tdecl.accept(this);
    for (FunDecl fdecl : node.fdecls) 
      fdecl.accept(this);

    // all done, pop the global table
    symbolTable.popEnvironment();
  }

  public void visit(TypeDecl node) throws MyPLException {
    // TODO  
    String typeName = node.typeName.lexeme();
    typeInfo.add(typeName);
    // function added to typeInfo in visit(FunDecl)
    // (1) add each field name and type to the symbol table
    for(VarDeclStmt v : node.vdecls) {
      String vname = v.varName.lexeme();
      String vtype = "";
      if(v.typeName != null) {
        vtype = v.typeName.lexeme();
      } 
      // check for duplicate field names
      if(symbolTable.nameExists(vname))
        error("field '" + vname + "' already defined", v.varName);
      if(v.expr != null) {
        v.expr.accept(this);
      }
      if(v.typeName == null) {
        vtype = currType;
      }
      // add to typeInfo and symbol table
      typeInfo.add(typeName, vname, vtype);
      symbolTable.add(vname, vtype);
    }

  }
  
  public void visit(FunDecl node) throws MyPLException {
    // TODO
    symbolTable.pushEnvironment();
    String funName = node.funName.lexeme();
    if(node.stmts != null) {
      for(Stmt s : node.stmts) {
        s.accept(this);
      }
    }
    returnFlag = false;
    if (returnFlag == true) {
      if (node.returnType.lexeme().equals("void")) {
        if (!currType.equals("void")) {
            error(currType + " is not of type void", node.returnType);
          }
        }
        else {
          if (!node.returnType.lexeme().equals(currType) && !currType.equals("void")) {
            error(currType + " doesnt match return type" + node.returnType.lexeme(), node.returnType);
          }
        }
    }
      
    symbolTable.popEnvironment();
  }

  //--------------------------------------------------------------------
  // statement nodes
  //--------------------------------------------------------------------
  
  public void visit(VarDeclStmt node) throws MyPLException {
    // TODO
    String v = node.varName.lexeme();
    String t = null;
    if(node.typeName != null) {
      t = node.typeName.lexeme();
    }
    // check for duplicate variable names
    if (symbolTable.nameExistsInCurrEnv(v)){
      String m = v + " is already defined:";
      error(m, node.varName);
    }
    // check righthand side type
    if(node.expr != null)
      node.expr.accept(this);
      String rhs = currType;
      if (rhs.equals("void")) {
        if (node.typeName == null) {
          error("invalid type", node.typeName);
        } else {
          symbolTable.add(node.varName.lexeme(), node.typeName.lexeme());
          currType = node.typeName.lexeme();
        }
      } else if (node.typeName != null) {
        String lhs = node.typeName.lexeme();
        if (!rhs.equals(lhs)) {
          error ("expected " + lhs + " but found " + rhs, getFirstToken(node.expr));
        } else {
          symbolTable.add(node.varName.lexeme(), rhs);
        }
      } else {
        symbolTable.add(node.varName.lexeme(), rhs);
      }
  }

  public void visit(AssignStmt node) throws MyPLException {
    // TODO
    String lhs = "void";
    node.expr.accept(this);
    String rhs = currType;
    String varName = node.lvalue.get(0).lexeme();
    String typeName = symbolTable.get(varName);
    String lv2 = "";
    for(int i = 1; i < node.lvalue.size(); i++) {
      Token l2 = node.lvalue.get(i);
      lv2 = l2.lexeme();
      currType = symbolTable.get(lv2);

      lhs = currType;
    }
  
  }
  
  public void visit(CondStmt node) throws MyPLException {
    // TODO
    symbolTable.pushEnvironment();
    node.ifPart.cond.accept(this);
    if (!currType.equals("bool")) {
      error(currType + " is not bool", getFirstToken(node.ifPart.cond));
    }
    for (Stmt stmt : node.ifPart.stmts) {
      stmt.accept(this);
    }
    for (BasicIf elif : node.elifs) {
      symbolTable.pushEnvironment();
      elif.cond.accept(this);
      if (!currType.equals("bool")) {
        error(currType + " is not bool", getFirstToken(node.ifPart.cond));
      }
      symbolTable.popEnvironment();
    }
    symbolTable.pushEnvironment();
    if (node.elseStmts != null) {
      for (Stmt stmt : node.elseStmts) {
        stmt.accept(this);
      }
    }
    symbolTable.popEnvironment();
    symbolTable.popEnvironment();
  }
  
  public void visit(WhileStmt node) throws MyPLException {
    // TODO
    symbolTable.pushEnvironment();
    // check for valid condition 
    node.cond.accept(this);
    if(currType != "bool")
      error("invalid condition", getFirstToken(node.cond));
    // check for valid body
    for(Stmt s : node.stmts) {
      s.accept(this);
    }
    symbolTable.popEnvironment();
  }
  
  public void visit(ForStmt node) throws MyPLException {
    // TODO
    symbolTable.pushEnvironment();
    String var = node.varName.lexeme();
    node.start.accept(this);
    symbolTable.add(var, currType);
    if (currType != "int") {
      error(("start expression is not of type int"), getFirstToken(node.start));
    }
    node.end.accept(this);
    if (currType != "int") {
      error(("end expression is not of type int"), getFirstToken(node.start));
    }
    if(node.stmts != null) {
      for(Stmt s : node.stmts) {
        s.accept(this);
      }
    }
    
    symbolTable.popEnvironment();
  }
  
  public void visit(ReturnStmt node) throws MyPLException {
    // TODO
    returnFlag = true;
    if(node.expr != null) {
      node.expr.accept(this);
    } else {
      currType = "void";
    }
  }
   
  public void visit(DeleteStmt node) throws MyPLException {
    // TODO
    if (!(symbolTable.get(node.varName.lexeme()).equals("type"))) {
      error(node.varName.lexeme() + " is undefined", node.varName);
    }
  }
  
  //----------------------------------------------------------------------
  // statement and rvalue node
  //----------------------------------------------------------------------

  private void checkBuiltIn(CallExpr node) throws MyPLException {
    String funName = node.funName.lexeme();
    if (funName.equals("print")) {
      node.args.get(0).accept(this);
      // has to have one argument, any type is allowed
      if (node.args.size() != 1)
        error("print expects one argument", node.funName);
      currType = "void";
    }
    else if (funName.equals("read")) {
      // no arguments allowed
      if (node.args.size() != 0)
        error("read takes no arguments", node.funName);
      currType = "string";
    }
    else if (funName.equals("length")) {
      // one string argument
      if (node.args.size() != 1)
        error("length expects one argument", node.funName);
      Expr e = node.args.get(0);
      e.accept(this);
      if(currType != "string")
        error("expecting string in length", getFirstToken(e));
      currType = "int";
    }
    else if (funName.equals("get")) {
      // one string and one int argument
      if (node.args.size() != 2)
        error("get expects two arguments", node.funName);
      Expr e1 = node.args.get(0);
      Expr e2 = node.args.get(1);
      e1.accept(this);
      if(currType != "int")
        error("expecting int in get", getFirstToken(e1));
      e2.accept(this);
      if(currType != "string")
        error("expecting string in get", getFirstToken(e2));
      currType = "char";
    }
    else if (funName.equals("stoi")) {
      // one string argument
      if (node.args.size() != 1)
        error("stoi expects one argument", node.funName);
      Expr e = node.args.get(0);
      e.accept(this);
      if(currType != "string")
        error("expecting string in stoi", getFirstToken(e));
      currType = "int";
    }
    else if (funName.equals("stod")) {
      // one string argument
      if (node.args.size() != 1)
        error("stod expects one argument", node.funName);
      Expr e = node.args.get(0);
      e.accept(this);
      if(currType != "string")
        error("expecting string in stod", getFirstToken(e));
      currType = "double";
    }
    else if (funName.equals("itos")) {
      // TODO
      // one int argument
      if (node.args.size() != 1)
        error("itos expects one argument", node.funName);
      Expr e = node.args.get(0);
      e.accept(this);
      if(currType != "int")
        error("expecting int in itos", getFirstToken(e));
      currType = "string";
    }
    else if (funName.equals("itod")) {
      // one int argument
      if (node.args.size() != 1)
        error("itod expects one argument", node.funName);
      Expr e = node.args.get(0);
      e.accept(this);
      if(currType != "int")
        error("expecting int in itod", getFirstToken(e));
      currType = "double";
    }
    else if (funName.equals("dtos")) {
      // one double argument
      if (node.args.size() != 1)
        error("dtos expects one argument", node.funName);
      Expr e = node.args.get(0);
      e.accept(this);
      if(currType != "double")
        error("expecting double in dtos", getFirstToken(e));
      currType = "string";
    }
    else if (funName.equals("dtoi")) {
      // TODO
      // one double argument
      if (node.args.size() != 1)
        error("dtoi expects one argument", node.funName);
      Expr e = node.args.get(0);
      e.accept(this);
      if(currType != "double")
        error("expecting double in dtoi", getFirstToken(e));
      currType = "int";
    }
  }

  public void visit(CallExpr node) throws MyPLException {
    // TODO
    List<String> builtInNames = new ArrayList<>(getBuiltinFunctions());
    if (builtInNames.contains(node.funName.lexeme())) {
      checkBuiltIn(node);
    }
    else {
      if (!symbolTable.nameExists(node.funName.lexeme())) {
        error(node.funName.lexeme() + " is undefined", node.funName);
      }
      if (!symbolTable.get(node.funName.lexeme()).equals("fun")) {
        error(node.funName.lexeme() + " is not a defined function", node.funName);
      }
      List<String> params = new ArrayList<>(typeInfo.components(node.funName.lexeme()));
      if (params.size() - 1 != node.args.size()) {
        error(node.funName.lexeme() + " function call does not contain the correct number of arguments", node.funName);
      }
      for (int i = 0; i < params.size() - 1; ++i) {
        String paramName = params.get(i);
        String paramType = typeInfo.get(node.funName.lexeme(), paramName);
        node.args.get(i).accept(this);
        if (currType != paramType && !currType.equals("void")) {
          error(currType + " does not match the param type", getFirstToken(node.args.get(i)));
        }
      }
      currType = typeInfo.get(node.funName.lexeme(), "return");
    }
  }
  
  //----------------------------------------------------------------------
  // rvalue nodes
  //----------------------------------------------------------------------
  
  public void visit(SimpleRValue node) throws MyPLException {
    TokenType tokenType = node.value.type();
    if (tokenType == TokenType.INT_VAL)
      currType = "int";
    else if (tokenType == TokenType.DOUBLE_VAL)
      currType = "double";
    else if (tokenType == TokenType.BOOL_VAL)
      currType = "bool";
    else if (tokenType == TokenType.CHAR_VAL)    
      currType = "char";
    else if (tokenType == TokenType.STRING_VAL)
      currType = "string";
    else if (tokenType == TokenType.NIL)
      currType = "void";
  }
    
  public void visit(NewRValue node) throws MyPLException {
    // TODO
    String typeName = node.typeName.lexeme();
    if (!symbolTable.nameExists(typeName))
      error("type '" + typeName + "' not defined", node.typeName);
    // check for call to new
    if(symbolTable.get(typeName) == "fun") {
      error("cannot use function as variable", node.typeName);
    }
    currType = symbolTable.get(typeName);
  }
  
  public void visit(IDRValue node) throws MyPLException {
    // check for undefined variables
    String id1 = node.path.get(0).lexeme();
    String type1 = symbolTable.get(id1);
    if(type1 == "fun") {
      error("cannot use function as variable", node.path.get(0));
    }
    currType = type1;
    for(int i = 1; i < node.path.size(); i++) {
      Token id = node.path.get(i);
      currType = symbolTable.get(id.lexeme());
    }
    //if(currType == "type") {
      //error("cannot use type as variable", node.path.get(0));
    //}
  }
      
  public void visit(NegatedRValue node) throws MyPLException {
    // TODO
    currType = "bool";
  }
  

  //----------------------------------------------------------------------
  // expression node
  //----------------------------------------------------------------------
  
  public void visit(Expr node) throws MyPLException {
    node.first.accept(this); 
    String lhsType = currType;
    if(node.logicallyNegated) {
      if(lhsType != "bool")
        error("expecting bool in logical negation", getFirstToken(node.first));
    }
    if(node.rest != null) {
      node.rest.accept(this);
      // function instead of variable
      String rhs = currType;
      if(lhsType == "void" && !getValidTypes().contains(currType))
        error("nil cannot be used as an expression", getFirstToken(node.first));
      String lhs = lhsType;
      if (node.op.lexeme().equals("==") || node.op.lexeme().equals("!=")) {
        if (!lhs.equals(rhs) && !(lhs.equals("void") || rhs.equals("void"))) {
          error("can't compare " + lhs + " and " + rhs + " with " + node.op.lexeme(), node.op);
        }
        currType = "bool";
      } else if (node.op.lexeme().equals("<") || node.op.lexeme().equals(">") 
                  || node.op.lexeme().equals("<=")|| node.op.lexeme().equals(">=")){
        if (!lhs.equals(rhs)) {
          error(lhs + " and " + rhs + " don't match", getFirstToken(node));
        }
        if (!(lhs.equals("double") || lhs.equals("char") || lhs.equals("int") || lhs.equals("string"))) {
          error(lhs + " isn't compatible with operator ", node.op);
        }
        currType = "bool";
      } else if (node.op.lexeme().equals("*") || node.op.lexeme().equals("/") || node.op.lexeme().equals("-")) {
        if (!lhs.equals(rhs)) {
          error(lhs + " and " + rhs + " don't match", getFirstToken(node));
        }
        if (!(lhs.equals("int") || lhs.equals("double"))) {
          error(lhs + " isn't compatible with operator " + node.op.lexeme(), node.op);
        }
        currType = lhs;
      } else if (node.op.lexeme().equals("+")) {
        if (lhs.equals("char") || lhs.equals("string")) {
          if (!(rhs.equals("char") || rhs.equals("string")) || (lhs.equals("char") && rhs.equals("char"))) {
            error(lhs + " and " + rhs + " can't be added", node.op);
          }
          currType = "string";
        } else {
          if (!lhs.equals(rhs)) {
            lhs = "string";
          }
          if (!(lhs.equals("int") || lhs.equals("double"))) {
            error(lhs + " isn't compatible with operator +", node.op);
          }
          currType = lhs;
        }
      } else if (node.op.lexeme().equals("%")) {
        if (!lhs.equals(rhs) || !lhs.equals("int")) {
          error("% only compatible with ints", node.op);
        }
        currType = lhs;
      } else if (node.op.lexeme().equals("neg")) {
        if (!lhs.equals("int") && lhs.equals("double")) {
          error(lhs + " isn't compatible with operator neg", node.op);
        }
        currType = lhs;
      } else if (node.op.lexeme().equals("and") || node.op.lexeme().equals("or") || node.op.lexeme().equals("not")) {
        if (!(lhs.equals("bool") || rhs.equals("bool") || lhs.equals("void") || rhs.equals("void"))) {
          error(lhs + " and " + rhs + " are not compatible with operator " + node.op.lexeme(), node.op);
        }
        currType = "bool";
      }
    }
    if (node.logicallyNegated && !currType.equals("bool")) {
      error("can't negate non-boolean expression", getFirstToken(node));
    }
    
     
  }

  //----------------------------------------------------------------------
  // terms
  //----------------------------------------------------------------------
  
  public void visit(SimpleTerm node) throws MyPLException {
    node.rvalue.accept(this);
  }
  
  public void visit(ComplexTerm node) throws MyPLException {
    node.expr.accept(this);
  }

}

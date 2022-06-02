/*
 * File: PrintVisitor.java
 * Date: Spring 2022
 * Auth: Ahmad Moltafet
 * Desc: PrintVisitor class that prints the tree in a pre-order traversal
 *      and in a post-order traversal.
 */

import java.io.PrintStream;


public class PrintVisitor implements Visitor {

  // output stream for printing
  private PrintStream out;
  // current indent level (number of spaces)
  private int indent = 0;
  // indentation amount
  private final int INDENT_AMT = 2;
  
  //------------------------------------------------------------
  // HELPER FUNCTIONS
  //------------------------------------------------------------
  
  private String getIndent() {
    return " ".repeat(indent);
  }

  private void incIndent() {
    indent += INDENT_AMT;
  }

  private void decIndent() {
    indent -= INDENT_AMT;
  }

  //------------------------------------------------------------
  // VISITOR FUNCTIONS
  //------------------------------------------------------------

  // Hint: To help deal with call expressions, which can be statements
  // or expressions, statements should not indent themselves and add
  // newlines. Instead, the function asking statements to print
  // themselves should add the indent and newlines.
  

  // constructor
  public PrintVisitor(PrintStream printStream) {
    out = printStream;
  }

  // top-level nodes

  @Override
  public void visit(Program node) throws MyPLException {
    // print type decls first
    for (TypeDecl d : node.tdecls)
      d.accept(this);
    // print function decls second
    for (FunDecl d : node.fdecls)
      d.accept(this);
  }

  @Override
  public void visit(TypeDecl node) throws MyPLException {
    out.println(getIndent() + "type " + node.typeName.lexeme() + " {");
    incIndent();
    for (VarDeclStmt d : node.vdecls) {
      out.print(getIndent());
      d.accept(this);
      out.println();
    }
    decIndent();
    out.println(getIndent() + "}");
  }

  @Override
  public void visit(FunDecl node) throws MyPLException {
    out.print("fun " + node.returnType.lexeme() + " " + node.funName.lexeme() + "(");
    if (node.params.size() > 0) {
      int i = 0;
      for(FunParam p : node.params) {
        out.print(p.paramType.lexeme() + " " + p.paramName.lexeme());
        if(i < node.params.size() - 1) {
          out.print(", ");
        }
        i++;
      }
    }
    out.println(") {");
    incIndent();
    for (Stmt s : node.stmts) {
      out.print(getIndent());
      s.accept(this);
      out.print("\n");
    }
    decIndent();
    out.println(getIndent() + "}");
  }

  @Override
  public void visit(VarDeclStmt node) throws MyPLException {
    if (node.typeName != null) {
      out.print("var " + node.typeName.lexeme() + " " + node.varName.lexeme() + " = ");
    } else {
      out.print("var " + node.varName.lexeme() + " = ");
    }
    node.expr.accept(this);
  }

  @Override
  public void visit(AssignStmt node) throws MyPLException {
    for(Token left : node.lvalue) {
      out.print(left.lexeme());
    }
    out.print(" = ");
    node.expr.accept(this);
  }

  @Override
  public void visit(CondStmt node) throws MyPLException {
    out.print("if ");
    node.ifPart.cond.accept(this);
    out.println(" {");
    incIndent();
    for(Stmt s : node.ifPart.stmts) {
      out.print(getIndent());
      s.accept(this);
      out.println();
    }
    decIndent();
    out.print(getIndent() + "}");
    for(BasicIf b : node.elifs) {
      out.print("\n" + getIndent() + "elif ");
      b.cond.accept(this);
      out.println(" {");
      incIndent();
      for (Stmt s : b.stmts) {
        out.print(getIndent());
        s.accept(this);
        out.println();
      }
      decIndent();
      out.print(getIndent() + "}");
    }
    if(node.elseStmts.size() > 0) {
      out.print("\n" + getIndent() + "else {" + "\n");
      incIndent();
      for (Stmt s : node.elseStmts) {
        out.print(getIndent());
        s.accept(this);
        out.println();
      }
      decIndent();
      out.print(getIndent() + "}");
    }
  }

  @Override
  public void visit(WhileStmt node) throws MyPLException {
    out.print("while ");
    node.cond.accept(this);
    out.println(" {");
    incIndent();
    for (Stmt s : node.stmts) {
      out.print(getIndent());
      s.accept(this);
      out.print("\n");
    }
    decIndent();
    out.print(getIndent() + "}");
  }

  @Override
  public void visit(ForStmt node) throws MyPLException {
    out.print("for " + node.varName.lexeme() + " from ");
    node.start.accept(this);
    if (node.upto == true) {
      out.print(" upto ");
    } else {
      out.print(" downto ");
    }
    node.end.accept(this);
    out.println(" {");
    incIndent();
    for (Stmt s : node.stmts) {
      out.print(getIndent());
      s.accept(this);
      out.println();
    }
    decIndent();
    out.print(getIndent() + "}");
  }

  @Override
  public void visit(ReturnStmt node) throws MyPLException {
    out.print("return ");
    node.expr.accept(this);
  }

  @Override
  public void visit(DeleteStmt node) throws MyPLException {
    out.print("delete " + node.varName.lexeme());
  }

  @Override
  public void visit(CallExpr node) throws MyPLException {
    out.print(node.funName.lexeme() + "(");
    for (int i = 0; i < node.args.size() - 1; ++i) {
      node.args.get(i).accept(this);
      out.print(", ");
    }
    node.args.get(node.args.size() - 1).accept(this);
    out.print(")");
  }

  @Override
  public void visit(SimpleRValue node) throws MyPLException {
    if(node.value.type() == TokenType.STRING_VAL) {
        out.print("\"" + node.value.lexeme() + "\"");
        return;
       }
    if(node.value.type() == TokenType.CHAR_VAL) {
        out.print("'" + node.value.lexeme() + "'");
        return;
    }
     else {
        out.print(node.value.lexeme());
    }
  }

  @Override
  public void visit(NewRValue node) throws MyPLException {
    out.print("new " + node.typeName.lexeme());
  }

  @Override
  public void visit(IDRValue node) throws MyPLException {
    for (int i = 0; i < node.path.size() - 1; ++i) {
      out.print(node.path.get(i).lexeme());
    }
    out.print(node.path.get(node.path.size() - 1).lexeme());
  }

  @Override
  public void visit(NegatedRValue node) throws MyPLException {
    out.print("neg ");
    node.expr.accept(this);
  }

  @Override
  public void visit(Expr node) throws MyPLException {
    if (node.op != null || node.logicallyNegated == true) {
      out.print("(");
    }
    if (node.logicallyNegated == true) {
      out.print("not ");
    }
    node.first.accept(this);
    if (node.op != null) {
      out.print(" " + node.op.lexeme() + " ");
    }
    if (node.rest != null) {
      node.rest.accept(this);
    }
    if (node.op != null || node.logicallyNegated == true) {
      out.print(")");
    }
  }

  @Override
  public void visit(SimpleTerm node) throws MyPLException {
    node.rvalue.accept(this);
  }

  @Override
  public void visit(ComplexTerm node) throws MyPLException {
    node.expr.accept(this);
  }

} 

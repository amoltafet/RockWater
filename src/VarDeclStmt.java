/*
 * File: VarDeclStmt.java
 * Date: Spring 2022
 * Auth: S. Bowers
 * Desc: An AST node for representing variable declarations.
 */


public class VarDeclStmt implements Stmt {

  public Token typeName = null;
  public Token varName = null;
  public Expr expr = null;
  
  @Override
  public void accept(Visitor visitor) throws MyPLException {
    visitor.visit(this);
  }

}

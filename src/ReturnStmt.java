/*
 * File: ReturnStmt.java
 * Date: Spring 2022
 * Auth: S. Bowers
 * Desc: An AST node for representing a return statement.
 */


public class ReturnStmt implements Stmt {

  public Expr expr = null;
  
  @Override
  public void accept(Visitor visitor) throws MyPLException {
    visitor.visit(this);
  }

}

/*
 * File: DeleteStmt.java
 * Date: Spring 2022
 * Auth: S. Bowers
 * Desc: An AST node for representing delete statements.
 */


public class DeleteStmt implements Stmt {

  public Token varName = null;
  
  @Override
  public void accept(Visitor visitor) throws MyPLException {
    visitor.visit(this);
  }

}

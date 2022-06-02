/*
 * File: ForStmt.java
 * Date: Spring 2022
 * Auth: S. Bowers
 * Desc: An AST node for representing for statements.
 */

import java.util.ArrayList;
import java.util.List;


public class ForStmt implements Stmt {

  public Token varName = null;
  public Expr start = null;
  public boolean upto = true;
  public Expr end = null;
  public List<Stmt> stmts = new ArrayList<>();
  
  @Override
  public void accept(Visitor visitor) throws MyPLException {
    visitor.visit(this);
  }

}

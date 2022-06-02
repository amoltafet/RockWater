/*
 * File: WhileStmt.java
 * Date: Spring 2022
 * Auth: S. Bowers
 * Desc: While statement AST Node
 */

import java.util.ArrayList;
import java.util.List;


public class WhileStmt implements Stmt {

  public Expr cond = null;
  public List<Stmt> stmts = new ArrayList<>();
  
  @Override
  public void accept(Visitor visitor) throws MyPLException {
    visitor.visit(this);
  }

  



}

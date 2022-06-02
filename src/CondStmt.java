/*
 * File: CondStmt.java
 * Date: Spring 2022
 * Auth: S. Bowers
 * Desc: An AST node for representing conditional (if-elif-else)
 *       statments. Note the elseStmts must be created if an else part
 *       is present.
 */

import java.util.ArrayList;
import java.util.List;


public class CondStmt implements Stmt {

  public BasicIf ifPart = null;
  public List<BasicIf> elifs = new ArrayList<>();
  public List<Stmt> elseStmts = null; 
  
  @Override
  public void accept(Visitor visitor) throws MyPLException {
    visitor.visit(this);
  }

}

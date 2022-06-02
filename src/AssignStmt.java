/*
 * File: AssignStmt.java
 * Date: Spring 2022
 * Auth: S. Bowers
 * Desc: An AST node representing variable assignment statements
 */

import java.util.ArrayList;
import java.util.List;

public class AssignStmt implements Stmt {

  public List<Token> lvalue = new ArrayList<>();
  public Expr expr = null;
  
  @Override
  public void accept(Visitor visitor) throws MyPLException {
    visitor.visit(this);
  }

}

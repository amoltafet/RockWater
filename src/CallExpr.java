/*
 * File: CallExpr.java
 * Date: Spring 2022
 * Auth: S. Bowers
 * Desc: AST Node for representing function calls. Function calls can
 *       be both statements and expression values.
 */

import java.util.ArrayList;
import java.util.List;


public class CallExpr implements RValue, Stmt {

  public Token funName = null;
  public List<Expr> args = new ArrayList<>();
  
  @Override
  public void accept(Visitor visitor) throws MyPLException {
    visitor.visit(this);
  }

}

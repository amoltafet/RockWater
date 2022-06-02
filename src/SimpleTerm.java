/*
 * File: SimpleTerm.java
 * Date: Spring 2022
 * Auth: S. Bowers
 * Desc: An AST node for representing simple (as opposed to complex)
 *       expression terms, which consist of a single RValue.
 */


public class SimpleTerm implements ExprTerm {

  public RValue rvalue = null;
  
  @Override
  public void accept(Visitor visitor) throws MyPLException {
    visitor.visit(this);
  }

  
}

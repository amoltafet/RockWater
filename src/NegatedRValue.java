/*
 * File: NegaedRValue.java
 * Date: Spring 2022
 * Auth: S. Bowers
 * Desc: An AST node for representing integer and double negation.
 */


public class NegatedRValue implements RValue {

  public Expr expr = null;

  @Override
  public void accept(Visitor visitor) throws MyPLException {
    visitor.visit(this);
  }

}

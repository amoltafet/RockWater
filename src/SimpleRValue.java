/*
 * File: SimpleRValue.java
 * Date: Spring 2022
 * Auth: S. Bowers
 * Desc: An AST node for representing simple, literal values. 
 */

public class SimpleRValue implements RValue {

  public Token value = null;

  @Override
  public void accept(Visitor visitor) throws MyPLException {
    visitor.visit(this);
  }

}

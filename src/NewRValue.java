/*
 * File: NewRValue.java
 * Date: Spring 2022
 * Auth: S. Bowers
 * Desc: An AST node for representing the creation of a new
 *       user-defined type object.
 */


public class NewRValue implements RValue {

  public Token typeName = null;

  @Override
  public void accept(Visitor visitor) throws MyPLException {
    visitor.visit(this);
  }

}

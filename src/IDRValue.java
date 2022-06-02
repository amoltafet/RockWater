/*
 * File: IDRValue.java
 * Date: Spring 2022
 * Auth: S. Bowers
 * Desc: An AST node for representing a simple path expression. A path
 *       expression provides access to the components of a
 *       user-defined type object.
 */

import java.util.ArrayList;
import java.util.List;


public class IDRValue implements RValue {

  public List<Token> path = new ArrayList<>();

  @Override
  public void accept(Visitor visitor) throws MyPLException {
    visitor.visit(this);
  }

}

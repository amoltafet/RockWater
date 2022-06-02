/*
 * File: ComplexTerm.java
 * Date: Spring 2022
 * Auth: S. Bowers
 * Desc: An AST Node for representing complex expression terms, which
 *       consist of an Expr AST Node. Complex terms are used to
 *       represent binary operations as well as parenthesized
 *       expressions.
 */


public class ComplexTerm implements ExprTerm {

  public Expr expr = null;
  
  @Override
  public void accept(Visitor visitor) throws MyPLException {
    visitor.visit(this);
  }

  
}

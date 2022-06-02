/*
 * File: Expr.java
 * Date: Spring 2022
 * Auth: S. Bowers
 * Desc: An AST Node for representing simple and complex
 *       expressions. An expression consists of a (potentially negated
 *       via not) term, followed by an optional binary operator and
 *       the rest of the expression. If the expression has a non-null
 *       operator, then it should also have a non-null rest
 *       expression.
 */


public class Expr implements ASTNode {

  public boolean logicallyNegated = false;
  public ExprTerm first = null;
  public Token op = null;
  public Expr rest = null;
  
  @Override
  public void accept(Visitor visitor) throws MyPLException {
    visitor.visit(this);
  }

  
}

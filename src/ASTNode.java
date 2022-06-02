/*
 * File: ASTNode.java
 * Date: Spring 2022
 * Auth: S. Bowers
 * Desc: The base interface for Abstract Syntax Tree (AST) node
 *       classes.
 */

public interface ASTNode {

  // for the visitor pattern
  public void accept(Visitor visitor) throws MyPLException;
}


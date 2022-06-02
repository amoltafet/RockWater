/*
 * File: FunDecl.java
 * Date: Spring 2022
 * Auth: S. Bowers
 * Desc: An AST node for representing function declarations.
 */

import java.util.ArrayList;
import java.util.List;


public class FunDecl implements ASTNode {

  public Token returnType = null;
  public Token funName = null;
  public List<FunParam> params = new ArrayList<>();
  public List<Stmt> stmts = new ArrayList<>();
  
  @Override
  public void accept(Visitor visitor) throws MyPLException {
    visitor.visit(this);
  }

}

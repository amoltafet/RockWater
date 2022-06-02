/*
 * File: TypeDecl.java
 * Date: Spring 2022
 * Auth: S. Bowers
 * Desc: An AST node for representing user-defined type declarations.
 */

import java.util.ArrayList;
import java.util.List;


public class TypeDecl implements ASTNode {

  public Token typeName = null;
  public List<VarDeclStmt> vdecls = new ArrayList<>();

  @Override
  public void accept(Visitor visitor) throws MyPLException {
    visitor.visit(this);
  }

}

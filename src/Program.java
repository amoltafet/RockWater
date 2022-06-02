/*
 * File: Program.java
 * Date: Spring 2022
 * Auth: S. Bowers
 * Desc: An AST node for representing a MyPL program. The Program node
 *       is the root level AST node for all MyPL programs.
 */

import java.util.ArrayList;
import java.util.List;

public class Program implements ASTNode {

  public List<FunDecl> fdecls = new ArrayList<>();
  public List<TypeDecl> tdecls = new ArrayList<>();

  @Override
  public void accept(Visitor visitor) throws MyPLException {
    visitor.visit(this);
  }
}

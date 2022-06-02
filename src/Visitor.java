/*
 * File: Visitor.java
 * Date: Spring 2022
 * Auth: S. Bowers
 * Desc: Interface (functions) for the MyPL AST visitor classes
 */


public interface Visitor {

  // top-level nodes
  public void visit(Program node) throws MyPLException;
  public void visit(TypeDecl node) throws MyPLException;
  public void visit(FunDecl node) throws MyPLException;

  // statement nodes
  public void visit(VarDeclStmt node) throws MyPLException;
  public void visit(AssignStmt node) throws MyPLException;  
  public void visit(CondStmt node) throws MyPLException;
  public void visit(WhileStmt node) throws MyPLException;
  public void visit(ForStmt node) throws MyPLException;  
  public void visit(ReturnStmt node) throws MyPLException;  
  public void visit(DeleteStmt node) throws MyPLException;  

  // statement and rvalue node
  public void visit(CallExpr node) throws MyPLException;  

  // rvalue nodes
  public void visit(SimpleRValue node) throws MyPLException;    
  public void visit(NewRValue node) throws MyPLException;      
  public void visit(IDRValue node) throws MyPLException;      
  public void visit(NegatedRValue node) throws MyPLException;      

  // expression node
  public void visit(Expr node) throws MyPLException;

  // terms
  public void visit(SimpleTerm node) throws MyPLException;
  public void visit(ComplexTerm node) throws MyPLException;

}

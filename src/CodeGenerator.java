/*
 * File: CodeGenerator.java
 * Date: Spring 2022
 * Auth:
 * Desc: 
 */

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;


public class CodeGenerator implements Visitor {

  // the user-defined type and function type information
  private TypeInfo typeInfo = null;

  // the virtual machine to add the code to
  private VM vm = null;

  // the current frame
  private VMFrame currFrame = null;

  // mapping from variables to their indices (in the frame)
  private Map<String,Integer> varMap = null;

  // the current variable index (in the frame)
  private int currVarIndex = 0;

  // to keep track of the typedecl objects for initialization
  Map<String,TypeDecl> typeDecls = new HashMap<>();

  // used for checking if a return stmt exists
  private Boolean returnStmt = false;


  //----------------------------------------------------------------------
  // HELPER FUNCTIONS
  //----------------------------------------------------------------------
  
  // helper function to clean up uneeded NOP instructions
  private void fixNoOp() {
    int nextIndex = currFrame.instructions.size();
    // check if there are any instructions
    if (nextIndex == 0)
      return;
    // get the last instuction added
    VMInstr instr = currFrame.instructions.get(nextIndex - 1);
    // check if it is a NOP
    if (instr.opcode() == OpCode.NOP)
      currFrame.instructions.remove(nextIndex - 1);
  }

  private void fixCallStmt(Stmt s) {
    // get the last instuction added
    if (s instanceof CallExpr) {
      VMInstr instr = VMInstr.POP();
      instr.addComment("clean up call return value");
      currFrame.instructions.add(instr);
    }

  }
  
  //----------------------------------------------------------------------  
  // Constructor
  //----------------------------------------------------------------------

  public CodeGenerator(TypeInfo typeInfo, VM vm) {
    this.typeInfo = typeInfo;
    this.vm = vm;
  }

  
  //----------------------------------------------------------------------
  // VISITOR FUNCTIONS
  //----------------------------------------------------------------------
  
  public void visit(Program node) throws MyPLException {

    // store UDTs for later
    for (TypeDecl tdecl : node.tdecls) {
      // add a mapping from type name to the TypeDecl
      typeDecls.put(tdecl.typeName.lexeme(), tdecl);
    }
    // only need to translate the function declarations
    for (FunDecl fdecl : node.fdecls)
      fdecl.accept(this);
  }

  public void visit(TypeDecl node) throws MyPLException {
    // Intentionally left blank -- nothing to do here
  }
  
  public void visit(FunDecl node) throws MyPLException {
    // TODO: 
    currFrame = new VMFrame(node.funName.lexeme(), node.params.size());
    vm.add(currFrame);
    currVarIndex = 0;
    varMap = new HashMap<>();

    for (FunParam param : node.params) {
      varMap.put(param.paramName.lexeme(), currVarIndex);
      currFrame.instructions.add(VMInstr.STORE(varMap.get(param.paramName.lexeme())));
      currFrame.pc += 1;
      currVarIndex += 1;
    }
  
    if (node.stmts.size() > 0) {
      for (Stmt stmt : node.stmts) {
        stmt.accept(this);
      }

      if (!(node.stmts.get(node.stmts.size() - 1) instanceof ReturnStmt)) {
        currFrame.instructions.add(VMInstr.PUSH(VM.NIL_OBJ));
        currFrame.pc += 1;
        currFrame.instructions.add(VMInstr.VRET());
        currFrame.pc += 1;
      }
    }
    else {
      currFrame.instructions.add(VMInstr.PUSH(VM.NIL_OBJ));
      currFrame.pc += 1;
      currFrame.instructions.add(VMInstr.VRET());
      currFrame.pc += 1;
    }
  }
  
  public void visit(VarDeclStmt node) throws MyPLException {
    // TODO
    node.expr.accept(this); 
    varMap.put(node.varName.lexeme(), currVarIndex);
    currVarIndex += 1;
    currFrame.instructions.add(VMInstr.STORE(varMap.get(node.varName.lexeme())));
    currFrame.pc += 1;
  }
  
  public void visit(AssignStmt node) throws MyPLException {
    // TODO
    node.expr.accept(this);
    if (node.lvalue.size() >= 2) {
      currFrame.instructions.add(VMInstr.LOAD(varMap.get(node.lvalue.get(0).lexeme())));
      currFrame.pc += 1;
      for (int i = 1; i < node.lvalue.size() - 1; ++i) {
        currFrame.instructions.add(VMInstr.GETFLD(node.lvalue.get(i).lexeme())); 
        currFrame.pc += 1;
      }
      currFrame.instructions.add(VMInstr.SWAP());
      currFrame.pc += 1;
      currFrame.instructions.add(VMInstr.SETFLD(node.lvalue.get(node.lvalue.size() - 1).lexeme()));
      currFrame.pc += 1;
    } else {
      currFrame.instructions.add(VMInstr.STORE(varMap.get(node.lvalue.get(0).lexeme())));
      currFrame.pc += 1;
    }
  }
  
  public void visit(CondStmt node) throws MyPLException {
    // TODO
    List<Integer> jmpfIndexes = new ArrayList<>(); 
    node.ifPart.cond.accept(this);
    int index = currFrame.instructions.size();
    currFrame.instructions.add(VMInstr.JMPF(-1));
    currFrame.pc += 1;
    for (Stmt stmt : node.ifPart.stmts) {
      stmt.accept(this);
    }
    if (node.elifs.size() > 0) {
      for (BasicIf elifStmt : node.elifs) {
        elifStmt.cond.accept(this);
        jmpfIndexes.add(currFrame.instructions.size());
        currFrame.instructions.add(VMInstr.JMPF(-1));
        currFrame.pc += 1;
        for (Stmt stmt : elifStmt.stmts) {
          stmt.accept(this);
        }
      }
    }
    if(node.elseStmts != null) {
      for (Stmt stmt : node.elseStmts) {
        stmt.accept(this);
      }
    }
    currFrame.instructions.add(VMInstr.NOP());
    currFrame.pc++;
    currFrame.instructions.set(index, VMInstr.JMPF(currFrame.pc - 1));
    for (int i : jmpfIndexes) {
      currFrame.instructions.set(i, VMInstr.JMPF(currFrame.pc - 1));
    }
  }

  public void visit(WhileStmt node) throws MyPLException {
    // TODO
    int startIndex = currFrame.pc;
    node.cond.accept(this);
    int index = currFrame.instructions.size();
    currFrame.instructions.add(VMInstr.JMPF(-1));
    currFrame.pc++;
    for (Stmt stmt : node.stmts) {
      stmt.accept(this);
    }
    currFrame.instructions.add(VMInstr.JMP(startIndex));
    currFrame.pc++;
    currFrame.instructions.add(VMInstr.NOP());
    currFrame.pc++;
    currFrame.instructions.set(index, VMInstr.JMPF(currFrame.pc - 1));
  }

  public void visit(ForStmt node) throws MyPLException {
    int c = 0;
    if (varMap.get(node.varName.lexeme()) != null) {
      c = varMap.get(node.varName.lexeme());
    }
    else { 
      varMap.put(node.varName.lexeme(), currVarIndex);
      c = currVarIndex;
      currVarIndex += 1;
    }
    node.start.accept(this);
    currFrame.instructions.add(VMInstr.STORE(c));
    currFrame.pc++;
    int i = currFrame.pc;
    currFrame.instructions.add(VMInstr.LOAD(c));
    currFrame.pc++;
    node.end.accept(this);
    if (node.upto == true) {
      currFrame.instructions.add(VMInstr.CMPLE());
      currFrame.pc++;
    }
    else {
      currFrame.instructions.add(VMInstr.CMPGE());
      currFrame.pc++;
    }
    int index = currFrame.instructions.size();
    currFrame.instructions.add(VMInstr.JMPF(-1));
    currFrame.pc++;
    if(node.stmts != null) {
      for (Stmt stmt : node.stmts) {
        stmt.accept(this);
      }
    }
    currFrame.instructions.add(VMInstr.LOAD(c));
    currFrame.pc++;
    currFrame.instructions.add(VMInstr.PUSH(1)); 
    currFrame.pc++;
    if (node.upto == true) {
      currFrame.instructions.add(VMInstr.ADD());
      currFrame.pc++;
    }
    else {
      currFrame.instructions.add(VMInstr.SUB());
      currFrame.pc++;
    }
    currFrame.instructions.add(VMInstr.STORE(c));
    currFrame.pc++;
    currFrame.instructions.add(VMInstr.JMP(i));
    currFrame.pc++;
    currFrame.instructions.add(VMInstr.NOP());
    currFrame.pc++;
    currFrame.instructions.set(index, VMInstr.JMPF(currFrame.pc - 1));
  }
  
  public void visit(ReturnStmt node) throws MyPLException {
    // TODO
    if (node.expr != null) {
      node.expr.accept(this);
      currFrame.instructions.add(VMInstr.VRET());
    }
    else {
      currFrame.instructions.add(VMInstr.PUSH(VM.NIL_OBJ));
      currFrame.pc++;
      currFrame.instructions.add(VMInstr.VRET());
    }
    currFrame.pc++;
  }
  
  public void visit(DeleteStmt node) throws MyPLException {
    // TODO
    currFrame.instructions.add(VMInstr.LOAD(varMap.get(node.varName.lexeme())));
    currFrame.pc += 1;
    currFrame.instructions.add(VMInstr.FREE());
    currFrame.pc += 1;
    currFrame.instructions.add(VMInstr.PUSH(VM.NIL_OBJ)); // 
    currFrame.pc += 1; // 
    currFrame.instructions.add(VMInstr.STORE(varMap.get(node.varName.lexeme()))); //
    currFrame.pc += 1; // 
   
  }

  public void visit(CallExpr node) throws MyPLException {
    // TODO: Finish the following (partially completed)
    // push args (in order)
    for (Expr arg : node.args)
      arg.accept(this);
    // built-in functions:
    if (node.funName.lexeme().equals("print")) {
      currFrame.instructions.add(VMInstr.WRITE());
      currFrame.pc++;
      currFrame.instructions.add(VMInstr.PUSH(VM.NIL_OBJ));
      currFrame.pc++;
      fixCallStmt(node);
    }
    else if (node.funName.lexeme().equals("read")) {
      currFrame.instructions.add(VMInstr.READ());
      currFrame.pc++;
    }
    // TODO: add remaining built in functions
    else if (node.funName.lexeme().equals("get")) {
      currFrame.instructions.add(VMInstr.GETCHR());
      currFrame.pc++;    
    }
    else if (node.funName.lexeme().equals("length")) {
      currFrame.instructions.add(VMInstr.LEN());
      currFrame.pc++;
    }
    else if (node.funName.lexeme().equals("stoi")) {
      currFrame.instructions.add(VMInstr.TOINT());
      currFrame.pc++;
    }
    else if (node.funName.lexeme().equals("stod")) {
      currFrame.instructions.add(VMInstr.TODBL());
      currFrame.pc++;
    }
    else if (node.funName.lexeme().equals("itos")) {
      currFrame.instructions.add(VMInstr.TOSTR());
      currFrame.pc++;
    }
    else if (node.funName.lexeme().equals("dtos")) {
      currFrame.instructions.add(VMInstr.TOSTR());
      currFrame.pc++;
    }
    else if (node.funName.lexeme().equals("dtoi")) {
      currFrame.instructions.add(VMInstr.TOINT());
      currFrame.pc++;
    }
    // user-defined functions
    else
      currFrame.instructions.add(VMInstr.CALL(node.funName.lexeme()));
      currFrame.pc++;
  }
  
  public void visit(SimpleRValue node) throws MyPLException {
    if (node.value.type() == TokenType.INT_VAL) {
      int val = Integer.parseInt(node.value.lexeme());
      currFrame.instructions.add(VMInstr.PUSH(val));
      currFrame.pc += 1;
    }
    else if (node.value.type() == TokenType.DOUBLE_VAL) {
      double val = Double.parseDouble(node.value.lexeme());
      currFrame.instructions.add(VMInstr.PUSH(val));
      currFrame.pc += 1;
    }
    else if (node.value.type() == TokenType.BOOL_VAL) {
      if (node.value.lexeme().equals("true")) {
        currFrame.instructions.add(VMInstr.PUSH(true));
        currFrame.pc += 1;
      }
      else {
        currFrame.instructions.add(VMInstr.PUSH(false));   
        currFrame.pc += 1;     
      }
    }
    else if (node.value.type() == TokenType.CHAR_VAL) {
      String s = node.value.lexeme();
      s = s.replace("\\n", "\n");
      s = s.replace("\\t", "\t");
      s = s.replace("\\r", "\r");
      s = s.replace("\\\\", "\\");
      currFrame.instructions.add(VMInstr.PUSH(s));
      currFrame.pc += 1;
    }
    else if (node.value.type() == TokenType.STRING_VAL) {
      String s = node.value.lexeme();
      s = s.replace("\\n", "\n");
      s = s.replace("\\t", "\t");
      s = s.replace("\\r", "\r");
      s = s.replace("\\\\", "\\");
      currFrame.instructions.add(VMInstr.PUSH(s));
      currFrame.pc += 1;
    }
    else if (node.value.type() == TokenType.NIL) {
      currFrame.instructions.add(VMInstr.PUSH(VM.NIL_OBJ));
      currFrame.pc += 1;
    }
  }
  
  public void visit(NewRValue node) throws MyPLException {
    // TODO
    List<String> args = new ArrayList<>();
    TypeDecl tdecl = typeDecls.get(node.typeName.lexeme());
    if (tdecl != null) {
      for (VarDeclStmt vdecl : tdecl.vdecls) {
        args.add(vdecl.varName.lexeme());
      }
      currFrame.instructions.add(VMInstr.ALLOC(args));
      currFrame.pc += 1;
      for (VarDeclStmt vdecl : tdecl.vdecls) {
        currFrame.instructions.add(VMInstr.DUP());
        currFrame.pc += 1;
        vdecl.expr.accept(this);
        currFrame.instructions.add(VMInstr.SETFLD(vdecl.varName.lexeme()));
        currFrame.pc += 1;
      }
    }

  }

  public void visit(IDRValue node) throws MyPLException {
    // TODO
    currFrame.instructions.add(VMInstr.LOAD(varMap.get(node.path.get(0).lexeme())));
    currFrame.pc += 1;
    for (Token i : node.path.subList(1, node.path.size())) {
      if(VMInstr.GETFLD(i.lexeme()) != null) {
      currFrame.instructions.add(VMInstr.GETFLD(i.lexeme()));
      currFrame.pc += 1;
    }
    }
  }
      
  public void visit(NegatedRValue node) throws MyPLException {
    // TODO
    node.expr.accept(this);
    currFrame.instructions.add(VMInstr.NEG());
    currFrame.pc += 1;
  }

  public void visit(Expr node) throws MyPLException {
    // TODO
    node.first.accept(this);
    if (node.rest != null) {
      node.rest.accept(this);
      if (node.op.lexeme().equals("+")) {
        currFrame.instructions.add(VMInstr.ADD());
        currFrame.pc += 1;
      }
      else if (node.op.lexeme().equals("-")) {
        currFrame.instructions.add(VMInstr.SUB());
        currFrame.pc += 1;
      }
      else if (node.op.lexeme().equals("*")) {
        currFrame.instructions.add(VMInstr.MUL());
        currFrame.pc += 1;
      }
      else if (node.op.lexeme().equals("/")) {
        currFrame.instructions.add(VMInstr.DIV());
        currFrame.pc += 1;
      }
      else if (node.op.lexeme().equals("%")) {
        currFrame.instructions.add(VMInstr.MOD());
        currFrame.pc += 1;
      }
      else if (node.op.lexeme().equals("and")) {
        currFrame.instructions.add(VMInstr.AND());
        currFrame.pc += 1;
      }
      else if (node.op.lexeme().equals("or")) {
        currFrame.instructions.add(VMInstr.OR());
        currFrame.pc += 1;
      }
      else if (node.op.lexeme().equals("<=")) {
        currFrame.instructions.add(VMInstr.CMPLE());
        currFrame.pc += 1;
      }
      else if (node.op.lexeme().equals(">=")) {
        currFrame.instructions.add(VMInstr.CMPGE());
        currFrame.pc += 1;
      }
      else if (node.op.lexeme().equals("<")) {
        currFrame.instructions.add(VMInstr.CMPLT());
        currFrame.pc += 1;
      }
      else if (node.op.lexeme().equals(">")) {
        currFrame.instructions.add(VMInstr.CMPGT());
        currFrame.pc += 1;
      }
      else if (node.op.lexeme().equals("==")) {
        currFrame.instructions.add(VMInstr.CMPEQ());
        currFrame.pc += 1;
      }
      else if (node.op.lexeme().equals("!=")) {
        currFrame.instructions.add(VMInstr.CMPNE());
        currFrame.pc += 1;
      }
    }
    if (node.logicallyNegated == true) {
      currFrame.instructions.add(VMInstr.NOT());
      currFrame.pc += 1;
    }
  }

  public void visit(SimpleTerm node) throws MyPLException {
    node.rvalue.accept(this);
  }
  
  public void visit(ComplexTerm node) throws MyPLException {
    node.expr.accept(this);
  }

}


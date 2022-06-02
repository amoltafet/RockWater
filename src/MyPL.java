/*
 * File: MyPL.java
 * Date: Spring 2022
 * Auth: S. Bowers
 * Desc: Driver program for HW-7
 */

import java.io.InputStream;
import java.nio.file.StandardCopyOption;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class MyPL {

  public static void main(String[] args) throws IOException {
    try {

      boolean lexerMode = false;
      boolean parseMode = false;
      boolean printMode = false;
      boolean checkMode = false;
      boolean outIRMode = false;
      int argCount = args.length;
      InputStream input = System.in;

      // check for too many command line args
      if (argCount > 2) {
        displayUsageInfo();
        System.exit(1);
      }
      
      // check if in lexer or print mode
      if (argCount > 0 && args[0].equals("--lex"))
        lexerMode = true;
      else if (argCount > 0 && args[0].equals("--parse"))
        parseMode = true;
      else if (argCount > 0 && args[0].equals("--print"))
        printMode = true;
      else if (argCount > 0 && args[0].equals("--check"))
        checkMode = true;
      else if (argCount > 0 && args[0].equals("--ir"))
        outIRMode = true;

      // to check modes
      boolean specialMode = lexerMode || printMode || parseMode ||
        checkMode || outIRMode;

      // check if incorrect args 
      if (argCount == 2 && !specialMode) {
        displayUsageInfo();
        System.exit(1);
      }

      // grab input file
      String inFile = null;
      String mainFile = "";
      if (argCount == 2) {
        input = new FileInputStream(args[1]);
        mainFile = args[1];
      }
      else if (argCount == 1 && !specialMode) {
        input = new FileInputStream(args[0]);
        mainFile = args[0];
      }
      // ------------------------------------------------------------
      // Preprocess the input file
      Scanner scanner = new Scanner(input);
      // check for import statements
      ArrayList<String> imports = new ArrayList<String>();
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        if (line.startsWith("import")) {
          imports.add(line);
        }
      }
      // reset scanner
      scanner = new Scanner(input);
      for (String line : imports) {
        // grab the file name
        String fileName = line.substring(7, line.length());
        // add directory to file name
        // merge the file
        input = mergeTwoFiles(fileName, mainFile);
      }
      // ------------------------------------------------------------
      // create the lexer
      Lexer lexer = new Lexer(input);

      // run in lexer mode
      if (lexerMode) {
        Token t = lexer.nextToken();
        while (t.type() != TokenType.EOS) {
          System.out.println(t);
          t = lexer.nextToken();
        }
      }
      // run in parser mode
      else if (parseMode) {
        Parser parser = new Parser(lexer);
        parser.parse();
      }
      // run in print mode
      else if (printMode) {
        ASTParser parser = new ASTParser(lexer);
        Program program = parser.parse();
        PrintVisitor visitor = new PrintVisitor(System.out);
        program.accept(visitor);
      }
      // run in static checker mode
      else if (checkMode) {
        ASTParser parser = new ASTParser(lexer);
        Program program = parser.parse();
        TypeInfo typeInfo = new TypeInfo();
        StaticChecker checkVisitor = new StaticChecker(typeInfo);
        program.accept(checkVisitor);
      }
      // run in intermediate-representation mode
      else if (outIRMode) {
        ASTParser parser = new ASTParser(lexer);
        Program program = parser.parse();
        TypeInfo typeInfo = new TypeInfo();
        StaticChecker checkVisitor = new StaticChecker(typeInfo);
        program.accept(checkVisitor);
        VM vm = new VM();
        CodeGenerator genVisitor = new CodeGenerator(typeInfo, vm);
        program.accept(genVisitor);
        System.out.println(vm);
      }
      // run normally
      else {
        ASTParser parser = new ASTParser(lexer);
        Program program = parser.parse();
        TypeInfo typeInfo = new TypeInfo();
        program.accept(new StaticChecker(typeInfo));
        VM vm = new VM();
        CodeGenerator genVisitor = new CodeGenerator(typeInfo, vm);
        program.accept(genVisitor);
        vm.run();
      }
      // delete the merged file
      File newFile = new File("examples/newFile.mypl");
      
      File newFile2 = new File("examples/newFile2.mypl");
      newFile2.delete();
    }
    catch (MyPLException e) {
      System.err.println(e.getMessage());
      System.exit(1);
    }
    catch (FileNotFoundException e) {
      int i = args.length == 1 ? 0 : 1;
      System.err.println("ERROR: Unable to open file '" + args[i] + "'");
      System.exit(1);
    }
  }

  private static void displayUsageInfo() {
    System.out.println("Usage: ./mypl [flag] [script-file]");
    System.out.println("Options:");
    System.out.println("  --lex      Display token information.");
    System.out.println("  --parse    Check for valid syntax.");
    System.out.println("  --print    Pretty print the program.");
    System.out.println("  --check    Statically check program.");
    System.out.println("  --ir       Print intermediate code.");
  }
  
  private static InputStream mergeTwoFiles(String fileToBeCopied, String filename) throws IOException, MyPLException {
    try {
      fileToBeCopied = "examples/" + fileToBeCopied + ".mypl";
      File file = new File(fileToBeCopied);
      File mainFile = new File(filename);
      File newFile = new File("examples/newFile.mypl");
      FileInputStream in = new FileInputStream(file);
      
      Lexer lexer = new Lexer(in);
      ASTParser parser = new ASTParser(lexer);
      Program program = parser.parse();
      
      file = renameFunctionsAndType(program, "myTestModule");
      in = new FileInputStream(file);
      FileInputStream in2 = new FileInputStream(mainFile);
      FileOutputStream out = new FileOutputStream(newFile);
      // copy the contents of the file to the new file
      int c;
      while ((c = in.read()) != -1) {
        out.write(c);
      }
      while ((c = in2.read()) != -1) {
        out.write(c);
      }
      in.close();
      in2.close();
      out.close();
      return new FileInputStream(newFile);
    }
    catch (FileNotFoundException e) {
      System.err.println("ERROR: Unable to open file '" + fileToBeCopied + "'");
      System.exit(1);
    }
    return null;
  }

  private static File renameFunctionsAndType(Program checker, String moduleName) {
    // open the file
    try {
      BufferedReader br = new BufferedReader(new FileReader("examples/myTestModule.mypl"));
      BufferedWriter bw = new BufferedWriter(new FileWriter("examples/newFile2.mypl"));
      String ln;
      
      while((ln = br.readLine()) != null)
        {
          if(ln.contains("type")) {
            String[] split = ln.split(" ");
            for(int i = 0; i < split.length; i++) {
              for(int j = 0; j < checker.tdecls.size(); j++) {
                if(split[i].equals(checker.tdecls.get(j).typeName.lexeme() )) {
                  split[i] = moduleName + "@" + split[i] ;
                }
              }
            }
            String newLine = "";
            for(int i = 0; i < split.length; i++) {
              newLine = newLine + split[i] + " ";
            }
            bw.write(newLine);
            bw.newLine();
          }
          else if(ln.contains("fun")) {
            String[] split = ln.split(" ");
            String remainder = "";
          
            for(int i = 0; i < split.length; i++) {
              if(split[i].contains("(")) {
                String str = split[i];
                split[i] = split[i].substring( 0, split[i].indexOf("("));
                remainder = str.substring(str.indexOf("("), str.length());
              }
              for(int j = 0; j < checker.fdecls.size(); j++) {
                if(split[i].equals(checker.fdecls.get(j).funName.lexeme() )) {
                  split[i] = moduleName + "@" + split[i];
                  split[i] = split[i] + remainder;
                }
              }
            }
            String newLine = "";
            for(int i = 0; i < split.length; i++) {
              newLine = newLine + split[i] + " ";
            }
            bw.write(newLine);
            bw.newLine();
          }
          else {
            bw.write(ln);
            bw.newLine();
          }
        }
        br.close();
        bw.close();
        return new File("examples/newFile2.mypl");
    }
    catch (IOException e)
    {
        e.printStackTrace();
    }
    return null;
  }
  
}

# myPL (my Programming Language)
My own programming language with the following capabilities
 - Module System
 - Lexer
 - AST Parser
 - Prettify (Format)
 - Basic Call Expressions

# To Run
Build the project
    bazel build //:mypl
To run a specific example file (/examples)
   <p> bazel-bin/mypl examples/exec-basic-function.mypl <p>
To run test file (basic module test)
    bazel test --test_output=all //:module-test


The generator directory is a basic code generator that will be able to generate java source code or byte
code from the same input (instructions).  The idea behind this was that code could be generated via
statements which could then be written as java source or byte code.

Todo:

    - create two subpackages:
        - java      - Impl of the code writer for .java
        - class     - Impl of the code writer for .class
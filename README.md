# Couscous

Convert Java source code to other languages. C# and Python currently supported.

This project was intended as a fun side project, and has many oddities.
Using it in production is not recommended!

## Hello world: Java to C#

1.  Build the JAR:

    ```
    mvn package -Dmaven.test.skip=true
    ```

2.  Create a Java source file:

    ```java
    package com.example;

    public class Example {
        public static void main() {
            System.out.println("Hello world");
        }
    }
    ```
    
    Assuming the normal Maven layout,
    this file should be at `src/main/java/com/example/Example.java`.

3.  Create a file called `couscous.json`:

    ```
    {
      "backend": "csharp",
      "sourcepath": [
        "src/main/java"
      ],
      "files": [
        "src/main/java/com/example",
      ],
      "output": "dotnet/generated.cs",
      "namespace": "Example"
    }
    ```

4.  In the directory containing `couscous.json`:

    ```
    java -jar path/to/couscous.jar
    ```
    
    This should generate the file `dotnet/generated.cs`, which looks something like:
    
    ```csharp
    namespace Example.com.example {
        internal class Example {
            public static void main() {
                (Example.java.lang.System._out).println("Hello world");
            }
        }
    }
    ```
    
    Couscous provides very little runtime support: in this case, an
    implementation of `System.out.println(String)` will need to be written.

## TODO

* Put for-loops and similar into their own block. Otherwise, for instance,
  two for-loops in the same scope using the same name for their loop
  variable will cause a variable re-definition in C#.

* Multiple constructors

* Remove all direct runtime support

* Type names for generics

* Type erasure

* Boxing and unboxing for `String`s

* Handle boxing/unboxing when overriding method in supertype with more/less
  specific return types/arguments. For instance, if an interface defines a
  method that returns `Object`, then a subtype may override that method
  returning `Integer`. We want `Integer` to be unboxed (in general),
  so that calling the method on the subtype returns an unboxed `Integer`,
  but if the caller is calling the method on the supertype, it will expect
  an `Object`, meaning it will need to be boxed. This probably means having
  two overloads -- one for the supertype, one for the subtype.

  This needs doing regardless of changes to boxing/unboxing primitives since
  we currently use raw strings rather than boxing them into Java strings.

## Python precedence

The table below gives precedence from lowest to highest in Python 3.5.
Some rows have the same precedence for serialisation to improve code
clarity.

Operator | Precedence (serialisation)
---------|-----------
`lambda` | 0
`if – else` | 10
`or` | 20
`and` | 20
`not x`  | 40
`in`, `not in`, `is`, `is not`, `<`, `<=`, `>`, `>=`, `!=`, `==` | 50
`\|` | 60
`^` | 70
`&` | 80
`<<`, `>>` | 90
`+`, `-` | 100
`*`, `@`, `/`, `//`, `%` | 110
`+x`, `-x`, `~x` | 120
`**` | 130
`await x` | 140
`x[index]`, `x[index:index]`, `x(arguments...)`, `x.attribute` | 150
`(expressions...)`, `[expressions...]`, `{key: value...}`, `{expressions...}` | 160

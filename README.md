## TODO

* Switch statements

* Type names for generics

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
`|` | 60
`^` | 70
`&` | 80
`<<`, `>>` | 90
`+`, `-` | 100
`*`, `@`, `/`, `//`, `%` | 110
`+x`, `-x`, `~x` | 120
`**` | 130
`await x` | 140
`x[index]`, `x[index:index]`, `x(arguments...)`, `x.attribute` | 150
`(expressions...)`, `[expressions...]`, `{key: value...}`, `{expressions...}`` | 160
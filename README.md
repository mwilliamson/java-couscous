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
## TODO

* `String::lower`, `String::equals`, `String::add`

* Boxing and unboxing for `String`s

* Method overloads, probably by renaming (since some languages don't support
  overloading, and relying on other languages overloading semantics is risky).
  Having a consistent way of naming methods would be nice,
  but using the fully-qualified names of arguments/return types would be
  rather long. Referencing those by an integer ID is an alternative,
  albeit less readable (and prone to change). Perhaps a reference by
  just the simple name of the class, with an integer ID if the codebase
  has more than one type of the same name?

* Treat boxed primitives the same as the raw primitives.

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

* Support adding other paths to source e.g. third party libraries

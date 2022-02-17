# Oxidized

A microlibrary of [MTL](https://github.com/typelevel/cats-mtl) typeclasses and instances for [Cats Effect](https://github.com/typelevel/cats-effect).

* `oxidized-kernel`: `ConcurrentStateful` typeclass that relaxes the laws of `Stateful`
* `oxidized-std`: `ConcurrentStateful` for `F` via a `Ref`
* `oxidized`: all of the above, plus `Local` and `Stateful` via an `IOLocal` for `IO`

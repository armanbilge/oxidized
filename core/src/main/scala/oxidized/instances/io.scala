/*
 * Copyright 2021 Arman Bilge
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package oxidized.instances

import cats.mtl.{Local, Stateful}
import cats.effect.IO
import cats.{Applicative, Monad}
import cats.effect.IOLocal

object io extends IOInstances

trait IOInstances {
  implicit def catsMtlEffectStatefulForIO[A](implicit local: IOLocal[A]): Stateful[IO, A] =
    new Stateful[IO, A] {
      override def monad: Monad[IO] = IO.asyncForIO
      override def get: IO[A] = local.get
      override def set(s: A): IO[Unit] = local.set(s)
    }

  implicit def catsMtlEffectLocalForIO[E](implicit ioLocal: IOLocal[E]): Local[IO, E] =
    new Local[IO, E] {
      override def local[A](fa: IO[A])(f: E => E): IO[A] = ioLocal.get.flatMap { initial =>
        ioLocal.set(f(initial)) >> fa.guarantee(ioLocal.set(initial))
      }

      override def applicative: Applicative[IO] = IO.asyncForIO

      override def ask[E2 >: E]: IO[E2] = ioLocal.get
    }
}

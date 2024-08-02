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

package oxidized
package laws

import cats.Monad
import cats.effect.kernel.Unique
import cats.kernel.laws.IsEq
import cats.syntax.all._

trait ConcurrentStatefulLaws[F[_], S] {
  implicit def concurrentStateful: ConcurrentStateful[F, S]
  implicit def monad: Monad[F] = concurrentStateful.monad
  implicit def unique: Unique[F] = concurrentStateful.unique

  def setThenSetOverwritesFirst: F[Boolean] = for {
    u1 <- unique.unique
    u2 <- unique.unique
    _ <- concurrentStateful.set(u1)
    _ <- concurrentStateful.set(u2)
    got <- concurrentStateful.get
  } yield got != u1
}

object ConcurrentStatefulLaws {
  def apply[F[_], S](
      implicit cs: ConcurrentStateful[F, S]): ConcurrentStatefulLaws[F, S] =
    new ConcurrentStatefulLaws[F, S] {
      val concurrentStateful = cs
    }
}

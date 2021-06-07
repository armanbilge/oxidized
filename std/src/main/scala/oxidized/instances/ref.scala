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

import cats.Functor
import cats.effect.kernel.Ref
import oxidized.ConcurrentStateful

object ref extends RefInstances

trait RefInstances {

  implicit def oxidizedInstancesAtomicStatefulForRef[F[_]: Functor, S](
      implicit ref: Ref[F, S]): ConcurrentStateful[F, S] =
    new ConcurrentStateful[F, S] {
      override def functor: Functor[F] = Functor[F]
      override def get: F[S] = ref.get
      override def set(s: S): F[Unit] = ref.set(s)
      override def modify(f: S => S): F[Unit] = ref.modify(f.andThen((_, ())))
    }
}

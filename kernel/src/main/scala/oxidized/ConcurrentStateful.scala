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

import cats.Functor
import cats.mtl.Stateful

trait ConcurrentStateful[F[_], S] extends Serializable {
  def functor: Functor[F]

  def get: F[S]
  def set(s: S): F[Unit]
  def modify(f: S => S): F[Unit]
  def inspect[A](f: S => A): F[A] = functor.map(get)(f)
  // TODO To be useful will probably need more Ref-like methods
}

object ConcurrentStateful {
  def apply[F[_], S](implicit stateful: ConcurrentStateful[F, S]): ConcurrentStateful[F, S] =
    stateful

  def get[F[_], S](implicit ev: ConcurrentStateful[F, S]): F[S] =
    ev.get

  def set[F[_], S](newState: S)(implicit ev: ConcurrentStateful[F, S]): F[Unit] =
    ev.set(newState)

  def setF[F[_]]: setFPartiallyApplied[F] = new setFPartiallyApplied[F]

  final private[oxidized] class setFPartiallyApplied[F[_]](val dummy: Boolean = false)
      extends AnyVal {
    @inline def apply[E, A](e: E)(implicit state: ConcurrentStateful[F, E]): F[Unit] =
      state.set(e)
  }

  def modify[F[_], S](f: S => S)(implicit state: ConcurrentStateful[F, S]): F[Unit] =
    state.modify(f)

  def inspect[F[_], S, A](f: S => A)(implicit state: ConcurrentStateful[F, S]): F[A] =
    state.inspect(f)

  implicit def oxidizedConcurrentStatefulForStateful[F[_], S](
      implicit F: Stateful[F, S]): ConcurrentStateful[F, S] =
    new ConcurrentStateful[F, S] {
      override def functor: Functor[F] = F.monad

      override def get: F[S] = F.get
      override def set(s: S): F[Unit] = F.set(s)
      override def modify(f: S => S): F[Unit] = F.modify(f)
      override def inspect[A](f: S => A): F[A] = F.inspect(f)
    }
}

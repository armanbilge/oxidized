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

import cats.Monad
import cats.data.State
import cats.effect.kernel.Ref
import cats.effect.kernel.Unique
import cats.mtl.MonadPartialOrder
import cats.mtl.Stateful
import cats.syntax.all._

trait ConcurrentStateful[F[_], S] extends Serializable {
  def monad: Monad[F]
  def unique: Unique[F]

  def get: F[S]

  def set(s: S): F[Unit]

  def inspect[A](f: S => A): F[A] = monad.map(get)(f)

  /**
   * Updates the current value using `f` and returns the previous value.
   *
   * In case of retries caused by concurrent modifications, the returned value will be the last
   * one before a successful update.
   */
  def getAndUpdate(f: S => S): F[S] = modify { s => (f(s), s) }

  /**
   * Replaces the current value with `a`, returning the previous value.
   */
  def getAndSet(s: S): F[S] = getAndUpdate(_ => s)

  /**
   * Updates the current value using `f`, and returns the updated value.
   */
  def updateAndGet(f: S => S): F[S] =
    modify { s =>
      val newS = f(s)
      (newS, newS)
    }

  /**
   * Attempts to modify the current value once, returning `false` if another concurrent
   * modification completes between the time the variable is read and the time it is set.
   */
  def tryUpdate(f: S => S): F[Boolean] =
    monad.map(tryModify(s => (f(s), ())))(_.isDefined)

  /**
   * Like `tryUpdate` but allows the update function to return an output value of type `B`. The
   * returned action completes with `None` if the value is not updated successfully and
   * `Some(b)` otherwise.
   */
  def tryModify[A](f: S => (S, A)): F[Option[A]]

  /**
   * Modifies the current value using the supplied update function. If another modification
   * occurs between the time the current value is read and subsequently updated, the
   * modification is retried using the new value. Hence, `f` may be invoked multiple times.
   *
   * Satisfies: `r.update(_ => a) == r.set(a)`
   */
  def update(f: S => S): F[Unit] = modify(s => (f(s), ()))

  /**
   * Like `tryModify` but does not complete until the update has been successfully made.
   */
  def modify[A](f: S => (S, A)): F[A]

  /**
   * Update the value of this ref with a state computation.
   *
   * The current value of this ref is used as the initial state and the computed output state is
   * stored in this ref after computation completes. If a concurrent modification occurs, `None`
   * is returned.
   */
  def tryModifyState[A](state: State[S, A]): F[Option[A]] =
    tryModify(state.run(_).value)

  /**
   * Like [[tryModifyState]] but retries the modification until successful.
   */
  def modifyState[A](state: State[S, A]): F[A] =
    modify(state.run(_).value)

}

object ConcurrentStateful extends LowPriorityConcurrentStatefulInstances {
  @inline def apply[F[_], S](implicit cs: ConcurrentStateful[F, S]): ConcurrentStateful[F, S] =
    cs

  /**
   * Creates a `ConcurrentStateful` backed by a `Ref`
   */
  def ref[F[_]: Monad: Unique, S](s: S)(implicit mk: Ref.Make[F]): F[ConcurrentStateful[F, S]] =
    mk.refOf(s).map(fromRef(_))

  def fromRef[F[_], S](
      ref: Ref[F, S])(implicit F: Monad[F], U: Unique[F]): ConcurrentStateful[F, S] =
    new ConcurrentStateful[F, S] {
      def monad = F
      def unique = U
      def get = ref.get
      def set(s: S) = ref.set(s)
      def tryModify[A](f: S => (S, A)) = ref.tryModify(f)
      def modify[A](f: S => (S, A)) = ref.modify(f)
    }

  @inline def get[F[_], S](implicit cs: ConcurrentStateful[F, S]): F[S] =
    cs.get

  @inline def set[F[_], S](newState: S)(implicit cs: ConcurrentStateful[F, S]): F[Unit] =
    cs.set(newState)

  @inline def inspect[F[_], S, A](f: S => A)(implicit cs: ConcurrentStateful[F, S]): F[A] =
    cs.inspect(f)

  @inline def getAndUpdate[F[_], S](f: S => S)(implicit cs: ConcurrentStateful[F, S]): F[S] =
    cs.getAndUpdate(f)

  @inline def getAndSet[F[_], S](s: S)(implicit cs: ConcurrentStateful[F, S]): F[S] =
    cs.getAndSet(s)

  @inline def updateAndGet[F[_], S](f: S => S)(implicit cs: ConcurrentStateful[F, S]): F[S] =
    cs.updateAndGet(f)

  @inline def tryUpdate[F[_], S](f: S => S)(implicit cs: ConcurrentStateful[F, S]): F[Boolean] =
    cs.tryUpdate(f)

  @inline def tryModify[F[_], S, A](f: S => (S, A))(
      implicit cs: ConcurrentStateful[F, S]): F[Option[A]] =
    cs.tryModify(f)

  @inline def update[F[_], S](f: S => S)(implicit cs: ConcurrentStateful[F, S]): F[Unit] =
    cs.update(f)

  @inline def modify[F[_], S, A](f: S => (S, A))(implicit cs: ConcurrentStateful[F, S]): F[A] =
    cs.modify(f)

  @inline def tryModifyState[F[_], S, A](state: State[S, A])(
      implicit cs: ConcurrentStateful[F, S]): F[Option[A]] =
    cs.tryModifyState(state)

  @inline def modifyState[F[_], S, A](state: State[S, A])(
      implicit cs: ConcurrentStateful[F, S]): F[A] =
    cs.modifyState(state)

  implicit def concurrentStatefulForStateful[F[_], S](
      implicit stateful: Stateful[F, S],
      U: Unique[F]): ConcurrentStateful[F, S] =
    new ConcurrentStateful[F, S] {
      def monad = stateful.monad
      def unique = U

      def get = stateful.get

      def set(s: S) = stateful.set(s)

      def modify[A](f: S => (S, A)) =
        monad.flatMap(get) { s =>
          val (s1, a) = f(s)
          monad.as(set(s1), a)
        }

      def tryModify[A](f: S => (S, A)) =
        monad.map(modify(f))(Some(_))
    }
}

private[oxidized] trait LowPriorityConcurrentStatefulInstances {

  implicit def concurrentStatefulForPartialOrder[F[_], G[_], S](
      implicit liftF: MonadPartialOrder[
        F,
        G
      ], // NB don't make this the *second* parameter; it won't infer
      cs: ConcurrentStateful[F, S]): ConcurrentStateful[G, S] =
    new ConcurrentStateful[G, S] {
      def monad = liftF.monadG
      def unique = new Unique[G] {
        def applicative = monad
        def unique = liftF(cs.unique.unique)
      }
      def get = liftF(cs.get)
      def set(s: S) = liftF(cs.set(s))
      def tryModify[A](f: S => (S, A)) = liftF(cs.tryModify(f))
      def modify[A](f: S => (S, A)) = liftF(cs.modify(f))
    }
}

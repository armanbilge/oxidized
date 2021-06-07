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

import cats.effect.testkit.TestInstances
import cats.mtl.laws.discipline.{HandleTests, StatefulTests}
import cats.effect.IOLocal
import cats.effect.IO
import org.specs2.mutable.Specification
import org.typelevel.discipline.specs2.mutable.Discipline
import oxidized.instances.io._
import cats.effect.kernel.Outcome

class IOSpec extends Specification with Discipline with TestInstances {
  implicit val ticker: Ticker = Ticker()

  // Terrible hack!
  implicit val local: IOLocal[Int] = unsafeRun(IOLocal(0)) match {
    case Outcome.Succeeded(Some(local)) => local
    case _ => throw new RuntimeException
  }
  checkAll("Stateful[IO]", StatefulTests[IO, Int].stateful)
  checkAll("Handle[IO]", HandleTests[IO, Throwable].handle[Int])

}

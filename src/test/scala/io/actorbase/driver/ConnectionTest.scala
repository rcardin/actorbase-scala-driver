package io.actorbase.driver

import akka.actor.{ActorSelection, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import io.actorbase.actor.api.Api.Request.CreateCollection
import io.actorbase.actor.api.Api.Response.{CreateCollectionAck, CreateCollectionNAck}
import io.actorbase.driver.exceptions.CreateCollectionException
import org.scalatest._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Failure

/**
  * The MIT License (MIT)
  *
  * Copyright (c) 2015 - 2017 Riccardo Cardin
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in all
  * copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  *
  * Tests relative to connection to an Actorbase
  */
class ConnectionTest extends TestKit(ActorSystem("testSystemDriverActorbase"))
  with ImplicitSender
  with WordSpecLike
  with MustMatchers
  with BeforeAndAfter
  with BeforeAndAfterAll {

  val collectionName = "coll"

  var connection: Connection = _

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A connection" must {
    "create a new collection using a name" in {
      val probe = TestProbe.apply()(system)
      connection = new Connection(ActorSelection(probe.ref, "/"))
      val result: Future[String] = connection.createCollection(collectionName)
      probe.expectMsg(CreateCollection(collectionName))
      probe.reply(CreateCollectionAck(collectionName))
      result.onComplete { maybeAName =>
        assert(maybeAName.isSuccess)
        assert(maybeAName.get == collectionName)
      }
    }

    "fail gracefully is collection creation does not succeed" in {
      val error = "Some error has occured"
      val probe = TestProbe.apply()(system)
      connection = new Connection(ActorSelection(probe.ref, "/"))
      val result: Future[String] = connection.createCollection(collectionName)
      probe.expectMsg(CreateCollection(collectionName))
      probe.reply(CreateCollectionNAck(collectionName, error))
      result.onComplete {
        case Failure(ex) => assert(ex == CreateCollectionException(collectionName, error))
        case _ => fail()
      }
    }
  }

}


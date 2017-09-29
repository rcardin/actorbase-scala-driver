package io.actorbase.driver

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import io.actorbase.actor.api.Api.Request.CreateCollection
import io.actorbase.actor.api.Api.Response.{CreateCollectionAck, CreateCollectionNAck, CreationResponse}
import io.actorbase.driver.exceptions.CreateCollectionException

import scala.concurrent.Future
import scala.concurrent.duration.DurationLong

/**
  * The MIT License (MIT)
  *
  * Copyright (c) 2017 Riccardo Cardin
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
  * An active connection to an Actorbase instance.
  *
  * @author Riccardo Cardin
  * @version 1.0
  * @since 1.0
  */
class Connection(uri: String) {
  implicit val context: ActorSystem = ActorSystem("actorbase-driver")
  private val mainActor = context.actorSelection(uri)

  /**
    * Gets the reference to an existing collection
    *
    * @param name Name of the collection
    * @return A collection
    */
  def collection(name: String): Collection = Collection(name, mainActor)

  def newCollection(name: String): Future[Collection] = {
    implicit val timeout: Timeout = Timeout(5 seconds)
    (mainActor ? CreateCollection(name))
      .mapTo[CreationResponse]
      .collect {
        case CreateCollectionAck(coll) => Collection(coll, mainActor)
        case CreateCollectionNAck(coll, error) => throw CreateCollectionException(coll, error)
      }
  }
}

object Connection {
  /**
    * Creates a connection to the actorbase instance listening at `uri`.
    *
    * @param uri Location of the actorbase instance
    * @return The active connection
    */
  def apply(uri: String): Connection = new Connection(uri)
}


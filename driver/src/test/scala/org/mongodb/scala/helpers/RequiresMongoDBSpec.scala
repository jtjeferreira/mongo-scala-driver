/**
 * Copyright 2010-2014 MongoDB, Inc. <http://www.mongodb.org>
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 * For questions and comments about this product, please see the project page at:
 *
 * https://github.com/mongodb/mongo-scala-driver
 *
 */
package org.mongodb.scala.helpers

import java.util.logging.Level

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Properties

import org.scalatest._
import org.scalatest.concurrent.ScalaFutures

import org.mongodb.Document
import org.mongodb.diagnostics.Loggers

import org.mongodb.scala.{MongoClientURI, MongoClient, MongoCollection, MongoDatabase}
import org.scalatest.time.{Millis, Seconds, Span}


trait RequiresMongoDBSpec extends FlatSpec with Matchers with ScalaFutures with BeforeAndAfterAll {

  implicit val defaultPatience = PatienceConfig(timeout =  Span(30, Seconds), interval = Span(5, Millis))
  implicit val ec = scala.concurrent.ExecutionContext.Implicits.global

  private val DEFAULT_URI: String = "mongodb://localhost:27017"
  private val MONGODB_URI_SYSTEM_PROPERTY_NAME: String = "org.mongodb.test.uri"
  private val WAIT_DURATION = Duration(500, "millis")
  private var _currentTestName: Option[String] = None

  protected override def runTest(testName: String, args: Args): Status = {
    _currentTestName = Some(testName)
    super.runTest(testName, args)
  }

  /**
   * The database name to use for this test
   */
  def databaseName: String = "mongo-scala-" + suiteName
  /**
   * The collection name to use for this test
   */
  def collectionName: String = _currentTestName.getOrElse(suiteName).filter(_.isLetterOrDigit)

  val mongoClientURI = {
    val mongoURIString = Properties.propOrElse(MONGODB_URI_SYSTEM_PROPERTY_NAME, DEFAULT_URI)
    MongoClientURI(mongoURIString)
  }

  def mongoClient = MongoClient(mongoClientURI)

  lazy val mongoDbOnline: Boolean = {
    try {
      Await.result(mongoClient.admin.databaseNames, WAIT_DURATION)
      true
    } catch {
      case t: Throwable => false
    }
  }

  def checkMongoDB() {
    if (!mongoDbOnline) cancel("No Available Database")
  }

  def withDatabase(testCode: MongoDatabase => Any) {
    checkMongoDB()
    val mongoDatabase = mongoClient(databaseName)
    try testCode(mongoDatabase) // "loan" the fixture to the test
    finally Await.result(mongoDatabase.admin.drop(), WAIT_DURATION) // clean up the fixture
  }

  def withCollection(testCode: MongoCollection[Document] => Any) {
    checkMongoDB()
    val mongoDatabase = mongoClient(databaseName)
    val mongoCollection = mongoDatabase(collectionName)

    try testCode(mongoCollection) // "loan" the fixture to the test
    finally Await.result(mongoDatabase.admin.drop(), WAIT_DURATION) // clean up the fixture
  }

  override def beforeAll() {
    // Turn off org.mongodb's noisy connection INFO logging
    Loggers.getLogger("cluster").setLevel(Level.WARNING)
    Loggers.getLogger("connection").setLevel(Level.WARNING)
  }

  override def afterAll() {
    Await.result(mongoClient(databaseName).admin.drop(), WAIT_DURATION)
  }

}

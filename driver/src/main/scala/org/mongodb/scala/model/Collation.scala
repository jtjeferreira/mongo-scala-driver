/*
 * Copyright 2016 MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mongodb.scala.model

import com.mongodb.client.model.{Collation => JCollation}

/**
 * The options regarding collation support in MongoDB 3.4+
 *
 * @note Requires MongoDB 3.4 or greater
 * @since 1.2
 */
object Collation {

  /**
   * Creates a builder for creating the Collation.
   *
   * @return a new Builder for creating the Collation.
   */
  def builder(): Builder = JCollation.builder()

  /**
   * Collation builder type
   */
  type Builder = JCollation.Builder

}

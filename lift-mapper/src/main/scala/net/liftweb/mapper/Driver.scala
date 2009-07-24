package net.liftweb.mapper

/*
 * Copyright 2006-2008 WorldWide Conferencing, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */

import _root_.net.liftweb.util._

abstract class DriverType(val name : String) {
  def binaryColumnType: String
  def clobColumnType: String
  def booleanColumnType: String
  def dateTimeColumnType: String
  def integerColumnType: String
  def integerIndexColumnType: String
  def enumColumnType: String
  def longForeignKeyColumnType: String
  def longIndexColumnType: String
  def enumListColumnType: String
  def longColumnType: String
  def doubleColumnType: String

  def supportsForeignKeys_? : Boolean = false
  def createTablePostpend: String = ""

  /**
   * Whether this database supports LIMIT clause in SELECTs.
   */
  def brokenLimit_? : Boolean = false

  /**
   * Whether the primary key has been defined by the index column.
   */
  def pkDefinedByIndexColumn_? : Boolean = false

  /**
   * Maximum value of the LIMIT clause in SELECT.
   */
  def maxSelectLimit : String = _root_.java.lang.Long.MAX_VALUE.toString

  /**
   * Whether this database supports returning autogenerated keys (JDBC3)
   */
  def brokenAutogeneratedKeys_? : Boolean = false

  /**
    * Is the foreign key mechanism so totally broken that it doesn't allow a "RETURNING" in the query
    */
  def wickedBrokenAutogeneratedKeys_? : Boolean = false

  /**
   * Name of the default db schema. If not set, then the schema is assumed to
   * equal the db user name.
   */
  def defaultSchemaName : Box[String] = Empty

  /**
   * Allow the driver to do specific remapping of column types for cases
   * where not all types are supported.
   */
  def columTypeMap : PartialFunction[Int,Int] = {
    case x => x
  }
}

object DerbyDriver extends DriverType("Apache Derby") {
  def binaryColumnType = "LONG VARCHAR FOR BIT DATA"
  def booleanColumnType = "SMALLINT"
  def clobColumnType = "LONG VARCHAR"
  def dateTimeColumnType = "TIMESTAMP"
  def integerColumnType = "INTEGER"
  def integerIndexColumnType = "INTEGER NOT NULL GENERATED ALWAYS AS IDENITY"
  def enumColumnType = "BIGINT"
  def longForeignKeyColumnType = "BIGINT"
  def longIndexColumnType = "BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY"
  def enumListColumnType = "BIGINT"
  def longColumnType = "BIGINT"
  def doubleColumnType = "DOUBLE"

  override def brokenLimit_? : Boolean = true
}

object MySqlDriver extends DriverType("MySQL") {
  def binaryColumnType = "MEDIUMBLOB"
  def clobColumnType = "LONGTEXT"
  def booleanColumnType = "BOOLEAN"
  def dateTimeColumnType = "DATETIME"
  def integerColumnType = "INTEGER"
  def integerIndexColumnType = "INTEGER NOT NULL AUTO_INCREMENT UNIQUE"
  def enumColumnType = "BIGINT"
  def longForeignKeyColumnType = "BIGINT UNSIGNED"
  def longIndexColumnType = "BIGINT UNSIGNED NOT NULL AUTO_INCREMENT UNIQUE KEY"
  def enumListColumnType = "BIGINT"
  def longColumnType = "BIGINT"
  def doubleColumnType = "DOUBLE"

  override def createTablePostpend: String = " ENGINE = InnoDB "
}

object H2Driver extends DriverType("H2") {
  def binaryColumnType = "BINARY"
  def clobColumnType = "LONGVARCHAR"
  def booleanColumnType = "BOOLEAN"
  def dateTimeColumnType = "TIMESTAMP"
  def integerColumnType = "INTEGER"
  def integerIndexColumnType = "INTEGER NOT NULL AUTO_INCREMENT"
  def enumColumnType = "BIGINT"
  def longForeignKeyColumnType = "BIGINT"
  def longIndexColumnType = "BIGINT NOT NULL AUTO_INCREMENT"
  def enumListColumnType = "BIGINT"
  def longColumnType = "BIGINT"
  def doubleColumnType = "DOUBLE"

  /**
   * Whether the primary key has been defined by the index column.
   * H2 creates primary key for a table, when AUTO_INCREMENT type
   * is used.
   */
  override def pkDefinedByIndexColumn_? : Boolean = true

  override def maxSelectLimit = "0";
}

object PostgreSqlDriver extends DriverType("PostgreSQL") {
  def binaryColumnType = "BYTEA"
  def clobColumnType = "TEXT"
  def booleanColumnType = "BOOLEAN"
  def dateTimeColumnType = "TIMESTAMP"
  def integerColumnType = "INTEGER"
  def integerIndexColumnType = "SERIAL"
  def enumColumnType = "BIGINT"
  def longForeignKeyColumnType = "BIGINT"
  def longIndexColumnType = "BIGSERIAL"
  def enumListColumnType = "BIGINT"
  def longColumnType = "BIGINT"
  def doubleColumnType = "DOUBLE"

  /**
   * Whether this database supports returning autogenerated keys (JDBC3)
   */
  override def brokenAutogeneratedKeys_? : Boolean = true

  override def wickedBrokenAutogeneratedKeys_? : Boolean = true

  override def maxSelectLimit = "ALL"

  /**
   * "$user" schema is searched before "public", but it does not exist by default,
   * so "public" is our default choice.
   */
  override def defaultSchemaName : Box[String] = Full("public")
}

object SqlServerDriver extends DriverType("Microsoft SQL Server") {
  def binaryColumnType = "VARBINARY(MAX)"
  def booleanColumnType = "BIT"
  def clobColumnType = "VARCHAR(MAX)"
  def dateTimeColumnType = "TIMESTAMP"
  def integerColumnType = "INT"
  def integerIndexColumnType = "INT IDENTITY NOT NULL"
  def enumColumnType = "BIGINT"
  def longForeignKeyColumnType = "BIGINT"
  def longIndexColumnType = "BIGINT IDENTITY NOT NULL"
  def enumListColumnType = "BIGINT"
  def longColumnType = "BIGINT"
  def doubleColumnType = "FLOAT"

  override def defaultSchemaName : Box[String] = Full("dbo")
}

object OracleDriver extends DriverType("Oracle") {
  def binaryColumnType = "LONG RAW"
  def booleanColumnType = "NUMBER"
  def clobColumnType = "CLOB"
  def dateTimeColumnType = "TIMESTAMP"
  def integerColumnType = "NUMBER"
  def integerIndexColumnType = "ROWID"
  def enumColumnType = "NUMBER"
  def longForeignKeyColumnType = "NUMBER"
  def longIndexColumnType = "ROWID"
  def enumListColumnType = "NUMBER"
  def longColumnType = "NUMBER"
  def doubleColumnType = "NUMBER"

  import _root_.java.sql.Types
  override def columTypeMap = {
    case Types.BOOLEAN => Types.INTEGER
  }
}

object MaxDbDriver extends DriverType("MaxDB") {
  def binaryColumnType = "BLOB"
  def booleanColumnType = "BOOLEAN"
  def clobColumnType = "CLOB"
  def dateTimeColumnType = "TIMESTAMP"
  def integerColumnType = "INTEGER"
  def integerIndexColumnType = "FIXED(10) DEFAULT SERIAL"
  def enumColumnType = "FIXED(38)"
  def longForeignKeyColumnType = "FIXED(38)"
  def longIndexColumnType = "FIXED(38) DEFAULT SERIAL"
  def enumListColumnType = "FIXED(38)"
  def longColumnType = "FIXED(38)"
  def doubleColumnType = "FLOAT(38)"
}

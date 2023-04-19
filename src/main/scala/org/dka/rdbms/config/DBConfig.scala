package org.dka.rdbms.config

//import com.typesafe.config.Config
import pureconfig._
import pureconfig.generic.ProductHint

/**
 *  A postgres instance can have multiple databases running within it.
 *  Each database is logically separate from others in terms of access and content.
 *  This means that you can't connect to more than one db at a time.  User accounts (names, password, etc.)
 *  are not shared across instances and so must be managed on a per database instance level.
 *
 *
 *  Postgres has the concept of ''schemas''.  They are conceptually ''name spaces'' and are
 *  used to logically separate different parts of the database.  A schema holds tables, views, indexes, etc.
 *  All users accounts belong to the same database instance and so they can be centrally managed.  Grant's are given
 *  to accounts to manage access to the data based on schemas rather than on database instances.
 *
 *  A typical use case for schemas is ''multi-tenant'' applications where all applications share the
 *  same database, but they each operate on different tables etc.  Often the tables, indices etc. are all the same
 *  but the data the schemas are to be kept strictly separate.
 *
 *  Another possible use case for schemas is to support data migration.
 *  Let's suppose you have a schema (set of database objects) and you have applications that run on that data
 *  Now it is time for a new version of the application and/or database.  But you will have old applications
 *  running against the old structure, so you can't change that.
 *
 *  Rather than setting up an entirely new database instance with its costs and headaches, you can create a
 *  new schema in an existing database.  The tables etc. in the new schema can be setup however you need and you
 *  do a data migration (from within a database instance) to transform the old data (while leaving it in place)
 *  into the new structure.
 *
 *
 *  So there are multiple ways of configuring for testing:
 *  1. use different databases. one for each ''environment''
 *    i. requires setting up different database instances for each developer, demo, qa, integration, prod, etc.
 *    i. however, each was created from the same ddl and its structure is therefore the exact same
 *    i. it is an '''explicit''' connection and so testing in one database can not conflict with testing in another
 *  1. use one database, but separate schemas (one for each developer, demo, qa, integration, prod etc.)
 *    i. one schema for local, one for qa, one for ??? etc.
 *    i. each schema is created from the same ddl so they all match
 *    i. in the configuration of the connection, '''explicitly''' set the schema.
 *    i. tests run against one schema should act the same as a test run against a different
 *    schema and will not affect data used by other environments.
 *    i. the downside of this is complicated user management, i.e. the same user/password credentials are used for all schemas
*     i. one user has access to multiple schemas so tests run intended for local, ''could'' run against
 *      other schemas, if the wrong schema name is used at connection time.  (I thought I ran this test 'locally' but it actually ran on qa)
 */


final case class DBConfig(
  connectionPool: String,
  dataSourceClass: String,
  properties: Properties,
  numThreads: Int) {

  val url: String = s"jdbc:postgresql://${properties.host}:${properties.port}/${properties.database}?user=${properties.user}&password=${properties.password}&currentSchema=${properties.schema}"
}

object DBConfig {
  implicit def hint[DBConfig]: ProductHint[DBConfig] =
    ProductHint[DBConfig](ConfigFieldMapping(CamelCase, CamelCase))

}
final case class Properties(
                             host: String,
                             port: Int,
                             database: String, // change to database when no longer read directly from slick
                             schema: String,
                             user: String,
                             password: String) {
}


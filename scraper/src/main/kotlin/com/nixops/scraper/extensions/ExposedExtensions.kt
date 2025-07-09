package com.nixops.scraper.extensions

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.UpsertStatement
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.upsert

inline fun <T : Table, E : Entity<*>> T.genericUpsert(
    entityClass: EntityClass<*, E>,
    crossinline body: T.(UpsertStatement<*>) -> Unit
): E = transaction {
  val result = this@genericUpsert.upsert { statement -> this.body(statement) }
  entityClass.wrapRow(result.resultedValues!!.first())
}

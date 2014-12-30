package com.womply.transactions.db

import com.websudos.phantom.Implicits._
import org.joda.time.DateTime

case class Transaction(merchantID: String, time: DateTime, amount: BigDecimal)

sealed class Transactions extends CassandraTable[Transactions, Transaction] {
  object merchant_id extends StringColumn(this) with PartitionKey[String]
  object time extends DateTimeColumn(this) with PrimaryKey[DateTime]
  object amount extends BigDecimalColumn(this)

  override def fromRow(r: Row) =
    Transaction(merchant_id(r), time(r), amount(r))
}

object Transactions extends Transactions with CassandraConnector {

  def insertTransaction(t: Transaction) =
    insert
      .value(_.merchant_id, t.merchantID)
      .value(_.time, t.time)
      .value(_.amount, t.amount)
}


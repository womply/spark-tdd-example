package com.womply.transactions.db

import com.websudos.phantom.Implicits._
import org.joda.time.DateTime

case class Ranking(
  merchantID: String,
  date: DateTime,
  merchantIDs: List[String],
  amountSums: List[BigDecimal])

sealed class Rankings extends CassandraTable[Rankings, Ranking] {
  object merchant_id extends StringColumn(this) with PartitionKey[String]
  object date extends DateTimeColumn(this) with PrimaryKey[DateTime]
  object merchant_ids extends ListColumn[Rankings, Ranking, String](this)
  object amount_sums extends ListColumn[Rankings, Ranking, BigDecimal](this)

  override def fromRow(r: Row) =
    Ranking(merchant_id(r), date(r), merchant_ids(r), amount_sums(r))
}

object Rankings extends Rankings with CassandraConnector


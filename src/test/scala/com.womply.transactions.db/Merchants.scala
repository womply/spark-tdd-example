package com.womply.transactions.db

import com.websudos.phantom.Implicits._

case class Merchant(
  id: String,
  name: Option[String],
  latitude: Option[Double],
  longitude: Option[Double])

sealed class Merchants extends CassandraTable[Merchants, Merchant] {
  object id extends StringColumn(this) with PartitionKey[String]
  object name extends OptionalStringColumn(this)
  object latitude extends OptionalDoubleColumn(this)
  object longitude extends OptionalDoubleColumn(this)

  override def fromRow(r: Row) =
    Merchant(id(r), name(r), latitude(r), longitude(r))
}

object Merchants extends Merchants with CassandraConnector {

  def insertMerchant(merchant: Merchant) =
    insert
      .value(_.id, merchant.id)
      .value(_.name, merchant.name)
      .value(_.latitude, merchant.latitude)
      .value(_.longitude, merchant.longitude)
}


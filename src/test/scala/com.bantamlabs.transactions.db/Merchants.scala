package com.bantamlabs.transactions.db

import com.websudos.phantom.Implicits._

case class Merchant(
  id: String,
  name: String,
  latitude: Double,
  longitude: Double)

sealed class Merchants extends CassandraTable[Merchants, Merchant] {
  object id extends StringColumn(this) with PartitionKey[String]
  object name extends StringColumn(this)
  object latitude extends DoubleColumn(this)
  object longitude extends DoubleColumn(this)

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


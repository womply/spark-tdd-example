package com.womply.transactions.db

import com.websudos.phantom.zookeeper.SimpleCassandraConnector

trait CassandraConnector extends SimpleCassandraConnector {
  override val keySpace = "spark_tdd_example"
}


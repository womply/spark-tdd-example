package com.bantamlabs.transactions.db

import com.websudos.phantom.zookeeper.SimpleCassandraConnector

trait CassandraConnector extends SimpleCassandraConnector {
  val keySpace = "transaction_example"
}


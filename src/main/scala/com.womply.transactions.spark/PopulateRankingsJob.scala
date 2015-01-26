package com.womply.transactions.spark

import com.datastax.spark.connector._
import com.datastax.spark.connector.rdd._
import com.womply.transactions.util.Distance._
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.joda.time.DateTime

object PopulateRankingsJob {

  val NearestMerchantsToTake = 2

  def run(sc: SparkContext) {
    val merchantsRDD = sc.cassandraTable[(String, Option[Double], Option[Double])]("phantom", "merchants")
      .select("id", "latitude", "longitude")
      .collect({
        case m if m._2.isDefined && m._3.isDefined => (m._1 -> (m._2.get, m._3.get))
      })
    val merchantsBroadcast = sc.broadcast(merchantsRDD.collect)

    val nearestMerchantsRDD = merchantsRDD
      .flatMap({ targetMerchant =>
        val nearestMerchants = merchantsBroadcast.value
          .map({ merchant =>
            val distance = distanceInMiles(targetMerchant._2._1, targetMerchant._2._2,
              merchant._2._1, merchant._2._2)
            (distance, merchant._1)
          })
          .sortBy(_._1)
          .take(NearestMerchantsToTake)
          .map(_._2)

        (nearestMerchants :+ targetMerchant._1).map(m => (m, targetMerchant._1))
      })

    val rankingsRDD = sc.cassandraTable[(String, DateTime, BigDecimal)]("phantom", "transactions")
      .map({t =>
        val date = t._2.withTime(0, 0, 0, 0)
        ((t._1, date) -> t._3)
      })
      .reduceByKey(_ + _)
      .map(t => (t._1._1, (t._1._2, t._2)))
      .groupByKey()
      .rightOuterJoin(nearestMerchantsRDD)
      .map(t => (t._2._2, (t._1, t._2._1)))
      .groupByKey()
      .map({ t =>
        t._2.map({ mr =>
          val b = mr._2 match {
            case Some(drs) => drs.map(dr => (mr._1 -> Some(dr._2, mr._1)))
            case None => (mr._1 ->
              None.asInstanceOf[Option[Iterable[(String, Some[(BigDecimal, String)])]]])
          }
        })
      })

    val saveColumns = SomeColumns("merchant_id", "date", "merchant_ids", "amount_sums")
    rankingsRDD.saveToCassandra("phantom", "rankings", saveColumns)
  }
}


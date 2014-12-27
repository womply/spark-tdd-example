package com.bantamlabs.transactions.spark

import com.bantamlabs.transactions.db._
import com.bantamlabs.transactions.db.{Transactions, Transaction}
import com.datastax.driver.core.{Cluster, Session}
import com.websudos.phantom.testing._
import org.apache.spark.SparkContext
import org.joda.time.{DateTime, DateTimeZone}
import org.scalatest.BeforeAndAfter
import org.scalatest.fixture
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.Random
import com.bantamlabs.transactions.util.Distance._

class PopulateRankingsJobSpec extends fixture.FlatSpec
    with TestZookeeperConnector with CassandraTest with BeforeAndAfter {

  type FixtureParam = SparkContext

  def withFixture(test: OneArgTest) = {
    val sparkContext = new SparkContext("local", "scalatest")

    try test(sparkContext)
    finally sparkContext.stop
  }

  behavior of "PopulateRankingsJob"

  before {
    val tables = List(Merchants, Transactions, Rankings)

    val tableCreateFutures = tables.map(t => t.create.future)
    Await.ready(Future.sequence(tableCreateFutures), 3.seconds)

    val tableTruncateFutures = tables.map(t => t.truncate.future)
    Await.ready(Future.sequence(tableTruncateFutures), 3.seconds)
  }

  it should "rank each merchant's closest two other merchants by total revenue grouped by day" in { sparkContext =>
    val merchants = List(
      Merchant("303030303", "Womply Cafe - SF", 37.778574, -122.391721),
      Merchant("505050505", "Womply Cafe - Madison", 43.075746, -89.383367),
      Merchant("606060606", "Womply Cafe - DC", 38.890000, -77.087565),
      Merchant("909090909", "Womply Cafe - London", 51.554906, -0.258154)
    )
    val insertMerchantFutures = merchants.map(m =>  Merchants.insertMerchant(m).future)

    val targetDate = new DateTime(DateTimeZone.UTC).withTime(0, 0, 0, 0)
    val random = new Random()
    val transactionsByMerchantID = merchants.map { merchant =>
      val transactionsToCreate = random.nextInt(100) + 1
      val secondsToIncrement = 86400 / (transactionsToCreate + 1)
      val dates = List(targetDate.minusDays(1), targetDate, targetDate.plusDays(1))
      val transactionsByDate = dates.map { date =>
        val transactions = (0 to transactionsToCreate).map { index =>
          val time = date.plusSeconds(index * secondsToIncrement)
          val amount = BigDecimal(random.nextInt(20000), 2)

          Transaction(merchant.id, time, amount)
        }

        (date -> transactions)
      }

      (merchant -> transactionsByDate)
    }

    val allTransactions = transactionsByMerchantID.flatMap(_._2).flatMap(_._2)
    val insertTransactionFutures = allTransactions.map(t => Transactions.insertTransaction(t).future)
    Await.ready(Future.sequence(insertMerchantFutures ++ insertTransactionFutures), 3.seconds)

    val amountSumsByMerchant = transactionsByMerchantID
      .map({ case (merchant, transactionsByDate) =>
        val sum = transactionsByDate.filter(_._1 == targetDate).flatMap(_._2.map(_.amount)).sum
        (merchant -> sum)
      })
      .toMap

    val rankedMerchantsByTarget = merchants
      .map({ targetMerchant =>
        val closestMerchants = merchants
          .collect({
            case merchant if merchant != targetMerchant => {
              val distance = distanceInMiles(targetMerchant.latitude, targetMerchant.longitude,
                merchant.latitude, merchant.longitude)

              (merchant, distance)
            }
          })
          .sortWith((x, y) => x._2 < y._2)
          .take(2)

        val rankedClosestMerchants = (closestMerchants.map(_._1) :+ targetMerchant)
          .map(m => (m, amountSumsByMerchant.get(m).get))
          .sortWith((x, y) => x._2 > y._2)

        (targetMerchant.id -> rankedClosestMerchants)
      })
      .toMap

    // TODO Run Spark job

    val rankingsSelectFuture = Rankings.select.fetch
    whenReady(rankingsSelectFuture) { results =>
      val rankings = results.toList
      rankings.size shouldEqual merchants.size

      rankings.foreach { ranking =>
        val rankedMerchantsOption = rankedMerchantsByTarget.get(ranking.merchantID)
        rankedMerchantsOption shouldBe 'defined
        val rankedMerchants = rankedMerchantsOption.get

        val expectedMerchantIDs = rankedMerchants.map(_._1)
        ranking.merchantIDs should equal(expectedMerchantIDs)

        val expectedAmountSums = rankedMerchants.map(_._2)
        ranking.amountSums should equal(expectedAmountSums)
      }
    }
  }
}


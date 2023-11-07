package tme.api

import cats.effect.Temporal
import cats.syntax.all._
import org.typelevel.log4cats.LoggerFactory
import tme.data.DataStore

import java.time.{LocalDateTime, ZoneId}
import scala.concurrent.duration.Duration

class Deleter[F[_]: LoggerFactory: Temporal](dataStore: DataStore[F], interval: Duration) {

  private val logger = LoggerFactory[F].getLogger

  def start(): F[Unit] = (for {
    _ <- logger.info(s"Waiting for $interval")
    _ <- Temporal[F].sleep(interval)
    _ <- logger.info("Deleting old emails")
    i <- dataStore.deleteOlderThan(LocalDateTime.now(ZoneId.of("Z")).minusSeconds(interval.toSeconds))
    _ <- logger.info(s"Deleted [$i] temporary email addresses")
  } yield ()).foreverM

}

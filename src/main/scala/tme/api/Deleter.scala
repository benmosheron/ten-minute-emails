package tme.api

import cats.effect.Temporal
import cats.effect.std.Console
import cats.syntax.all._
import tme.data.DataStore

import java.time.{LocalDateTime, ZoneId}
import scala.concurrent.duration.Duration

class Deleter[F[_]: Console: Temporal](dataStore: DataStore[F], interval: Duration) {

  def start(): F[Unit] = (for {
    _ <- Console[F].println(s"Deleter: Waiting for $interval")
    _ <- Temporal[F].sleep(interval)
    _ <- Console[F].println("Deleter: Deleting old emails")
    i <- dataStore.deleteOlderThan(LocalDateTime.now(ZoneId.of("Z")).minusSeconds(interval.toSeconds))
    _ <- Console[F].println(s"Deleter: Deleted [$i] temporary email addresses")
  } yield ()).foreverM

}

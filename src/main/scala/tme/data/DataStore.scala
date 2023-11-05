package tme.data

import tme.data.Model.{Email, TemporaryEmail}

import java.time.LocalDateTime

trait DataStore[F[_]] {

  def createTemporaryEmail(temporaryEmail: TemporaryEmail, createdDateTime: LocalDateTime): F[Unit]

  def addEmail(key: TemporaryEmail, email: Email): F[Unit]

  def getEmails(key: TemporaryEmail): F[Vector[Email]]

  def deleteOlderThan(dateTime: LocalDateTime): F[Int]

}

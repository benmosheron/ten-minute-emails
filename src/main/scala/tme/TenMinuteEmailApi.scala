package tme

import tme.data.Model._

trait TenMinuteEmailApi[F[_]] {

  def createTemporaryEmail(): F[TemporaryEmail]

  def addEmail(key: TemporaryEmail, email: Email): F[Unit]

  def getEmails(key: TemporaryEmail): F[Vector[Email]]

}

package tme.data

import java.time.LocalDateTime

object Model {

  final case class TemporaryEmail (prefix: String) {
    def email = s"$prefix@tme.com"
  }

  final case class Inbox(createdDateTime: LocalDateTime, emails: Vector[Email]){
    def +(email: Email): Inbox = Inbox(createdDateTime, emails :+ email)
  }
  object Inbox {
    def empty(createdDateTime: LocalDateTime): Inbox = Inbox(createdDateTime, Vector())
  }

  final case class Email(data: String)

}

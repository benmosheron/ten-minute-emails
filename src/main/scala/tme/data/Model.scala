package tme.data

import java.time.LocalDateTime

object Model {

  case class TemporaryEmail (prefix: String) {
    def email = s"$prefix@tme.com"
  }

  case class Inbox(createdDateTime: LocalDateTime, emails: Vector[Email]){
    def +(email: Email): Inbox = Inbox(createdDateTime, emails :+ email)
  }
  object Inbox {
    def empty(createdDateTime: LocalDateTime): Inbox = Inbox(createdDateTime, Vector())
  }

  case class Email(data: String)

}

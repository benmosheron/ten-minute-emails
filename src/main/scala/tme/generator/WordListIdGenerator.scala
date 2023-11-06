package tme.generator

import cats.MonadThrow
import cats.effect.std.Random
import cats.syntax.all._
import tme.data.HumanReadableWords
import tme.data.Model.TemporaryEmail

class WordListIdGenerator[F[_]: Random: MonadThrow] extends IdGenerator[F] {

  private def randomWord(): F[String] = Random[F].elementOf(HumanReadableWords.words)

  override def generate(): F[TemporaryEmail] = for {
    first <- randomWord()
    second <- randomWord()
    third <- randomWord()
    fourth <- randomWord()
  } yield TemporaryEmail(s"$first-$second-$third-$fourth")
}

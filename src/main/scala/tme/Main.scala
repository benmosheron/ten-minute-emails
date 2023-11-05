package tme

import cats.effect.{ExitCode, IO, IOApp}

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = IO.println("hi").as(ExitCode.Success)

}
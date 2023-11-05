package tme

import tme.data.Model.TemporaryEmail

trait IdGenerator[F[_]] {
  def generate(): F[TemporaryEmail]
}

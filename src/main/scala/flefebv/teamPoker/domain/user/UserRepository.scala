package flefebv.teamPoker.domain.user

import scala.util.Try

trait UserRepository {

  def get(id: UserId): Try[Option[User]]

  def save(user: User): Try[User]

}

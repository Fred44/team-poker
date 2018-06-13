package flefebv.teamPoker.domain.user

import flefebv.teamPoker.domain.Entity

case class User (
  id: UserId,
  name: String,
  email: String
) extends Entity[UserId]

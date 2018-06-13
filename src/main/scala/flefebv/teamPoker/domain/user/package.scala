package flefebv.teamPoker.domain

package object user {

  case class UserId(value: String) extends AnyVal with EntityId
  object UserId {
    def newId(implicit idGen: EntityIdGenerator): UserId = UserId(idGen.genId())
  }
}

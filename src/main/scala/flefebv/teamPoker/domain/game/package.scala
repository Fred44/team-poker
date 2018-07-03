package flefebv.teamPoker.domain

import flefebv.teamPoker.domain.user.User

package object game {

  case class GameId(value: String) extends AnyVal with EntityId
  object GameId {
    def newId(implicit idGen: EntityIdGenerator): GameId =
      GameId(idGen.genId())
  }

  case class PlayerId (value: String) extends AnyVal with EntityId
  object PlayerId {
    def newId(implicit idGen: EntityIdGenerator): PlayerId =
      PlayerId(idGen.genId())
  }

  case class Player (id: PlayerId, name: String, email: Option[String] = None) extends Entity[PlayerId]


  case class Card (value: String) extends AnyVal with Value[String]
  case class Question (value: String) extends AnyVal with Value[String]
  case class PokerGameName (value: String) extends AnyVal with Value[String]

  sealed abstract class GameStatus(val value: String)
  object GameStatus {

    case object Opened extends GameStatus("OPENED")

    case object Closed extends GameStatus(value = "CLOSED")
  }

  implicit class Owner(user: User) {

    def createPokerGame(name: PokerGameName, cards: List[Card])(implicit idGen: EntityIdGenerator): PokerGame =
      PokerGame(GameId.newId, name, GameStatus.Opened, cards, Seq.empty[Player])

  }

}

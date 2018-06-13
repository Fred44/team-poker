package flefebv.teamPoker.domain.game

import flefebv.teamPoker.domain.DomainError

sealed trait PokerGameError extends DomainError {
  val pokerGame: PokerGame
}

case class InvalidCard(pokerGame: PokerGame, card: Card) extends PokerGameError
case class UnknownPlayer(pokerGame: PokerGame, player: Player) extends PokerGameError
case class PlayerNameAlreadyUsedError(pokerGame: PokerGame, playerName: String) extends PokerGameError
case class ClosedGameError(pokerGame: PokerGame) extends PokerGameError
case class ClosedPollError(pokerGame: PokerGame) extends PokerGameError
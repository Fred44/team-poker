package flefebv.teamPoker.domain.game

import flefebv.teamPoker.domain.{Entity, EntityIdGenerator}

case class PokerGame(
                      id: GameId,
                      name: PokerGameName,
                      state: GameStatus = GameStatus.Opened,
                      cards: List[Card] = List.empty,
                      players: Seq[Player] = Nil,
                      poll: Poll = NoPoll()
) extends Entity[GameId] {

  def newPoll(question: Question): Either[PokerGameError, PokerGame] = {
    if (state == GameStatus.Closed)
      Left(ClosedGameError(this))
    else {
      // TODO: close current poll if needed
      val newPoll = OpenedPoll(question)
      Right(copy(poll = newPoll))
    }
  }

  def closePoll(): PokerGame = {
    poll match {
      case NoPoll() => this
      case _ => copy(poll = NoPoll())
    }
  }

  def vote(player: Player, card: Card): Either[PokerGameError, PokerGame] = {
    poll match {
      case NoPoll()       => Left(ClosedPollError(this))
      case p: OpenedPoll  =>
        if (!cards.contains(card))
          Left(InvalidCard(this, card))

        else if(!players.contains(player))
          Left(UnknownPlayer(this, player))

        else
          Right(copy(poll = p.vote(player, card)))
    }
  }

  def join(playerName: String, playerEmail: Option[String] = None)(implicit idGen: EntityIdGenerator): Either[PokerGameError, PokerGame] = {
    if (state == GameStatus.Closed)
      Left(ClosedGameError(this))

    else if (containsPlayer(playerName))
      Left(PlayerNameAlreadyUsedError(this, playerName))

    else
      Right(copy(players = players :+ Player(PlayerId.newId, playerName, playerEmail)))
  }

  def showVotes(): Either[PokerGameError, PokerGame] =
    poll match {
      case NoPoll()       => Left(ClosedPollError(this))
      case p: OpenedPoll  => Right(copy(poll = p.showVote))
    }

  def clearVotes(): Either[PokerGameError, PokerGame] =
    poll match {
      case NoPoll()       => Left(ClosedPollError(this))
      case p: OpenedPoll  => Right(copy(poll = p.clearVotes))
    }

  def closeTheGame: PokerGame =
    state match {
      case GameStatus.Closed => this
      case GameStatus.Opened => copy(state = GameStatus.Closed)
    }

  def getPlayer(playerId: PlayerId): Option[Player] =
    players find(_.id == playerId)

  private def containsPlayer(playerName: String): Boolean =
    players exists(_.name == playerName)
}

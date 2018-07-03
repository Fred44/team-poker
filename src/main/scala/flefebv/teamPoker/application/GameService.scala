package flefebv.teamPoker.application

import flefebv.teamPoker.domain.EntityIdGenerator
import flefebv.teamPoker.domain.game._
import flefebv.teamPoker.domain.user.{UserId, UserRepository}

trait GameService {

  implicit val entityIdGenerator: EntityIdGenerator

  val gameRepository: PokerGameRepository
  val userRepository: UserRepository

  implicit val infraErrorHandler: Throwable => ServiceError
  implicit val pokerGameErrorHandler: PokerGameError => ServiceError = (e) =>
    IllegalGameOperationError(e.pokerGame)


  def createNewPokerGame(name: PokerGameName, me: UserId, cards: List[Card]): Either[ServiceError, PokerGame] = {
    for {
      owner <- userRepository.get(me) ifNotExists NotFoundError("USER", me)
      createdGame = owner.createPokerGame(name, cards)
      savedGame <- gameRepository.save(createdGame) ifFailureThen asServiceError
    } yield savedGame
  }

  def closeAPokerGame(gameId: GameId, me: UserId): Either[ServiceError, PokerGame] = {
    for {
      game <- gameRepository.get(gameId) ifNotExists asGameNotFoundError(gameId)
      closedGame = game.closeTheGame
      savedGame <- gameRepository.save(closedGame) ifFailureThen asServiceError
    } yield savedGame
  }

  def joinAPokerGame(gameId: GameId, playerName: String, playerEmail: Option[String] = None): Either[ServiceError, PokerGame] = {
    for {
      game <- gameRepository.get(gameId) ifNotExists asGameNotFoundError(gameId)
      joinedGame <- game.join(playerName, playerEmail) ifLeftThen asServiceError
      savedGame <- gameRepository.save(joinedGame) ifFailureThen asServiceError
    } yield savedGame
  }

  def openAPoll(gameId: GameId, question: Question): Either[ServiceError, PokerGame] = {
    for {
      game <- gameRepository.get(gameId) ifNotExists asGameNotFoundError(gameId)
      updatedGame <- game.newPoll(question) ifLeftThen asServiceError
      savedGame <- gameRepository.save(updatedGame)  ifFailureThen asServiceError
    } yield savedGame
  }

  def vote(gameId: GameId, playerId: PlayerId, card: Card): Either[ServiceError, PokerGame] = {
    for {
      game <- gameRepository.get(gameId) ifNotExists asGameNotFoundError(gameId)
      player <- game.getPlayer(playerId) ifNotExists NotFoundError("PLAYER", playerId)
      updatedGame <- game.vote(player, card) ifLeftThen asServiceError
      savedGame <- gameRepository.save(updatedGame) ifFailureThen asServiceError
    } yield savedGame
  }

  def clearVotes(gameId: GameId, me: UserId): Either[ServiceError, PokerGame] = {
    for {
      game <- gameRepository.get(gameId) ifNotExists asGameNotFoundError(gameId)
      updatedGame <- game.clearVotes() ifLeftThen asServiceError
      savedGame <- gameRepository.save(updatedGame) ifFailureThen asServiceError
    } yield savedGame
  }

  def showVotes(gameId: GameId, me: UserId): Either[ServiceError, PokerGame] = {
    for {
      game <- gameRepository.get(gameId) ifNotExists asGameNotFoundError(gameId)
      updatedGame <- game.showVotes() ifLeftThen asServiceError
      savedGame <- gameRepository.save(updatedGame) ifFailureThen asServiceError
    } yield savedGame
  }

}



package flefebv.teamPoker.application

import flefebv.teamPoker.domain.EntityIdGenerator
import flefebv.teamPoker.domain.game.GameStatus.Closed
import flefebv.teamPoker.domain.game.{Card, GameId, GameStatus, NoPoll, Player, PokerGame, PokerGameName, PokerGameRepository}
import flefebv.teamPoker.domain.user.{User, UserId, UserRepository}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.EitherValues._

import scala.util.Success

class GameServiceSpec extends FlatSpec
    with Matchers with MockFactory {

  trait PokerGameService {

    val gameRepo = mock[PokerGameRepository]
    val userRepo = stub[UserRepository]
    val idGenerator = stub[EntityIdGenerator]

    val mrX = User(UserId("X"), "Mr X", "my@email.com")
    val emptyGame = PokerGame(GameId("P"), PokerGameName("Empty game"))

    val closedGame = PokerGame(GameId("C"), PokerGameName("Closed game"), state = Closed)

    val gameService = new GameService {
      override implicit val entityIdGenerator: EntityIdGenerator = idGenerator
      override val gameRepository: PokerGameRepository = gameRepo
      override val userRepository: UserRepository = userRepo
      override implicit val infraErrorHandler: Throwable => ServiceError = {
        case e => SystemError(e)
      }
    }
  }

  "createNewPokerGame" should "save a new poker game" in new PokerGameService {

    (userRepo.get _).when(*).returns(Success(Some(mrX)))
    (idGenerator.genId _).when().returns("P")
    (gameRepo.save _).expects(where { pg: PokerGame => pg.name == PokerGameName("Test Game") })
      .onCall { pg: PokerGame => Success(pg) }

    val res: Either[ServiceError, PokerGame] =
      gameService.createNewPokerGame(PokerGameName("Test Game"), UserId("X"), List(Card("A"), Card("B"), Card("C")))

    res.right.value.state should be (GameStatus.Opened)
    res.right.value.players shouldBe empty
    res.right.value.cards should be (List(Card("A"), Card("B"), Card("C")))
    res.right.value.poll shouldBe a [NoPoll]
  }

  it should "raise a not found error if user doesn't exist" in new PokerGameService {

    (userRepo.get _).when(*).returns(Success(None))

    val res: Either[ServiceError, PokerGame] =
      gameService.createNewPokerGame(PokerGameName("Test Game"), UserId("X"), List(Card("A"), Card("B"), Card("C")))

    res.left.value should matchPattern { case NotFoundError("USER", UserId("X")) => }
  }

  "closeAPokerGame" should "save the close status" in new PokerGameService {

    (userRepo.get _).when(*).returns(Success(Some(mrX)))
    (gameRepo.get _).expects(GameId("P")).returning(Success(Some(emptyGame)))
    (gameRepo.save _).expects(where { pg: PokerGame => pg.state == GameStatus.Closed })
      .onCall { pg: PokerGame => Success(pg) }

    gameService.closeAPokerGame(GameId("P"), UserId("X"))
  }

  it should "return a game not found error" in new PokerGameService {

    (userRepo.get _).when(*).returns(Success(Some(mrX)))
    (gameRepo.get _).expects(GameId("P")).returning(Success(None))

    gameService.closeAPokerGame(GameId("P"), UserId("X")) should matchPattern {
      case Left(NotFoundError("POKER-GAME", GameId("P"))) =>
    }
  }

  "join a poker game" should "save the new player in the game" in new PokerGameService {

    (userRepo.get _).when(*).returns(Success(Some(mrX)))
    (gameRepo.get _).expects(GameId("P")).returning(Success(Some(emptyGame)))
    (gameRepo.save _).expects(where { pg: PokerGame => pg.players map(_.name) contains("foo")  })
      .onCall { pg: PokerGame => Success(pg) }

    gameService.joinAPokerGame(GameId("P"), "foo")
  }

  it should "return an invalid game operation if it's closed" in new PokerGameService {

    (userRepo.get _).when(*).returns(Success(Some(mrX)))
    (gameRepo.get _).expects(GameId("C")).returning(Success(Some(closedGame)))

    gameService.joinAPokerGame(GameId("C"), "foo") should matchPattern {
      case Left(err: ApplicationError) if err.errorCode == ServiceErrorCodes.InvalidGameOperation =>
    }
  }
}

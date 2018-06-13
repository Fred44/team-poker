package flefebv.teamPoker.domain.game

import flefebv.teamPoker.domain.EntityIdGenerator
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.EitherValues._

class PokerGameSpec extends FlatSpec with Matchers {

  implicit val entityIdGenerator: EntityIdGenerator = () => "A"

  def playerA = Player(PlayerId("A"), "Player A")

  trait EmptyPokerGame {
    val pokerGame = PokerGame(
      GameId("1"),
      PokerGameName("Test game"),
      cards = List(Card("1"), Card("3"), Card("5"))
    )
  }

  trait ClosedPokerGame {
    val pokerGame = PokerGame(
      GameId("1"),
      PokerGameName("Test game"),
      GameStatus.Closed,
      cards = List(Card("1"), Card("3"), Card("5"))
    )
  }

  trait InTheGamePoker {
    val pokerGame = PokerGame(
      GameId("1"),
      PokerGameName("Test game"),
      cards = List(Card("1"), Card("3"), Card("5")),
      players = Seq(playerA),
      poll = OpenedPoll(Question("User registration story"), Map(playerA -> Card("3")))
    )
  }

  "join" should "return a poker game with one player" in new EmptyPokerGame {

    pokerGame.join("Player A").right.value should have ('players (List(playerA)))
  }

  it should "return a ClosedGameError error" in new ClosedPokerGame {

    pokerGame.join("Player A").left.value shouldBe a [ClosedGameError]
  }

  it should "return a PlayerNameAlreadyUsedError" in new InTheGamePoker {

    pokerGame.join("Player A").left.value shouldBe a [PlayerNameAlreadyUsedError]
  }

  "close a poll" should "make the game have no current poll" in new InTheGamePoker {

    pokerGame.closePoll().poll shouldBe an [NoPoll]
  }

  "vote" should "return a ClosedPollError" in new EmptyPokerGame {

    pokerGame.vote(playerA, Card("3")).left.value shouldBe a [ClosedPollError]
  }

  it should "return an InvalidCard error" in new InTheGamePoker {

    pokerGame.vote(playerA, Card("???")).left.value shouldBe a [InvalidCard]
  }

  it should "return an UnknownPlayer error" in new InTheGamePoker {

    pokerGame.vote(Player(PlayerId("B"), "Player B"), Card("3")).left.value shouldBe a [UnknownPlayer]
  }

  it should "add the vote to the current poll" in new InTheGamePoker {

    pokerGame.vote(playerA, Card("3")).right.value.poll match {
      case NoPoll() =>
        fail("poll isn't opened")
      case OpenedPoll(_, votes, _) =>
        votes should contain (playerA -> Card("3"))
    }
  }

  it should "keep the latest by player" in new InTheGamePoker {
    for {
      pokerGame <-     pokerGame.vote(playerA, Card("3"))
      pokerGame <-     pokerGame.vote(playerA, Card("5"))
    } yield pokerGame.poll match {
      case NoPoll() =>
        fail("poll isn't opened")
      case OpenedPoll(_, votes, _) => {
        votes should have size 1
        votes should contain(playerA -> Card("5"))
      }
    }
  }

  "open poll" should "open a new poll" in new EmptyPokerGame {

    val poll: Poll = pokerGame.newPoll(Question("How much for ... ?")).right.value.poll
    poll shouldBe an [OpenedPoll]
    poll should have (
      'question ("How much for ... ?"),
      'votesAreShown (false)
    )
  }

  it should "return a ClosedGameError" in new ClosedPokerGame {

    pokerGame.newPoll(Question("Do you like it ?")).left.value shouldBe a [ClosedGameError]
  }

  "show votes" should "set vote shown flag to true" in new InTheGamePoker {

    pokerGame.showVotes().right.value.poll should have ('votesAreShown (true))
  }

  it should "return a ClosedPollError" in new EmptyPokerGame {

    pokerGame.showVotes().left.value shouldBe a [ClosedPollError]
  }

  "clear votes" should "remove all player votes" in new InTheGamePoker {

    pokerGame.clearVotes().right.value.poll should have ('playerVote (Map.empty))
  }

  it should "return a ClosedPollError" in new EmptyPokerGame {

    pokerGame.clearVotes().left.value shouldBe a [ClosedPollError]
  }

}

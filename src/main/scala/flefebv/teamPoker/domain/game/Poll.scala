package flefebv.teamPoker.domain.game

sealed abstract class Poll()

case class NoPoll () extends Poll

case class OpenedPoll (
                  question: Question,
                  playerVote: Map[Player, Card] = Map.empty,
                  votesAreShown: Boolean = false
) extends Poll {

  def vote(player: Player, card: Card): OpenedPoll = {
    copy(
      playerVote = playerVote + (player -> card)
    )
  }

  def showVote: OpenedPoll =
    copy(
      votesAreShown = true
    )

  def clearVotes: OpenedPoll =
    copy(
      playerVote = Map.empty
    )

  def close: NoPoll = NoPoll()
}

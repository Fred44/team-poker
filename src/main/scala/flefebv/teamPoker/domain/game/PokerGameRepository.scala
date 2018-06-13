package flefebv.teamPoker.domain.game

import scala.util.Try

trait PokerGameRepository {

  def get(id: GameId): Try[Option[PokerGame]]

  def save(game: PokerGame): Try[PokerGame]
}

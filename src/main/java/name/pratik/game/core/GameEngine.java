/**
 *
 */
package name.pratik.game.core;

import java.util.List;

/**
 * @author Pratik Soares
 *
 */
public interface GameEngine {

	GameState getCurrentGameState();

	GameState move(Move move);

	GameState peekNewState(Move m);

	GameState peekNewState(GameState gameState, Move m);

	Player getNextPlayer();

	Player getNextPlayer(GameState gameState);

	int getNextMovesCount(GameState gameState);

	int getNextMovesCount(GameState gameState, boolean simulationMode);

	List<? extends Move> getNextMoves(GameState gameState, boolean simulationMode);

	double getHeuristicScore(GameState n, Player player);

	double getHeuristicScore(GameState value, Player player, GameState stopState);

	void randomizeHiddenInfo(GameState gameState, Player player);

	void clearRandomizedHiddenInfo();

}

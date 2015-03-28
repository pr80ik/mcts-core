/**
 *
 */
package name.pratik.game.core;

/**
 * @author Pratik Soares
 *
 */
public interface GameState {

	Player getCurrentPlayer();

	Move getCurrentMove();

	interface Builder {
		GameState build();
	}

}

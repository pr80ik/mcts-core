/**
 *
 */
package name.pratik.game.core;

/**
 * @author Pratik Soares
 *
 */
public interface CompoundGameState extends GameState {

	GameState getPreviousState();
}

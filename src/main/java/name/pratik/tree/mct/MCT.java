/**
 *
 */
package name.pratik.tree.mct;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import name.pratik.game.core.GameEngine;
import name.pratik.game.core.GameState;
import name.pratik.game.core.Move;
import name.pratik.game.core.Player;
import name.pratik.tree.Node;

/**
 * Monte Carlo Tree
 *
 * see https://en.wikipedia.org/wiki/Monte_Carlo_tree_search (version: https://en.wikipedia.org/w/index.php?title=Monte_Carlo_tree_search&oldid=637122013)
 *
 * @author Pratik Soares
 *
 */
public class MCT {

	private static final Random RANDOM = new Random();

	private final GameEngine gameEngine;

	private Node<GameState> tree;

	public MCT(GameEngine gameEngine) {
		this(gameEngine, null);
	}

	public MCT(GameEngine gameEngine, Node<GameState> rootNode) {
		super();
		this.gameEngine = gameEngine;
		this.tree = rootNode;
	}

	/**
	 * Play a random game and feedback the win/loss score to each parent back
	 * to the root.
	 * @param player
	 *
	 */
	public void playout(Player player) {
		if(tree==null){
			GameState startState = gameEngine.getCurrentGameState();
			tree = new Node<GameState>(startState);
		}

		Node<GameState> c = expand(select());

		backPropagate(c, simulate(c, player));
	}

	private void determinization(Node<GameState> c, Player player) {
		gameEngine.randomizeHiddenInfo(c.getValue(), player);
	}

	private void clearDeterminization() {
		gameEngine.clearRandomizedHiddenInfo();
	}

	/**
	 * Selection: Starting at the root of the tree, choose a child node
	 * according to a multi-armed bandit algorithm (see below). Then select a
	 * child of this child, and so on until you reach a node with untried moves.
	 *
	 * @return The selected Node
	 */
	public Node<GameState> select() {
		return select(tree);
	}

	protected Node<GameState> select(Node<GameState> node) {
		if(node.getChildren()==null || node.getChildren().size()==0){
			return node;
		}

		int maxClildren = gameEngine.getNextMovesCount(node.getValue());
		if(node.getChildren().size() < maxClildren){
			return node;
		}

		double maxUCT = Double.MIN_VALUE;
		Node<GameState>  selectedChild = null;
		for(Node<GameState> child : node.getChildren()) {
			double uct = getUctScore(child);

			if(Double.isNaN(uct)) {
				uct = -1;
			}

			if(selectedChild==null || (uct > maxUCT)) {
				selectedChild = child;
				maxUCT = uct;
			}
		}

		if(selectedChild==null){
			throw new NullPointerException("Node " + node + " has no suitable child node(s)");
		}

		return select(selectedChild);
	}

	/**
	 * Expansion: unless L ends the game, Add a node corresponding to one of
	 * the untried moves, chosen at random node C. If no child was created,
	 * start simulation from L.
	 *
	 * @return
	 */
	public Node<GameState> expand(Node<GameState> l) {
		return expand(l, false);
	}

	public Node<GameState> expand(Node<GameState> l, boolean simulationMode) {
		GameState gameState = l.getValue();
		List<? extends Move> allNextMoves = gameEngine.getNextMoves(gameState, simulationMode);

		if(allNextMoves!=null && allNextMoves.size()>0) {
			List<? extends Move> untriedMoves = getAllUntriedMoves(l, allNextMoves);

			if(untriedMoves.size()>0){
				Move randomNextMove = untriedMoves.get(RANDOM.nextInt(untriedMoves.size()));

				GameState randomNextState = gameEngine.peekNewState(gameState, randomNextMove);
				Node<GameState> c = new Node<GameState>(l, randomNextState);

				l.addChild(c);

				return c;
			}
		} else {
			//no children available
			l.addChild(null);
		}

		return l;
	}


	/**
	 * Simulation: play a random playout from node C until a terminal (end of
	 * game) state is reached.
	 * @param player
	 * @return
	 */
	public double simulate(Node<GameState> c, Player player) {
		determinization(c, player);

		Node<GameState> next = c;

		while(!next.isLeafNode()) {
			next = expand(next, true);
		}

		clearDeterminization();

		double heuristicScore = gameEngine.getHeuristicScore(next.getValue(), player);

		//don't add the simulation nodes to the main tree
		c.disownChilden();

		return heuristicScore;
	}

	/**
	 * Back-propagation: using the result of the play-out, update information in
	 * the nodes on the path from C to Root.
	 *
	 */
	public void backPropagate(Node<GameState> c, double heuristicScore){
		c.setTotal(c.getTotal()+1);
		c.setHeuristicScore(c.getHeuristicScore() + heuristicScore);
		c.setWin(1);

//		if(heuristicScore>0.5){
//			//TODO PSS: how to determine win?
//			c.setWin(c.getWin()+1);
//		}

		Node<GameState> parent = c.getParent();
		if(parent!=null){
			backPropagate(parent, heuristicScore);
		}
	}

	protected double getUctScore(Node<GameState> node) {
		double ni = node.getTotal();

		if(ni==0) {
			return Double.NaN;
		}

		double di = node.getHeuristicScore();
		double wi = node.getWin();

		double c = Math.sqrt(2);

		Node<GameState> parent = node.getParent();
		double t = (parent!=null && parent!=tree)? parent.getWin() : node.getWin();
		double ln_t = Math.log(t);

		double exploitationFactor = (di * wi) / ni;
		double explorationFactor = c * Math.sqrt(ln_t / ni);

		return exploitationFactor + explorationFactor;
	}

	public List<Node<GameState>> getBestMoves() {
		List<Node<GameState>> children = new ArrayList<Node<GameState>>(tree.getChildren());
		Collections.sort(children, new Comparator<Node<GameState>>() {

			public int compare(Node<GameState> n1, Node<GameState> n2) {
				double diff = n2.getHeuristicScore() - n1.getHeuristicScore();

				if(diff > 0){
					return 1;
				} else if(diff < 0){
					return -1;
				}

				return 0;
			}
		});

		return children;
	}


	private static List<? extends Move> getAllUntriedMoves(Node<GameState> l, List<? extends Move> allNextMoves) {
		List<Move> triedMoves;

		List<Node<GameState>> children = l.getChildren();
		if(children!=null){
			triedMoves = new ArrayList<Move>(children.size());
			for (Node<GameState> child : children) {
				triedMoves.add(child.getValue().getCurrentMove());
			}
		} else {
			triedMoves = Collections.emptyList();
		}

		List<? extends Move> untriedMoves = new ArrayList<Move>(allNextMoves);
		untriedMoves.removeAll(triedMoves);

		return untriedMoves;
	}

	private static boolean isChildPresent(Node<GameState> l, Move move) {
		List<Node<GameState>> children = l.getChildren();
		if(children!=null){
			for (Node<GameState> child : children) {
				if(child.getValue().getCurrentMove().equals(move)){
					return true;
				}
			}
		}

		return false;
	}

	public String print() {
		StringBuilder sb = new StringBuilder();
		if(tree!=null){
			tree.print(sb);
		}

		return sb.toString();
	}

}

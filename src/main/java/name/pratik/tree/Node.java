/**
 *
 */
package name.pratik.tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Pratik Soares
 *
 */
public class Node<E> {

	private final Node<E> parent;

	private double heuristicScore;

	private double win;

	private double total;

	private List<Node<E>> children;

	private final E value;

	public Node(E value) {
		this(null, value);
	}

	public Node(Node<E> parent, E value) {
		super();
		this.parent = parent;
		this.value = value;
	}

	public double getHeuristicScore() {
		return heuristicScore;
	}

	public void setHeuristicScore(double heuristicScore) {
		this.heuristicScore = heuristicScore;
	}

	public double getWin() {
		return win;
	}

	public void setWin(double win) {
		this.win = win;
	}

	public double getTotal() {
		return total;
	}

	public void setTotal(double total) {
		this.total = total;
	}

	public List<Node<E>> getChildren() {
		if(children!=null){
			return Collections.unmodifiableList(children);
		}

		return null;
	}

	public void setChildren(List<Node<E>> children) {
		this.children = children;
	}

	public void addChild(Node<E>... newChildren) {
		if(newChildren!=null){
			if(this.children == null){
				this.children = new ArrayList<Node<E>>();
			}

			children.addAll(Arrays.asList(newChildren));
		} else {
			if(children==null){
				children = Collections.emptyList();
			}
		}
	}

	public void disownChilden() {
		children = null;
	}

	public boolean isLeafNode(){
		if(children==null) {
			return false;
		}

		if(children.size()==0) {
			return true;
		}

		return false;
	}

	public Node<E> getParent() {
		return parent;
	}

	public E getValue() {
		return value;
	}

	@Override
	public String toString() {
		return String.format("%s [%s:%s/%s]",
				value,
				win/total, win, total);
	}

	public void print(StringBuilder sb) {
		print(sb, "", true);
	}

	public void print(StringBuilder sb, String prefix, boolean tail) {
		/*
		 * https://stackoverflow.com/questions/4965335/how-to-print-binary-tree-diagram
		 * */
		sb.append(prefix)
			.append(tail ? "\u2514" : "\u251C")
			.append("── ")
			.append(toString())
			.append("\n");

		if(children!=null){
			String childPrefix = prefix + (tail ? "    " : "\u2502   ");

			for (int i = 0; i < children.size() - 1; i++) {
				children.get(i).print(sb, childPrefix, false);
			}

			if (children.size() > 0) {
				children.get(children.size() - 1).print(sb, childPrefix, true);
			}
		}
	}

}

package computeraccessoriesshop;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class TrieTreeNode {

	// Map containing all child nodes to the current node
	private final Map<Character, TrieTreeNode> Children = new HashMap<>();
	// WordEnd is true if the current node is the end of a word
	private boolean WordEnd = false;

	// Adds a word to the trie tree
	final public void Add(String Word, int index) {
		// Recursively adds each character to the trie tree
		if (index == Word.length()) {
			WordEnd = true;
			return;
		}
		if (!Children.containsKey(Word.charAt(index))) {
			Children.put(Word.charAt(index), new TrieTreeNode());
		}
		Children.get(Word.charAt(index)).Add(Word, index + 1);
	}

	// Returns whether or not a word is in the trie or whether or not a word is a
	// prefix to another word in the tree
	final public boolean Contains(String Word, int index, boolean isPrefix) {
		// Recursively searches for each character in the trie tree
		if (index == Word.length()) {
			if (isPrefix) {
				return true;
			} else {
				return WordEnd;
			}
		}
		if (Children.containsKey(Word.charAt(index))) {
			return Children.get(Word.charAt(index)).Contains(Word, index + 1, isPrefix);
		}
		return false;
	}

	// Gets the node at the end of a word
	final private TrieTreeNode GetNode(String Word, int index) {
		// Recursively searches for each character in the trie tree
		if (index == Word.length()) {
			return this;
		}
		if (Children.containsKey(Word.charAt(index))) {

			return Children.get(Word.charAt(index)).GetNode(Word, index + 1);
		}
		return null;
	}

	// Returns a set of all the words which the specified word is a prefix of
	final public Set<String> PrefixOf(String Word) {
		// Gets node at end of the word
		TrieTreeNode Root = GetNode(Word, 0);
		// Performs depth first search to find words that specified word is a prefix of
		if (Root != null) {
			return Root.DFS(Word);
		} else {
			return new HashSet<>();
		}
	}

	// Performs depth first search to find words that specified word is a prefix of
	final private Set<String> DFS(String Word) {
		// Adds all words found so far to set
		Set<String> Result = new HashSet<>();
		if (WordEnd) {
			Result.add(Word);
		}
		// Calls recursively on all child nodes
		for (char Character : Children.keySet()) {
			Result.addAll(Children.get(Character).DFS(Word + Character));
		}
		return Result;
	}
}

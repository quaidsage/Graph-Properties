package nz.ac.auckland.se281.datastructures;

import java.util.Comparator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

/**
 * A graph that is composed of a set of verticies and edges.
 *
 * <p>You must NOT change the signature of the existing methods or constructor of this class.
 *
 * @param <T> The type of each vertex, that have a total ordering.
 */
public class Graph<T extends Comparable<T>> {

  private Set<T> verticies;
  private Set<Edge<T>> edges;
  private List<List<T>> adjVerticies;

  /** Constructor for specific graph with set of verticies and edges. */
  public Graph(Set<T> verticies, Set<Edge<T>> edges) {
    this.verticies = verticies;
    this.edges = edges;
    this.adjVerticies = new ArrayList<>(verticies.size());

    // Create a list of lists containing the destination of each vertex
    for (int i = 0; i < verticies.size(); i++) {
      adjVerticies.add(new ArrayList<>());
    }

    for (T vertex : verticies) {
      for (Edge<T> edge : edges) {
        if (edge.getSource() == vertex) {
          adjVerticies.get(getIndex(vertex)).add(edge.getDest());
        }
      }
    }

  }

  /** Returns a Set<T> containing the root verticies of the graph. */
  public Set<T> getRoots() {

    // Initialise root set and temporary root set to help find roots
    Set<T> roots = new HashSet<>();
    Set<T> tempRoots = new HashSet<>(verticies);
    
    // Remove any verticies that have an in degree > 0 from temp root set
    for (Edge<T> edge : edges) {
      tempRoots.remove(edge.getDest());
    }

    // If any of the remaining verticies in temp root have out degree != 0 
    // from temp roots, they are added to root set
    for (T temp : tempRoots) {

      for (Edge<T> edge : edges) {

        if (temp.equals(edge.getSource())) {

          roots.add(temp);

        }
      }
    }

    // If the graph is not an equivalence relation, skip searching for equivalence 
    // classes and return roots
    if (!isEquivalence()) {

      return roots;
    }

    // Get minimum values from equivalence classes and add to roots set
    for (T vertex: verticies) {

      Set<T> equivClass = getEquivalenceClass(vertex);

      if (equivClass.size() != 0) {

        roots.add(getLowestValue(equivClass));
      }
    }

    return roots;

  }

  /** Returns true or false given the current graph is reflexive or not. */
  public boolean isReflexive() {

    // Iterate through all edges checking that every vertex points to itself
    int count = 0;

    for (Edge<T> edge : edges) {

      if (edge.getSource().equals(edge.getDest())) {

        count++;
      }
    }

    // Check that all verticies point to themselves, thus making graph reflexive
    if (count == verticies.size()) {

      return true;
    }

    return false;

  }

  /** Returns true or false given the current graph is symmetric or not. */
  public boolean isSymmetric() {

    // Iterate through all edges to check if one vertex points to another, 
    // that vertex will point back to the original vertex
    int count = 0;
    for (Edge<T> edge : edges) {
      for (Edge<T> edge2 : edges) {
        if ((edge.getSource().equals(edge2.getDest())) && 
        (edge.getDest().equals(edge2.getSource()))) {
          count++;
        }
      }
    }
    
    // If all edges point both ways for all verticies, graph is symmetric
    if (count == edges.size()) {
      return true;
    }
    return false;
  }

  /** Returns true or false given the current graph is transitive or not. */
  public boolean isTransitive() {
    
    // Iterate for all verticies for transitive property
    for (T vertex : verticies) {
      // Get list of all verticies pointing out of original
      List<T> adjTemp = adjVerticies.get(getIndex(vertex));
      for (int i = 0; i < adjTemp.size(); i++) {
        // Get list of all verticies pointing out of the verticies that pointed out of the original
        List<T> adjTemp2 = adjVerticies.get(getIndex(adjTemp.get(i)));

        // Check if the original vertex points to the verticies 
        // in adjTemp2 if not, graph is transitive
        for (int j = 0; j < adjTemp2.size(); j++) {
          if (!adjTemp.contains(adjTemp2.get(j))) {

            return false;
          }
        }
      }
    }

    return true;

  }

  /** Returns true or false given the current graph is anti-symmetric or not. */
  public boolean isAntiSymmetric() {
    // Iterate for all verticies that for all verticies they point to, they do not point back.
    for (T vertex : verticies) {

      // Create list of verticies that the vertex points to
      List<T> adjTemp = adjVerticies.get(getIndex(vertex));

      for (T temp : adjTemp) {

        // Check for all the verticies that they do not point back to the 
        // vertex and that it does not include itself
        if ((adjVerticies.get(getIndex(temp)).contains(vertex)) && vertex != temp) {

          return false;
        }

      }
    }
    return true;

  }
  
  /** Returns true or false given the current graph is an equivalence relation or not. */
  public boolean isEquivalence() {

    // If graph is reflexive, symmetric, and transitive it is said to be a equivalence relation.
    if (isReflexive() && isSymmetric() && isTransitive()) {

      return true;
    }

    return false;

  }

  /**
   * 
   * <p> Get the equivalence class that contains the given vertex. <\p>
   * 
   * @param vertex The vertex that will be part of the returned equivalence class
   * @return The equivalence class, if none, return empty set
   */
  public Set<T> getEquivalenceClass(T vertex) {

    // Initialise equivalence class set
    Set<T> equivClass = new HashSet<>();

    // Initialise boolean array for depth first search (dfs)
    boolean[] visited = new boolean[verticies.size()];

    // Initialise temporary arrays of original graph verticies, edges, and 
    // adjacencies for after method is finished running
    Set<T> saveVert = verticies;
    Set<Edge<T>> saveEdge = edges;
    List<List<T>> saveadjVerticies = adjVerticies;

    // Get all nodes connected to selected vertex
    dfs(vertex, visited, equivClass);

    // If any verticies connected those in the equivalence class are not a 
    // part of the current equivalence class of chosen vertex, it cannot be equivalent 
    // thus return empty set
    for (T vert : equivClass) {

      for (Edge<T> edge : edges) {

        if (edge.getDest().equals(vert) && !equivClass.contains(edge.getSource())) {

          return new HashSet<>();
        }
      }
    }

    //  Check if this class is equivilence relation

    // Replace verticies and edges with those within the class
    Set<Edge<T>> tempEdge = new HashSet<>();
    for (T vert : equivClass) {
      for (Edge<T> edge : edges) {
        if (edge.getSource().equals(vert) || edge.getDest().equals(vert)) {
          tempEdge.add(edge);
        }
      }
    }
    this.verticies = equivClass;
    this.edges = tempEdge;


    // Adjust adjacent vertex list to that relevant to current class
    adjVerticies = new ArrayList<>(verticies.size());

    for (int i = 0; i < verticies.size(); i++) {
      adjVerticies.add(new ArrayList<>());
    }

    for (T vert : verticies) {
      for (Edge<T> edge : edges) {
        if (edge.getSource() == vert) {
          adjVerticies.get(getIndex(vert)).add(edge.getDest());
        }
      }
    }

    // If it is not an equivalence relation, return empty set, else return equivalence class
    if (!isEquivalence()) {
      return new HashSet<>();
    }
    
    // Reset values back to original graph values.
    this.verticies = saveVert;
    this.edges = saveEdge;
    this.adjVerticies = saveadjVerticies;

    return equivClass;

  }

  /**
   * 
   * <p>Initialises an Iterative Breadth First Search on the graph 
   * visiting all verticies in an iterative manner.
   * <\p>
   * 
   * @return The ordered search of all verticies via IBFS
   */
  public List<T> iterativeBreadthFirstSearch() {
    // Get roots to start searchs from
    Set<T> roots = getRoots();

    // Initialise array that will give the ordered list of verticies visited by search
    List<T> visited = new ArrayList<>();
    
    // Iterate through all roots so that the entire valid graph is covered
    while (!roots.isEmpty()) {

      // Get next lowest value root as starting point and start search via ibfs
      T start = getLowestValue(roots);
      Queue<T> queue = new LinkedList<>();

      queue.add(start);
      visited.add(start);

      while (!queue.isEmpty()) {

        T tempVert = queue.poll();

        List<T> tempAdj = adjVerticies.get(getIndex(tempVert));
        sortList(tempAdj);

        for (int i = 0; i < tempAdj.size(); i++) {

          if (!visited.contains(tempAdj.get(i))) {

            queue.add(tempAdj.get(i));
            visited.add(tempAdj.get(i));

          }
        }
      }

      roots.remove(start);

    }
    
    return visited;

  }

  /**
   * 
   * <p>Initialises an Iterative Depth First Search on the graph 
   * visiting all verticies in an iterative manner.
   * <\p>
   * 
   * @return The ordered search of all verticies via IDFS
   */
  public List<T> iterativeDepthFirstSearch() {
    // Initialise stack and array of visited verticies in order of search
    List<T> visited = new ArrayList<>();
    Stack<T> stack = new Stack<>();

    // Get roots to start searchs from
    Set<T> roots = getRoots();

    // Iterate through all roots of the valid graph to visit all verticies
    while (!roots.isEmpty()) {
      T start = getLowestValue(roots);
      stack.push(start);

      // Search through graph using IDFS
      while (!stack.isEmpty()) {
        T tempVert = stack.pop();
        if (!visited.contains(tempVert)) {
          visited.add(tempVert);
          List<T> tempAdj = adjVerticies.get(getIndex(tempVert));
          sortList(tempAdj);
          reverseList(tempAdj);
          System.out.println(tempAdj);
          for (int i = 0; i < tempAdj.size(); i++) {
            if (!visited.contains(tempAdj.get(i))) {
              stack.push(tempAdj.get(i));
            }
          }
        }
      }
      roots.remove(start);
    }
    
    return visited;

  }

  /**
   * 
   * <p>Initialises an Recursive Breadth First Search on the graph visiting 
   * all verticies in a recursive manner.
   * <\p>
   * 
   * @return The ordered search of all verticies via RBFS
   */
  public List<T> recursiveBreadthFirstSearch() {
    // Get roots from where we start searches from
    Set<T> roots = getRoots();
    // Initialise visited vertex set and queue
    List<T> visited = new ArrayList<>();
    Queue<T> queue = new LinkedList<>();

    // Iterate through all roots to start search from recursively
    while (!roots.isEmpty()) {
      recursiveBreadthHelp(getLowestValue(roots), visited, queue);
      roots.remove(getLowestValue(roots));
    }
    
    return visited;

  }

  /**
   * 
   * <p>Recursive function for Recursive Breadth First Search which searches through all 
   * verticies from a given vertex.
   * <\p>
   */
  private void recursiveBreadthHelp(T tempVert, List<T> visited, Queue<T> queue) {

    // Add all distinct verticies adjacent to the given vertex to 
    // queue in an ordered matter recursively

    if (tempVert == null || visited.contains(tempVert)) {
      return;
    }
    
    visited.add(tempVert);

    List<T> tempAdj = adjVerticies.get(getIndex(tempVert));
    sortList(tempAdj);
    
    for (int i = 0; i < tempAdj.size(); i++) {
      if (!visited.contains(tempAdj.get(i))) {
        queue.add(tempAdj.get(i));
      }
    }

    if (!queue.isEmpty()) {
      T nextVert = queue.poll();
      recursiveBreadthHelp(nextVert, visited, queue);
    }

  }

  /**
   * 
   * <p>Initialises an Recursive Depth First Search on the graph visiting all 
   * verticies in a recursive manner.
   * <\p>
   * 
   * @return The ordered search of all verticies via RDFS.
   */
  public List<T> recursiveDepthFirstSearch() {
    // Get roots from where to start searches from
    Set<T> roots = getRoots();

    // Initialise visited vertex set
    List<T> visited = new ArrayList<>();

    //Iterate through all roots to start start from recursively
    while (!roots.isEmpty()) {
      recursiveDepthHelp(getLowestValue(roots), visited);
      roots.remove(getLowestValue(roots));
    }

    return visited;
    
  }

  /**
   * 
   * <p>Recursive function for Recursive Depth First Search which searches through all 
   * verticies from a given vertex.
   * <\p>
   */
  private void recursiveDepthHelp(T tempVert, List<T> visited) {

    // Search through all verticies recursively following Depth First Search method

    if (tempVert == null || visited.contains(tempVert)) {
      return;
    }

    visited.add(tempVert);

    List<T> tempAdj = adjVerticies.get(getIndex(tempVert));
    sortList(tempAdj);

    for (int i = 0; i < tempAdj.size(); i++) {
      recursiveDepthHelp(tempAdj.get(i), visited);
    }
  }

  /**
   * 
   * <p>Helper function that gives the relevant index of a particular vertex by 
   * converting it to a list.
   * <\p>
   * 
   * @return The index value of particular vertex
   */
  private int getIndex(T vertex) {

    List<T> list = new ArrayList<>(verticies);
    int index = list.indexOf(vertex);

    return index;
  }

  /**
   * 
   * <p>Helper function that utilises Depth First Searching to get an equivalence
   * class for the getEquivalenceClass() method.
   * <\p>
   */
  private void dfs(T vertex, boolean[] visited, Set<T> equivClass) {
    visited[getIndex(vertex)] = true;
    equivClass.add(vertex);

    List<T> adjTemp = adjVerticies.get(getIndex(vertex));
    for (T adj : adjTemp) {
      if (!visited[getIndex(adj)]) {
        dfs(adj, visited, equivClass);
      }
    }
  }

  /**
   * 
   * <p>Given a Set<T> find its lowest value using the helper function compare().<\p>
   * 
   * @return Lowest value T in Set<T>
   */
  private T getLowestValue(Set<T> set) {

    T lowest = null;

    for (T element : set) {

      if (lowest == null || compare(element, lowest) < 0) {

        lowest = element;
      }
    }

    return lowest;
  }

  /**
   * 
   * <p>Helper function that compares two elements of type T.<\p>
   * 
   * @return integer indicating which element is greater or less
   */
  private int compare(T element, T lowest) {

    Comparator<T> comparator = Comparator.naturalOrder();

    return comparator.compare(element, lowest);
  }

  /** Helper function that organises a list from lowest value to greatest value.  */
  private void sortList(List<T> list) {

    int n = list.size();

    // Iterate through all elements of list using bubble sort to organise from least to most value.
    for (int i = 0; i < n - 1; i++) {

      for (int j = 0; j < n - i - 1; j++) {

        if (compare(list.get(j), list.get(j+1)) > 0) {

          T temp = list.get(j);

          list.set(j, list.get(j+1));
          list.set(j+1, temp);

        }
      }
    }
  }

  /** Helper function that reverses a given list. */
  private void reverseList(List<T> list) {

    // Swap start and end elements and moving closer to center 
    // element of list until list is reversed
    int start = 0;
    int end = list.size() - 1;

    while (start < end) {

      T temp = list.get(start);

      list.set(start, list.get(end));
      list.set(end, temp);

      start++;
      end--;

    }
  }
}

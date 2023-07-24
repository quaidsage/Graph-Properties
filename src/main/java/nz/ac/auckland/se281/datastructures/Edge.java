package nz.ac.auckland.se281.datastructures;

/**
 * An edge in a graph that connects two verticies.
 *
 * <p>You must NOT change the signature of the constructor of this class.
 *
 * @param <T> The type of each vertex.
 */
public class Edge<T> {

  private T source;
  private T destination;

  /** Edge constructor requiring source and destination for edge. */
  public Edge(T source, T destination) {

    this.source = source;
    this.destination = destination;

  }

  /** Returns the source of the edge. */
  public T getSource() {

    return source;

  }

  /** Returns the destination of the edge. */
  public T getDest() {

    return destination;

  }
}

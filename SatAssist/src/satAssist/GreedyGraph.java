package satAssist;

import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.SimpleWeightedGraph;

public class GreedyGraph<V extends SatAssistNode, E extends TimedWeightedEdge> extends SimpleWeightedGraph<V, E> {

	private static final long serialVersionUID = 1L;

	public GreedyGraph(Class<? extends E> edgeClass) {
        this(new ClassBasedEdgeFactory<V, E>(edgeClass));
    }
	
    public GreedyGraph(EdgeFactory<V, E> ef) {
        super(ef);
    }
    
    public V getVertexByName(String name) {
		for (V s : this.vertexSet()) {
			if (s.getName().compareTo(name) == 0) {
				return s;
			}
		}
		return null;
    }
	
}

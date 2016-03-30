package satAssist;

import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

public class EventGraph<V extends TimedNode, E extends EventEdge> extends SimpleDirectedWeightedGraph<V, E> {

	private static final long serialVersionUID = 1L;
	
	private V source;
	private V sink;

	public EventGraph(Class<? extends E> edgeClass) {
        this(new ClassBasedEdgeFactory<V, E>(edgeClass));
    }
	
    public EventGraph(EdgeFactory<V, E> ef) {
        super(ef);
    }
    
    public void setSource(V s) {
    	this.source = s;
    }
    
    public void setSink(V t) {
    	this.sink = t;
    }
    
    public V getSource() {
    	return this.source;
    }
    
    public V getSink() {
    	return this.sink;
    }
}
	
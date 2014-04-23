package org.gephi.simulation;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.openide.util.Lookup;

public class EdgeWeight {

    private final AttributeColumn edgeColumn;

    public EdgeWeight() {
        AttributeModel am = Lookup.getDefault().lookup(AttributeController.class).getModel();
        edgeColumn = am.getEdgeTable().getColumn(DynamicModel.TIMEINTERVAL_COLUMN);
    }

    public int getEdgeWeighForInterval(double from, double to, Edge edge) {
        int weight = 0;
        if (edgeColumn != null) {
            Object obj = edge.getEdgeData().getAttributes().getValue(edgeColumn.getIndex());
            if (obj != null) {
                TimeInterval timeInterval = (TimeInterval) obj;
                for (int i = (int) from; i < (int) to; i++) {
                    if (timeInterval.isInRange(i, i)) {
                        weight += 1;
                    }
                }
            }
        }
        return weight;
    }
    
    public void setEdgeWeight(Graph g, double from, double to) {
        for (Edge e : g.getEdges()) {
            System.err.println(" " + getEdgeWeighForInterval(from, to, e));          
        }
    }

}

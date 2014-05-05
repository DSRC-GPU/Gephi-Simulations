package org.gephi.simulation;

import java.util.Collection;
import org.gephi.data.attributes.type.FloatList;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import static org.gephi.simulation.DirectionSim.RES_VECTOR;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = SimModel.class)
public class SmoothAlgoSim extends SimModel {

    private GraphModel graphModel;
    private Graph directionSimGraph;
    private int step;

    public SmoothAlgoSim() {
        setName("SmoothAlgoSim");
        priority = 5;
    }

    private void warn(boolean exp, String msg) {
        if (!exp) {
            System.err.println("WARNING: " + msg);
        }
    }

    @Override
    public void run(double from, double to, boolean changed) {
        System.err.println("SmoothAlgoSim step: " + step);
        step++;
        if (step == 1) {
            return;
        }

        Graph g = graphModel.getGraphVisible();
        for (Node n : g.getNodes()) {
            n.getNodeData().setX((float) (Math.random() * 200));
            n.getNodeData().setY((float) (Math.random() * 200));
        }

    }

    private FloatList getDirectionVector(Node n) {
        Node nn;
        nn = directionSimGraph.getNode(n.getNodeData().getId());
        if (nn == null) {
            System.out.println("null node: " + n.getId() + " label: " + n.getNodeData().getLabel());
        }
        
        FloatList vals = (FloatList) nn.getAttributes().getValue(RES_VECTOR);
        warn(vals != null, "vals is null node: " + n);
        warn(vals.size() == 2, "vals size < 2: got: " + vals.size() + " node: " + n);
        return vals;
    }

    @Override
    public void init() {
        step = 0;
        graphModel = Lookup.getDefault().lookup(GraphController.class).getModel(ws);

        Collection<? extends SimModel> sims = Lookup.getDefault().lookupAll(SimModel.class);
        SimModel[] simsArray = sims.toArray(new SimModel[0]);

        DirectionSim ds = null;
        for (int i = 0; i < simsArray.length; i++) {
            SimModel sm = simsArray[i];
            if (sm.getName().equals(DirectionSim.NAME)) {
                ds = (DirectionSim) sm;
                break;
            }
        }

        if (ds == null) {
            System.err.println("WARNING: Cannot find DirectionSim model");
            return;
        }

        directionSimGraph = Lookup.getDefault().lookup(GraphController.class).getModel(ds.ws).getGraph();
    }

    @Override
    public void end() {

    }

}

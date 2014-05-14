package org.gephi.simulation;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.type.FloatList;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = SimModel.class)
public class SmoothAlgoSim extends SimModel {

    public final static boolean WEIGHTED = true;
    public final static boolean SCALING = true;
    public final static int NO_ROUNDS = 5;
    public final static float INHIBIT_FACTOR = .1f;
    public final static String RES_VECTOR = "SmoothResultVector";

    private final HashMap<Integer, List<Coords>> state;
    private final HashMap<Integer, List<Double>> cosineSim;
    private final GroupSimilarity gs;
    private PrintWriter writer;
    private NodeScaler nodeScaler;
    private GraphModel graphModel;
    private Graph directionSimGraph;
    private int step;

    public SmoothAlgoSim() {
        setName("SmoothAlgoSim");
        priority = 5;
        gs = new GroupSimilarity(RES_VECTOR);
        state = new HashMap<Integer, List<Coords>>();
        cosineSim = new HashMap<Integer, List<Double>>();
    }

    private void warn(boolean exp, String msg) {
        if (!exp) {
            System.err.println("WARNING: SmoothAlgoSim: " + msg);
        }
    }

    public void initialize(Graph g) {
        for (Node n : graphModel.getGraph().getNodes()) {
            if (n.getAttributes().getValue(RES_VECTOR) == null) {
                FloatList vals = getDirectionVector(n);
                n.getAttributes().setValue(RES_VECTOR, vals);
            }
        }
    }

    public void startRound(Graph g) {
        for (Node n : g.getNodes()) {
            List<Coords> nodeVals = state.get(n.getId());
            List<Double> nodeCosineSim = cosineSim.get(n.getId());
            nodeVals.clear();
            nodeCosineSim.clear();

            FloatList vals = (FloatList) n.getAttributes().getValue(RES_VECTOR);
            warn(vals.size() == 2, "vals size < 2: got: " + vals.size() + " node: " + n);

            nodeCosineSim.add(1.0);
            nodeVals.add(new Coords(vals.getItem(0), vals.getItem(1)));
        }
    }

    public void runRound(Graph g) {
        for (Node n : g.getNodes()) {
            List<Coords> nodeVals = state.get(n.getId());
            List<Double> nodeCosineSim = cosineSim.get(n.getId());
            FloatList nDir = (FloatList) n.getAttributes().getValue(RES_VECTOR);

            for (Node neighbour : g.getNeighbors(n)) {
                FloatList neighbourDir = (FloatList) neighbour.getAttributes().getValue(RES_VECTOR);
                double cs = CosineSimilarity.angularSimilarity(nDir, neighbourDir);
                if (!Double.isNaN(cs)) {
                    nodeCosineSim.add(cs);
                } else {
                    nodeCosineSim.add(.0);
                }
                Coords c = new Coords(neighbourDir.getItem(0), neighbourDir.getItem(1));
                nodeVals.add(c);
            }
        }
    }

    public void endRound(Graph g) {
        nodeScaler.reset();
        for (Node n : g.getNodes()) {
            List<Coords> nodeVals = state.get(n.getId());
            List<Double> nodeCosineSim = cosineSim.get(n.getId());
            FloatList dir = getDirectionVector(n);
            //FloatList dir = (FloatList) n.getAttributes().getValue(RES_VECTOR);
            warn(nodeVals.size() == nodeCosineSim.size(), "List size differ");

            float sumX = 0;
            float sumY = 0;
            float sumWeight = 0;
            int localDensity = nodeVals.size();
            for (int i = 0; i < localDensity; i++) {
                Coords c = nodeVals.get(i);
                if (WEIGHTED) {
                    double cs = nodeCosineSim.get(i);
                    sumX += c.x * cs;
                    sumY += c.y * cs;
                    sumWeight += cs;
                } else {
                    sumX += c.x;
                    sumY += c.y;
                }
            }

            if (WEIGHTED) {
                sumX /= sumWeight;
                sumY /= sumWeight;
            } else {
                sumX /= localDensity;
                sumY /= localDensity;
            }

            float x = (1 - INHIBIT_FACTOR) * sumX + INHIBIT_FACTOR * dir.getItem(0);
            float y = (1 - INHIBIT_FACTOR) * sumY + INHIBIT_FACTOR * dir.getItem(1);
            nodeScaler.update(x, y);
            n.getAttributes().setValue(RES_VECTOR, new FloatList(new Float[]{x, y}));
        }

        for (Node n : g.getNodes()) {
            FloatList vals = (FloatList) n.getAttributes().getValue(RES_VECTOR);
            float x = vals.getItem(0);
            float y = vals.getItem(1);
            if (SCALING) {
                Coords c = nodeScaler.getScaled(x, y, 1000);
                x = c.x;
                y = c.y;
            }
            n.getNodeData().setX(x);
            n.getNodeData().setY(y);
        }
    }

    @Override
    public synchronized void run(double from, double to, boolean changed) {
        System.err.println("SmoothAlgoSim step: " + step);
        step++;
        if (step == 1) {
            return;
        }

        Graph g = graphModel.getGraphVisible();
        initialize(g);
        for (int i = 0; i < NO_ROUNDS; i++) {
            startRound(g);
            runRound(g);
            endRound(g);
        }

        writer.println("from " + from + " to " + to);
        gs.printGroupSimilarity(writer, 1, g);
        gs.printGroupSimilarity(writer, 2, g);
    }

    private FloatList getDirectionVector(Node n) {
        Node nn;
        nn = directionSimGraph.getNode(n.getNodeData().getId());
        if (nn == null) {
            System.out.println("null node: " + n.getId() + " label: " + n.getNodeData().getLabel());
            return null;
        }

        FloatList vals = (FloatList) nn.getAttributes().getValue(DirectionSim.RES_VECTOR);
        warn(vals != null, "vals is null node: " + n);
        warn(vals.size() == 2, "vals size < 2: got: " + vals.size() + " node: " + n);
        return vals;
    }

    @Override
    public void init() {
        step = 0;
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
        graphModel = Lookup.getDefault().lookup(GraphController.class).getModel(ws);
        nodeScaler = new NodeScaler(graphModel.getGraph().getNodeCount());

        AttributeController attributeController = Lookup.getDefault().lookup(AttributeController.class);
        AttributeTable nodesTable = attributeController.getModel(ws).getNodeTable();
        if (nodesTable.hasColumn(RES_VECTOR) == false) {
            nodesTable.addColumn(RES_VECTOR, AttributeType.LIST_FLOAT, AttributeOrigin.COMPUTED);
        }

        state.clear();
        cosineSim.clear();
        for (Node n : graphModel.getGraph().getNodes()) {
            state.put(n.getId(), new ArrayList<Coords>());
            cosineSim.put(n.getId(), new ArrayList<Double>());
            n.getAttributes().setValue(RES_VECTOR, null);
        }

        try {
            writer = new PrintWriter("Smooth_" + ForceAtlas2SIm.TIME_WINDOW, "UTF-8");
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void end() {
        writer.close();
    }

}

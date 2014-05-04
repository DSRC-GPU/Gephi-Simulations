package org.gephi.simulation;

import java.util.Random;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.type.FloatList;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = SimModel.class)
public class ForceAtlas2SIm extends SimModel {

    public final static int TIME_WINDOW = 20;
    private final static int iters = 100;
    private final static int seed = 42;

    private GraphModel graphModel;
    private final ForceAtlas2 layout;
    public final static String PREV_POSITION = "PrevPos";
    public final static String X_VECTOR = "VectorXCoord";
    public final static String Y_VECTOR = "VectorYCoord";

    private int step;
    private EdgeWeight edgeWeight;

    public ForceAtlas2SIm() {
        super();
        priority = 1;
        setName("ForceAtlas2Sim");
        layout = new ForceAtlas2(null);
    }

    @Override
    public void setWorkspace(Workspace ws) {
        super.setWorkspace(ws);
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.openWorkspace(ws);
    }

    private void addValue(Node n, float val, String column) {
        FloatList vals = (FloatList) n.getAttributes().getValue(column);
        int newSize = Math.min(TIME_WINDOW, vals.size() + 1);
        Float[] nl = new Float[newSize];

        for (int i = 0; i < newSize - 1; i++) {
            nl[i + 1] = vals.getItem(i);
        }

        nl[0] = val;
        /*
         System.err.print("ode: " + n.getNodeData().getId());
         for (int i = 0; i < newSize; i++) {
         System.err.print(" " + nl[i]);
         }
         System.err.println("");
         */
        n.getAttributes().setValue(column, new FloatList(nl));
    }

    @Override
    public void run(double from, double to, boolean changed) {

        System.err.println("ForceAtlas2Sim step: " + step + " changed " + changed);

        if (changed) {
            //edgeWeight.setEdgeWeight(graphModel.getGraphVisible(), from, to);
            layout.setGraphModel(graphModel);
            layout.initAlgo();
            layout.setEdgeWeightInfluence((double) 0);

            for (int i = 0; i < iters && layout.canAlgo(); i++) {
                layout.goAlgo();
            }

            layout.endAlgo();
        }

        for (Node n : graphModel.getGraphVisible().getNodes()) {
            FloatList prevPos = (FloatList) n.getAttributes().getValue(PREV_POSITION);

            float x = n.getNodeData().x();
            float y = n.getNodeData().y();

            float prevX = prevPos.getItem(0);
            float prevY = prevPos.getItem(1);

            if (step > 0) {
                addValue(n, x - prevX, X_VECTOR);
                addValue(n, y - prevY, Y_VECTOR);
            }

            n.getAttributes().setValue(PREV_POSITION, new FloatList(new Float[]{x, y}));
        }

        if (changed) {
            step++;
        }
    }

    @Override
    public void init() {
        step = 0;
        GraphModel gm = Lookup.getDefault().lookup(GraphController.class).getModel();
        edgeWeight = new EdgeWeight(ws);

        graphModel = Lookup.getDefault().lookup(GraphController.class).getModel(ws);

        AttributeController attributeController = Lookup.getDefault().lookup(AttributeController.class);
        AttributeTable nodesTable = attributeController.getModel(ws).getNodeTable();
        if (nodesTable.hasColumn(PREV_POSITION) == false) {
            nodesTable.addColumn(PREV_POSITION, AttributeType.LIST_FLOAT, AttributeOrigin.COMPUTED);
            nodesTable.addColumn(X_VECTOR, AttributeType.LIST_FLOAT, AttributeOrigin.COMPUTED);
            nodesTable.addColumn(Y_VECTOR, AttributeType.LIST_FLOAT, AttributeOrigin.COMPUTED);
        }

        for (Node n : graphModel.getGraph().getNodes()) {
            Random generator = new Random(seed);
            float x = (float) ((0.01 + generator.nextDouble()) * 1000) - 500;
            float y = (float) ((0.01 + generator.nextDouble()) * 1000) - 500;
            n.getNodeData().setX(x);
            n.getNodeData().setY(y);
            n.getAttributes().setValue(PREV_POSITION, new FloatList(new Float[]{x, y}));
            n.getAttributes().setValue(X_VECTOR, new FloatList(new Float[]{}));
            n.getAttributes().setValue(Y_VECTOR, new FloatList(new Float[]{}));
        }
    }

    @Override
    public void end() {
    }
}

package org.gephi.simulation;

import java.io.PrintWriter;
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
public class DirectionSim extends SimModel {

    public final static String NAME = "DirectionSim";
    public final static String RES_VECTOR = "ResultVector";

    private int step;
    private final boolean Scaling = true;
    private final boolean Median = false;
    private final int axisLen = 500;
    
    private GraphModel graphModel;
    private NodeScaler nodeScalerVector;
    private NodeScaler nodeScalerMinMax;
    private PrintWriter writer;
    private final GroupSimilarity gs;
    private Graph forceAtlasGraph;

    public DirectionSim() {
        setName(NAME);
        priority = 2;
        gs = new GroupSimilarity(RES_VECTOR);
    }

    private void computeDirectionVector(Graph g) {
        nodeScalerMinMax.reset();
        for (Node n : g.getNodes()) {
            Node nn = getNode(n);
            if (nn == null) {
                continue;
            }

            FloatList valsX = (FloatList) nn.getAttributes().getValue(ForceAtlas2SIm.X_VECTOR);
            FloatList valsY = (FloatList) nn.getAttributes().getValue(ForceAtlas2SIm.Y_VECTOR);
            warn(valsX != null && valsY != null, "vals is null");
            warn(valsX.size() == valsY.size(), "vals size differ");
            warn(valsX.size() != 0, "Size is zero, got: " + valsX.size() + " step: " + step);

            nodeScalerVector.reset();
            for (int i = 0; i < valsX.size(); i++) {
                nodeScalerVector.update(valsX.getItem(i), valsY.getItem(i));
            }
            Coords c = Median ? nodeScalerVector.getMedian() : 
                                nodeScalerVector.getMean();
            
            n.getAttributes().setValue(RES_VECTOR, new FloatList(new Float[]{c.x, c.y}));
            nodeScalerMinMax.update(c.x, c.y);
        }
    }
    
    private void setDirectionVector(Graph g) {     
        for (Node n : g.getNodes()) {
            FloatList vals = (FloatList) n.getAttributes().getValue(RES_VECTOR);
            float x = vals.getItem(0);
            float y = vals.getItem(1);
            Coords c = Scaling ? nodeScalerMinMax.getScaled(x, y, 1000) : 
                                 nodeScalerMinMax.getNormlized(x, y, 2 * axisLen);


            n.getNodeData().setX(c.x);
            n.getNodeData().setY(c.y);
        }  
    }

    private Node getNode(Node n) {
        Node nn;
        nn = forceAtlasGraph.getNode(n.getNodeData().getId());
        if (nn == null) {
            System.out.println("null node: " + n.getId() + " label: " + n.getNodeData().getLabel());
        }
        return nn;
    }

    @Override
    public synchronized void run(double from, double to, boolean changed) {
        System.err.println("DirectionSim step: " + step);
        if (step++ == 0) {
            return;
        }

        Graph g = graphModel.getGraphVisible();
        computeDirectionVector(g);
        setDirectionVector(g);

         writer.println("from " + from + " to " + to);
         gs.printGroupSimilarity(writer, 1, g);
         gs.printGroupSimilarity(writer, 2, g);
    }

    @Override
    public void init() {
        forceAtlasGraph = Lookup.getDefault().lookup(GraphController.class).getModel().getGraph();
        graphModel = Lookup.getDefault().lookup(GraphController.class).getModel(ws);

        Graph g = graphModel.getGraph();
        g.clearEdges();

        step = 0;
        nodeScalerVector = new NodeScaler(ForceAtlas2SIm.TIME_WINDOW);
        nodeScalerMinMax = new NodeScaler(g.getNodeCount());

        //CreateAxis();
        AttributeController attributeController = Lookup.getDefault().lookup(AttributeController.class);
        AttributeTable nodesTable = attributeController.getModel(ws).getNodeTable();
        if (nodesTable.hasColumn(RES_VECTOR) == false) {
            nodesTable.addColumn(RES_VECTOR, AttributeType.LIST_FLOAT, AttributeOrigin.COMPUTED);
        }

        try {
            writer = new PrintWriter("cosine_sim_" + ForceAtlas2SIm.TIME_WINDOW, "UTF-8");
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void end() {
        writer.close();
    }

    private void warn(boolean exp, String msg) {
        if (!exp) {
            System.err.println("WARNING: DirectionSim: " + msg);
        }
    }

    private void CreateAxis() {
        Graph mainGraph = graphModel.getGraph();
        GraphModel model = graphModel;

        Node origin = mainGraph.getNode("origin");
        if (origin == null) {
            origin = model.factory().newNode("origin");
            origin.getNodeData().setLabel("origin");
            origin.getNodeData().setX(0);
            origin.getNodeData().setY(0);
            mainGraph.addNode(origin);
        }

        Node up = mainGraph.getNode("up");
        if (up == null) {
            up = model.factory().newNode("up");
            up.getNodeData().setLabel("up");
            up.getNodeData().setX(0);
            up.getNodeData().setY(axisLen);
            mainGraph.addNode(up);
        }

        Node left = mainGraph.getNode("left");
        if (left == null) {
            left = model.factory().newNode("left");
            left.getNodeData().setLabel("left");
            left.getNodeData().setX(-axisLen);
            left.getNodeData().setY(0);
            mainGraph.addNode(left);
        }

        Node right = mainGraph.getNode("right");
        if (right == null) {
            right = model.factory().newNode("right");
            right.getNodeData().setLabel("right");
            right.getNodeData().setX(axisLen);
            right.getNodeData().setY(0);
            mainGraph.addNode(right);
        }

        Node down = mainGraph.getNode("down");
        if (down == null) {
            down = model.factory().newNode("down");
            down.getNodeData().setLabel("down");
            down.getNodeData().setX(0);
            down.getNodeData().setY(-axisLen);
            mainGraph.addNode(down);
        }

        if (mainGraph.getEdge(origin, down) == null) {
            mainGraph.addEdge(model.factory().newEdge(origin, down, 5.0f, false));
        }

        if (mainGraph.getEdge(origin, up) == null) {
            mainGraph.addEdge(model.factory().newEdge(origin, up, 5.0f, false));
        }

        if (mainGraph.getEdge(origin, right) == null) {
            mainGraph.addEdge(model.factory().newEdge(origin, right, 5.0f, false));
        }

        if (mainGraph.getEdge(origin, left) == null) {
            mainGraph.addEdge(model.factory().newEdge(origin, left, 5.0f, false));
        }
    }
}

package org.gephi.visualization;

import java.util.HashMap;
import java.util.Hashtable;
import org.gephi.data.attributes.type.FloatList;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

/**
 *
 * @author vlad
 */
public class DirectionSim extends SimModel {

    private GraphModel graphModel;
    private final int axisLen = 700;

    public DirectionSim(Workspace ws) {
        super(ws);
    }

    private Node getNode(Node n, Graph g) {
        Node nn;
        nn = g.getNode(String.valueOf(n.getId()));
        if (nn != null) {
            return nn;
        }

        nn = g.getNode(n.getId());
        if (nn == null) {
            System.out.println("null node: " + n.getId() + " label: " + n.getNodeData().getLabel());
        }

        return nn;
    }

    @Override
    public void run(double from, double to) {
        Graph graph = Lookup.getDefault().lookup(GraphController.class).getModel().getGraph();
        Graph g = graphModel.getGraphVisible();

        float maxX, maxY;
        float minX, minY;

        minX = minY = Float.MAX_VALUE;
        maxX = maxY = Float.MIN_VALUE;

        for (Node n : g.getNodes()) {
            Node nn = getNode(n, graph);
            if (nn == null) {
                continue;
            }

            FloatList vals = (FloatList) nn.getAttributes().getValue("Direction");
            if (vals == null) {
                continue;
            }

            float x = vals.getItem(0);
            float y = vals.getItem(1);

            if (x > maxX) {
                maxX = x;
            } else if (x < minX) {
                minX = x;
            }

            if (y > maxY) {
                maxY = y;
            } else if (y < minY) {
                minY = y;
            }
        }

        for (Node n : g.getNodes()) {
            Node nn = getNode(n, graph);
            if (nn == null) {
                continue;
            }

            FloatList vals = (FloatList) nn.getAttributes().getValue("Direction");
            if (vals == null) {
                continue;
            }

            float x = (axisLen * (vals.getItem(0) - minX)) / (maxX - minX);
            float y = (axisLen * (vals.getItem(1) - minY)) / (maxY - minY);

            n.getNodeData().setX(x);
            n.getNodeData().setY(y);
        }
    }

    @Override
    public void init() {       
        graphModel = Lookup.getDefault().lookup(GraphController.class).getModel(ws);
        graphModel.getGraph().clearEdges();
        
        //CreateAxis();
    }

    @Override
    public void end() {
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

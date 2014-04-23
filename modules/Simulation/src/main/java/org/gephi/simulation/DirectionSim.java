package org.gephi.simulation;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.type.FloatList;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.ProjectInformation;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = SimModel.class)
public class DirectionSim extends SimModel {

    public boolean Scaling = true;
    public boolean Median = false;
    private final int axisLen = 1000;

    public final static String RES_VECTOR = "ResultVector";
    private int step;
    private GraphModel graphModel;
    private final MaxMinResults mmr;
    private final Float[] xs;
    private final Float[] ys;
    private final ArrayList<Node> sameGroup;
    private final ArrayList<Node> otherGroup;
    private final ArrayList<Double> cosineSimResults;
    private PrintWriter writer;

    private void warn(boolean exp, String msg) {
        if (!exp) {
            System.err.println("WARNING: " + msg);
        }
    }

    private class MaxMinResults {

        public float maxX;
        public float minX;
        public float maxY;
        public float minY;

        public void reset() {
            minX = minY = Float.MAX_VALUE;
            maxX = maxY = Float.MIN_VALUE;
        }

        public void updateX(float x) {
            maxX = Math.max(x, maxX);
            minX = Math.min(x, minX);
        }

        public void updateY(float y) {
            maxY = Math.max(y, maxY);
            minY = Math.min(y, minY);
        }
    }

    public DirectionSim() {
        setName("DirectionSim");
        priority = 2;
        mmr = new MaxMinResults();
        xs = new Float[100];
        ys = new Float[100];
        sameGroup = new ArrayList<Node>();
        otherGroup = new ArrayList<Node>();
        cosineSimResults = new ArrayList<Double>();
    }

    private void computeDirectionVector(Graph g, Graph graph, MaxMinResults mmr) {
        mmr.reset();
        for (Node n : g.getNodes()) {
            Node nn = getNode(n, graph);
            if (nn == null) {
                continue;
            }

            FloatList valsX = (FloatList) nn.getAttributes().getValue(ForceAtlas2SIm.X_VECTOR);
            FloatList valsY = (FloatList) nn.getAttributes().getValue(ForceAtlas2SIm.Y_VECTOR);

            warn(valsX != null && valsY != null, "vals is null");
            warn(valsX.size() == valsY.size(), "vals size differ");
            int size = valsX.size();
            warn(size != 0, "Size is zero, got: " + size + " step: " + step);

            float x = 0;
            float y = 0;

            if (Median) {
                for (int i = 0; i < size; i++) {
                    xs[i] = valsX.getItem(i);
                    ys[i] = valsY.getItem(i);
                }
                Arrays.sort(xs, 0, size);
                Arrays.sort(ys, 0, size);

                if (size % 2 == 0) {
                    x = (xs[size / 2] + xs[size / 2 - 1]) / 2;
                    y = (ys[size / 2] + ys[size / 2 - 1]) / 2;
                } else {
                    x = xs[size / 2];
                    y = ys[size / 2];
                }
            } else {
                for (int i = 0; i < size; i++) {
                    x += valsX.getItem(i);
                    y += valsY.getItem(i);
                }

                x /= size;
                y /= size;
            }

            n.getAttributes().setValue(RES_VECTOR, new FloatList(new Float[]{x, y}));
            mmr.updateX(x);
            mmr.updateY(y);
        }
    }

    private Node getNode(Node n, Graph g) {
        Node nn;
        nn = g.getNode(n.getNodeData().getId());
        if (nn == null) {
            System.out.println("null node: " + n.getId() + " label: " + n.getNodeData().getLabel());
        }

        return nn;
    }

    @Override
    public void run(double from, double to) {
        System.err.println("DirectionSim step: " + step);
        step++;
        if (step == 1) {
            return;
        }

        Graph graph = Lookup.getDefault().lookup(GraphController.class).getModel().getGraph();
        Graph g = graphModel.getGraphVisible();

        computeDirectionVector(g, graph, mmr);

        for (Node n : g.getNodes()) {
            Node nn = getNode(n, graph);
            if (nn == null) {
                continue;
            }

            FloatList vals = (FloatList) n.getAttributes().getValue(RES_VECTOR);

            warn(vals != null, "vals is null");
            warn(vals.size() == 2, "vals size != 2");
            float x = vals.getItem(0);
            float y = vals.getItem(1);

            if (Scaling) {
                x = (2 * axisLen * x) / (mmr.maxX - mmr.minX);
                y = (2 * axisLen * y) / (mmr.maxY - mmr.minY);
            } else {
                double mag = Math.sqrt(x * x + y * y);
                if (mag != 0) {
                    x /= mag;
                    y /= mag;
                    x *= axisLen;
                    y *= axisLen;
                }
            }

            n.getNodeData().setX(x);
            n.getNodeData().setY(y);
        }

        writer.println("from " + from + " to " + to);
        printGroupSimilarity(1, g);
        printGroupSimilarity(2, g);
    }

    private void printGroupSimilarity(int group, Graph graph) {
        double gs;
        writer.print(group);
        gs = getGroupSimilarity(group, graph, false);
        writer.print(" " + gs);
        writer.print(" " + getStdDev(gs));
        gs = getGroupSimilarity(group, graph, true);
        writer.print(" " + gs);
        writer.print(" " + getStdDev(gs));
        writer.println();
    }

    private FloatList getDirectionVector(Node n) {
        FloatList vals = (FloatList) n.getAttributes().getValue(RES_VECTOR);
        warn(vals != null, "vals is null node: " + n);
        warn(vals.size() == 2, "vals size < 2: got: " + vals.size() + " node: " + n);
        return vals;
    }

    private double getStdDev(double groupSimilarity) {
        double sum = 0;
        for (Double d : cosineSimResults) {
            sum += Math.pow(d - groupSimilarity, 2);
        }
        return Math.sqrt(sum / cosineSimResults.size());
    }

    private double getGroupSimilarity(int group, Graph graph, boolean betweenGroups) {

        sameGroup.clear();
        otherGroup.clear();
        cosineSimResults.clear();
        double groupSimilarity = 0;
        int count = 0;

        for (Node n : graph.getNodes()) {
            Integer g = (Integer) n.getAttributes().getValue("Group");
            if (g == null) {
                continue;
            }
            if (g == group) {
                sameGroup.add(n);
            } else {
                otherGroup.add(n);
            }
        }

        if (betweenGroups) {
            for (Node n1 : sameGroup) {
                FloatList vals1 = getDirectionVector(n1);
                for (Node n2 : otherGroup) {
                    FloatList vals2 = getDirectionVector(n2);
                    count++;
                    double cs = CosineSimilarity.similarity(vals1, vals2);
                    cosineSimResults.add(cs);
                    groupSimilarity += cs;
                }
            }
        } else {
            for (int i = 0; i < sameGroup.size(); i++) {
                FloatList vals1 = getDirectionVector(sameGroup.get(i));
                for (int j = i + 1; j < sameGroup.size(); j++) {
                    FloatList vals2 = getDirectionVector(sameGroup.get(j));
                    count++;
                    double cs = CosineSimilarity.similarity(vals1, vals2);
                    cosineSimResults.add(cs);
                    groupSimilarity += cs;
                }
            }
        }

        warn(count != 0, "Count is zero");
        return groupSimilarity / count;
    }

    @Override
    public void init() {

        graphModel = Lookup.getDefault().lookup(GraphController.class).getModel(ws);
        graphModel.getGraph().clearEdges();
        step = 0;

        //CreateAxis();
        AttributeController attributeController = Lookup.getDefault().lookup(AttributeController.class);
        AttributeTable nodesTable = attributeController.getModel(ws).getNodeTable();
        if (nodesTable.hasColumn(RES_VECTOR) == false) {
            nodesTable.addColumn(RES_VECTOR, AttributeType.LIST_FLOAT, AttributeOrigin.COMPUTED);
        }

        for (Node n : graphModel.getGraph().getNodes()) {
            n.getAttributes().setValue(RES_VECTOR, new FloatList(new Float[]{}));
        }
        try {
            ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
            Project currentProject = pc.getCurrentProject();
            String fname = pc.getCurrentProject().getLookup().lookup(ProjectInformation.class).getFileName();
            if (fname.equals("")) {
                fname = "cosine_sim";
            }
            writer = new PrintWriter(fname + "_" + ForceAtlas2SIm.TIME_WINDOW, "UTF-8");
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (UnsupportedEncodingException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void end() {
        writer.close();
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

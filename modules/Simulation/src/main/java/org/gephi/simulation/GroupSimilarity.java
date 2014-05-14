package org.gephi.simulation;

import java.io.PrintWriter;
import java.util.ArrayList;
import org.gephi.data.attributes.type.FloatList;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;

public class GroupSimilarity {

    private final String columnName;
    private final ArrayList<Node> sameGroup;
    private final ArrayList<Node> otherGroup;
    private final ArrayList<Double> cosineSimResults;

    public GroupSimilarity(String columnName) {
        this.columnName = columnName;
        sameGroup = new ArrayList<Node>();
        otherGroup = new ArrayList<Node>();
        cosineSimResults = new ArrayList<Double>();
    }

    public void printGroupSimilarity(PrintWriter writer, int group, Graph graph) {
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

    public double getStdDev(double groupSimilarity) {
        double sum = 0;
        for (Double d : cosineSimResults) {
            sum += Math.pow(d - groupSimilarity, 2);
        }
        return Math.sqrt(sum / cosineSimResults.size());
    }

    public double getGroupSimilarity(int group, Graph graph, boolean betweenGroups) {

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
                    
                    double cs = CosineSimilarity.similarity(vals1, vals2);
                    if (!Double.isNaN(cs)) {
                        cosineSimResults.add(cs);
                        groupSimilarity += cs;
                        count++;
                    } else {
                        warn(false, "CosineSimilarity betweenGroups is Nan: " + n1.getId() + "[ " + vals1 + " ] " + n2.getId() + "[ " + vals2 + " ]");
                    }
                }
            }
        } else {
            for (int i = 0; i < sameGroup.size(); i++) {
                Node n1 = sameGroup.get(i);
                FloatList vals1 = getDirectionVector(n1);
                for (int j = i + 1; j < sameGroup.size(); j++) {
                    Node n2 = sameGroup.get(j);
                    FloatList vals2 = getDirectionVector(n2);

                    double cs = CosineSimilarity.similarity(vals1, vals2);
                    if (!Double.isNaN(cs)) {
                        cosineSimResults.add(cs);
                        groupSimilarity += cs;
                        count++;
                    } else {
                        warn(false, "CosineSimilarity innerGroup is Nan: " + n1.getId() + "[ " + vals1 + " ] " + n2.getId() + "[ " + vals2 + " ]");
                    }
                }
            }
        }

        warn(count != 0, "Count is zero");
        return groupSimilarity / count;
    }

    private FloatList getDirectionVector(Node n) {
        FloatList vals = (FloatList) n.getAttributes().getValue(columnName);
        warn(vals != null, "vals is null node: " + n);
        warn(vals.size() == 2, "vals size < 2: got: " + vals.size() + " node: " + n);
        return vals;
    }

    private void warn(boolean exp, String msg) {
        if (!exp) {
            System.err.println("WARNING: GroupSimilarity: " + msg);
        }
    }

}

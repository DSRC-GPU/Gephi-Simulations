
package org.gephi.simulation;

import java.io.PrintWriter;
import java.util.ArrayList;
import org.gephi.data.attributes.type.FloatList;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import static org.gephi.simulation.DirectionSim.RES_VECTOR;


public class GroupSimilarity {
    
    private final ArrayList<Node> sameGroup;
    private final ArrayList<Node> otherGroup;
    private final ArrayList<Double> cosineSimResults;
      
    public GroupSimilarity() {
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
                    }
                }
            }
        } else {
            for (int i = 0; i < sameGroup.size(); i++) {
                FloatList vals1 = getDirectionVector(sameGroup.get(i));
                for (int j = i + 1; j < sameGroup.size(); j++) {
                    FloatList vals2 = getDirectionVector(sameGroup.get(j));

                    double cs = CosineSimilarity.similarity(vals1, vals2);
                    if (!Double.isNaN(cs)) {
                        cosineSimResults.add(cs);
                        groupSimilarity += cs;
                        count++;
                    }
                }
            }
        }

        warn(count != 0, "Count is zero");
        return groupSimilarity / count;
    }
    
     private FloatList getDirectionVector(Node n) {
        FloatList vals = (FloatList) n.getAttributes().getValue(RES_VECTOR);
        warn(vals != null, "vals is null node: " + n);
        warn(vals.size() == 2, "vals size < 2: got: " + vals.size() + " node: " + n);
        return vals;
    }
     
      private void warn(boolean exp, String msg) {
        if (!exp) {
            System.err.println("WARNING: " + msg);
        }
    }
    
}

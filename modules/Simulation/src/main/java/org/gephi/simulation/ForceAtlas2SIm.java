package org.gephi.simulation;

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

/**
 *
 * @author vlad
 */
@ServiceProvider(service = SimModel.class)
public class ForceAtlas2SIm extends SimModel {

    private GraphModel graphModel;
    private final ForceAtlas2 layout;

    public ForceAtlas2SIm() {
        super();
        setName("ForceAtlas2Sim");
        layout = new ForceAtlas2(null);
    }

    @Override
    public void setWorkspace(Workspace ws) {
        super.setWorkspace(ws);
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.openWorkspace(ws);
    }

    @Override
    public void run(double from, double to) {
        layout.setGraphModel(graphModel);
        layout.initAlgo();

        for (int i = 0; i < 100 && layout.canAlgo(); i++) {
            layout.goAlgo();
        }

        layout.endAlgo();

        for (Node n : graphModel.getGraphVisible().getNodes()) {
            FloatList vals = (FloatList) n.getAttributes().getValue("Direction");
            FloatList pos = (FloatList) n.getAttributes().getValue("Position");
            if (vals == null) {
                continue;
            }
            float x = vals.getItem(0) + n.getNodeData().x() - pos.getItem(0);
            float y = vals.getItem(1) + n.getNodeData().y() - pos.getItem(1);
            n.getAttributes().setValue("Direction", new FloatList(new Float[]{x, y}));
        }
    }

    @Override
    public void init() {
        GraphModel gm = Lookup.getDefault().lookup(GraphController.class).getModel();

        graphModel = Lookup.getDefault().lookup(GraphController.class).getModel(ws);

        AttributeController attributeController = Lookup.getDefault().lookup(AttributeController.class);
        AttributeTable nodesTable = attributeController.getModel(ws).getNodeTable();
        if (nodesTable.hasColumn("Direction") == false) {
            nodesTable.addColumn("Direction", AttributeType.LIST_FLOAT, AttributeOrigin.COMPUTED);
            nodesTable.addColumn("Position", AttributeType.LIST_FLOAT, AttributeOrigin.COMPUTED);
        }

        for (Node n : graphModel.getGraph().getNodes()) {
            n.getNodeData().setX((float) ((0.01 + Math.random()) * 1000) - 500);
            n.getNodeData().setY((float) ((0.01 + Math.random()) * 1000) - 500);
            n.getAttributes().setValue("Direction", new FloatList(new Float[]{0f, 0f}));
            n.getAttributes().setValue("Position", new FloatList(new Float[]{n.getNodeData().x(), n.getNodeData().y()}));
        }
    }

    @Override
    public void end() {
    }

}

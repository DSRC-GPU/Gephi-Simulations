/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.visualization;

import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

/**
 *
 * @author vlad
 */
public class ForceAtlas2SIm extends SimModel {

    private GraphModel graphModel;
    private ForceAtlas2 layout;

    public ForceAtlas2SIm(Workspace ws) {
        super(ws);
        layout = new ForceAtlas2(null);
    }

    @Override
    public void run(double from, double to) {
        graphModel = Lookup.getDefault().lookup(GraphController.class).getModel(ws);
        layout.setGraphModel(graphModel);
        layout.initAlgo();

        for (int i = 0; i < 100 && layout.canAlgo(); i++) {
            layout.goAlgo();
        }

        layout.endAlgo();
    }

    @Override
    public void init() {
    }

    @Override
    public void end() {
    }

}

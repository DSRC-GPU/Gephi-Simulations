package org.gephi.visualization;

import org.gephi.project.api.Workspace;

/**
 *
 * @author vlad
 */
public abstract class SimModel {
    
    protected Workspace ws;

    public SimModel(Workspace ws) {
        this.ws = ws;
    }

    public abstract void run(double from, double to);

    public abstract void init();

    public abstract void end();
}

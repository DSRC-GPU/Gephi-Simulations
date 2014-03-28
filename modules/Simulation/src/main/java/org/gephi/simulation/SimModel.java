package org.gephi.simulation;

import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

/**
 *
 * @author vlad
 */
public abstract class SimModel {
    
    protected Workspace ws;
    protected String name;
    
    public SimModel() {
    }
    
    public void setWorkspace(Workspace ws) {
        ws.add(this);
        this.ws = ws;
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        if (name != null) {
            pc.renameWorkspace(ws, name);
        }
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
    
    public abstract void run(double from, double to);
    
    public abstract void init();
    
    public abstract void end();
}

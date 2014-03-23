/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
*/
package org.gephi.visualization.opengl.compatibility.objects;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import org.gephi.graph.api.NodeData;
import org.gephi.visualization.VizController;
import org.gephi.visualization.VizModel;
import org.gephi.visualization.apiimpl.GraphDrawable;
import org.gephi.visualization.apiimpl.ModelImpl;
import org.gephi.lib.gleem.linalg.Vecf;
import org.gephi.visualization.opengl.octree.Octant;

/**
 *
 * @author Mathieu Bastian
 */
public class NodeRectangeModel extends ModelImpl<NodeData> {

    public boolean border = true;
    protected float width = 20f;
    protected float height = 20f;
    private VizController vizController;

    public NodeRectangeModel(VizController vizController) {
        this.vizController = vizController;
        octants = new Octant[1];
    }

    @Override
    public int[] octreePosition(float centerX, float centerY, float centerZ, float size) {
        //float radius = obj.getRadius();
        int index = 0;

        if (obj.y() < centerY) {
            index += 4;
        }
        if (obj.z() > centerZ) {
            index += 2;
        }
        if (obj.x() < centerX) {
            index += 1;
        }

        return new int[]{index};
    }

    @Override
    public boolean isInOctreeLeaf(Octant leaf) {
        if (Math.abs(obj.x() - leaf.getPosX()) > (leaf.getSize() / 2 - obj.getRadius())
                || Math.abs(obj.y() - leaf.getPosY()) > (leaf.getSize() / 2 - obj.getRadius())
                || Math.abs(obj.z() - leaf.getPosZ()) > (leaf.getSize() / 2 - obj.getRadius())) {
            return false;
        }
        return true;
    }

    @Override
    public void display(GL gl, GLU glu, VizModel vizModel) {
        boolean selec = selected;
        boolean neighbor = false;
        highlight = false;
        if (vizModel.isAutoSelectNeighbor() && mark && !selec) {
            selec = true;
            highlight = true;
            neighbor = true;
        }
        mark = false;
        if (vizModel.isAdjustByText()) {
            width = obj.getTextData().getWidth();
            height = obj.getTextData().getHeight();
        } else {
            float size = obj.getSize();
            width = size;
            height = size;
        }

        float w = width / 2f;
        float h = height / 2f;
        float x = obj.x();
        float y = obj.y();

        float borderSize = 1f;

        if (!selec) {
            if (vizModel.getConfig().isLightenNonSelected()) {
                float[] lightColor = vizModel.getConfig().getLightenNonSelectedColor();
                float lightColorFactor = vizModel.getConfig().getLightenNonSelectedFactor();
                float r = obj.r();
                float g = obj.g();
                float b = obj.b();
                if (border) {
                    float rborder = 0.498f * r;
                    float gborder = 0.498f * g;
                    float bborder = 0.498f * b;
                    gl.glColor3f(rborder + (lightColor[0] - rborder) * lightColorFactor, gborder + (lightColor[1] - gborder) * lightColorFactor, bborder + (lightColor[2] - bborder) * lightColorFactor);
                    gl.glVertex3f(x + w, y + h, 0);
                    gl.glVertex3f(x - w, y + h, 0);
                    gl.glVertex3f(x - w, y - h, 0);
                    gl.glVertex3f(x + w, y - h, 0);
                    w -= borderSize;
                    h -= borderSize;
                }
                gl.glColor3f(r + (lightColor[0] - r) * lightColorFactor, g + (lightColor[1] - g) * lightColorFactor, b + (lightColor[2] - b) * lightColorFactor);
            } else {
                float r = obj.r();
                float g = obj.g();
                float b = obj.b();
                if (border) {
                    float rborder = 0.498f * r;
                    float gborder = 0.498f * g;
                    float bborder = 0.498f * b;
                    gl.glColor3f(rborder, gborder, bborder);
                    gl.glVertex3f(x + w, y + h, 0);
                    gl.glVertex3f(x - w, y + h, 0);
                    gl.glVertex3f(x - w, y - h, 0);
                    gl.glVertex3f(x + w, y - h, 0);
                    w -= borderSize;
                    h -= borderSize;
                }
                gl.glColor3f(r, g, b);
            }
        } else {
            float r;
            float g;
            float b;
            float rborder;
            float gborder;
            float bborder;
            if (vizModel.isUniColorSelected()) {
                if (neighbor) {
                    r = vizModel.getConfig().getUniColorSelectedNeigborColor()[0];
                    g = vizModel.getConfig().getUniColorSelectedNeigborColor()[1];
                    b = vizModel.getConfig().getUniColorSelectedNeigborColor()[2];
                } else {
                    r = vizModel.getConfig().getUniColorSelectedColor()[0];
                    g = vizModel.getConfig().getUniColorSelectedColor()[1];
                    b = vizModel.getConfig().getUniColorSelectedColor()[2];
                }
                rborder = 0.498f * r;
                gborder = 0.498f * g;
                bborder = 0.498f * b;
            } else {
                rborder = obj.r();
                gborder = obj.g();
                bborder = obj.b();
                r = Math.min(1, 0.5f * rborder + 0.5f);
                g = Math.min(1, 0.5f * gborder + 0.5f);
                b = Math.min(1, 0.5f * bborder + 0.5f);
            }
            if (border) {
                gl.glColor3f(rborder, gborder, bborder);
                gl.glVertex3f(x + w, y + h, 0);
                gl.glVertex3f(x - w, y + h, 0);
                gl.glVertex3f(x - w, y - h, 0);
                gl.glVertex3f(x + w, y - h, 0);
                w -= borderSize;
                h -= borderSize;
            }
            gl.glColor3f(r, g, b);
        }

        gl.glVertex3f(x + w, y + h, 0);
        gl.glVertex3f(x - w, y + h, 0);
        gl.glVertex3f(x - w, y - h, 0);
        gl.glVertex3f(x + w, y - h, 0);
    }

    @Override
    public boolean selectionTest(Vecf distanceFromMouse, float selectionSize) {
        GraphDrawable drawable = vizController.getDrawable();
        if (distanceFromMouse.get(0) < width / 2 * Math.abs(drawable.getDraggingMarkerX()) && distanceFromMouse.get(1) < height / 2 * Math.abs(drawable.getDraggingMarkerY())) {
            return true;
        }
        return false;
    }

    @Override
    public float getCollisionDistance(double angle) {
        double angleSinus = Math.sin(angle);
        double angleCosinus = Math.cos(angle);
        angle %= Math.PI * 2;
        while (angle < 0) {
            angle += Math.PI * 2;
        }

        if (angle < Math.atan2(height / 2, width / 2)
                || (angle > Math.PI - Math.atan2(height / 2, width / 2) && angle < Math.PI + Math.atan2(height / 2, width / 2))
                || angle > 2 * Math.PI - Math.atan2(height / 2, width / 2)) {
            return (float) Math.sqrt((width * width / 4) / (1 - angleSinus * angleSinus));
        } else {
            return (float) Math.sqrt((height * height / 4) / (1 - angleCosinus * angleCosinus));
        }
    }

    @Override
    public String toSVG() {
        return null;
    }
}

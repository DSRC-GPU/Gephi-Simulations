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
package org.gephi.visualization.opengl.text;

import org.gephi.visualization.impl.TextDataImpl;
import javax.swing.ImageIcon;
import org.gephi.visualization.VizController;
import org.gephi.visualization.apiimpl.GraphDrawable;
import org.gephi.visualization.apiimpl.ModelImpl;

/**
 *
 * @author Mathieu Bastian
 */
public class FixedSizeMode implements SizeMode {

    //private static float FACTOR_3D = 800f;
    private GraphDrawable drawable;
    private VizController vizController;
    
   public FixedSizeMode(VizController vizController) {
       this.vizController = vizController;
   }

    public void init() {
        drawable = vizController.getDrawable();
    }

    public void setSizeFactor2d(float sizeFactor, TextDataImpl text, ModelImpl model) {
        float factor = sizeFactor * 1.9f + 0.1f;        //Between 0.1 and 2
        factor *= text.getSize();
        text.setSizeFactor(factor);
    }

    public void setSizeFactor3d(float sizeFactor, TextDataImpl text, ModelImpl model) {
        float factor = sizeFactor / drawable.getViewportWidth() * model.getCameraDistance();
        factor *= text.getSize();
        text.setSizeFactor(factor);
    }

    public String getName() {
        return "Fixed";
    }

    public ImageIcon getIcon() {
        return new ImageIcon(getClass().getResource("/org/gephi/visualization/opengl/text/FixedSizeMode.png"));
    }

    @Override
    public String toString() {
        return getName();
    }
}

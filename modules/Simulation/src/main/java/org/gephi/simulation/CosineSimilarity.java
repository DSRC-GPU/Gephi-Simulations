package org.gephi.simulation;

import org.gephi.data.attributes.type.FloatList;


public class CosineSimilarity {
    
    public static double similarity(FloatList l1, FloatList l2) 
    {
        float x1, x2, y1, y2;
        
        x1 = l1.getItem(0);
        x2 = l2.getItem(0);
        y1 = l1.getItem(1);
        y2 = l2.getItem(1);
        
        double mag1 = Math.sqrt(x1 * x1 + y1 * y1);
        double mag2 = Math.sqrt(x2 * x2 + y2 * y2);
        
        if (mag1 == 0) {
            System.err.println("mag1 is zero");
            return Double.NaN;
        }
        
        if (mag2 == 0) {
            System.err.println("mag2 is zero");
            return Double.NaN;
        }
        
        return (x1 * x2 + y1 * y2) / (mag1 * mag2);
    }
}

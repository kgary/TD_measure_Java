package teamDynamics;

import java.util.*;

public class InfluenceProcessor {

    public static Map<String, Double> processCommunicationData(Integer[][] communicationData) {
        
        Map<String, Double> results = new LinkedHashMap<>();

        if (communicationData == null || communicationData.length == 0) {
            System.out.println("Processing influence data for 0 time points. Returning empty results.");
            return results;
        }
        
        try {
            double[][] x_xyz = new double[communicationData.length][2];
            double[][] y_xyz = new double[communicationData.length][2];
            double[][] z_xyz = new double[communicationData.length][2];
            
            for (int i = 0; i < communicationData.length; i++) {
                int x = communicationData[i][0];
                int y = communicationData[i][1];
                int z = communicationData[i][2];
                
                
                int xyz = Integer.parseInt("" + x + y + z);
                
                x_xyz[i][0] = x;
                x_xyz[i][1] = xyz;
                
                y_xyz[i][0] = y;
                y_xyz[i][1] = xyz;
                
                z_xyz[i][0] = z;
                z_xyz[i][1] = xyz;
            }
            
            
            int[][] nBins = {{10}};
            int nLags = 0;
            
            
            Object[] result_x = ami.ami(x_xyz, nBins, nLags);
            Object[] result_y = ami.ami(y_xyz, nBins, nLags);
            Object[] result_z = ami.ami(z_xyz, nBins, nLags);
            
            
            double influence_x = 0.0;
            double influence_y = 0.0;
            double influence_z = 0.0;
            
            if (result_x != null) {
                double[][] ami_x = (double[][]) result_x[0];
                influence_x = ami_x[0][0];
            }
            
            if (result_y != null) {
                double[][] ami_y = (double[][]) result_y[0];
                influence_y = ami_y[0][0];
            }
            
            if (result_z != null) {
                double[][] ami_z = (double[][]) result_z[0];
                influence_z = ami_z[0][0];
            }
            
          
            results.put("trainee1", influence_x);
            results.put("trainee2", influence_y);
            results.put("trainee3", influence_z);
            
          
            System.out.println("Trainee1 Influence: " + influence_x);
            System.out.println("Trainee2 Influence: " + influence_y);
            System.out.println("Trainee3 Influence: " + influence_z);
            
            return results;
            
        } catch (Exception e) {
            System.err.println("Error processing influence data: " + e.getMessage());
            e.printStackTrace();
            return results; // Return whatever results were collected, or an empty map on failure.
        }
    }
    
}
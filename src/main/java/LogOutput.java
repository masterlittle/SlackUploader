
import hudson.model.BuildListener;
import java.io.IOException;
import org.apache.commons.io.IOUtils;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Shitij
 */
public class LogOutput {
    
    public void logOutput(BuildListener listener, Process process) throws IOException {
        
        if (process != null ){
        String output = IOUtils.toString(process.getInputStream());
        String errorOutput = IOUtils.toString(process.getErrorStream());
        
        logToJvm(output, errorOutput);
            
        logToConsole(listener, output, errorOutput);
        }
        
    }

    private static void logToConsole(BuildListener listener, String output, String errorOutput) {
        listener.getLogger().print(output);
        listener.getLogger().print(errorOutput);
    }

    private static void logToJvm(String output, String errorOutput) {
        System.out.println("Input Stream: " + output);
        System.out.println("Error Stream: " + errorOutput);
    }
    
}

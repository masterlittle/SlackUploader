/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.FreeStyleProject;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import hudson.util.StreamTaskListener;
import hudson.views.ViewsTabBar;
import hudson.views.ViewsTabBarDescriptor;
import java.io.IOException;
import javax.servlet.ServletException;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 *
 * @author Shitij
 */
public class SlackBuilder extends Recorder {
   
    private final String channel;
    private final String token;
    private final String filePath;
    
    @DataBoundConstructor
    public SlackBuilder(String channel, String token, String filePath) {
        this.channel = channel;
        this.token = token;
        this.filePath = filePath;
        System.out.println("Entry");
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        //To change body of generated methods, choose Tools | Templates.
        
        Runtime runtime = Runtime.getRuntime();

        try {
            String script = generateScript();
            
            Process process = runtime.exec(new String[]{"/bin/bash", "-c", script});
            int resultCode = process.waitFor();
            String output = IOUtils.toString(process.getInputStream());
            String errorOutput = IOUtils.toString(process.getErrorStream());
            System.out.println("is: " + output);
            System.out.println("es: " + errorOutput);
            
            listener.getLogger().print(output);
            listener.getLogger().print(errorOutput);
            
//            if (resultCode == 0) {
                // all is good
//            } 
        } catch (Throwable cause) {
    // process cause
            System.out.println(cause.getCause() + "\n");
            System.out.println(cause.getMessage());
        }
        return true;
    }

    private String generateScript() {
        String loop = "for file in $(ls " + filePath + ");";
        loop+="do ";
        String curlRequest = loop + "curl -F file=@$file -F channels=" + channel +" -F token=" + token + " https://slack.com/api/files.upload ;";
        String loopDone = curlRequest + "done;";
        return loopDone;
    }

    @Override
    public BuildStepDescriptor getDescriptor() {
        return (BuildDescriptor)super.getDescriptor(); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    @Extension
    public static final class BuildDescriptor extends BuildStepDescriptor<Publisher> {
        
        private String channel;
        private String token;
        private String path;
        
        public BuildDescriptor(){
            load();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return FreeStyleProject.class.isAssignableFrom(jobType);
        }

        @Override
        public String getDisplayName() {
            System.out.println("Middle");
            return "Post files to Slack";
        }

        @Override
        public void doHelp(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
            super.doHelp(req, rsp); //To change body of generated methods, choose Tools | Templates.
        }
        
        public FormValidation doCheckChannel(@QueryParameter String channel) {
            if (channel.length() == 0) {
                return FormValidation.error("Cannot be empty");
            }
            for (int i = 0; i< channel.length(); i++) {
                if (channel.charAt(i) == ',' && channel.charAt(i+1) !='#') {
                    return FormValidation.error("Channels should be specified wihtout anything between comma. eg - #ch1,#ch2,#ch3");
                }
            }
            return FormValidation.ok();
        }
        
        public FormValidation doCheckFilePath(@QueryParameter String filePath) {
            if (filePath.length() == 0) {
                return FormValidation.error("Cannot be empty");
            }
            return FormValidation.ok();
        }
        
        public FormValidation doCheckToken (@QueryParameter String token) {
            if (token.length() == 0) {
                return FormValidation.error("Cannot be empty");
            }
            return FormValidation.ok();
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
            channel = json.getString("channel");
            token = json.getString("token");
            path = json.getString("fielPath");
            
            save();
            return super.configure(req, json);
        }

        public String getChannel() {
            return channel;
        }

        public String getToken() {
            return token;
        }

        public String getPath() {
            return path;
        }
        
    }
}

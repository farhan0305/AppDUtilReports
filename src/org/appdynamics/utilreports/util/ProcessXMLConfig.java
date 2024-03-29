/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.appdynamics.utilreports.util;

import org.appdynamics.utilreports.conf.*;
import org.appdynamics.utilreports.files.ProcessExcelFile;
import org.appdynamics.utilreports.resources.AppDUtilReportS;

import org.appdynamics.appdrestapi.RESTAccess;
import org.appdynamics.crypto.*;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.ArrayList;

/**
 *
 * @author gilbert.solorzano
 * 
 * This is going to get the AppHCReport and process it.
 * 
 * First thing is to get the server information.
 * 
 */
public class ProcessXMLConfig {
    private static Logger logger=Logger.getLogger(ProcessXMLConfig.class.getName());
    private AppDHCReport appDHC;
    private RESTAccess access;
    private ArrayList<GatherLoadCheck> loadChecks=new ArrayList<GatherLoadCheck>();
    
    public ProcessXMLConfig(AppDHCReport appDHC){
        this.appDHC=appDHC;
    }
    
    public void init() throws Exception{
        //First stop is to get a connection;
        StringLogger sl = CryptoTool.getStringLogger();
        
        String pwd=sl.toLower1(sl.format1(appDHC.getServerConfig().getAccount().getPasswd()));
        ServerConfig srv=appDHC.getServerConfig();
        access = new RESTAccess(srv.getController().getFqdn(),srv.getController().getPort(),
                srv.getController().isUseSSL(),srv.getAccount().getUser(),pwd,srv.getAccount().getAccount());
        logger.log(Level.INFO,"Parsing XML objects");
        for(Load_Check_Set hcSet: appDHC.getHcLoadChecks().getLoadCheckSet()){
            GatherLoadCheck gc = new GatherLoadCheck(access,hcSet);
            gc.init();
            loadChecks.add(gc);
        }

        processOutputs();
    }
    
    public String printData(){
        StringBuilder msg=new StringBuilder();
        for(GatherLoadCheck lc: loadChecks) msg.append("\n\n").append(lc.printData());
        return msg.toString();
    }
    

    public void processOutputs(){
        Outputs outs=appDHC.getHcLoadChecks().getOutputs();
        for(Output out:outs.getOutpus()){
            
            if(out.getType().equals(AppDUtilReportS.OUTPUT_TYPE_STDOUT) 
                    && out.getFormat().equals(AppDUtilReportS.OUTPUT_FORMAT_PIPEDELIMITED)) logger.log(Level.INFO,printData());
            //logger.log(Level.INFO,"\n\nCreating EXCEL sheet");
            if(out.getType().equals(AppDUtilReportS.OUTPUT_TYPE_FILE) && out.getFormat().equals(AppDUtilReportS.OUTPUT_FORMAT_EXCEL)){
                //This is when we are going to write the file.
                ProcessExcelFile excelFile = new ProcessExcelFile(loadChecks, out.getFile());
                excelFile.init();
            }
        }
    }
    
    
    
}

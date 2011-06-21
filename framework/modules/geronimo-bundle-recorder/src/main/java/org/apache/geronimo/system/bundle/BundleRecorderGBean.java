package org.apache.geronimo.system.bundle;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.util.FileUtils;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.xbean.osgi.bundle.util.BundleUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.startlevel.StartLevel;
import org.osgi.util.tracker.BundleTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GBean
public class BundleRecorderGBean implements BundleRecorder, GBeanLifecycle{
    
    private static final Logger log = LoggerFactory.getLogger(BundleRecorderGBean.class);
    // A BundleRecord list maintained in runtime
    private List<BundleRecord> bundleRecords = new ArrayList<BundleRecord>();
    
    private BundleContext bundleContext;
    StartLevel startLevelService;
    int defaultBundleStartLevel;
        
    private File recordFile;
    private File recordDir;
    
    private BundleTracker uninstallBundleTracker;
    
    public BundleRecorderGBean(@ParamSpecial(type = SpecialAttributeType.kernel) Kernel kernel,
                                @ParamSpecial(type = SpecialAttributeType.bundle) Bundle bundle,
                                @ParamReference(name = "ServerInfo") final ServerInfo serverInfo,
                                @ParamAttribute(name = "bundleRecordsFile") String bundleRecordsFile) throws DeploymentException, IOException {
        
        bundleContext = bundle.getBundleContext();
        startLevelService = getStartLevelService(bundleContext);
        defaultBundleStartLevel = startLevelService.getInitialBundleStartLevel();
        
        recordDir = serverInfo.resolveServer("repository/recorded-bundles/");
        if (!recordDir.exists() && !recordDir.mkdirs()) {
            throw new DeploymentException("Could not create directory for Bundle Recorder " + recordDir);
        }
        
        recordFile = serverInfo.resolveServer(bundleRecordsFile);
        if(!recordFile.exists() || !recordFile.isFile() || !recordFile.canRead()) {
            throw new IllegalArgumentException("File does not exist or not a normal file or not readable. " + recordFile);
        }
        
        List<String[]> recordsList = CSVFileUtils.loadCSVFile(recordFile);
        
        if (recordsList == null || recordsList.isEmpty()){
            throw new RuntimeException("the record file should at least contains one header line : \"index, Location URI, Start Level\"");
        }
        
        //the first line must be header
        recordsList.remove(0);
        
        for (String[] records : recordsList){
            // check, user can only add a location uri in record file without specifying a start level
            if (records.length != 2 && records.length != 1){
                log.error("Invalid record: " + records);
            }
            // get uri
            URI uri = null;
            try {
                uri = new URI(records[0]);
            } catch (URISyntaxException e) {
                log.error(e.getMessage());
                log.error("Invalid URI found in records: " + records);
                continue;
            }
            // get start level
            int startLevel = defaultBundleStartLevel;
            if (records.length == 2){
                int sl = Integer.valueOf(records[1]);
                if (sl <= 0){
                    log.warn("Invalid StartLevel found in records: " + records + ", use default bundle start level.");
                } else {
                    startLevel = sl;
                }
            }
            
            bundleRecords.add(new BundleRecord(uri, startLevel));
        }
        
        // Track the uninstalled bundle, if we recorded it, erase the record.
        uninstallBundleTracker = new BundleTracker(bundleContext, Bundle.UNINSTALLED, null){
            @Override
            public Object addingBundle(Bundle bundle, BundleEvent event) {
                
                BundleRecord bundleRecord = null;
                for (Iterator<BundleRecord> it = bundleRecords.iterator(); it.hasNext();){
                    BundleRecord record = it.next();
                    if (record.uri.toString().equals(bundle.getLocation())){
                        bundleRecord = record;
                        //del from runtime
                        it.remove();
                        break;
                    }
                }
                
                if (bundleRecord != null){
                    // del from record file
                    try {
                        CSVFileUtils.deleteByKeywordInCSVFile(recordFile, bundleRecord.uri.toString());
                    } catch (IOException e) {
                        log.error(e.getMessage());
                    }
                    
                    // del file if not inplace (i.e. copied to repository/recorded-bundles folder)
                    if (!recordDir.toURI().relativize(bundleRecord.uri).equals(bundleRecord.uri)){
                        new File(bundleRecord.uri).delete();
                    }
                
                }//else we don't care the bundles not recorded here
                
                return bundle;
            }
        };
        
    }
    
    private static class BundleRecord {        
        private URI uri;
        private int startLevel;
        
        public BundleRecord(URI uri, int startLevel){
            this.uri = uri;
            this.startLevel = startLevel;
        }

    }
    
    @Override
    public void doStart() throws Exception {
        
        // sort the BundleRecord list according to their start-level
        Collections.sort(bundleRecords, new Comparator<BundleRecord>(){
            @Override
            public int compare(BundleRecord recordA, BundleRecord recordB) {
                if (recordA == null && recordB ==null) return 0;
                if (recordA == null) return -1;
                if (recordB == null) return 1;
                return recordA.startLevel - recordB.startLevel;
            }
        });
        
        // per the osgi spec, if the bundle has been installed before, 
        // the installBundle method should return immediately with the installed bundle
        for (BundleRecord bundleRecord : bundleRecords){
            // install the record
            Bundle installedBundle = installBundleRecord(bundleRecord);
            
            // start
            if (installedBundle != null) {
                if (BundleUtils.canStart(installedBundle)) {
                    try {
                        installedBundle.start(Bundle.START_TRANSIENT);
                    } catch (BundleException e) {
                        log.error("Bundle starts failed: " + bundleRecord.uri);
                    }
                }
            }
        }
        
        // open the bundle tracker
        uninstallBundleTracker.open();
    }

    @Override
    public void doStop() throws Exception {
        // Do nothing
    }
    
    @Override
    public void doFail() {
        // Do nothing
    }
    
    private StartLevel getStartLevelService(BundleContext bundleContext){
        ServiceReference startLevelRef = bundleContext.getServiceReference(StartLevel.class.getCanonicalName());
        return (StartLevel) bundleContext.getService(startLevelRef);
    }
        
    /**
     * install the bundle to framework, and record the bundle object in the bundleRecord
     * @param bundleRecord
     * @return the bundle object of the installed bundle. null if install failed.
     */
    private Bundle installBundleRecord(BundleRecord bundleRecord) {
            
        try {
            // install
            Bundle installedBundle = bundleContext.installBundle(bundleRecord.uri.toString());
            // set start level
            startLevelService.setBundleStartLevel(installedBundle, bundleRecord.startLevel);
            
            return installedBundle;
        } catch (BundleException e) {
            log.error("Bundle installation failed: " + bundleRecord.uri);
        }

        
        return null;
    }
    
    @Override
    public long recordInstall(File bundleFile, boolean inplace, int startLevel) throws IOException{
        File target = bundleFile;
        if (!inplace){
            // cp to repository/recorded-bundles
            String fileName = bundleFile.getName();
            target = new File(recordDir, fileName);
            FileUtils.copyFile(bundleFile, target);
        }
        
        if (startLevel <= 0){
            log.info("Invalid start level or no start level specified, use defalut bundle start level");
            startLevel = defaultBundleStartLevel;
        }
        
        BundleRecord newRecord = new BundleRecord(target.toURI(),startLevel);
        Bundle bundle = this.installBundleRecord(newRecord);
        if (bundle != null) {
            // check if we have recorded this
            boolean recordedFlag = false;
            for (BundleRecord record : bundleRecords){
                if (record.uri.equals(newRecord.uri)){
                    recordedFlag = true;
                    log.warn("This bundle uri has been recorded: "+ newRecord.uri);
                    break;
                }
            }
            
            if (!recordedFlag){
                // reocrd it in runtime
                bundleRecords.add(newRecord);
                // record it in file
                CSVFileUtils.appendToCSVFile(recordFile, new String[]{newRecord.uri.toString(), String.valueOf(newRecord.startLevel)});
            }
            
            return bundle.getBundleId();
        }else{
            return -1;
        }

    }
    
}

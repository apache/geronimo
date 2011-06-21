package org.apache.geronimo.system.bundle;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.geronimo.kernel.util.IOUtils;

public class CSVFileUtils {
    
    public static void appendToCSVFile(File file, String[] values) throws IOException{
               
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file,true));
            writer.newLine();
            String line = createCSVLine(values);
            if (line!=null){
                writer.append(line);
            }
            writer.flush();
        } finally {
            IOUtils.close(writer);
        }
    }
    
    public static void deleteByKeywordInCSVFile(File file, String keyword) throws IOException{
        BufferedReader reader = null;
        BufferedWriter writer = null;

        File tmpFile = new File(file.getAbsolutePath()+".tmpfile");
        try {
            reader = new BufferedReader(new FileReader(file));
            writer = new BufferedWriter(new FileWriter(tmpFile,true));
            String line = null;
            while (( line = reader.readLine()) != null){
                String[] values = line.split(",");
                boolean flag = false;
                for (String value : values){
                    if (value.contains(keyword)){
                        flag = true;
                        break;
                    }
                }
                if (flag) continue;
                
                writer.append(line);
                writer.newLine();
            }
            
        } finally {
            reader.close();
            writer.close();
        }
        
        if (file.delete()){
            tmpFile.renameTo(file);
        }else{
            tmpFile.delete();
            throw new RuntimeException("Can not delete a line in file: " + file.getAbsolutePath());
        }

    }
    
    public static List<String[]> loadCSVFile(File file) throws IOException{
        BufferedReader reader = null;
        List<String[]> results = new ArrayList<String[]>();
        
        try {
            reader = new BufferedReader(new FileReader(file));
            String line=null;
            while ((line = reader.readLine())!=null){
                if (line.trim().isEmpty()){ 
                    continue;
                }

                results.add(line.split(","));
            }
        } finally {
            IOUtils.close(reader);
        }
        
        return results;
    }
    
    public static void overrideCSVFile(File file, List<String[]> valuesList) throws IOException {
        BufferedWriter writer = null;

        File tmpFile = new File(file.getAbsolutePath()+".tmpfile");
        try {
            writer = new BufferedWriter(new FileWriter(tmpFile,true));
            
            for (String[] values : valuesList){
                String line = createCSVLine(values);
                
                if (line!=null){
                    writer.append(line);
                    writer.newLine();
                }
            }

        } finally {
            writer.close();
        }
        
        if (file.delete()){
            tmpFile.renameTo(file);
        }else{
            tmpFile.delete();
            throw new RuntimeException("Can not delete the old record file: " + file.getAbsolutePath());
        }
    }
    
    private static String createCSVLine(String[] values){
        String line = null;
        if (values != null) {
            line = "";
            if (values.length > 0) {
                line = values[0];
                for (int i = 1; i < values.length; i++) {
                    line += "," + values[i];
                }
            }
        }
        return line;
    }
}

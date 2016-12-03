import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataEvaluation_to_Annotation {
    public HashMap<String, String> intercorpAspect;

    public DataEvaluation_to_Annotation(){
        this.intercorpAspect = new HashMap<>();
    }



    //TODO:
    public void readIntercorpVerbAspect(String filename) {
        Path path = Paths.get(filename);
        try (BufferedReader bufferedReader = Files.newBufferedReader(path);
             CSVReader reader = new CSVReader(bufferedReader)) {
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                String verb = nextLine[0];
                String aspect = nextLine[1].trim();
                String aspectValue = "";
                if (aspect.equals("pf")) {
                    aspectValue = "telic";
                } else {
                    aspectValue = "atelic";
                }
                //System.out.println(verb + " " + aspectValue + "\n");
                this.intercorpAspect.put(verb, aspectValue);
                //intercorpAspect.add(aspectValue);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(this.intercorpAspect);

    }


    public static void main(String[] args){
        String intercorpVerbsFile = "intercorpVerbAspect/verbKeyAspect.csv";
        DataEvaluation_to_Annotation gatheredAnnotation = new DataEvaluation_to_Annotation();
        gatheredAnnotation.readIntercorpVerbAspect(intercorpVerbsFile);



    }

}
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import com.opencsv.CSVReader;
import de.tudarmstadt.ukp.dkpro.statistics.agreement.coding.CodingAnnotationStudy;
import de.tudarmstadt.ukp.dkpro.statistics.agreement.coding.FleissKappaAgreement;
import de.tudarmstadt.ukp.dkpro.statistics.agreement.visualization.CoincidenceMatrixPrinter;

import static de.tudarmstadt.ukp.dkpro.statistics.agreement.coding.CodingAnnotationStudy.countAnnotationsPerCategory;
import static de.tudarmstadt.ukp.dkpro.statistics.agreement.coding.CodingAnnotationStudy.countTotalAnnotationsPerCategory;

public class AgreementForAnnotations {

    private final int numberAnnotators;
    private final CodingAnnotationStudy targetTypeAgreement;
    private final CodingAnnotationStudy aspectClAgreement;
    private final CodingAnnotationStudy telicityAgreement;

    private List<List<List<Annotation>>> differences;
    //private List<List<List<Annotation>>> telicityDifferences;

    public AgreementForAnnotations(int numberAnnotators) {
        this.numberAnnotators = numberAnnotators;
        this.targetTypeAgreement = new CodingAnnotationStudy(
                this.numberAnnotators);
        this.aspectClAgreement = new CodingAnnotationStudy(
                this.numberAnnotators);
        this.telicityAgreement = new CodingAnnotationStudy(
                this.numberAnnotators);

        this.differences = new ArrayList<List<List<Annotation>>>();

    }

    /**
     * @param documentName , ArrayList <HashMap<spanId:AnnotationObject>>
     *                     parsedAnnotations <- comes from getParsedAnnotations( String
     *                     documentName, List<String> annotationFiles)
     * @return void; builds annotObjectList[annotObject_1 - ..._n] and passes it
     * to addElement()
     */
    public void addDocument(String documentName,
                            ArrayList<HashMap<Integer, Annotation>> parsedAnnotations) {
        List<List<Annotation>> differencesProDocument = new ArrayList<List<Annotation>>();
        Set<Integer> span_id_keys = parsedAnnotations.get(0).keySet();
        for (Integer span_id_key : span_id_keys) {
            ArrayList<Annotation> annotObjectsList = new ArrayList<Annotation>();
            for (HashMap<Integer, Annotation> annotationMap : parsedAnnotations) {
                // value for the id key: Annotation object
                Annotation annotObject = annotationMap.get(span_id_key);
                annotObjectsList.add(annotObject);
            }
            addElement(annotObjectsList);
            if (!hasOnlyEqualObjects(annotObjectsList)) {
                differencesProDocument.add(annotObjectsList);
                // System.out.println(annotObjectsList);

            }
        }
        this.differences.add(differencesProDocument);
    }

    private void addElement(List<Annotation> annotObjectsList) {
        if (annotObjectsList.contains(null)) {
            return;
        }

        // List for all corresponding IDs [annotation objects]
        // ANNOTATION OBJECT
        List<String> targetTypeAnnotations = new ArrayList<String>();
        List<String> aspClassAnnotations = new ArrayList<String>();
        List<String> telicityAnnotations = new ArrayList<String>();
        for (Annotation annotObject : annotObjectsList) {
            // targetType
            String targetTypeAnnot = annotObject.targetType;
            targetTypeAnnotations.add(targetTypeAnnot);
            // aspClass
            String aspClassAnnot = annotObject.aspClass;
            aspClassAnnotations.add(aspClassAnnot);
            // telicity
            String telicityAnnot = annotObject.telicity;
            telicityAnnotations.add(telicityAnnot);
        }

        if (!targetTypeAnnotations.contains(null)) {
            this.targetTypeAgreement.addItem(targetTypeAnnotations.toArray());
        }
        if (!aspClassAnnotations.contains(null)) {
            this.aspectClAgreement.addItem(aspClassAnnotations.toArray());
        }
        if (!telicityAnnotations.contains(null)) {
            this.telicityAgreement.addItem(telicityAnnotations.toArray());
        }
    }
//TODO:
    public static void readIntercorpVerbAspect(String filename){
        Path path = Paths.get(filename);
        try(BufferedReader bufferedReader = Files.newBufferedReader(path);
        CSVReader reader = new CSVReader(bufferedReader)){
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                String verb = nextLine[0];
                String aspect = nextLine[1].trim();
                String aspectValue = "";
                if (aspect.equals("pf")){
                    aspectValue = "telic";
                } else {
                    aspectValue = "atelic";
                }
                System.out.println(verb + "\n" + aspectValue);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public double[] getTargetTypeAgreement() {
        return getAllAgreementMeasures(this.targetTypeAgreement);
    }

    public double[] getAspectClAgreement() {
        return getAllAgreementMeasures(this.aspectClAgreement);
    }

    public double[] getTelicityAgreement() {
        return getAllAgreementMeasures(this.telicityAgreement);
    }


//    private FleissKappaAgreement getFleissKappaAgreement(CodingAnnotationStudy study) {
//        FleissKappaAgreement agreement = new FleissKappaAgreement(study);
//        //return agreement.calculateAgreement();
//        return  agreement;
//    }
    private double getFleissKappaAgreementValue(FleissKappaAgreement agreement) {
        return agreement.calculateAgreement();
    }
    private double getObservedAgreement(FleissKappaAgreement agreement) {
        return agreement.calculateObservedAgreement();
    }
    private double getExpectedAgreement(FleissKappaAgreement agreement) {
        return agreement.calculateExpectedAgreement();
    }
    private double[] getAllAgreementMeasures(CodingAnnotationStudy study) {
        FleissKappaAgreement agreement = new FleissKappaAgreement(study);
        double fk = getFleissKappaAgreementValue(agreement);
        double observed = getObservedAgreement(agreement);
        double expected = getExpectedAgreement(agreement);
        return new double[] {fk, observed, expected};
    }


    public List<List<List<Annotation>>> getDifferences() {
        return differences;
    }

    /**
     * @param annotations
     * @return true if all annotation fields in all [annotation objects] are
     * equal
     */
    public static boolean hasOnlyEqualObjects(List<Annotation> annotations) {
        return annotations.stream().distinct().limit(2).count() <= 1;
    }

    public String printStudyAgreement(double[] agreementValues) {
        return new String("Fleiss K agreement: " + String.valueOf(agreementValues[0]) + "\n"
                + "Observed Agreement: " + String.valueOf(agreementValues[1]) + "\n"
                + "Expected Agreement: " + String.valueOf(agreementValues[2]) + "\n");
    }

    @Override
    public String toString() {
        return new String("Target type agreement: " + "\n"
                + printStudyAgreement(this.getTargetTypeAgreement()) + "\n"
                + "Aspectual class agreement: " + "\n"
                + printStudyAgreement(this.getAspectClAgreement()) + "\n"
                + "Telicity agreement: " + "\n"
                + printStudyAgreement(this.getTelicityAgreement()) + "\n");
    }

    public void printCoincidenceMatrix() {
        System.out.println("Target type coincidence matrix: ");
        new CoincidenceMatrixPrinter().print(System.out,
                this.targetTypeAgreement);
        System.out.println("total number items " + this.targetTypeAgreement.getItemCount());
        System.out.println("Situation Entity: " + Arrays.toString(countAnnotationsPerCategory(this.targetTypeAgreement).get("Situation Entity")));
        System.out.println("None: " + Arrays.toString(countAnnotationsPerCategory(this.targetTypeAgreement).get("None")));
        System.out.println("total target type distribution " + countTotalAnnotationsPerCategory(this.targetTypeAgreement));
        System.out.println();

        System.out.println("Aspectual class coincidence matrix: ");
        new CoincidenceMatrixPrinter()
                .print(System.out, this.aspectClAgreement);
        System.out.println("dynamic: " + Arrays.toString(countAnnotationsPerCategory(this.aspectClAgreement).get("dynamic")));
        System.out.println("stative: " + Arrays.toString(countAnnotationsPerCategory(this.aspectClAgreement).get("stative")));
        System.out.println("total aspect class distribution: " + countTotalAnnotationsPerCategory(this.aspectClAgreement));
        System.out.println();

        System.out.println("Telicity coincidence matrix: ");
        new CoincidenceMatrixPrinter()
                .print(System.out, this.telicityAgreement);
        System.out.println();
        System.out.println("telicity items " + this.telicityAgreement.getItemCount());
        System.out.println("telic: rater distribution " + Arrays.toString(countAnnotationsPerCategory(this.telicityAgreement).get("telic")));
        System.out.println("atelic: rater distribution " + Arrays.toString(countAnnotationsPerCategory(this.telicityAgreement).get("atelic")));
        //System.out.println("raters " + Arrays.toString(countAnnotationsPerCategory(this.telicityAgreement.extractRaters(0)).get("atelic")));
        System.out.println("total telicity distribution " + countTotalAnnotationsPerCategory(this.telicityAgreement));
    }


    // /////////// ALTER CODE ////////////////////////////////////////////

    // // public static boolean hasDifferentValues(List<String> annotations)
    // {
    // // long uniqueValues =
    // annotations.stream().distinct().limit(2).count();
    // // return uniqueValues > 1;

    public static void main(String[] args){
        String fileIntercorpVerbs = "intercorpVerbAspect/verbKeyAspect.csv";
        readIntercorpVerbAspect(fileIntercorpVerbs);

    }

}

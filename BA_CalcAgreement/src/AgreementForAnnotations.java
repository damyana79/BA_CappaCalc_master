import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import de.tudarmstadt.ukp.dkpro.statistics.agreement.coding.CodingAnnotationStudy;
import de.tudarmstadt.ukp.dkpro.statistics.agreement.coding.FleissKappaAgreement;
import de.tudarmstadt.ukp.dkpro.statistics.agreement.visualization.CoincidenceMatrixPrinter;

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

    public double getTargetTypeAgreement() {
        return getFleissKappaAgreement(this.targetTypeAgreement);
    }

    public double getAspectClAgreement() {
        return getFleissKappaAgreement(this.aspectClAgreement);
    }

    public double getTelicityAgreement() {
        return getFleissKappaAgreement(this.telicityAgreement);
    }

    private double getFleissKappaAgreement(CodingAnnotationStudy study) {
        FleissKappaAgreement agreement = new FleissKappaAgreement(study);
        return agreement.calculateAgreement();
    }



    private double getObservedAndExpectedAgreement(CodingAnnotationStudy study) {
        FleissKappaAgreement agreement = new FleissKappaAgreement(study);
        return agreement.calculateAgreement();
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

    @Override
    public String toString() {
        String agreement = new String("Target type agreement: "
                + String.valueOf(this.getTargetTypeAgreement()) + "\n"
                + "Aspectual class agreement: "
                + String.valueOf(this.getAspectClAgreement()) + "\n"
                + "Telicity agreement: "
                + String.valueOf(this.getTelicityAgreement()) + "\n");
        return agreement;
    }

    public void printCoincidenceMatrix() {
        System.out.println("Target type coincidence matrix: ");
        new CoincidenceMatrixPrinter().print(System.out,
                this.targetTypeAgreement);
        System.out.println();
        System.out.println("Aspectual class coincidence matrix: ");
        new CoincidenceMatrixPrinter()
                .print(System.out, this.aspectClAgreement);
        System.out.println();
        System.out.println("Telicity coincidence matrix: ");
        new CoincidenceMatrixPrinter()
                .print(System.out, this.telicityAgreement);
        System.out.println();
        System.out.println("telicity items " + this.telicityAgreement.getItemCount());
    }

    // /////////// ALTER CODE ////////////////////////////////////////////

    // // public static boolean hasDifferentValues(List<String> annotations)
    // {
    // // long uniqueValues =
    // annotations.stream().distinct().limit(2).count();
    // // return uniqueValues > 1;

}

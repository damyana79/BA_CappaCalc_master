import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;

class AnnotationDocument {
    String rawText;
    String xmlDocumentFilename;

    AnnotationDocument(String xmlDocumentFilename, String projectName, String textDocPathName) throws IOException {
        Path rawTextFile = getRawTextname(xmlDocumentFilename, projectName, textDocPathName);
        this.rawText = new String(Files.readAllBytes(rawTextFile), "UTF-8");
        this.xmlDocumentFilename = xmlDocumentFilename;
    }

    @Override
    public String toString() {
        return this.rawText;
    }

    public Path getRawTextname(String documentName, String projectName, String textDocPathName) {
        Path path = Paths.get(documentName);
        String rawName = path.getFileName().toString().replace(projectName, "");
        int underscore = rawName.lastIndexOf("_");
        rawName = rawName.substring(0, underscore);
        return Paths.get(textDocPathName, rawName + ".txt");
    }

}

/**
 * Container class for an annotation
 */
public class Annotation {
    int begin, end;
    String targetType, aspClass, telicity;
    AnnotationDocument annotationDocument;

    /**
     * @param begin
     * @param end
     * @param annotationDocument
     * @param targetType
     * @param aspClass
     * @param telicity
     */
    Annotation(int begin, int end, AnnotationDocument annotationDocument, String targetType,
               String aspClass, String telicity) {
        this.begin = begin;
        this.end = end;
        this.targetType = targetType;
        this.aspClass = aspClass;
        this.telicity = telicity;
        this.annotationDocument = annotationDocument;

    }

    @Override
    public String toString() {
        String[] fields = new String[]{String.valueOf(this.begin),
                String.valueOf(this.end), this.annotationDocument.xmlDocumentFilename, this.targetType,
                this.aspClass, this.telicity};
        return Arrays.toString(fields);

    }

    public String getVerb() {
        return this.annotationDocument.toString().substring(this.begin, this.end);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Annotation) {
            Annotation annotation = (Annotation) o;
            return this.begin == annotation.begin && this.end == annotation.end
                    && Objects.equals(this.targetType, annotation.targetType)
                    && Objects.equals(this.aspClass, annotation.aspClass)
                    && Objects.equals(this.telicity, annotation.telicity);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return begin;
    }
}

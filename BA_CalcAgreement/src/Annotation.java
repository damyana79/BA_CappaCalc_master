import java.util.Arrays;
import java.util.Objects;

/**
 * Container class for an annotation
 */
public class Annotation {
	int begin, end;
	String document;
	String targetType, aspClass, telicity;

	/**
	 *
	 * @param begin
	 * @param end
	 * @param document
	 * @param targetType
	 * @param aspClass
	 * @param telicity
	 */
	Annotation(int begin, int end, String document, String targetType,
			String aspClass, String telicity) {
		this.begin = begin;
		this.end = end;
		this.document = document;
		this.targetType = targetType;
		this.aspClass = aspClass;
		this.telicity = telicity;

	}

	public void printOut() {
		String[] fields = new String[] { String.valueOf(this.begin),
				String.valueOf(this.end), this.document, this.targetType,
				this.aspClass, this.telicity };
		System.out.println(Arrays.toString(fields));
	}

	@Override
	public String toString() {
		String[] fields = new String[] { String.valueOf(this.begin),
				String.valueOf(this.end), this.document, this.targetType,
				this.aspClass, this.telicity };
		return Arrays.toString(fields);

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

package universe;

/**
 * 
 * @author ktsubaki
 *
 */
public interface UniValidation {

	public abstract void validateForSave(UniContext uniContext) throws UniValidationException;
	public abstract void validateForDelete(UniContext uniContext) throws UniValidationException;
}

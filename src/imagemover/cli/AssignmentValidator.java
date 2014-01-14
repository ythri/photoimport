package imagemover.cli;

import com.beust.jcommander.ParameterException;
import com.beust.jcommander.IParameterValidator;

public class AssignmentValidator implements IParameterValidator {
	public void validate(String name, String value) throws ParameterException {
		if (!value.matches("^\\w+=[^,]+(,\\w+=[^,]+)*$")) {
			throw new ParameterException("Parameter " + name + " should by of the form \"Name=Value\" (found \"" + value +"\")");
		}
	}
}

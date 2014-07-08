package simpleorm.quickstart;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import simpleorm.quickstart.SimpleORMGenerator;

public class SimpleORMGeneratorTask extends Task implements PropertyProvider {
	public String getProperty(String key, String def) {
		String val = super.getProject().getProperty(key);
		if (val == null)
			val = System.getProperty(key, def);
		return val;
	}

	public String getProperty(String key) {
		return getProperty(key, null);
	}

	public void execute() throws BuildException {
		SimpleORMGenerator generator = new SimpleORMGenerator(this);
		try {
			generator.internalExecute();
		} catch (Exception e) {
			e.printStackTrace();
			throw new BuildException(e);
		}
	}
}

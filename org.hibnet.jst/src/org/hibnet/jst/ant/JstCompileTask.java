/*
 *  Copyright 2012 JST contributors
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.hibnet.jst.ant;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.hibnet.jst.JstStandaloneSetup;
import org.hibnet.jst.jvmmodel.JstBatchCompiler;
import org.hibnet.jst.jvmmodel.JstBatchCompiler.Logger;

import com.google.inject.Injector;

public class JstCompileTask extends Task {

	private String encoding;

	private File tempDirectory;

	private Path srcpath;

	private Path classpath;

	private File output;

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public void setTempDirectory(File tempDirectory) {
		this.tempDirectory = tempDirectory;
	}

	public void addSourcepath(Path srcpath) {
		this.srcpath = srcpath;
	}

	public void addClasspath(Path classpath) {
		this.classpath = classpath;
	}

	public void setOutput(File output) {
		this.output = output;
	}

	@Override
	public void execute() throws BuildException {
		Injector injector = new JstStandaloneSetup().createInjectorAndDoEMFRegistration();
		JstBatchCompiler compiler = injector.getInstance(JstBatchCompiler.class);

		compiler.setTempDirectory(tempDirectory.getAbsolutePath());
		compiler.setDeleteTempDirectory(false);
		compiler.setClassPath(classpath.toString());
		compiler.setSourcePath(srcpath.toString());
		compiler.setOutputPath(output.getAbsolutePath());
		compiler.setFileEncoding(encoding);
		compiler.setLog(new Logger() {

			@Override
			public boolean isDebugEnabled() {
				return true;
			}

			@Override
			public boolean isInfoEnabled() {
				return true;
			}

			@Override
			public void debug(String message) {
				log(message, Project.MSG_DEBUG);
			}

			@Override
			public void info(String message) {
				log(message, Project.MSG_INFO);
			}

			@Override
			public void warn(String message) {
				log(message, Project.MSG_WARN);
			}

			@Override
			public void error(String message) {
				log(message, Project.MSG_ERR);
			}

		});

		if (!compiler.compile()) {
			throw new BuildException("Error compiling jst templates");
		}
	}

}
/*
 * Copyright (C) 2016 DatioBD 
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.datiodb.sd.output;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.datiodb.sd.producer.Tuple;

/**
 * Implementación de Sink para volcado de datos a un fichero
 * 
 * @author jcorredera
 *
 */
public class FileSink implements Sink {
	
	private static final int DEFAULT_BUFFER_SIZE=1024*8;
	private static final String DEFAULT_PATH="tmp";
	private static final String DEFAULT_FILE="output";
	private static final String DEFAULT_SEPARATOR="|";
	
	private String path;
	private String filename;
	private File file;
	private String separator;
	private int bufferSize;
	private FileWriter output;
	private BufferedWriter bos;
	
	private FileSink() {
		bufferSize=DEFAULT_BUFFER_SIZE;
		separator=DEFAULT_SEPARATOR;
	}
	
	/**
	 * Devuelve el path al fichero de volcado
	 * 
	 * @return
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Devuelve el nombre del fichero de volcado
	 * 
	 * @return
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * Devuelve el caracter separador entre datos
	 * 
	 * @return
	 */
	public String getSeparator() {
		return separator;
	}

	/**
	 * Devuelve el tamaño del buffer utilizado para la generación 
	 * del fichero de volcado
	 *  
	 * @return
	 */
	public int getBufferSize() {
		return bufferSize;
	}

	/**
	 * Estable el path al fichero de volcado y genera el objeto configurador de este Sink
	 * 
	 * @param path
	 * @return		una instancia al configurador del sink
	 */
	public static Filename path(String path) {
		return new FileSink.Builder(path);
	}
	
	/**
	 * Configura el nombre del fichero de volcado
	 * 
	 * @author jcorredera
	 *
	 */
	public interface Filename {
		public Optionals filename(String filename);
	}
	
	/**
	 * Crea una implementación de Sink en base a la clase FileSink
	 * 
	 * @author jcorredera
	 *
	 */
	public interface Creator {
		public Sink create();
	}
	
	/**
	 * Define métodos opcionales para la configuracion de este Sink
	 * 
	 * @author jcorredera
	 *
	 */
	public interface Optionals extends Creator {
		public Optionals withSeparator(String separator);
		public Optionals bufferSize(int size);
	}
	
	@Override
	public void begin() {
		try {
			file=new File(getFilename());
			output = new FileWriter(file);
			bos=new BufferedWriter(output,getBufferSize());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void end() {
        if (output != null) {
            try {
            	bos.close();
				output.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
	}

	@Override
	public void process(Tuple tuple) {
		try {
			bos.write(processTuple(tuple));
			bos.newLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String processTuple(Tuple tuple) {
		StringBuffer buffer=new StringBuffer();
		for(int index=0;index<tuple.getSize();index++) {
			buffer.append(tuple.getValue(index));
			if(index!=tuple.getSize()-1) {
				buffer.append(getSeparator());
			}
		}
		return buffer.toString();
	}

	public static class Builder implements Filename, Optionals, Creator {
		
		private FileSink instance=new FileSink();
		
		public Builder(String path) {
			if(isValidString(path)) {
				instance.path=path;
			}
			else {
				instance.path=File.pathSeparator + DEFAULT_PATH;
			}
		}

		@Override
		public Sink create() {
			return instance;
		}

		@Override
		public Optionals withSeparator(String separator) {
			if(isValidString(separator)) {
				instance.separator=separator;
			}
			else {
				instance.separator=DEFAULT_SEPARATOR;
			}
			return this;
		}

		@Override
		public Optionals bufferSize(int size) {
			checkNotNull(size);
			if(size <= 0) {
				instance.bufferSize=DEFAULT_BUFFER_SIZE;
			}
			else {
				instance.bufferSize=size;
			}
			return this;
		}

		@Override
		public Optionals filename(String filename) {
			if(isValidString(filename)) {
				instance.filename=instance.filename=instance.path + File.separator + filename;
			}
			else {
				instance.filename=instance.path + File.separator + DEFAULT_FILE;
			}
			return this;
		}
		
		private boolean isValidString(String value) {
			return value!=null && !value.isEmpty();
		}

	}

}

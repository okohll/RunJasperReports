/*
 *  Copyright 2010 GT webMarque Ltd
 * 
 *  This file is part of RunJasperReports.
 *
 *  RunJasperReports is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  RunJasperReports is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with RunJasperReports.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gtwm.jasperexecute;

/**
 * These enumerations used when parsing command line
 */
public class OptionValues {

	public enum OutputType {
		PDF, HTML, TEXT, CSV, XLS;
		
		public String toString() {
			return this.name().toLowerCase();
		}
	}
	
	public enum ParamType {
		STRING, BOOLEAN, DOUBLE, INTEGER;
		
		public String toString() {
			return this.name().toLowerCase();
		}
	}
	
	
	public enum DatabaseType {
		POSTGRESQL("org.postgresql.Driver","jdbc:postgresql://","/"),
		MYSQL("com.mysql.jdbc.Driver","jdbc:mysql://","/"),
		FIREBIRD("org.firebirdsql.jdbc.FBDriver","jdbc:firebirdsql://","/");
		
		DatabaseType(String driverName, String urlProtocol, String separator) {
			this.driverName = driverName;
			this.urlProtocol = urlProtocol;
			this.separator = separator;
		}
		
		/**
		 * Get the String that needs to be used with Class.forName() when setting up JDBC
		 * 
		 * @see http://jdbc.postgresql.org/documentation/82/load.html
		 * @see http://www.kitebird.com/articles/jdbc.html
		 */
		public String getDriverName() {
			return this.driverName;
		}
		
		/**
		 * Get the URL part of the JDBC connection string before the hostname
		 */
		public String getProtocolUrl() {
			return this.urlProtocol;
		}
		
		/**
		 * Get the separator string the db driver needs between hostname and database name
		 * @return
		 */
		public String getSeparator() {
			return this.separator;
		}
		
		public String toString() {
			return this.name().toLowerCase();
		}
		
		private String driverName;
		
		private String urlProtocol;
		
		private String separator;
	}
}

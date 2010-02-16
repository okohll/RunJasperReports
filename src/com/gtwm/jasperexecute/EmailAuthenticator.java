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

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public class EmailAuthenticator extends Authenticator {

	public EmailAuthenticator(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	public PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(this.username, this.password);
	}
	
	private String username = "";
	
	private String password = "";
}

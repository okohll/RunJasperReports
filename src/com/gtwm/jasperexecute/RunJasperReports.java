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

import com.gtwm.jasperexecute.OptionValues.DatabaseType;
import com.gtwm.jasperexecute.OptionValues.OutputType;
import com.gtwm.jasperexecute.OptionValues.ParamType;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JExcelApiExporter;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRCsvExporterParameter;
import net.sf.jasperreports.engine.export.JRTextExporter;
import net.sf.jasperreports.engine.export.JRTextExporterParameter;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperExportManager;
import java.lang.IllegalArgumentException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Transport;
import javax.mail.BodyPart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.Multipart;
import javax.mail.internet.MimeMultipart;
import java.sql.Connection;
import java.sql.DriverManager;
import javax.activation.FileDataSource;
import javax.activation.DataSource;
import javax.activation.DataHandler;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.Properties;
import java.sql.SQLException;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.io.FileUtils;

/**
 * Command line application to generate reports from JasperReports XML report
 * definition files, e.g. those designed with iReport
 * 
 * One or more report definitions can be specified on the command line and a PDF
 * or HTML file will be generated from each. If email details are specified,
 * then they'll be attached to an email and sent. If not, they'll just be
 * written to disk
 * 
 * Example Usage: java -jar RunJasperReports.jar -dbtype postgresql -dbname
 * accountsdb -dbuser fd -dbpass secret -reports p_and_l.jrxml,sales.jrxml
 * -folder /var/www/financial/ -emailto directors@gtwm.co.uk -emailfrom
 * accounts@gtwm.co.uk -emailsubject FinancialReports -output pdf
 * 
 * @author Oliver Kohll
 * @version 1.1.2
 * @see http://www.agilebase.co.uk/opensource
 */
public class RunJasperReports {

	public RunJasperReports() {
	}

	public void generatePdfReport(String reportDefinitionFile, String outputFileName,
			DatabaseType dbType, String databaseName, String databaseUsername,
			String databasePassword, String databaseHost, Map parameters)
			throws FileNotFoundException, JRException, SQLException, IOException,
			ClassNotFoundException {
		JasperPrint print = this.generateReport(reportDefinitionFile, dbType, databaseName,
				databaseUsername, databasePassword, databaseHost, parameters);
		File outputFile = new File(outputFileName);
		outputFile.createNewFile();
		OutputStream output = new FileOutputStream(outputFile);
		JasperExportManager.exportReportToPdfStream(print, output);
		System.out.println("Output PDF file written: " + outputFileName);
	}

	public void generateHtmlReport(String reportDefinitionFile, String outputFileName,
			DatabaseType dbType, String databaseName, String databaseUsername,
			String databasePassword, String databaseHost, Map parameters)
			throws FileNotFoundException, JRException, SQLException, IOException,
			ClassNotFoundException {
		JasperPrint print = this.generateReport(reportDefinitionFile, dbType, databaseName,
				databaseUsername, databasePassword, databaseHost, parameters);
		JasperExportManager.exportReportToHtmlFile(print, outputFileName);
		// Images output by JasperReports don't have extensions, add them
		File imageFolder = new File(outputFileName + "_files");
		File[] imageFileArray = imageFolder.listFiles();
		if (imageFileArray != null) {
			List<File> imageFiles = Arrays.asList(imageFileArray);
			for (File imageFile : imageFiles) {
				if (!imageFile.getName().equals("px")) {
					File imageFileWithExtension = new File(imageFile.getAbsolutePath() + ".png");
					FileUtils.copyFile(imageFile, imageFileWithExtension, false);
				}
			}
		}
		System.out.println("Output HTML file written: " + outputFileName);
	}

	/**
	 * @author Bal‡zs B‡r‡ny
	 */
	public void generateTextReport(String reportDefinitionFile, String outputFileName,
			DatabaseType dbType, String databaseName, String databaseUsername,
			String databasePassword, String databaseHost, Map parameters)
			throws FileNotFoundException, JRException, SQLException, IOException,
			ClassNotFoundException {
		JasperPrint print = this.generateReport(reportDefinitionFile, dbType, databaseName,
				databaseUsername, databasePassword, databaseHost, parameters);
		JRTextExporter exporter = new JRTextExporter();
		exporter.setParameter(JRTextExporterParameter.JASPER_PRINT, print);
		exporter.setParameter(JRTextExporterParameter.OUTPUT_FILE_NAME, "tagesreport.txt");
		// exporter.setParameter(JRTextExporterParameter.PAGE_WIDTH, new
		// Integer(20));
		// exporter.setParameter(JRTextExporterParameter.PAGE_HEIGHT, new
		// Integer(40));
		exporter.setParameter(JRTextExporterParameter.CHARACTER_WIDTH, new Integer(50));
		exporter.setParameter(JRTextExporterParameter.CHARACTER_HEIGHT, new Integer(12));
		exporter.exportReport();
		System.out.println("Output text file written: " + outputFileName);
	}

	/**
	 * @author Bal‡zs B‡r‡ny
	 */
	public void generateCSVReport(String reportDefinitionFile, String outputFileName,
			DatabaseType dbType, String databaseName, String databaseUsername,
			String databasePassword, String databaseHost, Map parameters)
			throws FileNotFoundException, JRException, SQLException, IOException,
			ClassNotFoundException {
		JasperPrint print = this.generateReport(reportDefinitionFile, dbType, databaseName,
				databaseUsername, databasePassword, databaseHost, parameters);
		JRCsvExporter exporter = new JRCsvExporter();
		exporter.setParameter(JRCsvExporterParameter.JASPER_PRINT, print);
		exporter.setParameter(JRCsvExporterParameter.OUTPUT_FILE_NAME, outputFileName);
		// exporter.setParameter(JRCsvExporterParameter.CHARACTER_WIDTH, new
		// Integer(50));
		// exporter.setParameter(JRCsvExporterParameter.CHARACTER_HEIGHT, new
		// Integer(12));
		exporter.exportReport();
		System.out.println("Output CSV file written: " + outputFileName);
	}

	/**
	 * @author Charly Clairmont
	 */
	public void generateJxlsReport(String reportDefinitionFile, String outputFileName,
			DatabaseType dbType,
			// CCH20071129-02
			String databaseHost, String databaseName, String databaseUsername,
			String databasePassword, Map parameters) throws FileNotFoundException, JRException,
			SQLException, IOException, ClassNotFoundException {
		// ignorer la pagination
		parameters.put(net.sf.jasperreports.engine.JRParameter.IS_IGNORE_PAGINATION, Boolean.TRUE);
		JasperPrint print = generateReport(reportDefinitionFile, dbType, databaseName,
				databaseUsername, databasePassword, databaseHost, parameters);
		JExcelApiExporter exporter = new JExcelApiExporter();
		List jasperPrintList = new ArrayList();
		jasperPrintList.add(print);
		exporter.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrintList);
		exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, outputFileName);
		// exporter.setParameter(JExcelApiExporterParameter.IS_ONE_PAGE_PER_SHEET,
		// Boolean.TRUE);
		exporter.exportReport();
		System.out.println("Output XLS file written: " + outputFileName);
	}

	/**
	 * Perform a liberal check of boolean representation. Any string that starts
	 * with 't', 'y' or '1', case-insensitively, will return true - anything
	 * else, false, including null. Less stringent than Boolean.valueOf() and
	 * will never throw an Exception
	 */
	private boolean valueRepresentsBooleanTrue(String value) {
		if (value == null) {
			return false;
		}
		if (value.toLowerCase().startsWith("t")
				|| value.toLowerCase().startsWith("y") || value.startsWith("1")) {
			return true;
		} else {
			return false;
		}
	}

	private Map prepareParameters(String parametersString) {
		Map parameters = new HashMap();
		if (parametersString == null) {
			return parameters;
		}
		List<String> parameterList = Arrays.asList(parametersString.split(","));
		for (String parameter : parameterList) {
			String paramName = parameter.split("=", 2)[0];
			String paramTypeAndValue = parameter.split("=", 2)[1];
			String paramTypeString = paramTypeAndValue.split(":", 2)[0];
			ParamType paramType = ParamType.valueOf(paramTypeString.toUpperCase());
			String paramValueString = paramTypeAndValue.split(":", 2)[1];
			switch (paramType) {
			case BOOLEAN:
				parameters.put(paramName, valueRepresentsBooleanTrue(paramValueString));
				break;
			case STRING:
				parameters.put(paramName, paramValueString);
				break;
			case DOUBLE:
				parameters.put(paramName, Double.valueOf(paramValueString));
				break;
			case INTEGER:
				parameters.put(paramName, Integer.valueOf(paramValueString));
				break;
			}
		}
		System.out.println("Report parameters are: " + parameters);
		return parameters;
	}

	private JasperPrint generateReport(String reportDefinitionFile, DatabaseType dbType,
			String databaseName, String databaseUsername, String databasePassword, String dbHost,
			Map parameters) throws FileNotFoundException, JRException, SQLException,
			ClassNotFoundException {
		// parse JasperReport XML file
		InputStream input = new FileInputStream(new File(reportDefinitionFile));
		System.out.println("Read input file " + reportDefinitionFile);
		JasperDesign design = JRXmlLoader.load(input);
		JasperReport report = JasperCompileManager.compileReport(design);
		System.out.println("Compiled report from file " + reportDefinitionFile);
		// connect to db
		Class.forName(dbType.getDriverName());
		String connectionStatement = dbType.getProtocolUrl() + dbHost + dbType.getSeparator()
				+ databaseName;
		System.out.println("About to connect to dababase using connection string "
				+ connectionStatement);
		Properties connectionProperties = new Properties();
		if (databaseUsername != null) {
			connectionProperties.setProperty("user", databaseUsername);
		}
		if (databasePassword != null) {
			connectionProperties.setProperty("password", databasePassword);
		}
		if (dbType.equals(DatabaseType.FIREBIRD)) {
			// See issue http://www.firebirdfaq.org/faq320/
			connectionProperties.setProperty("charSet", "UTF-8");
			connectionProperties.setProperty("encoding", "UTF8");
		}
		Connection conn = DriverManager.getConnection(connectionStatement, connectionProperties);
		// Connection conn = DriverManager.getConnection(connectionStatement,
		// databaseUsername,
		// databasePassword);
		System.out.println("Connected to database " + databaseName);
		conn.setAutoCommit(false);
		// run report and write output
		JasperPrint print = JasperFillManager.fillReport(report, parameters, conn);
		System.out.println("Composed report output");
		conn.close();
		return print;
	}

	public void emailReport(String emailHost, String emailUser, String emailPass,
			Set<String> emailRecipients, String emailSender, String emailSubject,
			List<String> attachmentFileNames) throws MessagingException {
		Properties props = new Properties();
		//props.setProperty("mail.debug", "true");
		props.setProperty("mail.smtp.host", emailHost);
		if (emailUser != null) {
			props.setProperty("mail.smtp.auth", "true");
		}
		Authenticator emailAuthenticator = new EmailAuthenticator(emailUser, emailPass);
		Session mailSession = Session.getDefaultInstance(props, emailAuthenticator);
		MimeMessage message = new MimeMessage(mailSession);
		message.setSubject(emailSubject);
		for (String emailRecipient : emailRecipients) {
			Address toAddress = new InternetAddress(emailRecipient);
			message.addRecipient(Message.RecipientType.TO, toAddress);
		}
		Address fromAddress = new InternetAddress(emailSender);
		message.setFrom(fromAddress);
		// Message text
		Multipart multipart = new MimeMultipart();
		BodyPart textBodyPart = new MimeBodyPart();
		textBodyPart.setText("Database report attached\n\n");
		multipart.addBodyPart(textBodyPart);
		// Attachments
		for (String attachmentFileName : attachmentFileNames) {
			BodyPart attachmentBodyPart = new MimeBodyPart();
			DataSource source = new FileDataSource(attachmentFileName);
			attachmentBodyPart.setDataHandler(new DataHandler(source));
			String fileNameWithoutPath = attachmentFileName.replaceAll("^.*\\/", "");
			fileNameWithoutPath = fileNameWithoutPath.replaceAll("^.*\\\\", "");
			attachmentBodyPart.setFileName(fileNameWithoutPath);
			multipart.addBodyPart(attachmentBodyPart);
		}
		// add parts to message
		message.setContent(multipart);
		// send via SMTP
		Transport transport = mailSession.getTransport("smtp");
		// transport.connect(emailHost, emailUser, emailPass);
		transport.connect();
		transport.sendMessage(message, message.getAllRecipients());
		transport.close();
	}

	public static void main(String[] args) throws Exception {
		RunJasperReports runJasperReports = new RunJasperReports();
		// Set up command line parser
		Options options = new Options();
		Option reports = OptionBuilder.withArgName("reportlist").hasArg().withDescription(
				"Comma separated list of JasperReport XML input files").create("reports");
		options.addOption(reports);
		Option emailTo = OptionBuilder.withArgName("emailaddress").hasArg().withDescription(
				"Email address to send generated reports to").create("emailto");
		options.addOption(emailTo);
		Option emailFrom = OptionBuilder.withArgName("emailaddress").hasArg().withDescription(
				"Sender email address").create("emailfrom");
		options.addOption(emailFrom);
		Option emailSubjectLine = OptionBuilder.withArgName("emailsubject").hasArg()
				.withDescription("Subject line of email").create("emailsubject");
		options.addOption(emailSubjectLine);
		Option emailHostOption = OptionBuilder.withArgName("emailhost").hasArg().withDescription(
				"Address of email server").create("emailhost");
		options.addOption(emailHostOption);
		Option emailUsernameOption = OptionBuilder.withArgName("emailuser").hasArg()
				.withDescription("Username if email server requires authentication").create(
						"emailuser");
		options.addOption(emailUsernameOption);
		Option emailPasswordOption = OptionBuilder.withArgName("emailpass").hasArg()
				.withDescription("Password if email server requires authentication").create(
						"emailpass");
		options.addOption(emailPasswordOption);
		Option outputFolder = OptionBuilder
				.withArgName("foldername")
				.hasArg()
				.withDescription(
						"Folder to write generated reports to, with trailing separator (slash or backslash)")
				.create("folder");
		options.addOption(outputFolder);
		Option dbTypeOption = OptionBuilder.withArgName("databasetype").hasArg().withDescription(
				"Currently supported types are: " + Arrays.asList(DatabaseType.values())).create(
				"dbtype");
		options.addOption(dbTypeOption);
		Option dbNameOption = OptionBuilder.withArgName("databasename").hasArg().withDescription(
				"Name of the database to run reports against").create("dbname");
		options.addOption(dbNameOption);

		Option dbUserOption = OptionBuilder.withArgName("username").hasArg().withDescription(
				"Username to connect to databasewith").create("dbuser");
		options.addOption(dbUserOption);
		Option dbPassOption = OptionBuilder.withArgName("password").hasArg().withDescription(
				"Database password").create("dbpass");
		options.addOption(dbPassOption);
		Option outputTypeOption = OptionBuilder.withArgName("outputtype").hasArg().withDescription(
				"Output type, one of: " + Arrays.asList(OutputType.values())).create("output");
		options.addOption(outputTypeOption);
		Option outputFilenameOption = OptionBuilder.withArgName("outputfilename").hasArg()
				.withDescription("Output filename (excluding filetype suffix)").create("filename");
		options.addOption(outputFilenameOption);
		Option dbHostOption = OptionBuilder.withArgName("host").hasArg().withDescription(
				"Database host address").create("dbhost");
		options.addOption(dbHostOption);
		Option paramsOption = OptionBuilder
				.withArgName("parameters")
				.hasArg()
				.withDescription(
						"Parameters, e.g. param1=boolean:true,param2=string:ABC,param3=double:134.2,param4=integer:85")
				.create("params");
		options.addOption(paramsOption);
		// Parse command line
		CommandLineParser parser = new GnuParser();
		CommandLine commandLine = parser.parse(options, args);
		String reportsDefinitionFileNamesCvs = commandLine.getOptionValue("reports");
		if (reportsDefinitionFileNamesCvs == null) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("java -jar RunJasperReports.jar", options);
			System.out.println();
			System.out.println("See www.agilebase.co.uk/opensource for further documentation");
			System.out.println();
			throw new IllegalArgumentException("No reports specified");
		}
		String outputPath = commandLine.getOptionValue("folder");
		List<String> reportDefinitionFileNames = Arrays.asList(reportsDefinitionFileNamesCvs
				.split(","));
		List<String> outputFileNames = new ArrayList<String>();
		DatabaseType databaseType = DatabaseType.POSTGRESQL;
		String databaseTypeString = commandLine.getOptionValue("dbtype");
		if (databaseTypeString != null) {
			databaseType = DatabaseType.valueOf(commandLine.getOptionValue("dbtype").toUpperCase());
		}
		String databaseName = commandLine.getOptionValue("dbname");
		String databaseUsername = commandLine.getOptionValue("dbuser");
		String databasePassword = commandLine.getOptionValue("dbpass");
		String databaseHost = commandLine.getOptionValue("dbhost");
		if (databaseHost == null) {
			databaseHost = "localhost";
		}
		OutputType outputType = OutputType.PDF;
		String outputTypeString = commandLine.getOptionValue("output");
		if (outputTypeString != null) {
			outputType = OutputType.valueOf(outputTypeString.toUpperCase());
		}
		String parametersString = commandLine.getOptionValue("params");
		Map parameters = runJasperReports.prepareParameters(parametersString);
		String outputFilenameSpecified = commandLine.getOptionValue("filename");
		if (outputFilenameSpecified == null) {
			outputFilenameSpecified = "";
		}
		// Iterate over reports, generating output for each
		for (String reportsDefinitionFileName : reportDefinitionFileNames) {
			String outputFilename = null;
			if ((reportDefinitionFileNames.size() == 1) && (!outputFilenameSpecified.equals(""))) {
				outputFilename = outputFilenameSpecified;
			} else {
				outputFilename = outputFilenameSpecified
						+ reportsDefinitionFileName.replaceAll("\\..*$", "");
				outputFilename = outputFilename.replaceAll("^.*\\/", "");
				outputFilename = outputFilename.replaceAll("^.*\\\\", "");
			}
			outputFilename = outputFilename.replaceAll("\\W", "").toLowerCase() + "."
					+ outputType.toString().toLowerCase();
			if (outputPath != null) {
				if (!outputPath.endsWith("\\") && !outputPath.endsWith("/")) {
					outputPath += java.io.File.separator;
				}
				outputFilename = outputPath + outputFilename;
			}
			System.out.println("Going to generate report " + outputFilename);
			if (outputType.equals(OutputType.PDF)) {
				runJasperReports.generatePdfReport(reportsDefinitionFileName, outputFilename,
						databaseType, databaseName, databaseUsername, databasePassword,
						databaseHost, parameters);
			} else if (outputType.equals(OutputType.TEXT)) {
				runJasperReports.generateTextReport(reportsDefinitionFileName, outputFilename,
						databaseType, databaseName, databaseUsername, databasePassword,
						databaseHost, parameters);
			} else if (outputType.equals(OutputType.CSV)) {
				runJasperReports.generateCSVReport(reportsDefinitionFileName, outputFilename,
						databaseType, databaseName, databaseUsername, databasePassword,
						databaseHost, parameters);
			} else if (outputType.equals(OutputType.XLS)) {
				runJasperReports.generateJxlsReport(reportsDefinitionFileName, outputFilename,
						databaseType, databaseName, databaseUsername, databasePassword,
						databaseHost, parameters);
			} else {
				runJasperReports.generateHtmlReport(reportsDefinitionFileName, outputFilename,
						databaseType, databaseName, databaseUsername, databasePassword,
						databaseHost, parameters);
			}
			outputFileNames.add(outputFilename);
		}
		String emailRecipientList = commandLine.getOptionValue("emailto");
		if (emailRecipientList != null) {
			Set<String> emailRecipients = new HashSet<String>(Arrays.asList(emailRecipientList
					.split(",")));
			String emailSender = commandLine.getOptionValue("emailfrom");
			String emailSubject = commandLine.getOptionValue("emailsubject");
			if (emailSubject == null) {
				emailSubject = "Report attached";
			}
			String emailHost = commandLine.getOptionValue("emailhost");
			if (emailHost == null) {
				emailHost = "localhost";
			}
			String emailUser = commandLine.getOptionValue("emailuser");
			String emailPass = commandLine.getOptionValue("emailpass");
			System.out.println("Emailing reports to " + emailRecipients);
			runJasperReports.emailReport(emailHost, emailUser, emailPass, emailRecipients,
					emailSender, emailSubject, outputFileNames);
		} else {
			System.out.println("Email not generated (no recipients specified)");
		}
	}
}

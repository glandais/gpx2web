package org.glandais.digicamtools;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FileUtils;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;
import org.im4java.process.ProcessExecutor;

public class Converter {

	private static ProcessExecutor exec;
	private static ConvertCmd cmd;
	private static IMOperation op;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		exec = new ProcessExecutor();

		cmd = new ConvertCmd();
		op = new IMOperation();
		op.addImage();
		op.resize(null, 1024, ">");
		op.addImage();

		Options options = new Options();

		options.addOption("from", true, "Source folder (photos)");
		options.addOption("target", true, "Target folder");

		CommandLineParser parser = new PosixParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			printHelp(options);
			return;
		}

		if (cmd.hasOption("h")) {
			printHelp(options);
		} else if (cmd.hasOption("from")) {
			if (!cmd.hasOption("target")) {
				printHelp(options);
				return;
			}

			clean(new File(cmd.getOptionValue("target")),
					new File(cmd.getOptionValue("from")));
			convert(new File(cmd.getOptionValue("from")),
					new File(cmd.getOptionValue("target")));

		} else {
			printHelp(options);
		}

		exec.shutdown();

	}

	private static void clean(File target, File src) {
		if (!src.exists()) {
			FileUtils.deleteQuietly(target);
		} else {
			if (target.isDirectory()) {
				File[] listFiles = target.listFiles();
				for (File file : listFiles) {
					if (!file.getName().startsWith(".")) {
						clean(file, new File(src, file.getName()));
					}
				}
			}
		}
	}

	private static void convert(File src, File target) {
		if (src.isDirectory()) {
			File[] listFiles = src.listFiles();
			for (File file : listFiles) {
				if (!file.getName().startsWith(".")) {
					convert(file, new File(target, file.getName()));
				}
			}
		} else if (Importer.isPicture(src)) {
			if (!target.getParentFile().exists()) {
				target.getParentFile().mkdirs();
			}
			if (target.exists()) {
				System.out.println("Skipping " + target.getAbsolutePath());
			} else {
				try {
					cmd.run(op,
							src.getAbsolutePath(), target.getAbsolutePath());
//					ProcessTask pt = cmd.getProcessTask(op,
//							src.getAbsolutePath(), target.getAbsolutePath());
//					exec.execute(pt);
				} catch (Exception e) {
					System.err.println("Failed to process "
							+ target.getAbsolutePath());
					e.printStackTrace();
				}
			}
		}
	}

	private static void printHelp(Options options) {
		// automatically generate the help statement
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("digicamporter", options);
	}

}

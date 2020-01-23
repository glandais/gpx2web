package io.github.glandais;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.extern.slf4j.Slf4j;

//@SpringBootApplication
@Slf4j
public class GpxTool implements CommandLineRunner {

	private static GpxToolOptions OPTIONS;

	public static void main(String[] args) {
		if (parseOptions(args)) {
			SpringApplication.run(GpxTool.class, args);
		}
	}

	private static boolean parseOptions(String[] args) {
		OPTIONS = new GpxToolOptions();
		Options options = OPTIONS.getOptions();
		CommandLine cmd = null;
		try {
			cmd = getCommandLine(options, args);
		} catch (ParseException e) {
			log.error("Failed to parse options", e);
			printHelp(options);
		}

		if (cmd == null) {
			return false;
		}
		OPTIONS.parseCommandLine(cmd);
		if (!OPTIONS.isValid()) {
			printHelp(options);
			return false;
		}
		return true;
	}

	@Autowired
	private GpxProcessor gpxProcessor;

	@Override
	public void run(String... args) throws Exception {
		log.info("Options : {}", OPTIONS);
		for (File gpxFile : OPTIONS.getGpxFiles()) {
			gpxProcessor.process(gpxFile, OPTIONS);
		}
	}

	private static CommandLine getCommandLine(Options options, String... args) throws ParseException {
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse(options, args);

		if (cmd.hasOption("h") || cmd.getArgList().isEmpty()) {
			printHelp(options);
			return null;
		} else {
			return cmd;
		}
	}

	private static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("gpx-tool [OPTIONS] FILE/FOLDER...", options);
	}

}

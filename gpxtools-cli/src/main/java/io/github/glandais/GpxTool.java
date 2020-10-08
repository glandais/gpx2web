package io.github.glandais;

import io.github.glandais.process.GpxProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import picocli.CommandLine;

@SpringBootApplication(proxyBeanMethods = false)
@Slf4j
public class GpxTool implements CommandLineRunner, ExitCodeGenerator {

    private final CommandLine.IFactory factory;
    private final RootCommand rootCommand;
    private final GpxProcessor gpxProcessor;
    private int exitCode;

    public GpxTool(CommandLine.IFactory factory, RootCommand rootCommand, GpxProcessor gpxProcessor) {
        this.factory = factory;
        this.rootCommand = rootCommand;
        this.gpxProcessor = gpxProcessor;
    }

    @Override
    public void run(String... args) {

        exitCode = new CommandLine(rootCommand, factory).execute(args);
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }

    public static void main(String[] args) {

        System.exit(SpringApplication.exit(SpringApplication.run(GpxTool.class, args)));
    }

}

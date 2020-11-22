package io.github.glandais;

import io.github.glandais.export.ExportCommand;
import io.github.glandais.guesser.GuesserCommand;
import io.github.glandais.process.ProcessCommand;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

@Component
@CommandLine.Command(name = "gpxtools", mixinStandardHelpOptions = true, subcommands = {ProcessCommand.class, ExportCommand.class, GuesserCommand.class})
public class RootCommand {
}

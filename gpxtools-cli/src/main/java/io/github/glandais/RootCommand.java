package io.github.glandais;

import io.github.glandais.export.ExportCommand;
import io.github.glandais.guesser.GuesserCommand;
import io.github.glandais.process.ProcessCommand;
import io.github.glandais.virtualize.VirtualizeCommand;
import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.AutoComplete;
import picocli.CommandLine;

@TopCommand
@CommandLine.Command(name = "gpxtools-cli-runner", mixinStandardHelpOptions = true, subcommands = {
        ProcessCommand.class,
        ExportCommand.class,
        GuesserCommand.class,
        VirtualizeCommand.class,
        AutoComplete.GenerateCompletion.class
})
public class RootCommand {
}

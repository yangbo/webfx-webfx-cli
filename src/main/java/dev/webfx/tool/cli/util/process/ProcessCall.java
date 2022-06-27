package dev.webfx.tool.cli.util.process;

import dev.webfx.tool.cli.core.Logger;
import dev.webfx.tool.cli.util.os.OperatingSystem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author Bruno Salmon
 */
public class ProcessCall {

    private File workingDirectory;

    private String command;

    private boolean powershellCommand;
    private boolean bashCommand;

    private Predicate<String> logLineFilter;

    private Predicate<String> errorLineFilter;

    private final List<String> errorLines = new ArrayList<>();

    private Predicate<String> resultLineFilter;

    private String lastResultLine;

    private boolean logsCalling = true;

    private boolean logsCallDuration = true;

    private StreamGobbler streamGobbler;

    private int exitCode;

    private long callDurationMillis;

    public ProcessCall() {
    }

    public ProcessCall(String command) {
        setCommand(command);
    }

    public ProcessCall setCommand(String command) {
        this.command = command;
        return this;
    }

    public ProcessCall setPowershellCommand(boolean powershellCommand) {
        this.powershellCommand = powershellCommand;
        return this;
    }

    public ProcessCall setBashCommand(boolean bashCommand) {
        this.bashCommand = bashCommand;
        return this;
    }

    private String[] splitCommand() {
        String[] tokens;
        if (bashCommand)
            tokens = new String[] {"bash", "-c", command};
        else if (powershellCommand)
            tokens = new String[] {"powershell", "-Command", command};
        else if (OperatingSystem.isWindows())
            tokens = new String[] {"cmd", "/c", command}; // Required in Windows for Path resolution (otherwise it won't find commands like mvn)
        else
            tokens = command.split(" ");
        return tokens;
    }

    public ProcessCall setWorkingDirectory(Path workingDirectory) {
        return setWorkingDirectory(workingDirectory.toFile());
    }

    public ProcessCall setWorkingDirectory(File workingDirectory) {
        this.workingDirectory = workingDirectory;
        return this;
    }

    public File getWorkingDirectory() {
        return workingDirectory;
    }

    public ProcessCall setLogLineFilter(Predicate<String> logLineFilter) {
        this.logLineFilter = logLineFilter;
        return this;
    }

    public ProcessCall setResultLineFilter(Predicate<String> resultLineFilter) {
        this.resultLineFilter = resultLineFilter;
        return this;
    }

    public ProcessCall setErrorLineFilter(Predicate<String> errorLineFilter) {
        this.errorLineFilter = errorLineFilter;
        return this;
    }

    public ProcessCall setLogsCall(boolean logsCalling, boolean logsCallDuration) {
        this.logsCalling = logsCalling;
        this.logsCallDuration = logsCallDuration;
        return this;
    }

    public ProcessCall executeAndWait() {
        executeAndConsume(line -> {
            boolean log = false;
            if (errorLineFilter != null && errorLineFilter.test(line)) {
                errorLines.add(line);
                log = true;
            }
            if (logLineFilter == null || logLineFilter.test(line))
                log = true;
            if (resultLineFilter == null || resultLineFilter.test(line))
                lastResultLine = line;
            if (log)
                Logger.log(line);
        });
        return this;
    }

    public ProcessCall logCallCommand() {
        Logger.log((powershellCommand ? "PS " : "") + (workingDirectory == null ? "" : workingDirectory) + (OperatingSystem.isLinux() ? "$ " : OperatingSystem.isMacOs() ? " % " : "> ") + command);
        return this;
    }

    public ProcessCall logCallDuration() {
        Logger.log("Call duration: " + callDurationMillis + " ms");
        return this;
    }

    public ProcessCall onLastResultLine(Consumer<String> lastResultLineConsumer) {
        waitForStreamGobblerCompleted();
        lastResultLineConsumer.accept(lastResultLine);
        return this;
    }

    public int getExitCode() {
        return exitCode;
    }

    public String getLastResultLine() {
        waitForStreamGobblerCompleted();
        return lastResultLine;
    }

    public List<String> getErrorLines() {
        waitForStreamGobblerCompleted();
        return errorLines;
    }

    public String getLastErrorLine() {
        return getErrorLines().isEmpty() ? null : errorLines.get(errorLines.size()  - 1);
    }

    private void executeAndConsume(Consumer<String> outputLineConsumer) {
        if (logsCalling)
            logCallCommand();
        long t0 = System.currentTimeMillis();
        try {
            Process process = new ProcessBuilder()
                    .command(splitCommand())
                    .directory(workingDirectory)
                    .start();
            streamGobbler = new StreamGobbler(process.getInputStream(), outputLineConsumer);
            Executors.newSingleThreadExecutor().submit(streamGobbler);
            exitCode = process.waitFor();
            callDurationMillis = System.currentTimeMillis() - t0;
            if (logsCallDuration)
                logCallDuration();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void waitForStreamGobblerCompleted() {
        while (streamGobbler != null && !streamGobbler.isCompleted())
            try {
                synchronized (streamGobbler) {
                    streamGobbler.wait(1);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
    }

    public static int execute(String command) {
        return new ProcessCall(command).executeAndWait().getExitCode();
    }

}

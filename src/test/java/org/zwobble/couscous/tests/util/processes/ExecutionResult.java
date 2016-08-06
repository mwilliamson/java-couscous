package org.zwobble.couscous.tests.util.processes;

public class ExecutionResult {
    private final int exitCode;
    private final String stdout;
    private final String stderr;

    public ExecutionResult(int exitCode, String stdout, String stderr) {
        this.exitCode = exitCode;
        this.stdout = stdout;
        this.stderr = stderr;
    }

    public int getExitCode() {
        return exitCode;
    }

    public String getStdout() {
        return stdout;
    }

    public String getStderr() {
        return stderr;
    }

    public void assertSuccess() {
        if (exitCode != 0) {
            throw new RuntimeException("stderr was: " + stderr);
        }
    }
}

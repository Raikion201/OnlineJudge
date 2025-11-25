package com.codeexecutor.service;

import com.judgeservice.dto.JudgeExecuteRequest;
import com.judgeservice.dto.JudgeResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class CodeExecutionService {

    private static final Logger log = LoggerFactory.getLogger(CodeExecutionService.class);
    private static final String WORK_DIR = "/tmp/code-executor";
    private static final long DEFAULT_TIME_LIMIT_MS = 5000;
    private static final long DEFAULT_MEMORY_LIMIT_KB = 256 * 1024; // 256MB

    public CodeExecutionService() {
        // Create work directory
        try {
            Path workPath = Paths.get(WORK_DIR);
            if (!Files.exists(workPath)) {
                Files.createDirectories(workPath);
            }
        } catch (IOException e) {
            log.error("Failed to create work directory: {}", WORK_DIR, e);
        }
    }

    public JudgeResult executeCode(JudgeExecuteRequest request) {
        log.info("Executing code for submission {} test case {}", 
                request.getSubmissionId(), request.getTestCaseId());

        String language = request.getLanguage().toLowerCase();
        Path workDir = null;
        Path codeFile = null;

        try {
            // Create unique work directory for this execution
            workDir = createWorkDirectory(request.getSubmissionId(), request.getTestCaseId());
            codeFile = createCodeFile(workDir, request.getCode(), language);

            // Compile if needed
            CompilationResult compilationResult = compileCode(codeFile, language, workDir);
            if (!compilationResult.isSuccess()) {
                return createErrorResult(request, "COMPILATION_ERROR", 
                        compilationResult.getErrorMessage(), 0L, 0L);
            }

            // Execute code with resource limits
            ExecutionResult executionResult = executeCodeWithLimits(
                    codeFile, language, request.getInput(), 
                    request.getTimeLimit(), request.getMemoryLimit(), workDir);

            // Compare output
            String verdict = compareOutput(executionResult.getOutput(), request.getExpectedOutput());
            if (!"ACCEPTED".equals(verdict) && executionResult.getErrorMessage() != null) {
                // If there was an execution error, use that verdict
                verdict = executionResult.getVerdict();
            }

            return new JudgeResult(
                    request.getSubmissionId(),
                    request.getTestCaseId(),
                    verdict,
                    executionResult.getOutput(),
                    executionResult.getErrorMessage(),
                    executionResult.getExecutionTime(),
                    executionResult.getMemoryUsed()
            );

        } catch (Exception e) {
            log.error("Error executing code for submission {} test case {}", 
                    request.getSubmissionId(), request.getTestCaseId(), e);
            return createErrorResult(request, "SYSTEM_ERROR", 
                    "System error: " + e.getMessage(), 0L, 0L);
        } finally {
            // Cleanup
            cleanup(workDir);
        }
    }

    private Path createWorkDirectory(Long submissionId, Long testCaseId) throws IOException {
        String dirName = "sub_" + submissionId + "_tc_" + testCaseId + "_" + System.currentTimeMillis();
        Path workDir = Paths.get(WORK_DIR, dirName);
        Files.createDirectories(workDir);
        return workDir;
    }

    private Path createCodeFile(Path workDir, String code, String language) throws IOException {
        String extension = getFileExtension(language);
        String fileName = "Main" + extension;
        Path codeFile = workDir.resolve(fileName);
        Files.write(codeFile, code.getBytes());
        
        // Make file executable if needed
        try {
            Set<PosixFilePermission> perms = new HashSet<>();
            perms.add(PosixFilePermission.OWNER_READ);
            perms.add(PosixFilePermission.OWNER_WRITE);
            perms.add(PosixFilePermission.OWNER_EXECUTE);
            Files.setPosixFilePermissions(codeFile, perms);
        } catch (UnsupportedOperationException e) {
            // Windows doesn't support PosixFilePermission, skip
            log.debug("PosixFilePermission not supported, skipping");
        }
        
        return codeFile;
    }

    private String getFileExtension(String language) {
        return switch (language.toLowerCase()) {
            case "java" -> ".java";
            case "python", "py" -> ".py";
            case "cpp", "c++" -> ".cpp";
            case "c" -> ".c";
            case "javascript", "js" -> ".js";
            case "go" -> ".go";
            default -> ".txt";
        };
    }

    private CompilationResult compileCode(Path codeFile, String language, Path workDir) {
        if (!requiresCompilation(language)) {
            return new CompilationResult(true, null);
        }

        try {
            List<String> command = getCompileCommand(codeFile, language, workDir);
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(workDir.toFile());
            pb.redirectErrorStream(true);

            Process process = pb.start();
            String output = readProcessOutput(process);
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                return new CompilationResult(false, output);
            }
            return new CompilationResult(true, null);

        } catch (Exception e) {
            log.error("Compilation error", e);
            return new CompilationResult(false, "Compilation failed: " + e.getMessage());
        }
    }

    private boolean requiresCompilation(String language) {
        return switch (language.toLowerCase()) {
            case "java", "cpp", "c++", "c", "go" -> true;
            case "python", "py", "javascript", "js" -> false;
            default -> false;
        };
    }

    private List<String> getCompileCommand(Path codeFile, String language, Path workDir) {
        String fileName = codeFile.getFileName().toString();
        return switch (language.toLowerCase()) {
            case "java" -> Arrays.asList("javac", fileName);
            case "cpp", "c++" -> Arrays.asList("g++", "-o", "main", fileName, "-std=c++17");
            case "c" -> Arrays.asList("gcc", "-o", "main", fileName);
            case "go" -> Arrays.asList("go", "build", "-o", "main", fileName);
            default -> Collections.emptyList();
        };
    }

    private ExecutionResult executeCodeWithLimits(Path codeFile, String language, 
                                                  String input, Integer timeLimit, 
                                                  Integer memoryLimit, Path workDir) {
        long timeLimitMs = timeLimit != null ? timeLimit : DEFAULT_TIME_LIMIT_MS;
        long memoryLimitKb = memoryLimit != null ? memoryLimit * 1024L : DEFAULT_MEMORY_LIMIT_KB;

        try {
            List<String> command = getExecuteCommand(codeFile, language, workDir);
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(workDir.toFile());
            pb.redirectErrorStream(true);

            // Set resource limits using ulimit (Unix/Linux)
            // Note: ulimit needs to be set in the shell that executes the command
            if (System.getProperty("os.name").toLowerCase().contains("linux") || 
                System.getProperty("os.name").toLowerCase().contains("unix")) {
                // Use bash to set ulimit and execute
                List<String> bashCommand = new ArrayList<>();
                bashCommand.add("bash");
                bashCommand.add("-c");
                
                // Build ulimit command with proper quoting
                StringBuilder ulimitCmd = new StringBuilder();
                ulimitCmd.append("ulimit -t ").append(Math.max(1, timeLimitMs / 1000)); // CPU time in seconds (min 1)
                ulimitCmd.append(" && ulimit -v ").append(memoryLimitKb); // Virtual memory in KB
                ulimitCmd.append(" && cd ").append(workDir.toString());
                ulimitCmd.append(" && ");
                
                // Add actual execution command with proper quoting
                for (int i = 0; i < command.size(); i++) {
                    if (i > 0) ulimitCmd.append(" ");
                    String arg = command.get(i);
                    // Quote arguments that might contain spaces
                    if (arg.contains(" ") || arg.contains("$")) {
                        ulimitCmd.append("'").append(arg.replace("'", "'\"'\"'")).append("'");
                    } else {
                        ulimitCmd.append(arg);
                    }
                }
                
                bashCommand.add(ulimitCmd.toString());
                pb.command(bashCommand);
            }

            long startTime = System.currentTimeMillis();
            Process process = pb.start();

            // Write input to process
            if (input != null && !input.isEmpty()) {
                try (BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(process.getOutputStream()))) {
                    writer.write(input);
                    writer.flush();
                }
            }

            // Read output with timeout
            StringBuilder output = new StringBuilder();
            StringBuilder errorOutput = new StringBuilder();
            
            boolean finished = process.waitFor(timeLimitMs, TimeUnit.MILLISECONDS);
            long executionTime = System.currentTimeMillis() - startTime;

            if (!finished) {
                process.destroyForcibly();
                return new ExecutionResult("TIME_LIMIT_EXCEEDED", "", 
                        "Time limit exceeded", executionTime, 0L);
            }

            // Read stdout
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (output.length() > 0) output.append("\n");
                    output.append(line);
                }
            }

            // Read stderr
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (errorOutput.length() > 0) errorOutput.append("\n");
                    errorOutput.append(line);
                }
            }

            int exitCode = process.exitValue();
            String verdict = exitCode != 0 ? "RUNTIME_ERROR" : null;
            String errorMessage = exitCode != 0 ? errorOutput.toString() : null;

            // Check memory usage (simplified - in production, use proper monitoring)
            long memoryUsed = estimateMemoryUsage(process);

            return new ExecutionResult(verdict, output.toString(), 
                    errorMessage, executionTime, memoryUsed);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new ExecutionResult("TIME_LIMIT_EXCEEDED", "", 
                    "Execution interrupted", System.currentTimeMillis(), 0L);
        } catch (Exception e) {
            log.error("Execution error", e);
            return new ExecutionResult("RUNTIME_ERROR", "", 
                    "Runtime error: " + e.getMessage(), 0L, 0L);
        }
    }

    private List<String> getExecuteCommand(Path codeFile, String language, Path workDir) {
        String fileName = codeFile.getFileName().toString();
        return switch (language.toLowerCase()) {
            case "java" -> {
                // Extract class name (assumes class name matches file name without extension)
                String className = fileName.substring(0, fileName.lastIndexOf('.'));
                yield Arrays.asList("java", "-cp", workDir.toString(), className);
            }
            case "cpp", "c++", "c" -> Arrays.asList("./main");
            case "python", "py" -> Arrays.asList("python3", codeFile.toString());
            case "javascript", "js" -> Arrays.asList("node", codeFile.toString());
            case "go" -> Arrays.asList("./main");
            default -> Arrays.asList("./" + fileName);
        };
    }

    private long estimateMemoryUsage(Process process) {
        // Simplified memory estimation
        // In production, use proper process monitoring (e.g., /proc/<pid>/status on Linux)
        try {
            if (System.getProperty("os.name").toLowerCase().contains("linux")) {
                Path statusFile = Paths.get("/proc", String.valueOf(process.pid()), "status");
                if (Files.exists(statusFile)) {
                    List<String> lines = Files.readAllLines(statusFile);
                    for (String line : lines) {
                        if (line.startsWith("VmRSS:")) {
                            String[] parts = line.split("\\s+");
                            if (parts.length >= 2) {
                                return Long.parseLong(parts[1]); // Returns KB
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Could not read memory usage", e);
        }
        return 0L;
    }

    private String compareOutput(String actual, String expected) {
        if (actual == null) actual = "";
        if (expected == null) expected = "";

        // Normalize line endings and trim
        String normalizedActual = actual.replace("\r\n", "\n").trim();
        String normalizedExpected = expected.replace("\r\n", "\n").trim();

        if (normalizedActual.equals(normalizedExpected)) {
            return "ACCEPTED";
        } else {
            return "WRONG_ANSWER";
        }
    }

    private JudgeResult createErrorResult(JudgeExecuteRequest request, String verdict, 
                                          String errorMessage, long executionTime, long memoryUsed) {
        return new JudgeResult(
                request.getSubmissionId(),
                request.getTestCaseId(),
                verdict,
                "",
                errorMessage,
                executionTime,
                memoryUsed
        );
    }

    private void cleanup(Path workDir) {
        if (workDir != null) {
            try {
                Files.walk(workDir)
                        .sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                log.warn("Failed to delete file: {}", path, e);
                            }
                        });
            } catch (IOException e) {
                log.error("Failed to cleanup work directory: {}", workDir, e);
            }
        }
    }

    private String readProcessOutput(Process process) throws IOException {
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (output.length() > 0) output.append("\n");
                output.append(line);
            }
        }
        return output.toString();
    }

    // Helper classes
    private static class CompilationResult {
        private final boolean success;
        private final String errorMessage;

        public CompilationResult(boolean success, String errorMessage) {
            this.success = success;
            this.errorMessage = errorMessage;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    private static class ExecutionResult {
        private final String verdict;
        private final String output;
        private final String errorMessage;
        private final long executionTime;
        private final long memoryUsed;

        public ExecutionResult(String verdict, String output, String errorMessage, 
                              long executionTime, long memoryUsed) {
            this.verdict = verdict;
            this.output = output;
            this.errorMessage = errorMessage;
            this.executionTime = executionTime;
            this.memoryUsed = memoryUsed;
        }

        public String getVerdict() {
            return verdict;
        }

        public String getOutput() {
            return output;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public long getExecutionTime() {
            return executionTime;
        }

        public long getMemoryUsed() {
            return memoryUsed;
        }
    }
}


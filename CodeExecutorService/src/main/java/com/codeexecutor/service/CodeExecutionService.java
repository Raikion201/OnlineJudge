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
        log.info("Executing code for submission {} test case {}, leetcodeStyle={}",
                request.getSubmissionId(), request.getTestCaseId(), request.isLeetcodeStyle());

        String language = request.getLanguage().toLowerCase();
        Path workDir = null;
        Path codeFile = null;

        try {
            // Create unique work directory for this execution
            workDir = createWorkDirectory(request.getSubmissionId(), request.getTestCaseId());

            // Create code file(s) - for LeetCode-style, this creates both Solution.java and Main.java
            codeFile = createCodeFile(workDir, request.getCode(), language,
                    request.isLeetcodeStyle(), request.getFunctionName(), request.getFunctionSignature());

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
            String verdict = compareOutput(executionResult.output(), request.getExpectedOutput());
            if (!"ACCEPTED".equals(verdict) && executionResult.errorMessage() != null) {
                // If there was an execution error, use that verdict
                verdict = executionResult.status();
            }

            return new JudgeResult(
                    request.getSubmissionId(),
                    request.getTestCaseId(),
                    verdict,
                    executionResult.output(),
                    executionResult.errorMessage(),
                    executionResult.executionTime(),
                    executionResult.memoryUsage()
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
        return createCodeFile(workDir, code, language, false, null, null);
    }

    /**
     * Create code file(s) for execution.
     * For LeetCode-style Java problems, this creates both Solution.java and Main.java
     */
    private Path createCodeFile(Path workDir, String code, String language,
                                 boolean leetcodeStyle, String functionName, String functionSignature) throws IOException {
        String extension = getFileExtension(language);
        String fileName;
        String finalCode = code;

        // For Java LeetCode-style problems
        if ("java".equalsIgnoreCase(language) && leetcodeStyle && functionName != null && functionSignature != null) {
            // User writes a Solution class, we create a Main wrapper
            String className = extractJavaPublicClassName(code);

            // Ensure user's class is named Solution
            if (className != null && !className.equals("Solution")) {
                finalCode = code.replaceFirst(
                    "public\\s+class\\s+" + className,
                    "public class Solution"
                );
            }

            // Add common imports if not already present
            String solutionCode = addJavaImportsIfNeeded(finalCode);

            // Write Solution.java
            Path solutionFile = workDir.resolve("Solution.java");
            Files.write(solutionFile, solutionCode.getBytes());
            log.debug("Created Solution.java for LeetCode-style problem");

            // Generate and write Main.java wrapper
            String mainWrapper = generateJavaMainWrapper(functionName, functionSignature);
            Path mainFile = workDir.resolve("Main.java");
            Files.write(mainFile, mainWrapper.getBytes());
            log.debug("Created Main.java wrapper with function: {} signature: {}", functionName, functionSignature);

            // Return Main.java as the entry point (it will be compiled along with Solution.java)
            return mainFile;
        }

        // For traditional stdin/stdout style Java problems
        if ("java".equalsIgnoreCase(language)) {
            String className = extractJavaPublicClassName(code);
            if (className != null && !className.equals("Main")) {
                // Rename the public class to Main for consistency
                finalCode = code.replaceFirst(
                    "public\\s+class\\s+" + className,
                    "public class Main"
                );
                fileName = "Main" + extension;
            } else if (className != null) {
                fileName = className + extension;
            } else {
                // No public class found, use Main
                fileName = "Main" + extension;
            }
        } else {
            fileName = "Main" + extension;
        }

        Path codeFile = workDir.resolve(fileName);
        Files.write(codeFile, finalCode.getBytes());

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

    /**
     * Extract the public class name from Java code
     */
    private String extractJavaPublicClassName(String code) {
        // Pattern to match "public class ClassName"
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "public\\s+class\\s+(\\w+)"
        );
        java.util.regex.Matcher matcher = pattern.matcher(code);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * Add common Java imports to user code if not already present
     */
    private String addJavaImportsIfNeeded(String code) {
        // Common imports needed for LeetCode-style problems
        String commonImports = """
            import java.util.*;
            import java.util.stream.*;
            import java.math.*;

            """;

        // Check if code already has imports
        if (code.trim().startsWith("import ") || code.trim().startsWith("package ")) {
            // User has their own imports, don't add
            return code;
        }

        // Add imports before the class definition
        return commonImports + code;
    }

    /**
     * Generate Java Main wrapper for LeetCode-style Solution class
     * This wrapper parses input, calls the Solution method, and prints the result
     */
    private String generateJavaMainWrapper(String functionName, String functionSignature) {
        // Parse function signature: e.g., "int twoSum(int[] nums, int target)" or "int add(int a, int b)"
        // Format: returnType functionName(paramType1 paramName1, paramType2 paramName2, ...)

        StringBuilder wrapper = new StringBuilder();
        wrapper.append("import java.util.*;\n");
        wrapper.append("import java.io.*;\n\n");
        wrapper.append("public class Main {\n");
        wrapper.append("    public static void main(String[] args) {\n");
        wrapper.append("        Scanner scanner = new Scanner(System.in);\n");
        wrapper.append("        Solution solution = new Solution();\n\n");

        // Parse function signature to extract return type and parameters
        FunctionInfo funcInfo = parseFunctionSignature(functionSignature);

        if (funcInfo != null) {
            // Generate input parsing code for each parameter
            List<String> paramNames = new ArrayList<>();
            for (ParameterInfo param : funcInfo.parameters) {
                String parseCode = generateInputParseCode(param.type, param.name, "scanner");
                wrapper.append("        ").append(parseCode).append("\n");
                paramNames.add(param.name);
            }

            wrapper.append("\n");

            // Call solution method and print result
            String callParams = String.join(", ", paramNames);
            if ("void".equals(funcInfo.returnType)) {
                wrapper.append("        solution.").append(functionName).append("(").append(callParams).append(");\n");
            } else {
                wrapper.append("        ").append(funcInfo.returnType).append(" result = solution.")
                       .append(functionName).append("(").append(callParams).append(");\n");
                wrapper.append("        ").append(generateOutputCode(funcInfo.returnType, "result")).append("\n");
            }
        } else {
            // Fallback if parsing fails - simple case with no parameters
            wrapper.append("        // Could not parse function signature, using simple call\n");
            wrapper.append("        System.out.println(solution.").append(functionName).append("());\n");
        }

        wrapper.append("    }\n\n");

        // Add helper methods for parsing complex types
        wrapper.append(getHelperMethods());

        wrapper.append("}\n");

        return wrapper.toString();
    }

    /**
     * Parse function signature to extract return type and parameters
     */
    private FunctionInfo parseFunctionSignature(String signature) {
        if (signature == null || signature.isEmpty()) {
            return null;
        }

        try {
            // Pattern: returnType functionName(params)
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                "(\\S+)\\s+(\\w+)\\s*\\((.*)\\)"
            );
            java.util.regex.Matcher matcher = pattern.matcher(signature.trim());

            if (matcher.find()) {
                String returnType = matcher.group(1);
                String funcName = matcher.group(2);
                String paramsStr = matcher.group(3).trim();

                List<ParameterInfo> params = new ArrayList<>();
                if (!paramsStr.isEmpty()) {
                    // Split by comma, handling array types carefully
                    String[] paramParts = paramsStr.split(",");
                    for (String paramPart : paramParts) {
                        paramPart = paramPart.trim();
                        // Find last space to split type and name
                        int lastSpace = paramPart.lastIndexOf(' ');
                        if (lastSpace > 0) {
                            String type = paramPart.substring(0, lastSpace).trim();
                            String name = paramPart.substring(lastSpace + 1).trim();
                            params.add(new ParameterInfo(type, name));
                        }
                    }
                }

                return new FunctionInfo(returnType, funcName, params);
            }
        } catch (Exception e) {
            log.error("Error parsing function signature: {}", signature, e);
        }
        return null;
    }

    /**
     * Generate code to parse input for a given parameter type
     */
    private String generateInputParseCode(String type, String name, String scannerVar) {
        return switch (type) {
            case "int" -> "int " + name + " = " + scannerVar + ".nextInt();";
            case "long" -> "long " + name + " = " + scannerVar + ".nextLong();";
            case "double" -> "double " + name + " = " + scannerVar + ".nextDouble();";
            case "float" -> "float " + name + " = " + scannerVar + ".nextFloat();";
            case "String" -> "String " + name + " = " + scannerVar + ".next();";
            case "boolean" -> "boolean " + name + " = " + scannerVar + ".nextBoolean();";
            case "int[]" -> "int[] " + name + " = parseIntArray(" + scannerVar + ".nextLine().trim());";
            case "long[]" -> "long[] " + name + " = parseLongArray(" + scannerVar + ".nextLine().trim());";
            case "double[]" -> "double[] " + name + " = parseDoubleArray(" + scannerVar + ".nextLine().trim());";
            case "String[]" -> "String[] " + name + " = parseStringArray(" + scannerVar + ".nextLine().trim());";
            case "int[][]" -> "int[][] " + name + " = parseInt2DArray(" + scannerVar + ");";
            case "List<Integer>" -> "List<Integer> " + name + " = parseIntList(" + scannerVar + ".nextLine().trim());";
            case "List<String>" -> "List<String> " + name + " = parseStringList(" + scannerVar + ".nextLine().trim());";
            case "List<List<Integer>>" -> "List<List<Integer>> " + name + " = parseInt2DList(" + scannerVar + ");";
            default -> "// Unknown type: " + type + "\nObject " + name + " = null;";
        };
    }

    /**
     * Generate code to output the result based on return type
     */
    private String generateOutputCode(String returnType, String varName) {
        return switch (returnType) {
            case "int", "long", "double", "float", "boolean", "String" ->
                    "System.out.println(" + varName + ");";
            case "int[]" -> "System.out.println(Arrays.toString(" + varName + "));";
            case "long[]" -> "System.out.println(Arrays.toString(" + varName + "));";
            case "double[]" -> "System.out.println(Arrays.toString(" + varName + "));";
            case "String[]" -> "System.out.println(Arrays.toString(" + varName + "));";
            case "int[][]" -> "System.out.println(Arrays.deepToString(" + varName + "));";
            case "List<Integer>", "List<String>", "List<List<Integer>>" ->
                    "System.out.println(" + varName + ");";
            default -> "System.out.println(" + varName + ");";
        };
    }

    /**
     * Get helper methods for parsing various input types
     */
    private String getHelperMethods() {
        return """
            // Helper methods for parsing input
            private static int[] parseIntArray(String s) {
                if (s == null || s.isEmpty()) return new int[0];
                s = s.replaceAll("[\\\\[\\\\]\\\\s]", "");
                if (s.isEmpty()) return new int[0];
                String[] parts = s.split(",");
                int[] result = new int[parts.length];
                for (int i = 0; i < parts.length; i++) {
                    result[i] = Integer.parseInt(parts[i].trim());
                }
                return result;
            }

            private static long[] parseLongArray(String s) {
                if (s == null || s.isEmpty()) return new long[0];
                s = s.replaceAll("[\\\\[\\\\]\\\\s]", "");
                if (s.isEmpty()) return new long[0];
                String[] parts = s.split(",");
                long[] result = new long[parts.length];
                for (int i = 0; i < parts.length; i++) {
                    result[i] = Long.parseLong(parts[i].trim());
                }
                return result;
            }

            private static double[] parseDoubleArray(String s) {
                if (s == null || s.isEmpty()) return new double[0];
                s = s.replaceAll("[\\\\[\\\\]\\\\s]", "");
                if (s.isEmpty()) return new double[0];
                String[] parts = s.split(",");
                double[] result = new double[parts.length];
                for (int i = 0; i < parts.length; i++) {
                    result[i] = Double.parseDouble(parts[i].trim());
                }
                return result;
            }

            private static String[] parseStringArray(String s) {
                if (s == null || s.isEmpty()) return new String[0];
                s = s.replaceAll("[\\\\[\\\\]]", "").trim();
                if (s.isEmpty()) return new String[0];
                String[] parts = s.split(",");
                for (int i = 0; i < parts.length; i++) {
                    parts[i] = parts[i].trim().replaceAll("^\\"|\\"$", "");
                }
                return parts;
            }

            private static List<Integer> parseIntList(String s) {
                int[] arr = parseIntArray(s);
                List<Integer> list = new ArrayList<>();
                for (int val : arr) list.add(val);
                return list;
            }

            private static List<String> parseStringList(String s) {
                return Arrays.asList(parseStringArray(s));
            }

            private static int[][] parseInt2DArray(Scanner scanner) {
                String line = scanner.nextLine().trim();
                // Format: [[1,2],[3,4]] or similar
                line = line.substring(1, line.length() - 1); // Remove outer brackets
                List<int[]> rows = new ArrayList<>();
                int start = 0;
                int depth = 0;
                for (int i = 0; i < line.length(); i++) {
                    if (line.charAt(i) == '[') depth++;
                    else if (line.charAt(i) == ']') {
                        depth--;
                        if (depth == 0) {
                            String row = line.substring(start + 1, i);
                            rows.add(parseIntArray(row));
                            start = i + 2;
                        }
                    }
                }
                return rows.toArray(new int[0][]);
            }

            private static List<List<Integer>> parseInt2DList(Scanner scanner) {
                int[][] arr = parseInt2DArray(scanner);
                List<List<Integer>> result = new ArrayList<>();
                for (int[] row : arr) {
                    List<Integer> list = new ArrayList<>();
                    for (int val : row) list.add(val);
                    result.add(list);
                }
                return result;
            }
        """;
    }

    // Helper classes for function parsing
    private static class FunctionInfo {
        final String returnType;
        final String functionName;
        final List<ParameterInfo> parameters;

        FunctionInfo(String returnType, String functionName, List<ParameterInfo> parameters) {
            this.returnType = returnType;
            this.functionName = functionName;
            this.parameters = parameters;
        }
    }

    private static class ParameterInfo {
        final String type;
        final String name;

        ParameterInfo(String type, String name) {
            this.type = type;
            this.name = name;
        }
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
            case "java" -> {
                // Check if there are multiple Java files (LeetCode-style)
                try {
                    List<String> javaFiles = Files.list(workDir)
                            .filter(p -> p.toString().endsWith(".java"))
                            .map(p -> p.getFileName().toString())
                            .toList();
                    if (javaFiles.size() > 1) {
                        // Compile all Java files
                        List<String> cmd = new ArrayList<>();
                        cmd.add("javac");
                        cmd.addAll(javaFiles);
                        yield cmd;
                    }
                } catch (IOException e) {
                    log.debug("Error listing Java files", e);
                }
                // Single file
                yield Arrays.asList("javac", fileName);
            }
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
            } else {
                process.getOutputStream().close();
            }

            // Read output with timeout
            StringBuilder output = new StringBuilder();
            StringBuilder errorOutput = new StringBuilder();

            // Read output streams in separate threads to prevent blocking
            Thread outputThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        synchronized (output) {
                            if (output.length() > 0) output.append("\n");
                            output.append(line);
                        }
                    }
                } catch (IOException e) {
                    log.debug("Error reading output stream", e);
                }
            });

            Thread errorThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        synchronized (errorOutput) {
                            if (errorOutput.length() > 0) errorOutput.append("\n");
                            errorOutput.append(line);
                        }
                    }
                } catch (IOException e) {
                    log.debug("Error reading error stream", e);
                }
            });

            outputThread.start();
            errorThread.start();

            boolean finished = process.waitFor(timeLimitMs, TimeUnit.MILLISECONDS);
            long executionTime = System.currentTimeMillis() - startTime;

            if (!finished) {
                process.destroyForcibly();
                outputThread.interrupt();
                errorThread.interrupt();
                return new ExecutionResult("TIME_LIMIT_EXCEEDED", "",
                        "Time limit exceeded", executionTime, 0L);
            }

            // Wait for output threads to finish reading
            outputThread.join(1000);
            errorThread.join(1000);

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

    /**
     * Execute code for run code feature (test without submit) - legacy method
     */
    public ExecutionResult executeForRunCode(String code, String language, String input,
                                              int timeLimit, int memoryLimit) {
        return executeForRunCode(code, language, input, timeLimit, memoryLimit, false, null, null);
    }

    /**
     * Execute code for run code feature (test without submit) - with LeetCode-style support
     */
    public ExecutionResult executeForRunCode(String code, String language, String input,
                                              int timeLimit, int memoryLimit,
                                              boolean leetcodeStyle, String functionName, String functionSignature) {
        log.info("Executing code for run code feature, language={}, leetcodeStyle={}", language, leetcodeStyle);

        String lang = language.toLowerCase();
        Path workDir = null;
        Path codeFile = null;

        try {
            // Create unique work directory
            String dirName = "run_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
            workDir = Paths.get(WORK_DIR, dirName);
            Files.createDirectories(workDir);

            codeFile = createCodeFile(workDir, code, lang, leetcodeStyle, functionName, functionSignature);

            // Compile if needed
            CompilationResult compilationResult = compileCode(codeFile, lang, workDir);
            if (!compilationResult.isSuccess()) {
                return new ExecutionResult(
                        "COMPILATION_ERROR",
                        "",
                        compilationResult.getErrorMessage(),
                        0L,
                        0L
                );
            }

            // Execute code with resource limits
            InternalExecutionResult result = executeCodeWithLimitsInternal(
                    codeFile, lang, input, timeLimit, memoryLimit, workDir);

            String status = result.verdict != null ? result.verdict : "ACCEPTED";

            return new ExecutionResult(
                    status,
                    result.output,
                    result.errorMessage,
                    result.executionTime,
                    result.memoryUsed
            );

        } catch (Exception e) {
            log.error("Error executing code for run code", e);
            return new ExecutionResult(
                    "SYSTEM_ERROR",
                    "",
                    "System error: " + e.getMessage(),
                    0L,
                    0L
            );
        } finally {
            cleanup(workDir);
        }
    }

    private InternalExecutionResult executeCodeWithLimitsInternal(Path codeFile, String language,
                                                  String input, Integer timeLimit,
                                                  Integer memoryLimit, Path workDir) {
        long timeLimitMs = timeLimit != null ? timeLimit : DEFAULT_TIME_LIMIT_MS;
        long memoryLimitKb = memoryLimit != null ? memoryLimit * 1024L : DEFAULT_MEMORY_LIMIT_KB;

        try {
            List<String> command = getExecuteCommand(codeFile, language, workDir);
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(workDir.toFile());

            if (System.getProperty("os.name").toLowerCase().contains("linux") ||
                System.getProperty("os.name").toLowerCase().contains("unix")) {
                List<String> bashCommand = new ArrayList<>();
                bashCommand.add("bash");
                bashCommand.add("-c");

                StringBuilder ulimitCmd = new StringBuilder();
                ulimitCmd.append("ulimit -t ").append(Math.max(1, timeLimitMs / 1000));
                ulimitCmd.append(" && ulimit -v ").append(memoryLimitKb);
                ulimitCmd.append(" && cd ").append(workDir.toString());
                ulimitCmd.append(" && ");

                for (int i = 0; i < command.size(); i++) {
                    if (i > 0) ulimitCmd.append(" ");
                    String arg = command.get(i);
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
            } else {
                process.getOutputStream().close();
            }

            StringBuilder output = new StringBuilder();
            StringBuilder errorOutput = new StringBuilder();

            // Read output streams in separate threads to prevent blocking
            Thread outputThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        synchronized (output) {
                            if (output.length() > 0) output.append("\n");
                            output.append(line);
                        }
                    }
                } catch (IOException e) {
                    log.debug("Error reading output stream", e);
                }
            });

            Thread errorThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        synchronized (errorOutput) {
                            if (errorOutput.length() > 0) errorOutput.append("\n");
                            errorOutput.append(line);
                        }
                    }
                } catch (IOException e) {
                    log.debug("Error reading error stream", e);
                }
            });

            outputThread.start();
            errorThread.start();

            boolean finished = process.waitFor(timeLimitMs, TimeUnit.MILLISECONDS);
            long executionTime = System.currentTimeMillis() - startTime;

            if (!finished) {
                process.destroyForcibly();
                outputThread.interrupt();
                errorThread.interrupt();
                return new InternalExecutionResult("TIME_LIMIT_EXCEEDED", "",
                        "Time limit exceeded", executionTime, 0L);
            }

            // Wait for output threads to finish reading
            outputThread.join(1000);
            errorThread.join(1000);

            int exitCode = process.exitValue();
            String verdict = exitCode != 0 ? "RUNTIME_ERROR" : null;
            String errorMessage = exitCode != 0 ? errorOutput.toString() : null;

            long memoryUsed = estimateMemoryUsage(process);

            return new InternalExecutionResult(verdict, output.toString(),
                    errorMessage, executionTime, memoryUsed);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new InternalExecutionResult("TIME_LIMIT_EXCEEDED", "",
                    "Execution interrupted", System.currentTimeMillis(), 0L);
        } catch (Exception e) {
            log.error("Execution error", e);
            return new InternalExecutionResult("RUNTIME_ERROR", "",
                    "Runtime error: " + e.getMessage(), 0L, 0L);
        }
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

    private static class InternalExecutionResult {
        final String verdict;
        final String output;
        final String errorMessage;
        final long executionTime;
        final long memoryUsed;

        InternalExecutionResult(String verdict, String output, String errorMessage,
                              long executionTime, long memoryUsed) {
            this.verdict = verdict;
            this.output = output;
            this.errorMessage = errorMessage;
            this.executionTime = executionTime;
            this.memoryUsed = memoryUsed;
        }
    }

    // Public record for external use
    public record ExecutionResult(
            String status,
            String output,
            String errorMessage,
            Long executionTime,
            Long memoryUsage
    ) {}
}


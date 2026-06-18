import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.nio.file.StandardOpenOption;

public class Main {

    private static String[] parseCommand(String input) {
        java.util.ArrayList<String> args = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inSingle = false;
        boolean inDouble = false;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '\\' && !inSingle) {
                if (inDouble) {
                    if (i + 1 < input.length()) {
                        char next = input.charAt(i + 1);
                        if (next == '"' || next == '\\') {
                            current.append(next);
                            i++;
                        } else {
                            current.append(next);
                            i++;
                        }
                    }
                    continue;
                }
                if (i + 1 < input.length()) {
                    current.append(input.charAt(i + 1));
                    i++;
                }
                continue;
            }
            if (c == '\'' && !inDouble) {
                inSingle = !inSingle;
                continue;
            }
            if (c == '"' && !inSingle) {
                inDouble = !inDouble;
                continue;
            }
            if (Character.isWhitespace(c) && !inSingle && !inDouble) {
                if (current.length() > 0) {
                    args.add(current.toString());
                    current.setLength(0);
                }
                continue;
            }
            current.append(c);
        }
        if (current.length() > 0) {
            args.add(current.toString());
        }
        return args.toArray(new String[0]);
    }

    private static void ensureParentDirs(String filePath) throws Exception {
        Path p = Path.of(filePath);
        if (p.getParent() != null) {
            Files.createDirectories(p.getParent());
        }
    }

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        File currentDirectory = new File(System.getProperty("user.dir"));
        while (true) {
            System.out.print("$ ");
            String input = sc.nextLine();
            String[] parts = parseCommand(input);
            String stdoutFile = null;
            String stderrFile = null;
            boolean appendStdout = false;
            boolean appendStderr = false;
            java.util.ArrayList<String> commandParts = new java.util.ArrayList<>();

            for (int i = 0; i < parts.length; i++) {
                if (parts[i].equals(">") || parts[i].equals("1>")) {
                    if (i + 1 < parts.length) {
                        stdoutFile = parts[i + 1];
                        appendStdout = false;
                    }
                    i++;
                    continue;
                }
                if (parts[i].equals(">>") || parts[i].equals("1>>")) {
                    if (i + 1 < parts.length) {
                        stdoutFile = parts[i + 1];
                        appendStdout = true;
                    }
                    i++;
                    continue;
                }
                if (parts[i].equals("2>")) {
                    if (i + 1 < parts.length) {
                        stderrFile = parts[i + 1];
                        appendStderr = false;
                    }
                    i++;
                    continue;
                }
                if (parts[i].equals("2>>")) {
                    if (i + 1 < parts.length) {
                        stderrFile = parts[i + 1];
                        appendStderr = true;
                    }
                    i++;
                    continue;
                }
                commandParts.add(parts[i]);
            }
            parts = commandParts.toArray(new String[0]);

            if (stdoutFile != null) {
                ensureParentDirs(stdoutFile);
                if (!appendStdout) {
                    Files.writeString(Path.of(stdoutFile), "",
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                } else {
                    Files.writeString(Path.of(stdoutFile), "",
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                }
            }
            if (stderrFile != null) {
                ensureParentDirs(stderrFile);
                if (!appendStderr) {
                    Files.writeString(Path.of(stderrFile), "",
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                } else {
                    Files.writeString(Path.of(stderrFile), "",
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                }
            }

            if (parts.length == 0) {
                continue;
            }
            String cmd = parts[0];

            if (cmd.equals("exit")) {
                break;
            }
            else if (cmd.equals("pwd")) {
                String output = currentDirectory.getAbsolutePath() + System.lineSeparator();
                if (stdoutFile != null) {
                    if (appendStdout) {
                        Files.writeString(Path.of(stdoutFile), output,
                            StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                    } else {
                        Files.writeString(Path.of(stdoutFile), output);
                    }
                } else {
                    System.out.print(output);
                }
            }
            else if (cmd.equals("cd")) {
                if (parts.length < 2) continue;
                String path = parts[1];
                File newDir;
                if (path.equals("~")) {
                    newDir = new File(System.getenv("HOME"));
                } else if (path.startsWith("/")) {
                    newDir = new File(path);
                } else {
                    newDir = new File(currentDirectory, path);
                }
                if (newDir.exists() && newDir.isDirectory()) {
                    currentDirectory = newDir.getCanonicalFile();
                } else {
                    String error = "cd: " + path + ": No such file or directory" + System.lineSeparator();
                    if (stderrFile != null) {
                        if (appendStderr) {
                            Files.writeString(Path.of(stderrFile), error,
                                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                        } else {
                            Files.writeString(Path.of(stderrFile), error);
                        }
                    } else {
                        System.err.print(error);
                    }
                }
            }
            else if (cmd.equals("echo")) {
                StringBuilder output = new StringBuilder();
                for (int i = 1; i < parts.length; i++) {
                    if (i > 1) output.append(" ");
                    output.append(parts[i]);
                }
                if (stdoutFile != null) {
                    if (appendStdout) {
                        Files.writeString(Path.of(stdoutFile), output.toString() + System.lineSeparator(),
                            StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                    } else {
                        Files.writeString(Path.of(stdoutFile), output.toString() + System.lineSeparator());
                    }
                } else {
                    System.out.println(output);
                }
            }
            else if (cmd.equals("type")) {
                if (parts.length < 2) continue;
                String t = parts[1];
                String output = null;
                String error = null;
                if (t.equals("echo") || t.equals("exit") ||
                    t.equals("type") || t.equals("pwd") || t.equals("cd")) {
                    output = t + " is a shell builtin" + System.lineSeparator();
                } else {
                    String path = System.getenv("PATH");
                    boolean found = false;
                    for (String dir : path.split(File.pathSeparator)) {
                        File file = new File(dir, t);
                        if (file.exists() && file.canExecute()) {
                            output = t + " is " + file.getAbsolutePath() + System.lineSeparator();
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        error = t + ": not found" + System.lineSeparator();
                    }
                }
                if (output != null) {
                    if (stdoutFile != null) {
                        if (appendStdout) {
                            Files.writeString(Path.of(stdoutFile), output,
                                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                        } else {
                            Files.writeString(Path.of(stdoutFile), output);
                        }
                    } else {
                        System.out.print(output);
                    }
                }
                if (error != null) {
                    if (stderrFile != null) {
                        if (appendStderr) {
                            Files.writeString(Path.of(stderrFile), error,
                                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                        } else {
                            Files.writeString(Path.of(stderrFile), error);
                        }
                    } else {
                        System.err.print(error);
                    }
                }
            }
            else {
                boolean found = false;
                String path = System.getenv("PATH");
                for (String dir : path.split(File.pathSeparator)) {
                    File file = new File(dir, cmd);
                    if (file.exists() && file.canExecute()) {
                        ProcessBuilder pb = new ProcessBuilder(parts);
                        pb.directory(currentDirectory);
                        if (stdoutFile != null) {
                            if (appendStdout) {
                                pb.redirectOutput(ProcessBuilder.Redirect.appendTo(new File(stdoutFile)));
                            } else {
                                pb.redirectOutput(new File(stdoutFile));
                            }
                        }
                        if (stderrFile != null) {
                            if (appendStderr) {
                                pb.redirectError(ProcessBuilder.Redirect.appendTo(new File(stderrFile)));
                            } else {
                                pb.redirectError(new File(stderrFile));
                            }
                        }
                        Process process = pb.start();
                        if (stdoutFile == null) {
                            process.getInputStream().transferTo(System.out);
                        }
                        if (stderrFile == null) {
                            process.getErrorStream().transferTo(System.err);
                        }
                        process.waitFor();
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    String error = cmd + ": command not found" + System.lineSeparator();
                    if (stderrFile != null) {
                        if (appendStderr) {
                            Files.writeString(Path.of(stderrFile), error,
                                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                        } else {
                            Files.writeString(Path.of(stderrFile), error);
                        }
                    } else {
                        System.err.print(error);
                    }
                }
            }
        }
    }
}
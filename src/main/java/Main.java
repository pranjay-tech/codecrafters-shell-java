import java.io.File;
import java.util.Scanner;

public class Main {

    private static String[] parseCommand(String input) {
        java.util.ArrayList<String> args = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inSingle = false;
        boolean inDouble = false;
        // boolean escape = false;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            //BackSlash Handling
            if (c == '\\' && !inSingle) {
            // INSIDE DOUBLE QUOTES
                if (inDouble) {
                    if (i + 1 < input.length()) {
                        char next = input.charAt(i + 1);
                        if (next == '"' || next == '\\') {
                            current.append(next);
                            i++; // consume next char
                        } else {
                            current.append(next);
                            i++;
                        }
                    }
                    continue;
                }
                // OUTSIDE QUOTES
                if (i + 1 < input.length()) {
                    current.append(input.charAt(i + 1));
                    i++; // consume next char
                }
                continue;
            }
            // toggle Single if not in double
            if (c == '\'' && !inDouble) {
                inSingle = !inSingle;
                continue;
            }
            //toggle double if not in single
            if (c == '"' && !inSingle) {
                inDouble = !inDouble;
                continue;
            }
            //remove space outside the quotes
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


    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        File currentDirectory =
                new File(System.getProperty("user.dir"));
        while (true) {
            System.out.print("$ ");
            String input = sc.nextLine();
            String[] parts = parseCommand(input);
            String redirectFile = null;
            java.util.ArrayList<String> commandParts = new java.util.ArrayList<>();

            for (int i = 0; i < parts.length; i++) {
                if (parts[i].equals(">") || parts[i].equals("1>")) {
                    if (i + 1 < parts.length) {
                        redirectFile = parts[i + 1];
                    }
                    break;
                }
                commandParts.add(parts[i]);
            }
            parts = commandParts.toArray(new String[0]);
            if (parts.length == 0) {
                continue;
            }
            String cmd = parts[0];
            // exit
            if (cmd.equals("exit")) {
                break;
            }
            // pwd
            else if (cmd.equals("pwd")) {
                System.out.println(currentDirectory.getAbsolutePath());
            }
            // cd
            else if (cmd.equals("cd")) {
                if (parts.length < 2) continue;
                String path = parts[1];
                File newDir;
                if (path.equals("~")) {
                    newDir = new File(System.getenv("HOME"));
                }
                else if (path.startsWith("/")) {
                    newDir = new File(path);
                }
                else {
                    newDir = new File(currentDirectory, path);
                }
                if (newDir.exists() && newDir.isDirectory()) {
                    currentDirectory = newDir.getCanonicalFile();
                } else {
                    System.out.println("cd: " + path + ": No such file or directory");
                }
            }
            // echo
            else if (cmd.equals("echo")) {
                StringBuilder output = new StringBuilder();
                for (int i = 1; i < parts.length; i++) {
                    if (i > 1) output.append(" ");
                    output.append(parts[i]);
                }
                if (redirectFile != null) {
                    java.nio.file.Files.writeString(
                        java.nio.file.Path.of(redirectFile),
                        output.toString() + System.lineSeparator()
                    );
                } else {
                    System.out.println(output);
                }
            }
            // type
            else if (cmd.equals("type")) {
                if (parts.length < 2) continue;
                String t = parts[1];
                if (t.equals("echo") || t.equals("exit") || t.equals("type") || t.equals("pwd") || t.equals("cd")) {
                    System.out.println(t + " is a shell builtin");
                }
                else {
                    String path = System.getenv("PATH");
                    boolean found = false;
                    for (String dir : path.split(File.pathSeparator)) {
                        File file = new File(dir, t);
                        if (file.exists() && file.canExecute()) {
                            System.out.println(t + " is " + file.getAbsolutePath());
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        System.out.println(t + ": not found");
                    }
                }
            }
            // external commands
            else {
                boolean found = false;
                String path = System.getenv("PATH");
                for (String dir : path.split(File.pathSeparator)) {
                    File file = new File(dir, cmd);
                    if (file.exists() && file.canExecute()) {
                        ProcessBuilder pb = new ProcessBuilder(parts);
                        pb.directory(currentDirectory);
                        if (redirectFile != null) {
                            pb.redirectOutput(new File(redirectFile));
                        }
                        Process process = pb.start();
                        if (redirectFile == null) {
                            process.getInputStream().transferTo(System.out);
                        }
                        process.getErrorStream().transferTo(System.err);
                        process.waitFor();
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    System.out.println(cmd + ": command not found");
                }
            }
        }
    }
}
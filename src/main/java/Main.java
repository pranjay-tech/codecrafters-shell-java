import java.io.File;
import java.util.Scanner;

public class Main {

    private static String[] parseCommand(String input) {
        java.util.ArrayList<String> args = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inSingle = false;
        boolean inDouble = false;
        boolean escape = false;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            //Escape outside quotes
            if (!inSingle && !inDouble && escape) {
                current.append(c);
                escape = false;
                continue;
            }
            //Backslash outside quote
            if(c=='\\' && !inSingle && !inDouble) {
                escape = true;
                continue;
            }
            // toggle Single if not in double
            if (c == '\'' && !inDouble && !escape) {
                inSingle = !inSingle;
                continue;
            }
            //toggle double if not in single
            if (c == '"' && !inSingle && !escape) {
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
                for (int i = 1; i < parts.length; i++) {
                    if (i > 1) System.out.print(" ");
                    System.out.print(parts[i]);
                }
                System.out.println();
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
                        Process process = pb.start();
                        process.getInputStream().transferTo(System.out);
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
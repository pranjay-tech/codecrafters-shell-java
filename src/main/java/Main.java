import java.io.File;
import java.util.Scanner;


public class Main {
    private static String[] parseCommand(String input) {
        java.util.ArrayList<String> args = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inSingleQuote = false;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '\'') {
                inSingleQuote = !inSingleQuote;
            }
            else if (Character.isWhitespace(c) && !inSingleQuote) {
                if (current.length() > 0) {
                    args.add(current.toString());
                    current.setLength(0);
                }
            }
            else {
                current.append(c);
            }
        }
        if (current.length() > 0) {
            args.add(current.toString());
        }
        return args.toArray(new String[0]);
    }

    public static void main(String[] args) throws Exception {
        // TODO: Uncomment the code below to pass the first stage
        Scanner sc = new Scanner(System.in);
        

        File currentDirectory = new File(System.getProperty("user.dir"));
        while (true) {
            System.out.print("$ ");

            String input = sc.nextLine();

            String[] parts = parseCommand(input);

            if(parts.length == 0){
                continue;
            }
            if(input.equals("exit")){
                break;
            }
            else if (input.equals("pwd")){
                // System.out.println(System.getProperty("user.dir"));   //for pwd function
                System.out.println(currentDirectory.getAbsolutePath());  //for cd function
            }
            else if (input.startsWith("cd ")) {
                String path = input.substring(3);
                File newDir;
                if (path.equals("~")) {
                    String home = System.getenv("HOME");
                    newDir = new File(home);
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
            else if (parts[0].equals("echo")) {
                for (int i = 1; i < parts.length; i++) {
                    if (i > 1) {
                        System.out.print(" ");
                    }
                    System.out.print(parts[i]);
                }
                System.out.println();
            }
            else if (input.startsWith("type ")) {
                String cmd = input.substring(5);
                if (cmd.equals("echo") || cmd.equals("exit") || cmd.equals("type") || cmd.equals("pwd") || cmd.equals("cd")) {
                    System.out.println(cmd + " is a shell builtin");
                } 
                else{
                    String path = System.getenv("PATH");
                    boolean found = false;
                    
                    for(String dir : path.split(File.pathSeparator)){
                        File file = new File(dir, cmd);
                        if (file.exists() && file.canExecute()) {
                            System.out.println(cmd + " is " + file.getAbsolutePath());
                            found = true;
                            break;
                        }
                    }
                    if(!found){
                        System.out.println(cmd + ": not found");
                    }
                }
            }
            else {
                String path = System.getenv("PATH");
                boolean found = false;
                for (String dir : path.split(File.pathSeparator)) {
                    File file = new File(dir, parts[0]);
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
                    System.out.println(input + ": command not found");
                }
            }
        }
    }
}

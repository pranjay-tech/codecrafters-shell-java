import java.io.File;
import java.util.Scanner;

public class Main {
    private static File findExecutable(String cmd) {
        String path = System.getenv("PATH");
        for (String dir : path.split(File.pathSeparator)) {
            File file = new File(dir, cmd);
            if (file.exists() && file.canExecute()) {
                return file;
            }
        }
        return null;
    }
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.print("$ ");
            String input = sc.nextLine();
            if (input.equals("exit")) {
                break;
            }
            else if (input.equals("pwd")) {
                System.out.println(System.getProperty("user.dir"));
            }
            else if (input.startsWith("echo ")) {
                System.out.println(input.substring(5));
            }
            else if (input.startsWith("type ")) {
                String cmd = input.substring(5);
                if (cmd.equals("echo") || cmd.equals("exit") || cmd.equals("type") || cmd.equals("pwd")) {
                    System.out.println(cmd + " is a shell builtin");
                }
                else {
                    File exe = findExecutable(cmd);
                    if (exe != null) {
                        System.out.println(cmd + " is " + exe.getAbsolutePath());
                    } else {
                        System.out.println(cmd + ": not found");
                    }
                }
            }
            else {
                String[] parts = input.split(" ");
                File exe = findExecutable(parts[0]);
                if (exe != null) {
                    ProcessBuilder pb = new ProcessBuilder(parts);
                    Process process = pb.start();
                    process.getInputStream().transferTo(System.out);
                    process.waitFor();
                }
                else {
                    System.out.println(input + ": command not found");
                }
            }
        }
        sc.close();
    }
}
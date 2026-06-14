import java.io.File;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        // TODO: Uncomment the code below to pass the first stage
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.print("$ ");

            String input = sc.nextLine();
            if(input.equals("exit")){
                break;
            }
            else if (input.startsWith("echo ")) {
                System.out.println(input.substring(5));
            }
            else if (input.startsWith("type ")) {
                String cmd = input.substring(5);
                if (cmd.equals("echo") || cmd.equals("exit") || cmd.equals("type")) {
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
                System.out.println(input + ": command not found");
            }
        }
    }
}

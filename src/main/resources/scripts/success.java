import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Scanner;

public class RuntimeFunction {
    public static void main(String[] args) {
        Process proc;
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.print("请输入要发送的数据：");
            String input = scanner.nextLine(); // 从控制台读取输入文本 // Input data to be sent to the Python script

            String[] command = {"python", "C:\\Users\\86133\\IdeaProjects\\DBMS\\src\\main\\resources\\scripts\\main.py", input};
            proc = Runtime.getRuntime().exec(command);

            // Read Python script output
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
            in.close();

            // Read error output if any
            BufferedReader err = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            while ((line = err.readLine()) != null) {
                System.err.println(line);
            }
            err.close();

            proc.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

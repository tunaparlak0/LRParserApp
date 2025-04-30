package lr_Parstree;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

import java.io.*;
import java.util.*;

public class LRParser {
    private Map<String, String> actionTable = new HashMap<>();
    private Map<String, String> gotoTable = new HashMap<>();
    private List<String> inputTokens = new ArrayList<>();
    private Stack<String> stack = new Stack<>();
    private Stack<TreeNode> nodeStack = new Stack<>();

    public static void main(String[] args) {
        LRParser parser = new LRParser();
        try {
            parser.loadActionTable("ActionTable.txt");
            parser.loadGotoTable("GotoTable.txt");

            for (int i = 1; i <= 9; i++) {
                String inputFileName = "input" + i + ".txt";
                String outputFileName = "output" + i + ".txt";


                parser.loadInput(inputFileName);

                try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName))) {
                    writer.write("========== Parsing " + inputFileName + " ==========\n");
                    parser.parse(writer);
                }



                parser.reset();
            }
        } catch (IOException e) {
            System.out.println("Hata: " + e.getMessage());
        }
    }

    public void loadActionTable(String filename) throws IOException {
        InputStream is = getResourceAsStream(filename);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.trim().split("\\s+");
            if (parts.length == 3) {
                actionTable.put(parts[0] + "," + parts[1], parts[2]);
            }
        }
        br.close();
    }

    public void loadGotoTable(String filename) throws IOException {
        InputStream is = getResourceAsStream(filename);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.trim().split("\\s+");
            if (parts.length == 3) {
                gotoTable.put(parts[0] + "," + parts[1], parts[2]);
            }
        }
        br.close();
    }

    public void loadInput(String filename) throws IOException {
        InputStream is = getResourceAsStream(filename);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = br.readLine();
        if (line != null) {
            inputTokens.addAll(Arrays.asList(line.trim().split("\\s+")));
            inputTokens.add("$");
        }
        br.close();
    }

    public void parse(BufferedWriter writer) throws IOException {
        stack.push("0");
        nodeStack.clear();
        int index = 0;

        writer.write(String.format("%-30s %-30s %-10s\n", "STACK", "INPUT", "ACTION"));
        writer.newLine();

        while (true) {
            String state = stack.peek();
            String token = inputTokens.get(index);
            String key = state + "," + token;

            String action = actionTable.getOrDefault(key, "error");

            writeTrace(writer, stack, inputTokens, index, action);

            if (action.equals("acc")) {
                writer.write("Input accepted.\n");
                break;
            } else if (action.startsWith("s")) {
                stack.push(token);
                stack.push(action.substring(1));
                index++;
                nodeStack.push(new TreeNode(token));
            } else if (action.startsWith("r")) {
                int ruleNum = Integer.parseInt(action.substring(1));
                String lhs = getLHS(ruleNum);
                int rhsLen = getRHS(ruleNum).split(" ").length;

                for (int i = 0; i < rhsLen * 2; i++) stack.pop();
                List<TreeNode> children = new ArrayList<>();
                for (int i = 0; i < rhsLen; i++) {
                    children.add(nodeStack.pop());
                }
                Collections.reverse(children);
                TreeNode parent = new TreeNode(lhs);
                for (TreeNode child : children) {
                    parent.children.add(child);
                }
                nodeStack.push(parent);
                String prevState = stack.peek();
                stack.push(lhs);
                String gotoState = gotoTable.get(prevState + "," + lhs);
                if (gotoState == null) {
                    writer.write("Goto error.\n");
                    return;
                }
                stack.push(gotoState);
            } else {
                writer.write("Syntax error at token: " + token + "\n");
                break;
            }
        }
        if (!nodeStack.isEmpty()) {
            writer.newLine();
            writer.write("Parse tree:\n");
            ParseTreePrinter.printPaths(nodeStack.peek(), writer);
        }
    }

    private void writeTrace(BufferedWriter writer, Stack<String> stack, List<String> input, int index, String action) throws IOException {
        writer.write(String.format("%-30s %-30s %-10s\n",
                String.join(" ", stack),
                String.join(" ", input.subList(index, input.size())),
                action));
    }

    private String getLHS(int ruleNumber) {
        return switch (ruleNumber) {
            case 1, 2 -> "E";
            case 3, 4 -> "T";
            case 5, 6 -> "F";
            default -> "Unknown";
        };
    }

    private String getRHS(int ruleNumber) {
        return switch (ruleNumber) {
            case 1 -> "E + T";
            case 2 -> "T";
            case 3 -> "T * F";
            case 4 -> "F";
            case 5 -> "( E )";
            case 6 -> "id";
            default -> "";
        };
    }

    private InputStream getResourceAsStream(String filename) throws FileNotFoundException {
        InputStream is = getClass().getClassLoader().getResourceAsStream(filename);
        if (is == null) {
            throw new FileNotFoundException(filename + " not found in resources folder.");
        }
        return is;
    }

    // input veya stack sıfırlamak için reset() ekliyoruz
    public void reset() {
        stack.clear();
        inputTokens.clear();
        nodeStack.clear();
    }
}

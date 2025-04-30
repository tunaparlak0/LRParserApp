package lr_Parstree;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ParseTreePrinter {

    public static void printPaths(TreeNode node, BufferedWriter writer) throws IOException {
        print(node, writer, new ArrayList<>());
    }

    private static void print(TreeNode node, BufferedWriter writer, List<String> path) throws IOException {
        path.add(node.symbol);
        writer.write("/" + String.join("/", path));
        writer.newLine();
        for (TreeNode child : node.children) {
            print(child, writer, path);
        }
        path.remove(path.size() - 1);
    }
}
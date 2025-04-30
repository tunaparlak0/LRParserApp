package lr_Parstree;

import java.util.ArrayList;
import java.util.List;

public class TreeNode {
    public final String symbol;
    public final List<TreeNode> children = new ArrayList<>();

    public TreeNode(String symbol) {
        this.symbol = symbol;
    }
}
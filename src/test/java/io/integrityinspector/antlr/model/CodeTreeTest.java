package io.integrityinspector.antlr.model;

import com.github.tmatek.zhangshasha.TreeNode;
import com.github.tmatek.zhangshasha.TreeOperation;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class CodeTreeTest {

    @Test
    public void positionOfChildReturnsIndexForMatchingChild() {
        CodeTree parent = new CodeTree(1);
        CodeTree first = new CodeTree(2);
        CodeTree second = new CodeTree(3);
        parent.getChildren().add(first);
        parent.getChildren().add(second);

        assertEquals(1, parent.positionOfChild(second));
    }

    @Test
    public void positionOfChildReturnsZeroForMissingOrUnsupportedChild() {
        CodeTree parent = new CodeTree(1);
        parent.getChildren().add(new CodeTree(2));

        assertEquals(0, parent.positionOfChild(new CodeTree(99)));
        assertEquals(0, parent.positionOfChild(unsupportedTreeNode()));
    }

    @Test
    public void transformationCostIsOneForInsertAndDelete() {
        CodeTree tree = new CodeTree(1);

        assertEquals(1, tree.getTransformationCost(TreeOperation.OP_INSERT_NODE, new CodeTree(1)));
        assertEquals(1, tree.getTransformationCost(TreeOperation.OP_DELETE_NODE, new CodeTree(1)));
    }

    @Test
    public void transformationCostComparesStatesForChangeOperations() {
        CodeTree tree = new CodeTree(1);

        assertEquals(0, tree.getTransformationCost(TreeOperation.OP_RENAME_NODE, new CodeTree(1)));
        assertEquals(1, tree.getTransformationCost(TreeOperation.OP_RENAME_NODE, new CodeTree(2)));
    }

    @Test
    public void equalsCoversIdentityNullTypeAndFieldComparisons() {
        CodeTree tree = new CodeTree(1);

        assertTrue(tree.equals(tree));
        assertFalse(tree.equals(null));
        assertFalse(tree.equals("not a tree"));
        assertEquals(new CodeTree(1), tree);
        assertNotEquals(new CodeTree(2), tree);

        CodeTree withChild = new CodeTree(1);
        withChild.getChildren().add(new CodeTree(2));
        assertNotEquals(withChild, tree);

        CodeTree parentOne = new CodeTree(10);
        CodeTree parentTwo = new CodeTree(20);
        CodeTree childOne = new CodeTree(1);
        CodeTree childTwo = new CodeTree(1);
        childOne.setParent(parentOne);
        childTwo.setParent(parentTwo);
        assertNotEquals(childOne, childTwo);
    }

    @Test
    public void toStringIncludesNullAndPresentParentState() {
        CodeTree root = new CodeTree(1);
        CodeTree child = new CodeTree(2);
        child.setParent(root);

        assertTrue(root.toString().contains("parent state='null'"));
        assertTrue(child.toString().contains("parent state='1'"));
    }

    private TreeNode unsupportedTreeNode() {
        return new TreeNode() {
            @Override
            public List<? extends TreeNode> getChildren() {
                return Collections.emptyList();
            }

            @Override
            public TreeNode getParent() {
                return null;
            }

            @Override
            public int positionOfChild(TreeNode treeNode) {
                return 0;
            }

            @Override
            public int getTransformationCost(TreeOperation operation, TreeNode other) {
                return 0;
            }
        };
    }
}

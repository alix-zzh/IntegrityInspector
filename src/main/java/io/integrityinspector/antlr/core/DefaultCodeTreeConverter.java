package io.integrityinspector.antlr.core;

import io.integrityinspector.antlr.model.CodeTree;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

public class DefaultCodeTreeConverter implements CodeTreeConverter {
    @Override
    public CodeTree convertCodeTreeNode(ParseTree tree) {
        CodeTree node = new CodeTree(-1);
        if (tree instanceof ParserRuleContext) {
            parseParserRuleContext((ParserRuleContext) tree, node);
        }
        if (tree instanceof TerminalNode) {
            TerminalNode terminalNode = (TerminalNode) tree;
            node.setState(terminalNode.getSymbol().getType());
        }
        return node;
    }

    private void parseParserRuleContext(ParserRuleContext tree, CodeTree node) {
        for (int index = 0; index < tree.getChildCount(); index++) {
            CodeTree child = convertCodeTreeNode(tree.getChild(index));
            if (child == null) {
                break;
            }
            if (tree.getChildCount() == 1) {
                node.setState(child.getState());
                node.setChildren(child.getChildren());
                for (CodeTree adoptedChild : node.getChildren()) {
                    adoptedChild.setParent(node);
                }
            } else {
                node.getChildren().add(child);
                child.setParent(node);
            }
        }
    }
}

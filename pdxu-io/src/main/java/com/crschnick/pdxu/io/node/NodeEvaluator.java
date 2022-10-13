package com.crschnick.pdxu.io.node;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;

public class NodeEvaluator {

    private static final Context JAVASCRIPT_CONTEXT = Context.create("js");
    private static final NumberFormat FORMATTER = new DecimalFormat("#0.00");

    public static void evaluateArrayNode(ArrayNode arrayNode) {
        evaluateArrayNode(arrayNode, new NodeEnvironment(null, Map.of()));
    }

    public static void evaluateArrayNode(ArrayNode arrayNode, NodeEnvironment environment) {
        final NodeEnvironment[] currentEnvironment = {environment};
        arrayNode.forEach(
                (s, node) -> {
                    if (node.isValue()) {
                        var evaluated = evaluateValueNode(node.getValueNode(), currentEnvironment[0]);
                        if (evaluated.isValue() && evaluated != node) node.getValueNode().set(evaluated.getValueNode());
                        if (s != null && s.startsWith("@")) {
                            currentEnvironment[0].put(s.substring(1), evaluated);
                        }
                    } else if (node.isArray()) {
                        currentEnvironment[0] = currentEnvironment[0].copy(null);
                        evaluateArrayNode(node.getArrayNode(), currentEnvironment[0]);
                    }
                },
                true);
    }

    public static Node evaluateValueNode(ValueNode node, NodeEnvironment environment) {
        var expression = node.getInlineMathExpression();
        if (expression.isPresent()) {
            var string = expression.get();
            for (Map.Entry<String, Node> entry : environment.getVariables().entrySet()) {
                if (!entry.getValue().isValue()) {
                    continue;
                }

                string = string.replaceAll(entry.getKey(), entry.getValue().getValueNode().getString());
            }

            try  {
                Value eval = JAVASCRIPT_CONTEXT.eval("js", string);
                double result = eval.asDouble();
                return new ValueNode(FORMATTER.format(result), false);
            } catch (Throwable t) {
                var test = 0;
            }
        } else if (node.getString().startsWith("@")) {
            return environment.getVariables().get(node.getString().substring(1));
        }

        return node;
    }
}

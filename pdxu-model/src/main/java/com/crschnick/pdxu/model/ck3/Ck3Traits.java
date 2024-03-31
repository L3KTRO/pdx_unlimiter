package com.crschnick.pdxu.model.ck3;

import com.crschnick.pdxu.io.node.Node;

import java.util.List;
import java.util.stream.Collectors;

public class Ck3Traits {

    public static List<String> fromNode(Node n) {
        List<String> traits = n.getNodeForKey("traits_lookup").getNodeArray().stream().map(Node::getString).collect(Collectors.toList());
        System.out.println(traits);
        return traits;
    }

}

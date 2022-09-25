package org.zornco.energynodes.graph;

import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraftforge.common.util.INBTSerializable;
import org.zornco.energynodes.EnergyNodes;

import java.lang.ref.WeakReference;
import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public class ConnectionGraph implements INBTSerializable<CompoundTag> {

    final MutableValueGraph<Node, NodeConnection> NodeGraph;

    final WeakReference<Node> controllerNode;

    public ConnectionGraph(BlockPos controllerPos) {
        NodeGraph = ValueGraphBuilder.directed().build();
        Node cNode = new Node(controllerPos.immutable(), Collections.emptyList());
        NodeGraph.addNode(cNode);
        controllerNode = new WeakReference<>(cNode);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        final var inputNodes = Node.CODEC.listOf()
            .encodeStart(NbtOps.INSTANCE, List.copyOf(getInputNodes()))
            .getOrThrow(false, EnergyNodes.LOGGER::fatal);
        tag.put("inputNodes", inputNodes);
        final var outputNodes = Node.CODEC.listOf()
            .encodeStart(NbtOps.INSTANCE, List.copyOf(getOutputNodes()))
            .getOrThrow(false, EnergyNodes.LOGGER::fatal);
        tag.put("outputNodes", outputNodes);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        Node cNode = Objects.requireNonNull(controllerNode.get());
        if (nbt.contains("inputNodes")) {
            final List<Node> nodes = Node.CODEC.listOf().fieldOf("inputNodes")
                .codec().parse(NbtOps.INSTANCE, nbt)
                .getOrThrow(false, EnergyNodes.LOGGER::fatal);
            nodes.forEach(n -> {
                NodeGraph.addNode(n);
                NodeGraph.putEdgeValue(n, cNode,new NodeConnection());
            });
        }
        if (nbt.contains("outputNodes")) {
            final List<Node> nodes = Node.CODEC.listOf().fieldOf("outputNodes")
                .codec().parse(NbtOps.INSTANCE, nbt)
                .getOrThrow(false, EnergyNodes.LOGGER::fatal);
            nodes.forEach(n -> {
                NodeGraph.addNode(n);
                NodeGraph.putEdgeValue(cNode, n,new NodeConnection());
            });
        }
    }

    public WeakReference<Node> getInput(BlockPos pos)
    {
        Node cNode = Objects.requireNonNull(controllerNode.get());
        Node node = NodeGraph.predecessors(cNode).stream().filter(n -> n.pos().equals(pos))
            .findFirst().orElse(null);
        return new WeakReference<>(node);
    }
    public WeakReference<Node> getOutput(BlockPos pos)
    {
        Node cNode = Objects.requireNonNull(controllerNode.get());
        Node node = NodeGraph.successors(cNode).stream().filter(n -> n.pos().equals(pos))
            .findFirst().orElse(null);
        return new WeakReference<>(node);
    }

    public WeakReference<Node> addInput(BlockPos pos)
    {
        Node cNode = Objects.requireNonNull(controllerNode.get());
        Node node = new Node(pos, Collections.emptyList());
        NodeGraph.addNode(node);
        NodeGraph.putEdgeValue(node, cNode,new NodeConnection());
        return new WeakReference<>(node);
    }
    public WeakReference<Node> addOutput(BlockPos pos)
    {
        Node cNode = Objects.requireNonNull(controllerNode.get());
        Node node = new Node(pos, Collections.emptyList());
        NodeGraph.addNode(node);
        NodeGraph.putEdgeValue(cNode, node,new NodeConnection());
        return new WeakReference<>(node);
    }

    public void removeInput(BlockPos pos)
    {
        Node cNode = Objects.requireNonNull(controllerNode.get());
        NodeGraph.predecessors(cNode).stream().filter(n -> n.pos().equals(pos))
            .findFirst().ifPresent(NodeGraph::removeNode);
    }
    public void removeOutput(BlockPos pos)
    {
        Node cNode = Objects.requireNonNull(controllerNode.get());
        NodeGraph.successors(cNode).stream().filter(n -> n.pos().equals(pos))
            .findFirst().ifPresent(NodeGraph::removeNode);
    }

    public Set<Node> getInputNodes() {
        Node cNode = Objects.requireNonNull(controllerNode.get());
        return NodeGraph.predecessors(cNode);
    }

    public Set<Node> getOutputNodes() {
        Node cNode = Objects.requireNonNull(controllerNode.get());
        return NodeGraph.successors(cNode);
    }

    public int getSize() {
        return getInputNodes().size() + getOutputNodes().size();
    }

    public MutableValueGraph<Node, NodeConnection> getNodeGraph() {
        return NodeGraph;
    }
}

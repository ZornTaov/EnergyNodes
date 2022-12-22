package org.zornco.energynodes.graph;

import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.zornco.energynodes.EnergyNodes;
import org.zornco.energynodes.block.BaseNodeBlock;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class ConnectionGraph implements INBTSerializable<CompoundTag> {

    final MutableValueGraph<IGraphNode, INodeConnection> NodeGraph;

    final WeakReference<Node> controllerNode;

    final HashMap<SidedPos, CapabilityNode> outputs = new HashMap<>();

    public ConnectionGraph(BlockPos controllerPos) {
        NodeGraph = ValueGraphBuilder.directed().build();
        Node cNode = new Node(controllerPos.immutable());
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

    public WeakReference<Node> getController()
    {
        return controllerNode;
    }

    public Node getInput(BlockPos pos)
    {
        Node cNode = Objects.requireNonNull(controllerNode.get());
        Node node = NodeGraph.predecessors(cNode).stream()
            .filter(n  -> (n instanceof Node no) && no.pos().equals(pos))
            .map(Node.class::cast)
            .findFirst().orElse(null);
        return node;
    }
    public Node getOutput(BlockPos pos)
    {
        Node cNode = Objects.requireNonNull(controllerNode.get());
        Node node = NodeGraph.successors(cNode).stream()
            .filter(n  -> (n instanceof Node no) && no.pos().equals(pos))
            .map(Node.class::cast)
            .findFirst().orElse(null);
        return node;
    }

    public WeakReference<Node> addInput(BlockPos pos)
    {
        Node cNode = Objects.requireNonNull(controllerNode.get());
        Node node = new Node(pos);
        NodeGraph.addNode(node);
        NodeGraph.putEdgeValue(node, cNode,new NodeConnection());
        return new WeakReference<>(node);
    }
    public WeakReference<Node> addOutput(BlockPos pos)
    {
        Node cNode = Objects.requireNonNull(controllerNode.get());
        Node node = new Node(pos);
        NodeGraph.addNode(node);
        NodeGraph.putEdgeValue(cNode, node,new NodeConnection());
        return new WeakReference<>(node);
    }

    public void removeNode(Node node)
    {
        NodeGraph.removeNode(node);
    }
    public void removeInput(BlockPos pos)
    {
        Node cNode = Objects.requireNonNull(controllerNode.get());
        NodeGraph.predecessors(cNode).stream()
            .filter(n  -> (n instanceof Node no) && no.pos().equals(pos))
            .map(Node.class::cast)
            .findFirst().ifPresent(NodeGraph::removeNode);
    }

    public void removeOutput(BlockPos pos) {
        Node cNode = Objects.requireNonNull(controllerNode.get());

        NodeGraph.successors(cNode).stream()
            .filter(n -> (n instanceof Node no) && no.pos().equals(pos))
            .findFirst().ifPresent(oN -> {
               NodeGraph.successors(oN).stream().map(CapabilityNode.class::cast)
                   .forEach(capNode -> {
                       outputs.remove(capNode.sPos());
                       NodeGraph.removeNode(capNode);
                   });
               NodeGraph.removeNode(oN);
            });
    }

    public Set<Node> getInputNodes() {
        Node cNode = Objects.requireNonNull(controllerNode.get());
        return NodeGraph.predecessors(cNode).stream().filter(Node.class::isInstance).map(Node.class::cast).collect(Collectors.toSet());
    }

    public Set<Node> getOutputNodes() {
        Node cNode = Objects.requireNonNull(controllerNode.get());
        return NodeGraph.successors(cNode).stream().filter(Node.class::isInstance).map(Node.class::cast).collect(Collectors.toSet());
    }

    public int getSize() {
        return getInputNodes().size() + getOutputNodes().size();
    }

    public MutableValueGraph<IGraphNode, INodeConnection> getNodeGraph() {
        return NodeGraph;
    }

    public Node getNode(BaseNodeBlock.Flow flowDir, BlockPos pos) {
        return switch (flowDir) {
            case OUT -> getOutput(pos);
            case IN -> getInput(pos);
        };
    }

    public WeakReference<Node> addNode(BaseNodeBlock.Flow dir, BlockPos pos) {
        return switch (dir) {
            case OUT -> addOutput(pos);
            case IN -> addInput(pos);
        };
    }

    public void addOutputCap(BlockPos pos, Direction dir, LazyOptional<?> cap)
    {
        SidedPos sidedpos = new SidedPos(dir, pos);
        CapabilityNode node = new CapabilityNode(cap, sidedpos);
        outputs.putIfAbsent(sidedpos, node);
        BlockPos outPos = pos.relative(dir);
        Node output = getOutput(outPos);
        NodeGraph.putEdgeValue(output, node, new CapConnection(dir));
        cap.addListener(lo -> {
            NodeGraph.removeNode(node);
            outputs.remove(sidedpos);
        });
    }

    public HashMap<SidedPos, CapabilityNode> getAllOutputs() {
        return outputs;
    }

    public <T> Set<T> getAllOutputs(Predicate<T> storagePredicate) {
        return outputs.values().stream()
            .map(CapabilityNode::cap)
            .map(LazyOptional::<T>cast)
            .map(LazyOptional::resolve)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .filter(storagePredicate)
            .collect(Collectors.toSet());
    }
}

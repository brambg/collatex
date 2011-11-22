package eu.interedition.collatex2.implementation.vg_alignment;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.interedition.collatex2.implementation.containers.graph.VariantGraphEdge;
import eu.interedition.collatex2.implementation.containers.graph.VariantGraphVertex;
import eu.interedition.collatex2.implementation.vg_analysis.Analysis;
import eu.interedition.collatex2.implementation.vg_analysis.IAnalysis;
import eu.interedition.collatex2.implementation.vg_analysis.ISequence;
import eu.interedition.collatex2.implementation.vg_analysis.ITransposition;
import eu.interedition.collatex2.implementation.vg_analysis.SequenceDetection;
import eu.interedition.collatex2.interfaces.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VariantGraphBuilder {
  private static final Logger LOG = LoggerFactory.getLogger(VariantGraphBuilder.class);

  private final IVariantGraph graph;

  private ILinker tokenLinker = new TokenLinker();

  private Analysis analysis;

  public VariantGraphBuilder(IVariantGraph graph) {
    this.graph = graph;
  }

  public VariantGraphBuilder add(IWitness... witnesses) {
    for (IWitness witness : witnesses) {
      merge(witness);
    }
    return this;
  }

  protected void merge(IWitness witness) {
    LOG.debug("{} + {}: Match and link tokens", graph, witness);
    //TODO: the TokenLinker class should be replaced by the new linker class based on the decision graph
    //EditGraphLinker tokenLinker = new EditGraphLinker();
    Map<INormalizedToken, INormalizedToken> linkedTokens = tokenLinker.link(graph, witness);

    LOG.debug("{} + {}: Determine sequences", graph, witness);
    IWitness superbase = new Superbase(graph);
    SequenceDetection detection = new SequenceDetection();
    List<ISequence> sequences = detection.getSequences(linkedTokens, superbase, witness);


    LOG.debug("{} + {}: Determine transpositions of sequences", graph, witness);
    Analysis analysis = new Analysis(sequences, superbase);
    //NOTE: This is not very nice!
    this.analysis = analysis;
    List<ITransposition> transpositions = analysis.getTranspositions();

    LOG.debug("{} + {}: Determine aligned tokens", graph, witness);
    Map<INormalizedToken, INormalizedToken> alignedTokens = determineAlignedTokens(linkedTokens, transpositions, witness);

    LOG.debug("{} + {}: Merge comparand", graph, witness);
    IVariantGraphVertex previous =  graph.getStartVertex();
    for (INormalizedToken token : witness.getTokens()) {
      if (LOG.isTraceEnabled()) {
        LOG.trace("Match for {}: {}", token, linkedTokens.containsKey(token));
      }
      INormalizedToken vertexKey = linkedTokens.containsKey(token)  ? ((IVariantGraphVertex)linkedTokens.get(token)).getVertexKey() : token;
      IVariantGraphVertex vertex = alignedTokens.containsKey(token) ? (IVariantGraphVertex) linkedTokens.get(token) : addNewVertex(token.getNormalized(), vertexKey);
      IVariantGraphEdge edge = graph.getEdge(previous, vertex);
      if (edge == null) edge = addNewEdge(previous, vertex);
      vertex.addToken(witness, token);
      edge.addWitness(witness);
      previous = vertex;
    }
    IVariantGraphEdge edge = graph.getEdge(previous, graph.getEndVertex());
    if (edge == null) edge = addNewEdge(previous, graph.getEndVertex());
    edge.addWitness(witness);
  }

  //write
  private IVariantGraphVertex addNewVertex(String normalized, INormalizedToken vertexKey) {
    // System.out.println("Add vertex "+normalized);
    IVariantGraphVertex vertex = new VariantGraphVertex(normalized, vertexKey);
    graph.addVertex(vertex);
    return vertex;
  }

  //write
  private IVariantGraphEdge addNewEdge(IVariantGraphVertex begin, IVariantGraphVertex end) {
    // System.out.println("Add edge between "+begin.getNormalized()+ " and " + end.getNormalized());
    IVariantGraphEdge edge = new VariantGraphEdge();
    graph.addEdge(begin, end, edge);
    return edge;
  }

  public IVariantGraph getResult() {
    return graph;
  }


  public IAnalysis getAnalysis() {
    return analysis;
  }

  //NOTE: It would be better to not use getNormalized here!
  //NOTE: This does not work with a custom matching function
  static ITransposition findMirroredTransposition(final Stack<ITransposition> transToCheck, final ITransposition original) {
    for (final ITransposition transposition : transToCheck) {
      if (transposition.getSequenceA().getNormalized().equals(original.getSequenceB().getNormalized())) {
        if (transposition.getSequenceB().getNormalized().equals(original.getSequenceA().getNormalized())) {
          return transposition;
        }
      }
    }
    return null;
  }

  // Note: this only calculates the distance between the tokens in the witness.
  // Note: it does not take into account a possible distance in the vertices in the graph!
  private boolean transpositionsAreNear(ITransposition top, ITransposition mirrored, IWitness witness) {
    INormalizedToken lastToken = top.getSequenceB().getWitnessPhrase().getLastToken();
    INormalizedToken firstToken = mirrored.getSequenceB().getWitnessPhrase().getFirstToken();
    return witness.isNear(lastToken, firstToken);
  }

  // NOTE: this method should not return the original sequence when a mirror exists!
  private List<ISequence> getSequencesThatAreTransposed(List<ITransposition> transpositions, IWitness witness) {
    List<ISequence> transposedSequences = Lists.newArrayList();
    final Stack<ITransposition> transToCheck = new Stack<ITransposition>();
    transToCheck.addAll(transpositions);
    Collections.reverse(transToCheck);
    while (!transToCheck.isEmpty()) {
      final ITransposition top = transToCheck.pop();
      // System.out.println("Detected transposition: "+top.getSequenceA().toString());
      final ITransposition mirrored = VariantGraphBuilder.findMirroredTransposition(transToCheck, top);
      // remove mirrored transpositions (a->b, b->a) from transpositions
      if (mirrored != null && transpositionsAreNear(top, mirrored, witness)) {
        // System.out.println("Detected mirror: "+mirrored.getSequenceA().toString());
        // System.out.println("Keeping: transposition " + top.toString());
        // System.out.println("Removing: transposition " + mirrored.toString());
        transToCheck.remove(mirrored);
        transposedSequences.add(mirrored.getSequenceA());
      } else {
        transposedSequences.add(top.getSequenceA());
      }
    }
    return transposedSequences;
  }

  private Map<INormalizedToken, INormalizedToken> determineAlignedTokens(Map<INormalizedToken, INormalizedToken> linkedTokens, List<ITransposition> transpositions, IWitness witness) {
    Map<INormalizedToken, INormalizedToken> alignedTokens = Maps.newLinkedHashMap();
    alignedTokens.putAll(linkedTokens);
    List<ISequence> sequencesThatAreTransposed = getSequencesThatAreTransposed(transpositions, witness);
    for (ISequence sequenceA : sequencesThatAreTransposed) {
      for (INormalizedToken token : sequenceA.getWitnessPhrase().getTokens()) {
        alignedTokens.remove(token);
      }
    }
    return alignedTokens;
  }
}

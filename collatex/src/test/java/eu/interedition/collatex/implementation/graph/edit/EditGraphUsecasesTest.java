package eu.interedition.collatex.implementation.graph.edit;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.implementation.alignment.VariantGraphWitnessAdapter;
import eu.interedition.collatex.implementation.graph.db.PersistentVariantGraph;
import eu.interedition.collatex.implementation.matching.EqualityTokenComparator;
import eu.interedition.collatex.interfaces.INormalizedToken;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class EditGraphUsecasesTest extends AbstractTest {

  @Test
  public void testUsecase1() {
    //  <example>
    //  <witness>The black cat</witness>
    //  <witness>The black and white cat</witness>
    //  <witness>The black and green cat</witness>
    //  <witness>The black very special cat</witness>
    //  <witness>The black not very special cat</witness>
    //</example>
    final PersistentVariantGraph graph = merge("The black cat");
    EditGraphLinker linker = new EditGraphLinker();
    Map<INormalizedToken, INormalizedToken> link = linker.link(VariantGraphWitnessAdapter.create(graph), createWitnesses("The black and white cat")[0], new EqualityTokenComparator());
    assertEquals(3, link.size());
    //TODO: add asserts!
    //System.out.println(link);
  }
}
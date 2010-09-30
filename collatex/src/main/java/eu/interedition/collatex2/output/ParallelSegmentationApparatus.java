/**
 * CollateX - a Java library for collating textual sources,
 * for example, to produce an apparatus.
 *
 * Copyright (C) 2010 ESF COST Action "Interedition".
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.collatex2.output;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.interedition.collatex2.interfaces.ColumnState;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.INormalizedToken;

public class ParallelSegmentationApparatus {
  private static Logger logger = LoggerFactory.getLogger(ParallelSegmentationApparatus.class);
  
  private final List<ApparatusEntry> entries;
  private List<String> sigla;

  public List<ApparatusEntry> getEntries() {
    return entries;
  }

  public List<String> getSigla() {
    if (this.sigla == null) {
      final Set<String> sigli = Sets.newLinkedHashSet();
      for (final ApparatusEntry column : entries) {
        sigli.addAll(column.getSigla());
      }
      this.sigla = Lists.newArrayList(sigli);
    }
    return this.sigla;
  }

  /**
   * To merge or not to merge; that is the question.
   * 
   * @param alignmentTable
   * @return
   */
  public static ParallelSegmentationApparatus build(final IAlignmentTable alignmentTable) {
    final List<ApparatusEntry> entries = Lists.newArrayList();
    ApparatusEntry mergedEntry = null;
    IColumn previousEntry = null; // Note: in the next step we have to compare
                                   // two columns with each other
    for (final IColumn column : alignmentTable.getColumns()) {
      boolean needNewCell = previousEntry == null || !previousEntry.getState().equals(column.getState()) || !column.getSigla().equals(previousEntry.getSigla());
      if (previousEntry != null && previousEntry.getState() == ColumnState.VARIANT && previousEntry.getSigla().size() > column.getSigla().size()
          && previousEntry.getSigla().containsAll(column.getSigla())) {
        needNewCell = false;
      }
      if (needNewCell) {
        final List<String> sigli = alignmentTable.getSigla();
        logger.debug("!!!" + sigli);
        mergedEntry = new ApparatusEntry(sigli);
        entries.add(mergedEntry);
      }

      final List<String> sigli = alignmentTable.getSigla();
      for (final String sigil : sigli) {
        if (column.containsWitness(sigil)) {
          final INormalizedToken token = column.getToken(sigil);
          mergedEntry.addToken(sigil, token);
        }
      }

      previousEntry = column;

    }
    return new ParallelSegmentationApparatus(entries);
  }

  private ParallelSegmentationApparatus(final List<ApparatusEntry> entries) {
    this.entries = entries;
  }
}

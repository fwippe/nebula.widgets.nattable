/*******************************************************************************
 * Copyright (c) 2019 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.group.performance;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.group.RowGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.performance.GroupModel.Group;
import org.eclipse.nebula.widgets.nattable.group.performance.command.RowGroupCollapseCommand;
import org.eclipse.nebula.widgets.nattable.group.performance.command.RowGroupExpandCommand;
import org.eclipse.nebula.widgets.nattable.group.performance.command.UpdateRowGroupCollapseCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.AbstractRowHideShowLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.event.HideRowPositionsEvent;
import org.eclipse.nebula.widgets.nattable.hideshow.event.ShowRowPositionsEvent;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.event.VisualRefreshEvent;

/**
 * Layer that is used in combination with the performance
 * {@link RowGroupHeaderLayer} to support expand/collapse of row groups.
 *
 * @since 1.6
 */
public class RowGroupExpandCollapseLayer extends AbstractRowHideShowLayer {

    private final Map<Group, Collection<Integer>> hidden = new HashMap<Group, Collection<Integer>>();

    public RowGroupExpandCollapseLayer(IUniqueIndexLayer underlyingLayer) {
        super(underlyingLayer);
    }

    @Override
    public boolean doCommand(ILayerCommand command) {
        if (command instanceof RowGroupExpandCommand) {
            List<Group> groups = ((RowGroupExpandCommand) command).getGroups();

            Set<Integer> shownIndexes = new TreeSet<Integer>();

            for (Group group : groups) {
                // if group is not collapseable return without any further
                // operation
                if (group == null || !group.isCollapseable()) {
                    continue;
                }

                if (group.isCollapsed()) {
                    group.setCollapsed(false);
                    Collection<Integer> rowIndexes = this.hidden.get(group);
                    this.hidden.remove(group);
                    shownIndexes.addAll(rowIndexes);
                }
            }

            if (!shownIndexes.isEmpty()) {
                invalidateCache();
                fireLayerEvent(new ShowRowPositionsEvent(this, getRowPositionsByIndexes(shownIndexes)));
            } else {
                fireLayerEvent(new VisualRefreshEvent(this));
            }

            return true;
        } else if (command instanceof RowGroupCollapseCommand) {
            GroupModel groupModel = ((RowGroupCollapseCommand) command).getGroupModel();
            List<Group> groups = ((RowGroupCollapseCommand) command).getGroups();
            Collections.sort(groups, new Comparator<Group>() {

                @Override
                public int compare(Group o1, Group o2) {
                    return o2.getVisibleStartPosition() - o1.getVisibleStartPosition();
                }
            });

            Set<Integer> hiddenPositions = new TreeSet<Integer>();
            Set<Integer> hiddenIndexes = new TreeSet<Integer>();

            for (Group group : groups) {
                // if group is not collapseable return without any further
                // operation
                if (group == null || !group.isCollapseable()) {
                    continue;
                }

                Set<Integer> rowIndexes = new TreeSet<Integer>();
                if (!group.isCollapsed()) {
                    rowIndexes.addAll(group.getVisibleIndexes());
                    group.setCollapsed(true);
                } else if (!this.hidden.containsKey(group)) {
                    for (int member : group.getMembers()) {
                        int pos = groupModel.getPositionByIndex(member);
                        if (pos > -1) {
                            rowIndexes.add(pos);
                        }
                    }
                }

                modifyForVisible(group, rowIndexes);
                this.hidden.put(group, rowIndexes);

                hiddenPositions.addAll(getRowPositionsByIndexes(rowIndexes));
                hiddenIndexes.addAll(rowIndexes);
            }

            if (!hiddenPositions.isEmpty()) {
                invalidateCache();
                fireLayerEvent(new HideRowPositionsEvent(this, hiddenPositions, hiddenIndexes));
            } else {
                fireLayerEvent(new VisualRefreshEvent(this));
            }

            return true;
        } else if (command instanceof UpdateRowGroupCollapseCommand) {
            UpdateRowGroupCollapseCommand cmd = (UpdateRowGroupCollapseCommand) command;
            Group group = cmd.getGroup();
            Collection<Integer> hiddenRowIndexes = this.hidden.get(group);
            if (group.getVisibleIndexes().size() + hiddenRowIndexes.size() <= group.getOriginalSpan()) {
                Collection<Integer> indexesToHide = cmd.getIndexesToHide();
                Collection<Integer> indexesToShow = cmd.getIndexesToShow();

                // remove already hidden indexes
                indexesToHide.removeAll(hiddenRowIndexes);

                // remove static indexes
                modifyForVisible(group, indexesToHide);

                Collection<Integer> hiddenPositions = getRowPositionsByIndexes(indexesToHide);

                hiddenRowIndexes.addAll(indexesToHide);
                hiddenRowIndexes.removeAll(indexesToShow);

                invalidateCache();

                fireLayerEvent(new HideRowPositionsEvent(this, hiddenPositions, indexesToHide));
            }

            return true;
        }
        return super.doCommand(command);
    }

    /**
     * Ensure that a group is never hidden completely via collapse operations.
     * Removes either the configured static indexes of the group or the first
     * visible row in a group from the given collection of indexes for this.
     *
     * @param group
     *            The group to check.
     * @param rowIndexes
     *            The collection of indexes that should be hidden.
     */
    private void modifyForVisible(Group group, Collection<Integer> rowIndexes) {
        Collection<Integer> staticIndexes = group.getStaticIndexes();
        if (staticIndexes.isEmpty()) {
            // keep the first row
            rowIndexes.remove(group.getVisibleStartIndex());
        } else {
            // do not hide static indexes
            rowIndexes.removeAll(staticIndexes);
        }
    }

    @Override
    public boolean isRowIndexHidden(int rowIndex) {
        for (Collection<Integer> indexes : this.hidden.values()) {
            if (indexes.contains(Integer.valueOf(rowIndex))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Collection<Integer> getHiddenRowIndexes() {
        Set<Integer> hiddenRowIndexes = new TreeSet<Integer>();
        for (Collection<Integer> indexes : this.hidden.values()) {
            hiddenRowIndexes.addAll(indexes);
        }
        return hiddenRowIndexes;
    }

}

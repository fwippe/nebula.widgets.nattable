/*******************************************************************************
 * Copyright (c) 2012, 2013, 2014 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth - added ITraversalStrategy handling
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.selection.command.MoveSelectionCommand;

/**
 * Abstraction of the selection behavior during navigation in the grid.
 * Implementations of this class specify what to select when the selection moves
 * by responding to the {@link MoveSelectionCommand}.
 *
 * @param <T>
 *            The type of the {@link ILayerCommand} this
 *            {@link ILayerCommandHandler} handles. Needs to be a
 *            {@link MoveSelectionCommand} or subtype.
 *
 * @see MoveCellSelectionCommandHandler
 * @see MoveRowSelectionCommandHandler
 */
public abstract class MoveSelectionCommandHandler<T extends MoveSelectionCommand> implements ILayerCommandHandler<T> {

    /**
     * The SelectionLayer instance which is needed to perform selection
     * operations.
     */
    protected final SelectionLayer selectionLayer;

    /**
     * The strategy to use on traversal. Specifies the behavior when the
     * movement reaches a border.
     */
    protected final ITraversalStrategy traversalStrategy;

    /**
     * Create a MoveSelectionCommandHandler for the given {@link SelectionLayer}
     * . Uses the {@link ITraversalStrategy#AXIS_TRAVERSAL_STRATEGY} as default
     * strategy for selection movement.
     *
     * @param selectionLayer
     *            The {@link SelectionLayer} on which the selection should be
     *            performed.
     */
    public MoveSelectionCommandHandler(SelectionLayer selectionLayer) {
        this(selectionLayer, ITraversalStrategy.AXIS_TRAVERSAL_STRATEGY);
    }

    /**
     * Create a MoveSelectionCommandHandler for the given {@link SelectionLayer}
     * .
     *
     * @param selectionLayer
     *            The {@link SelectionLayer} on which the selection should be
     *            performed.
     * @param traversalStrategy
     *            The strategy that should be used for selection movements. Can
     *            not be <code>null</code>.
     */
    public MoveSelectionCommandHandler(SelectionLayer selectionLayer, ITraversalStrategy traversalStrategy) {
        if (traversalStrategy == null) {
            throw new IllegalArgumentException("You need to specify an ITraversalStrategy!"); //$NON-NLS-1$
        }
        this.selectionLayer = selectionLayer;
        this.traversalStrategy = traversalStrategy;
    }

    @Override
    public boolean doCommand(ILayer targetLayer, T command) {
        if (command.convertToTargetLayer(this.selectionLayer)) {
            moveSelection(command.getDirection(), getTraversalStrategy(command),
                    command.isShiftMask(), command.isControlMask());
            return true;
        }
        return false;
    }

    /**
     * Determines the {@link ITraversalStrategy} that should be used to move the
     * selection on handling the given command. The strategy is determined in
     * the following way:
     * <ol>
     * <li>Return the {@link ITraversalStrategy} carried by the command</li>
     * <li>If it doesn't contain a {@link ITraversalStrategy} but a carries a
     * dedicated step count, create a temporary {@link ITraversalStrategy} that
     * is configured with the locally configured {@link ITraversalStrategy} but
     * returns the step count carried by the command.</li>
     * <li>If the command doesn't carry a {@link ITraversalStrategy} and no
     * dedicated step count, the {@link ITraversalStrategy} registered with this
     * command handler is returned.</li>
     * </ol>
     *
     * @param command
     *            The current handled command.
     * @return The {@link ITraversalStrategy} that should be used to move the
     *         selection.
     */
    protected ITraversalStrategy getTraversalStrategy(final T command) {
        // if the command comes with a strategy we use it
        ITraversalStrategy result = command.getTraversalStrategy();

        if (result == null) {
            if (command.getStepSize() != null) {
                // command carries a step size, so we use the internal strategy
                // with the transported step size this is mainly for backwards
                // compatibility
                result = new ITraversalStrategy() {

                    @Override
                    public TraversalScope getTraversalScope() {
                        return MoveSelectionCommandHandler.this.traversalStrategy.getTraversalScope();
                    }

                    @Override
                    public boolean isCycle() {
                        return MoveSelectionCommandHandler.this.traversalStrategy.isCycle();
                    }

                    @Override
                    public int getStepCount() {
                        return command.getStepSize();
                    }
                };
            }
            else {
                result = this.traversalStrategy;
            }
        }

        return result;
    }

    /**
     * Moves the selection from the current position into the given move
     * direction.
     *
     * @param moveDirection
     *            The direction to move to.
     * @param traversalStrategy
     *            the traversal strategy to determine the number of steps to
     *            move and the behavior on moving over the border
     * @param withShiftMask
     *            boolean flag to indicate whether the shift key modifier is
     *            enabled or not
     * @param withControlMask
     *            boolean flag to indicate whether the control key modifier is
     *            enabled or not
     */
    protected void moveSelection(MoveDirectionEnum moveDirection, ITraversalStrategy traversalStrategy,
            boolean withShiftMask, boolean withControlMask) {

        switch (moveDirection) {
            case UP:
                moveLastSelectedUp(traversalStrategy, withShiftMask, withControlMask);
                break;
            case DOWN:
                moveLastSelectedDown(traversalStrategy, withShiftMask, withControlMask);
                break;
            case LEFT:
                moveLastSelectedLeft(traversalStrategy, withShiftMask, withControlMask);
                break;
            case RIGHT:
                moveLastSelectedRight(traversalStrategy, withShiftMask, withControlMask);
                break;
            default:
                break;
        }
    }

    /**
     * Moves the selection from the current position to the right.
     *
     * @param traversalStrategy
     *            the traversal strategy to determine the number of steps to
     *            move and the behavior on moving over the border
     * @param withShiftMask
     *            boolean flag to indicate whether the shift key modifier is
     *            enabled or not
     * @param withControlMask
     *            boolean flag to indicate whether the control key modifier is
     *            enabled or not
     */
    protected abstract void moveLastSelectedRight(ITraversalStrategy traversalStrategy,
            boolean withShiftMask, boolean withControlMask);

    /**
     * Moves the selection from the current position to the left.
     *
     * @param traversalStrategy
     *            the traversal strategy to determine the number of steps to
     *            move and the behavior on moving over the border
     * @param withShiftMask
     *            boolean flag to indicate whether the shift key modifier is
     *            enabled or not
     * @param withControlMask
     *            boolean flag to indicate whether the control key modifier is
     *            enabled or not
     */
    protected abstract void moveLastSelectedLeft(ITraversalStrategy traversalStrategy,
            boolean withShiftMask, boolean withControlMask);

    /**
     * Moves the selection from the current position up.
     *
     * @param traversalStrategy
     *            the traversal strategy to determine the number of steps to
     *            move and the behavior on moving over the border
     * @param withShiftMask
     *            boolean flag to indicate whether the shift key modifier is
     *            enabled or not
     * @param withControlMask
     *            boolean flag to indicate whether the control key modifier is
     *            enabled or not
     */
    protected abstract void moveLastSelectedUp(ITraversalStrategy traversalStrategy,
            boolean withShiftMask, boolean withControlMask);

    /**
     * Moves the selection from the current position down.
     *
     * @param traversalStrategy
     *            the traversal strategy to determine the number of steps to
     *            move and the behavior on moving over the border
     * @param withShiftMask
     *            boolean flag to indicate whether the shift key modifier is
     *            enabled or not
     * @param withControlMask
     *            boolean flag to indicate whether the control key modifier is
     *            enabled or not
     */
    protected abstract void moveLastSelectedDown(ITraversalStrategy traversalStrategy,
            boolean withShiftMask, boolean withControlMask);

}

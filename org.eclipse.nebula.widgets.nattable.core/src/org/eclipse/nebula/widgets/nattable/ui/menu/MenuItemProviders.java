/*******************************************************************************
 * Copyright (c) 2012, 2019 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - changed key for NatEventData and added column group menu items
 *     Thanh Liem PHAN (ALL4TEC) <thanhliem.phan@all4tec.net> - Bug 509361
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.ui.menu;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.nebula.widgets.nattable.Messages;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.columnCategories.ChooseColumnsFromCategoriesCommand;
import org.eclipse.nebula.widgets.nattable.columnChooser.command.DisplayColumnChooserCommand;
import org.eclipse.nebula.widgets.nattable.columnRename.DisplayColumnRenameDialogCommand;
import org.eclipse.nebula.widgets.nattable.export.ExportConfigAttributes;
import org.eclipse.nebula.widgets.nattable.export.command.ExportTableCommand;
import org.eclipse.nebula.widgets.nattable.export.command.ExportTableCommandHandler;
import org.eclipse.nebula.widgets.nattable.export.image.ImageExporter;
import org.eclipse.nebula.widgets.nattable.filterrow.command.ClearAllFiltersCommand;
import org.eclipse.nebula.widgets.nattable.filterrow.command.ToggleFilterRowCommand;
import org.eclipse.nebula.widgets.nattable.freeze.command.FreezeColumnCommand;
import org.eclipse.nebula.widgets.nattable.freeze.command.FreezePositionCommand;
import org.eclipse.nebula.widgets.nattable.freeze.command.FreezeRowCommand;
import org.eclipse.nebula.widgets.nattable.freeze.command.UnFreezeGridCommand;
import org.eclipse.nebula.widgets.nattable.group.command.CreateColumnGroupCommand;
import org.eclipse.nebula.widgets.nattable.group.command.CreateRowGroupCommand;
import org.eclipse.nebula.widgets.nattable.group.command.DisplayColumnGroupRenameDialogCommand;
import org.eclipse.nebula.widgets.nattable.group.command.DisplayRowGroupRenameDialogCommand;
import org.eclipse.nebula.widgets.nattable.group.command.OpenCreateColumnGroupDialog;
import org.eclipse.nebula.widgets.nattable.group.command.RemoveColumnGroupCommand;
import org.eclipse.nebula.widgets.nattable.group.command.RemoveRowGroupCommand;
import org.eclipse.nebula.widgets.nattable.group.command.UngroupColumnCommand;
import org.eclipse.nebula.widgets.nattable.group.command.UngroupRowCommand;
import org.eclipse.nebula.widgets.nattable.group.performance.gui.HeaderGroupNameDialog;
import org.eclipse.nebula.widgets.nattable.group.performance.gui.HeaderGroupNameDialog.HeaderGroupNameDialogLabels;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ColumnHideCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ColumnShowCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.RowHideCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.RowPositionHideCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.RowShowCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ShowAllColumnsCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ShowAllRowsCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.persistence.command.DisplayPersistenceDialogCommand;
import org.eclipse.nebula.widgets.nattable.resize.command.InitializeAutoResizeColumnsCommand;
import org.eclipse.nebula.widgets.nattable.resize.command.InitializeAutoResizeRowsCommand;
import org.eclipse.nebula.widgets.nattable.style.editor.command.DisplayColumnStyleEditorCommand;
import org.eclipse.nebula.widgets.nattable.ui.NatEventData;
import org.eclipse.nebula.widgets.nattable.util.GCFactory;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Widget;

/**
 * Helper class that provides several {@link IMenuItemProvider} for menu items
 * that can be used within a popup menu in the NatTable to execute NatTable
 * specific actions.
 */
public class MenuItemProviders {

    /**
     * Key that is used to put the NatEventData into the data of a menu.
     */
    public static final String NAT_EVENT_DATA_KEY = "natEventData"; //$NON-NLS-1$

    /**
     * Walk up the MenuItems (in case they are nested) and find the parent
     * {@link Menu}
     *
     * @param selectionEvent
     *            on the {@link MenuItem}
     * @return data associated with the parent {@link Menu}
     */
    public static NatEventData getNatEventData(SelectionEvent selectionEvent) {
        Widget widget = selectionEvent.widget;
        if (widget == null || !(widget instanceof MenuItem)) {
            return null;
        }

        MenuItem menuItem = (MenuItem) widget;
        Menu parentMenu = menuItem.getParent();
        Object data = null;
        while (parentMenu != null) {
            if (parentMenu.getData(NAT_EVENT_DATA_KEY) == null) {
                parentMenu = parentMenu.getParentMenu();
            } else {
                data = parentMenu.getData(NAT_EVENT_DATA_KEY);
                break;
            }
        }

        return data != null ? (NatEventData) data : null;
    }

    /**
     * Will create and return the {@link IMenuItemProvider} that adds the action
     * for executing the {@link ColumnHideCommand} to a popup menu. This command
     * is intended to hide the current selected column immediately.
     *
     * @return The {@link IMenuItemProvider} for the {@link MenuItem} that
     *         executes the {@link ColumnHideCommand}. The {@link MenuItem} will
     *         be shown with the localized default text configured in NatTable
     *         core.
     */
    public static IMenuItemProvider hideColumnMenuItemProvider() {
        return hideColumnMenuItemProvider("%MenuItemProviders.hideColumn"); //$NON-NLS-1$
    }

    /**
     * Will create and return the {@link IMenuItemProvider} that adds the action
     * for executing the {@link ColumnHideCommand} to a popup menu. This command
     * is intended to hide the current selected column immediately.
     * <p>
     * The {@link MenuItem} will be shown with the given menu label.
     *
     * @param menuLabel
     *            The text that will be showed for the generated
     *            {@link MenuItem}
     * @return The {@link IMenuItemProvider} for the {@link MenuItem} that
     *         executes the {@link ColumnHideCommand}.
     */
    public static IMenuItemProvider hideColumnMenuItemProvider(final String menuLabel) {
        return new IMenuItemProvider() {

            @Override
            public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
                MenuItem menuItem = new MenuItem(popupMenu, SWT.PUSH);
                menuItem.setText(Messages.getLocalizedMessage(menuLabel));
                menuItem.setImage(GUIHelper.getImage("hide_column")); //$NON-NLS-1$
                menuItem.setEnabled(true);

                menuItem.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent event) {
                        int columnPosition = getNatEventData(event).getColumnPosition();
                        natTable.doCommand(
                                new ColumnHideCommand(natTable, columnPosition));
                    }
                });
            }
        };
    }

    /**
     * Will create and return the {@link IMenuItemProvider} that adds the action
     * for executing the {@link ShowAllColumnsCommand} to a popup menu. This
     * command is intended to show all columns of the NatTable and is used to
     * unhide previous hidden columns.
     *
     * @return The {@link IMenuItemProvider} for the {@link MenuItem} that
     *         executes the {@link ShowAllColumnsCommand}. The {@link MenuItem}
     *         will be shown with the localized default text configured in
     *         NatTable core.
     */
    public static IMenuItemProvider showAllColumnsMenuItemProvider() {
        return showAllColumnsMenuItemProvider("%MenuItemProviders.showAllColumns"); //$NON-NLS-1$
    }

    /**
     * Will create and return the {@link IMenuItemProvider} that adds the action
     * for executing the {@link ShowAllColumnsCommand} to a popup menu. This
     * command is intended to show all columns of the NatTable and is used to
     * unhide previous hidden columns.
     * <p>
     * The {@link MenuItem} will be shown with the given menu label.
     *
     * @param menuLabel
     *            The text that will be showed for the generated
     *            {@link MenuItem}
     * @return The {@link IMenuItemProvider} for the {@link MenuItem} that
     *         executes the {@link ShowAllColumnsCommand}.
     */
    public static IMenuItemProvider showAllColumnsMenuItemProvider(final String menuLabel) {
        return showAllColumnsMenuItemProvider(menuLabel, GUIHelper.getImage("show_column")); //$NON-NLS-1$
    }

    /**
     * Will create and return the {@link IMenuItemProvider} that adds the action
     * for executing the {@link ShowAllColumnsCommand} to a popup menu. This
     * command is intended to show all columns of the NatTable and is used to
     * unhide previous hidden columns.
     * <p>
     * The {@link MenuItem} will be shown with the given menu label and the
     * given image.
     *
     * @param menuLabel
     *            The text that will be showed for the generated
     *            {@link MenuItem}
     * @param image
     *            The image that will be showed with the generated
     *            {@link MenuItem}. Can be <code>null</code> to not showing an
     *            image.
     * @return The {@link IMenuItemProvider} for the {@link MenuItem} that
     *         executes the {@link ShowAllColumnsCommand}.
     * @since 1.6
     */
    public static IMenuItemProvider showAllColumnsMenuItemProvider(final String menuLabel, final Image image) {
        return new IMenuItemProvider() {

            @Override
            public void addMenuItem(final NatTable natTable, Menu popupMenu) {
                MenuItem showAllColumns = new MenuItem(popupMenu, SWT.PUSH);
                showAllColumns.setText(Messages.getLocalizedMessage(menuLabel));
                showAllColumns.setImage(image);
                showAllColumns.setEnabled(true);

                showAllColumns.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        natTable.doCommand(new ShowAllColumnsCommand());
                    }
                });
            }
        };
    }

    /**
     * Will create and return the {@link IMenuItemProvider} that adds the action
     * for executing the {@link ColumnShowCommand} to a popup menu. This command
     * is intended to show the column(s) at the clicked position of the NatTable
     * and is used to unhide previous hidden columns.
     *
     * @param showAll
     *            Whether all hidden adjacent columns should be shown again or
     *            only the single direct adjacent column.
     * @return The {@link IMenuItemProvider} for the {@link MenuItem} that
     *         executes the {@link ColumnShowCommand}.
     * @since 1.6
     */
    public static IMenuItemProvider showColumnMenuItemProvider(final boolean showAll) {
        return showColumnMenuItemProvider(showAll, "%MenuItemProviders.showColumns"); //$NON-NLS-1$
    }

    /**
     * Will create and return the {@link IMenuItemProvider} that adds the action
     * for executing the {@link ColumnShowCommand} to a popup menu. This command
     * is intended to show the column(s) at the clicked position of the NatTable
     * and is used to unhide previous hidden columns.
     * <p>
     * The {@link MenuItem} will be shown with the given menu label.
     *
     * @param showAll
     *            Whether all hidden adjacent columns should be shown again or
     *            only the single direct adjacent column.
     * @param menuLabel
     *            The text that will be showed for the generated
     *            {@link MenuItem}
     * @return The {@link IMenuItemProvider} for the {@link MenuItem} that
     *         executes the {@link ColumnShowCommand}.
     * @since 1.6
     */
    public static IMenuItemProvider showColumnMenuItemProvider(final boolean showAll, final String menuLabel) {
        return showColumnMenuItemProvider(showAll, menuLabel, GUIHelper.getImage("show_column")); //$NON-NLS-1$
    }

    /**
     * Will create and return the {@link IMenuItemProvider} that adds the action
     * for executing the {@link ColumnShowCommand} to a popup menu. This command
     * is intended to show the column(s) at the clicked position of the NatTable
     * and is used to unhide previous hidden columns.
     * <p>
     * The {@link MenuItem} will be shown with the given menu label and the
     * given image.
     *
     * @param showAll
     *            Whether all hidden adjacent columns should be shown again or
     *            only the single direct adjacent column.
     * @param menuLabel
     *            The text that will be showed for the generated
     *            {@link MenuItem}
     * @param image
     *            The image that will be showed with the generated
     *            {@link MenuItem}. Can be <code>null</code> to not showing an
     *            image.
     * @return The {@link IMenuItemProvider} for the {@link MenuItem} that
     *         executes the {@link ColumnShowCommand}.
     * @since 1.6
     */
    public static IMenuItemProvider showColumnMenuItemProvider(final boolean showAll, final String menuLabel, final Image image) {
        return new IMenuItemProvider() {

            @Override
            public void addMenuItem(final NatTable natTable, Menu popupMenu) {
                MenuItem menuItem = new MenuItem(popupMenu, SWT.PUSH);
                menuItem.setText(Messages.getLocalizedMessage(menuLabel));
                menuItem.setImage(image);
                menuItem.setEnabled(true);

                menuItem.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent event) {
                        NatEventData natEventData = MenuItemProviders.getNatEventData(event);
                        int columnPosition = natEventData.getColumnPosition();
                        int diffToCellStart = natEventData.getOriginalEvent().x
                                - natTable.getStartXOfColumnPosition(columnPosition);
                        int diffToCellEnd = natTable.getStartXOfColumnPosition(columnPosition)
                                + natTable.getColumnWidthByPosition(columnPosition)
                                - natEventData.getOriginalEvent().x;
                        natTable.doCommand(
                                new ColumnShowCommand(natTable, columnPosition, diffToCellStart < diffToCellEnd, showAll));
                    }
                });
            }
        };
    }

    /**
     * Will create and return the {@link IMenuItemProvider} that adds the action
     * for executing the {@link RowHideCommand} to a popup menu. This command is
     * intended to hide the current selected row immediately.
     *
     * @return The {@link IMenuItemProvider} for the {@link MenuItem} that
     *         executes the {@link RowHideCommand}. The {@link MenuItem} will be
     *         shown with the localized default text configured in NatTable
     *         core.
     */
    public static IMenuItemProvider hideRowMenuItemProvider() {
        return hideRowMenuItemProvider("%MenuItemProviders.hideRow"); //$NON-NLS-1$
    }

    /**
     * Will create and return the {@link IMenuItemProvider} that adds the action
     * for executing the {@link RowHideCommand} to a popup menu. This command is
     * intended to hide the current selected row immediately.
     * <p>
     * The {@link MenuItem} will be shown with the given menu label.
     *
     * @param menuLabel
     *            The text that will be showed for the generated
     *            {@link MenuItem}
     * @return The {@link IMenuItemProvider} for the {@link MenuItem} that
     *         executes the {@link RowHideCommand}.
     */
    public static IMenuItemProvider hideRowMenuItemProvider(final String menuLabel) {
        return new IMenuItemProvider() {

            @Override
            public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
                MenuItem menuItem = new MenuItem(popupMenu, SWT.PUSH);
                menuItem.setText(Messages.getLocalizedMessage(menuLabel));
                menuItem.setImage(GUIHelper.getImage("hide_row")); //$NON-NLS-1$
                menuItem.setEnabled(true);

                menuItem.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent event) {
                        int rowPosition = getNatEventData(event).getRowPosition();
                        natTable.doCommand(
                                new RowHideCommand(natTable, rowPosition));
                    }
                });
            }
        };
    }

    /**
     * Will create and return the {@link IMenuItemProvider} that adds the action
     * for executing the {@link RowPositionHideCommand} to a popup menu. This
     * command is intended to hide the current selected row immediately.
     * <p>
     * With the additional column position information this command is intended
     * to be used with the HierarchicalTreeLayer to support hiding of multiple
     * rows on a spanned level header column.
     * </p>
     *
     * @return The {@link IMenuItemProvider} for the {@link MenuItem} that
     *         executes the {@link RowPositionHideCommand}. The {@link MenuItem}
     *         will be shown with the localized default text configured in
     *         NatTable core.
     *
     * @since 1.6
     */
    public static IMenuItemProvider hideRowPositionMenuItemProvider() {
        return hideRowPositionMenuItemProvider("%MenuItemProviders.hideRow"); //$NON-NLS-1$
    }

    /**
     * Will create and return the {@link IMenuItemProvider} that adds the action
     * for executing the {@link RowPositionHideCommand} to a popup menu. This
     * command is intended to hide the current selected row immediately.
     * <p>
     * With the additional column position information this command is intended
     * to be used with the HierarchicalTreeLayer to support hiding of multiple
     * rows on a spanned level header column.
     * </p>
     * <p>
     * The {@link MenuItem} will be shown with the given menu label.
     *
     * @param menuLabel
     *            The text that will be showed for the generated
     *            {@link MenuItem}
     * @return The {@link IMenuItemProvider} for the {@link MenuItem} that
     *         executes the {@link RowPositionHideCommand}.
     *
     * @since 1.6
     */
    public static IMenuItemProvider hideRowPositionMenuItemProvider(final String menuLabel) {
        return new IMenuItemProvider() {

            @Override
            public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
                MenuItem menuItem = new MenuItem(popupMenu, SWT.PUSH);
                menuItem.setText(Messages.getLocalizedMessage(menuLabel));
                menuItem.setImage(GUIHelper.getImage("hide_row")); //$NON-NLS-1$
                menuItem.setEnabled(true);

                menuItem.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent event) {
                        NatEventData data = MenuItemProviders.getNatEventData(event);
                        int rowPosition = data.getRowPosition();
                        int columnPosition = data.getColumnPosition();
                        natTable.doCommand(
                                new RowPositionHideCommand(natTable, columnPosition, rowPosition));
                    }
                });
            }
        };
    }

    /**
     * Will create and return the {@link IMenuItemProvider} that adds the action
     * for executing the {@link ShowAllRowsCommand} to a popup menu. This
     * command is intended to show all rows of the NatTable and is used to
     * unhide previous hidden rows.
     *
     * @return The {@link IMenuItemProvider} for the {@link MenuItem} that
     *         executes the {@link ShowAllRowsCommand}. The {@link MenuItem}
     *         will be shown with the localized default text configured in
     *         NatTable core.
     */
    public static IMenuItemProvider showAllRowsMenuItemProvider() {
        return showAllRowsMenuItemProvider("%MenuItemProviders.showAllRows"); //$NON-NLS-1$
    }

    /**
     * Will create and return the {@link IMenuItemProvider} that adds the action
     * for executing the {@link ShowAllRowsCommand} to a popup menu. This
     * command is intended to show all rows of the NatTable and is used to
     * unhide previous hidden rows.
     * <p>
     * The {@link MenuItem} will be shown with the given menu label.
     *
     * @param menuLabel
     *            The text that will be showed for the generated
     *            {@link MenuItem}
     * @return The {@link IMenuItemProvider} for the {@link MenuItem} that
     *         executes the {@link ShowAllRowsCommand}.
     */
    public static IMenuItemProvider showAllRowsMenuItemProvider(final String menuLabel) {
        return showAllRowsMenuItemProvider(menuLabel, GUIHelper.getImage("show_row")); //$NON-NLS-1$
    }

    /**
     * Will create and return the {@link IMenuItemProvider} that adds the action
     * for executing the {@link ShowAllRowsCommand} to a popup menu. This
     * command is intended to show all rows of the NatTable and is used to
     * unhide previous hidden rows.
     * <p>
     * The {@link MenuItem} will be shown with the given menu label and given
     * image.
     *
     * @param menuLabel
     *            The text that will be showed for the generated
     *            {@link MenuItem}
     * @param image
     *            The image that will be showed with the generated
     *            {@link MenuItem}. Can be <code>null</code> to not showing an
     *            image.
     * @return The {@link IMenuItemProvider} for the {@link MenuItem} that
     *         executes the {@link ShowAllRowsCommand}.
     * @since 1.6
     */
    public static IMenuItemProvider showAllRowsMenuItemProvider(final String menuLabel, final Image image) {
        return new IMenuItemProvider() {

            @Override
            public void addMenuItem(final NatTable natTable, Menu popupMenu) {
                MenuItem showAllRows = new MenuItem(popupMenu, SWT.PUSH);
                showAllRows.setText(Messages.getLocalizedMessage(menuLabel));
                showAllRows.setImage(image);
                showAllRows.setEnabled(true);

                showAllRows.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        natTable.doCommand(new ShowAllRowsCommand());
                    }
                });
            }
        };
    }

    /**
     * Will create and return the {@link IMenuItemProvider} that adds the action
     * for executing the {@link RowShowCommand} to a popup menu. This command is
     * intended to show the row(s) at the clicked position of the NatTable and
     * is used to unhide previous hidden rows.
     *
     * @param showAll
     *            Whether all hidden adjacent rows should be shown again or only
     *            the single direct adjacent row.
     * @return The {@link IMenuItemProvider} for the {@link MenuItem} that
     *         executes the {@link RowShowCommand}.
     * @since 1.6
     */
    public static IMenuItemProvider showRowMenuItemProvider(final boolean showAll) {
        return showRowMenuItemProvider(showAll, "%MenuItemProviders.showRow"); //$NON-NLS-1$
    }

    /**
     * Will create and return the {@link IMenuItemProvider} that adds the action
     * for executing the {@link RowShowCommand} to a popup menu. This command is
     * intended to show the row(s) at the clicked position of the NatTable and
     * is used to unhide previous hidden rows.
     * <p>
     * The {@link MenuItem} will be shown with the given menu label.
     *
     * @param showAll
     *            Whether all hidden adjacent rows should be shown again or only
     *            the single direct adjacent row.
     * @param menuLabel
     *            The text that will be showed for the generated
     *            {@link MenuItem}
     * @return The {@link IMenuItemProvider} for the {@link MenuItem} that
     *         executes the {@link RowShowCommand}.
     * @since 1.6
     */
    public static IMenuItemProvider showRowMenuItemProvider(final boolean showAll, final String menuLabel) {
        return showRowMenuItemProvider(showAll, menuLabel, GUIHelper.getImage("show_row")); //$NON-NLS-1$
    }

    /**
     * Will create and return the {@link IMenuItemProvider} that adds the action
     * for executing the {@link RowShowCommand} to a popup menu. This command is
     * intended to show the row(s) at the clicked position of the NatTable and
     * is used to unhide previous hidden rows.
     * <p>
     * The {@link MenuItem} will be shown with the given menu label and given
     * image.
     *
     * @param showAll
     *            Whether all hidden adjacent rows should be shown again or only
     *            the single direct adjacent row.
     * @param menuLabel
     *            The text that will be showed for the generated
     *            {@link MenuItem}
     * @param image
     *            The image that will be showed with the generated
     *            {@link MenuItem}. Can be <code>null</code> to not showing an
     *            image.
     * @return The {@link IMenuItemProvider} for the {@link MenuItem} that
     *         executes the {@link RowShowCommand}.
     * @since 1.6
     */
    public static IMenuItemProvider showRowMenuItemProvider(final boolean showAll, final String menuLabel, final Image image) {
        return new IMenuItemProvider() {

            @Override
            public void addMenuItem(final NatTable natTable, Menu popupMenu) {
                MenuItem menuItem = new MenuItem(popupMenu, SWT.PUSH);
                menuItem.setText(Messages.getLocalizedMessage(menuLabel));
                menuItem.setImage(image);
                menuItem.setEnabled(true);

                menuItem.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent event) {
                        NatEventData natEventData = MenuItemProviders.getNatEventData(event);
                        int rowPosition = natEventData.getRowPosition();
                        int diffToCellStart = natEventData.getOriginalEvent().y
                                - natTable.getStartYOfRowPosition(rowPosition);
                        int diffToCellEnd = natTable.getStartYOfRowPosition(rowPosition)
                                + natTable.getRowHeightByPosition(rowPosition)
                                - natEventData.getOriginalEvent().y;
                        natTable.doCommand(
                                new RowShowCommand(natTable, rowPosition, diffToCellStart < diffToCellEnd, showAll));
                    }
                });
            }
        };
    }

    public static IMenuItemProvider autoResizeColumnMenuItemProvider() {
        return autoResizeColumnMenuItemProvider("%MenuItemProviders.autoResizeColumn"); //$NON-NLS-1$
    }

    public static IMenuItemProvider autoResizeColumnMenuItemProvider(final String menuLabel) {
        return new IMenuItemProvider() {

            @Override
            public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
                MenuItem autoResizeColumns = new MenuItem(popupMenu, SWT.PUSH);
                autoResizeColumns.setText(Messages.getLocalizedMessage(menuLabel));
                autoResizeColumns.setImage(GUIHelper.getImage("auto_resize")); //$NON-NLS-1$
                autoResizeColumns.setEnabled(true);

                autoResizeColumns.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent event) {
                        int columnPosition = getNatEventData(event).getColumnPosition();
                        natTable.doCommand(
                                new InitializeAutoResizeColumnsCommand(
                                        natTable,
                                        columnPosition,
                                        natTable.getConfigRegistry(),
                                        new GCFactory(natTable)));
                    }
                });
            }
        };
    }

    public static IMenuItemProvider autoResizeRowMenuItemProvider() {
        return autoResizeRowMenuItemProvider("%MenuItemProviders.autoResizeRow"); //$NON-NLS-1$
    }

    public static IMenuItemProvider autoResizeRowMenuItemProvider(final String menuLabel) {
        return new IMenuItemProvider() {

            @Override
            public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
                MenuItem autoResizeRows = new MenuItem(popupMenu, SWT.PUSH);
                autoResizeRows.setText(Messages.getLocalizedMessage(menuLabel));
                autoResizeRows.setEnabled(true);

                autoResizeRows.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent event) {
                        int rowPosition = getNatEventData(event).getRowPosition();
                        natTable.doCommand(
                                new InitializeAutoResizeRowsCommand(
                                        natTable,
                                        rowPosition,
                                        natTable.getConfigRegistry(),
                                        new GCFactory(natTable)));
                    }
                });
            }
        };
    }

    public static IMenuItemProvider autoResizeAllSelectedColumnMenuItemProvider() {
        return autoResizeAllSelectedColumnMenuItemProvider("%MenuItemProviders.autoResizeAllSelectedColumns"); //$NON-NLS-1$
    }

    public static IMenuItemProvider autoResizeAllSelectedColumnMenuItemProvider(final String menuLabel) {
        return new IMenuItemProvider() {

            @Override
            public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
                MenuItem autoResizeColumns = new MenuItem(popupMenu, SWT.PUSH);
                autoResizeColumns.setText(Messages.getLocalizedMessage(menuLabel));
                autoResizeColumns.setEnabled(true);

                autoResizeColumns.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent event) {
                        int columnPosition = getNatEventData(event).getColumnPosition();
                        natTable.doCommand(
                                new InitializeAutoResizeColumnsCommand(
                                        natTable,
                                        columnPosition,
                                        natTable.getConfigRegistry(),
                                        new GCFactory(natTable)));
                    }
                });
            }

        };
    }

    public static IMenuItemProvider columnChooserMenuItemProvider() {
        return columnChooserMenuItemProvider("%MenuItemProviders.chooseColumns"); //$NON-NLS-1$
    }

    public static IMenuItemProvider columnChooserMenuItemProvider(final String menuLabel) {
        return new IMenuItemProvider() {

            @Override
            public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
                MenuItem columnChooser = new MenuItem(popupMenu, SWT.PUSH);
                columnChooser.setText(Messages.getLocalizedMessage(menuLabel));
                columnChooser.setImage(GUIHelper.getImage("column_chooser")); //$NON-NLS-1$
                columnChooser.setEnabled(true);

                columnChooser.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        natTable.doCommand(
                                new DisplayColumnChooserCommand(natTable));
                    }
                });
            }
        };
    }

    public static IMenuItemProvider columnStyleEditorMenuItemProvider() {
        return columnStyleEditorMenuItemProvider("%MenuItemProviders.editStyles"); //$NON-NLS-1$
    }

    public static IMenuItemProvider columnStyleEditorMenuItemProvider(final String menuLabel) {
        return new IMenuItemProvider() {

            @Override
            public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
                MenuItem columnStyleEditor = new MenuItem(popupMenu, SWT.PUSH);
                columnStyleEditor.setText(Messages.getLocalizedMessage(menuLabel));
                columnStyleEditor.setImage(GUIHelper.getImage("preferences")); //$NON-NLS-1$
                columnStyleEditor.setEnabled(true);

                columnStyleEditor.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent event) {
                        int rowPosition = getNatEventData(event).getRowPosition();
                        int columnPosition = getNatEventData(event).getColumnPosition();
                        natTable.doCommand(
                                new DisplayColumnStyleEditorCommand(
                                        natTable,
                                        natTable.getConfigRegistry(),
                                        columnPosition,
                                        rowPosition));
                    }
                });
            }

        };
    }

    public static IMenuItemProvider renameColumnMenuItemProvider() {
        return renameColumnMenuItemProvider("%MenuItemProviders.renameColumn"); //$NON-NLS-1$
    }

    public static IMenuItemProvider renameColumnMenuItemProvider(final String label) {
        return new IMenuItemProvider() {

            @Override
            public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
                MenuItem menuItem = new MenuItem(popupMenu, SWT.PUSH);
                menuItem.setText(Messages.getLocalizedMessage(label));
                menuItem.setEnabled(true);

                menuItem.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent event) {
                        natTable.doCommand(
                                new DisplayColumnRenameDialogCommand(
                                        natTable,
                                        getNatEventData(event).getColumnPosition()));
                    }
                });
            }
        };
    }

    public static IMenuItemProvider createColumnGroupMenuItemProvider() {
        return createColumnGroupMenuItemProvider("%MenuItemProviders.createColumnGroup"); //$NON-NLS-1$
    }

    public static IMenuItemProvider createColumnGroupMenuItemProvider(final String menuLabel) {
        return new IMenuItemProvider() {

            @Override
            public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
                MenuItem columnStyleEditor = new MenuItem(popupMenu, SWT.PUSH);
                columnStyleEditor.setText(Messages.getLocalizedMessage(menuLabel));
                columnStyleEditor.setEnabled(true);

                columnStyleEditor.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        natTable.doCommand(
                                new OpenCreateColumnGroupDialog(natTable.getShell()));
                    }
                });
            }
        };
    }

    /**
     *
     * @return The {@link IMenuItemProvider} for adding a menu item to create a
     *         column group with the new performance ColumnGroupHeaderLayer.
     *
     * @since 1.6
     */
    public static IMenuItemProvider createPerformanceColumnGroupMenuItemProvider() {
        return createPerformanceColumnGroupMenuItemProvider("%MenuItemProviders.createColumnGroup"); //$NON-NLS-1$
    }

    /**
     *
     * @param menuLabel
     *            The label to be used for showing the menu item.
     * @return The {@link IMenuItemProvider} for adding a menu item to create a
     *         column group with the new performance ColumnGroupHeaderLayer.
     *
     * @since 1.6
     */
    public static IMenuItemProvider createPerformanceColumnGroupMenuItemProvider(final String menuLabel) {
        return new IMenuItemProvider() {

            @Override
            public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
                MenuItem createColumnGroup = new MenuItem(popupMenu, SWT.PUSH);
                createColumnGroup.setText(Messages.getLocalizedMessage(menuLabel));
                createColumnGroup.setEnabled(true);

                createColumnGroup.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        HeaderGroupNameDialog dialog =
                                new HeaderGroupNameDialog(natTable.getShell(), HeaderGroupNameDialogLabels.CREATE_COLUMN_GROUP);
                        int result = dialog.open();
                        if (result == IDialogConstants.OK_ID) {
                            natTable.doCommand(new CreateColumnGroupCommand(dialog.getGroupName()));
                        }
                    }
                });
            }
        };
    }

    public static IMenuItemProvider ungroupColumnsMenuItemProvider() {
        return ungroupColumnsMenuItemProvider("%MenuItemProviders.ungroupColumns"); //$NON-NLS-1$
    }

    public static IMenuItemProvider ungroupColumnsMenuItemProvider(final String menuLabel) {
        return new IMenuItemProvider() {

            @Override
            public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
                MenuItem columnStyleEditor = new MenuItem(popupMenu, SWT.PUSH);
                columnStyleEditor.setText(Messages.getLocalizedMessage(menuLabel));
                columnStyleEditor.setEnabled(true);

                columnStyleEditor.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        natTable.doCommand(new UngroupColumnCommand());
                    }
                });
            }
        };
    }

    public static IMenuItemProvider inspectLabelsMenuItemProvider() {
        return new IMenuItemProvider() {

            @Override
            public void addMenuItem(NatTable natTable, Menu popupMenu) {
                MenuItem inspectLabelsMenuItem = new MenuItem(popupMenu, SWT.PUSH);
                inspectLabelsMenuItem.setText(Messages.getString("MenuItemProviders.debugInfo")); //$NON-NLS-1$
                inspectLabelsMenuItem.setEnabled(true);

                inspectLabelsMenuItem.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        NatEventData natEventData = getNatEventData(e);
                        NatTable natTable = natEventData.getNatTable();
                        int columnPosition = natEventData.getColumnPosition();
                        int rowPosition = natEventData.getRowPosition();

                        String msg = "Display mode: " + natTable.getDisplayModeByPosition(columnPosition, rowPosition) + "\nConfig labels: " //$NON-NLS-1$ //$NON-NLS-2$
                                + natTable.getConfigLabelsByPosition(columnPosition, rowPosition)
                                + "\nData value: " //$NON-NLS-1$
                                + natTable.getDataValueByPosition(columnPosition, rowPosition)
                                + "\n\nColumn position: " + columnPosition + "\nColumn index: " //$NON-NLS-1$ //$NON-NLS-2$
                                + natTable.getColumnIndexByPosition(columnPosition)
                                + "\n\nRow position: " + rowPosition + "\nRow index: " //$NON-NLS-1$ //$NON-NLS-2$
                                + natTable.getRowIndexByPosition(rowPosition);

                        MessageBox messageBox =
                                new MessageBox(natTable.getShell(), SWT.ICON_INFORMATION | SWT.OK);
                        messageBox.setText(Messages.getString("MenuItemProviders.debugInformation")); //$NON-NLS-1$
                        messageBox.setMessage(msg);
                        messageBox.open();
                    }
                });
            }
        };
    }

    public static IMenuItemProvider categoriesBasedColumnChooserMenuItemProvider() {
        return categoriesBasedColumnChooserMenuItemProvider("%MenuItemProviders.columnCategoriesChooser"); //$NON-NLS-1$
    }

    public static IMenuItemProvider categoriesBasedColumnChooserMenuItemProvider(final String menuLabel) {
        return new IMenuItemProvider() {

            @Override
            public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
                MenuItem columnChooser = new MenuItem(popupMenu, SWT.PUSH);
                columnChooser.setText(Messages.getLocalizedMessage(menuLabel));
                columnChooser.setImage(GUIHelper.getImage("column_categories_chooser")); //$NON-NLS-1$
                columnChooser.setEnabled(true);

                columnChooser.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        natTable.doCommand(
                                new ChooseColumnsFromCategoriesCommand(natTable));
                    }
                });
            }
        };
    }

    public static IMenuItemProvider clearAllFiltersMenuItemProvider() {
        return clearAllFiltersMenuItemProvider("%MenuItemProviders.clearAllFilters"); //$NON-NLS-1$
    }

    public static IMenuItemProvider clearAllFiltersMenuItemProvider(final String menuLabel) {
        return new IMenuItemProvider() {

            @Override
            public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
                MenuItem menuItem = new MenuItem(popupMenu, SWT.PUSH);
                menuItem.setText(Messages.getLocalizedMessage(menuLabel));
                menuItem.setImage(GUIHelper.getImage("remove_filter")); //$NON-NLS-1$
                menuItem.setEnabled(true);

                menuItem.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        natTable.doCommand(new ClearAllFiltersCommand());
                    }
                });
            }
        };
    }

    public static IMenuItemProvider clearToggleFilterRowMenuItemProvider() {
        return clearToggleFilterRowMenuItemProvider("%MenuItemProviders.toggleFilterRow"); //$NON-NLS-1$
    }

    public static IMenuItemProvider clearToggleFilterRowMenuItemProvider(final String menuLabel) {
        return new IMenuItemProvider() {

            @Override
            public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
                MenuItem menuItem = new MenuItem(popupMenu, SWT.PUSH);
                menuItem.setText(Messages.getLocalizedMessage(menuLabel));
                menuItem.setImage(GUIHelper.getImage("toggle_filter")); //$NON-NLS-1$
                menuItem.setEnabled(true);

                menuItem.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        natTable.doCommand(new ToggleFilterRowCommand());
                    }
                });
            }
        };
    }

    /**
     * Will create and return the {@link IMenuItemProvider} that adds the action
     * for executing the {@link DisplayPersistenceDialogCommand} to a popup
     * menu. This command is intended to open the DisplayPersistenceDialog for
     * managing NatTable states (also called view management).
     *
     * @return The {@link IMenuItemProvider} for the {@link MenuItem} that
     *         executes the {@link DisplayPersistenceDialogCommand} The
     *         {@link MenuItem} will be shown with the localized default text
     *         configured in NatTable core.
     */
    public static IMenuItemProvider stateManagerMenuItemProvider() {
        return stateManagerMenuItemProvider("%MenuItemProviders.stateManager"); //$NON-NLS-1$
    }

    /**
     * Will create and return the {@link IMenuItemProvider} that adds the action
     * for executing the {@link DisplayPersistenceDialogCommand} to a popup
     * menu. This command is intended to open the DisplayPersistenceDialog for
     * managing NatTable states (also called view management).
     * <p>
     * The {@link MenuItem} will be shown with the given menu label.
     *
     * @param menuLabel
     *            The text that will be showed for the generated
     *            {@link MenuItem}
     * @return The {@link IMenuItemProvider} for the {@link MenuItem} that
     *         executes the {@link DisplayPersistenceDialogCommand} The
     *         {@link MenuItem} will be shown with the localized default text
     *         configured in NatTable core.
     */
    public static IMenuItemProvider stateManagerMenuItemProvider(final String menuLabel) {
        return new IMenuItemProvider() {

            @Override
            public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
                MenuItem saveState = new MenuItem(popupMenu, SWT.PUSH);
                saveState.setText(Messages.getLocalizedMessage(menuLabel));
                saveState.setImage(GUIHelper.getImage("table_icon")); //$NON-NLS-1$
                saveState.setEnabled(true);

                saveState.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        natTable.doCommand(
                                new DisplayPersistenceDialogCommand(natTable));
                    }
                });
            }
        };
    }

    /**
     * @return An {@link IMenuItemProvider} for adding a separator to the popup
     *         menu.
     */
    public static IMenuItemProvider separatorMenuItemProvider() {
        return new IMenuItemProvider() {
            @Override
            public void addMenuItem(NatTable natTable, Menu popupMenu) {
                new MenuItem(popupMenu, SWT.SEPARATOR);
            }
        };
    }

    public static IMenuItemProvider renameColumnGroupMenuItemProvider() {
        return renameColumnGroupMenuItemProvider("%ColumnGroups.renameColumnGroup"); //$NON-NLS-1$
    }

    public static IMenuItemProvider renameColumnGroupMenuItemProvider(final String menuLabel) {
        return new IMenuItemProvider() {

            @Override
            public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
                MenuItem columnStyleEditor = new MenuItem(popupMenu, SWT.PUSH);
                columnStyleEditor.setText(Messages.getLocalizedMessage(menuLabel));
                columnStyleEditor.setEnabled(true);

                columnStyleEditor.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        NatEventData natEventData = MenuItemProviders.getNatEventData(e);
                        int columnPosition = natEventData.getColumnPosition();
                        natTable.doCommand(
                                new DisplayColumnGroupRenameDialogCommand(natTable, columnPosition));
                    }
                });
            }
        };
    }

    public static IMenuItemProvider removeColumnGroupMenuItemProvider() {
        return removeColumnGroupMenuItemProvider("%ColumnGroups.removeColumnGroup"); //$NON-NLS-1$
    }

    public static IMenuItemProvider removeColumnGroupMenuItemProvider(final String menuLabel) {
        return new IMenuItemProvider() {

            @Override
            public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
                MenuItem columnStyleEditor = new MenuItem(popupMenu, SWT.PUSH);
                columnStyleEditor.setText(Messages.getLocalizedMessage(menuLabel));
                columnStyleEditor.setEnabled(true);

                columnStyleEditor.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        NatEventData natEventData = MenuItemProviders.getNatEventData(e);
                        int columnPosition = natEventData.getColumnPosition();
                        int columnIndex = natEventData.getNatTable().getColumnIndexByPosition(columnPosition);
                        natTable.doCommand(
                                new RemoveColumnGroupCommand(columnIndex));
                    }
                });
            }
        };
    }

    /**
     * Will create and return the {@link IMenuItemProvider} that adds the action
     * for executing the {@link ExportTableCommand} to a popup menu. This
     * command is intended to to export the NatTable to image.
     *
     * <p>
     * <b>IMPORTANT:</b> the {@link ImageExporter} needs to be configured for
     * the configuration attribute {@link ExportConfigAttributes#TABLE_EXPORTER}
     * to really export to an image. Also the {@link ExportTableCommandHandler}
     * needs to be registered on an {@link ILayer} in the layer stack, e.g. the
     * GridLayer.
     * </p>
     *
     * @return The {@link IMenuItemProvider} for the {@link MenuItem} that
     *         executes the {@link ExportTableCommand}. The {@link MenuItem}
     *         will be shown with the localized default text configured in
     *         NatTable core.
     * @since 1.5
     */
    public static IMenuItemProvider exportToImageMenuItemProvider() {
        return exportToImageMenuItemProvider("%MenuItemProviders.exportToImage"); //$NON-NLS-1$
    }

    /**
     * Will create and return the {@link IMenuItemProvider} that adds the action
     * for executing the {@link ExportTableCommand} to a popup menu. This
     * command is intended to export the NatTable to image.
     * <p>
     * The {@link MenuItem} will be shown with the given menu label.
     * </p>
     * <p>
     * <b>IMPORTANT:</b> the {@link ImageExporter} needs to be configured for
     * the configuration attribute {@link ExportConfigAttributes#TABLE_EXPORTER}
     * to really export to an image. Also the {@link ExportTableCommandHandler}
     * needs to be registered on an {@link ILayer} in the layer stack, e.g. the
     * GridLayer.
     * </p>
     *
     * @param menuLabel
     *            The text that will be showed for the generated
     *            {@link MenuItem}
     * @return The {@link IMenuItemProvider} for the {@link MenuItem} that
     *         executes the {@link ExportTableCommand}.
     * @since 1.5
     */
    public static IMenuItemProvider exportToImageMenuItemProvider(final String menuLabel) {
        return new IMenuItemProvider() {

            @Override
            public void addMenuItem(final NatTable natTable, Menu popupMenu) {
                MenuItem exportToImage = new MenuItem(popupMenu, SWT.PUSH);
                exportToImage.setText(Messages.getLocalizedMessage(menuLabel));
                exportToImage.setImage(GUIHelper.getImage("export_image")); //$NON-NLS-1$
                exportToImage.setEnabled(true);

                exportToImage.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        natTable.doCommand(new ExportTableCommand(natTable.getConfigRegistry(), natTable.getShell()));
                    }
                });
            }
        };
    }

    /**
     *
     * @return The {@link IMenuItemProvider} for adding a menu item to create a
     *         row group with the new performance RowGroupHeaderLayer.
     *
     * @since 1.6
     */
    public static IMenuItemProvider createRowGroupMenuItemProvider() {
        return createRowGroupMenuItemProvider("%MenuItemProviders.createRowGroup"); //$NON-NLS-1$
    }

    /**
     *
     * @param menuLabel
     *            The label to be used for showing the menu item.
     * @return The {@link IMenuItemProvider} for adding a menu item to create a
     *         row group with the new performance RowGroupHeaderLayer.
     *
     * @since 1.6
     */
    public static IMenuItemProvider createRowGroupMenuItemProvider(final String menuLabel) {
        return new IMenuItemProvider() {

            @Override
            public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
                MenuItem createRowGroup = new MenuItem(popupMenu, SWT.PUSH);
                createRowGroup.setText(Messages.getLocalizedMessage(menuLabel));
                createRowGroup.setEnabled(true);

                createRowGroup.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        HeaderGroupNameDialog dialog =
                                new HeaderGroupNameDialog(natTable.getShell(), HeaderGroupNameDialogLabels.CREATE_ROW_GROUP);
                        int result = dialog.open();
                        if (result == IDialogConstants.OK_ID) {
                            natTable.doCommand(new CreateRowGroupCommand(dialog.getGroupName()));
                        }
                    }
                });
            }
        };
    }

    /**
     *
     * @return The {@link IMenuItemProvider} for adding a menu item to ungroup
     *         selected rows from an existing row group.
     *
     * @since 1.6
     */
    public static IMenuItemProvider ungroupRowsMenuItemProvider() {
        return ungroupRowsMenuItemProvider("%MenuItemProviders.ungroupRows"); //$NON-NLS-1$
    }

    /**
     *
     * @param menuLabel
     *            The label to be used for showing the menu item.
     * @return The {@link IMenuItemProvider} for adding a menu item to ungroup
     *         selected rows from an existing row group.
     *
     * @since 1.6
     */
    public static IMenuItemProvider ungroupRowsMenuItemProvider(final String menuLabel) {
        return new IMenuItemProvider() {

            @Override
            public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
                MenuItem ungroupRow = new MenuItem(popupMenu, SWT.PUSH);
                ungroupRow.setText(Messages.getLocalizedMessage(menuLabel));
                ungroupRow.setEnabled(true);

                ungroupRow.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        natTable.doCommand(new UngroupRowCommand());
                    }
                });
            }
        };
    }

    /**
     *
     * @return The {@link IMenuItemProvider} for adding a menu item for renaming
     *         a row group.
     *
     * @since 1.6
     */
    public static IMenuItemProvider renameRowGroupMenuItemProvider() {
        return renameRowGroupMenuItemProvider("%RowGroups.renameRowGroup"); //$NON-NLS-1$
    }

    /**
     *
     * @param menuLabel
     *            The label to be used for showing the menu item.
     * @return The {@link IMenuItemProvider} for adding a menu item for renaming
     *         a row group.
     *
     * @since 1.6
     */
    public static IMenuItemProvider renameRowGroupMenuItemProvider(final String menuLabel) {
        return new IMenuItemProvider() {

            @Override
            public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
                MenuItem renameRowGroup = new MenuItem(popupMenu, SWT.PUSH);
                renameRowGroup.setText(Messages.getLocalizedMessage(menuLabel));
                renameRowGroup.setEnabled(true);

                renameRowGroup.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        int rowPosition = getNatEventData(e).getRowPosition();
                        natTable.doCommand(new DisplayRowGroupRenameDialogCommand(natTable, rowPosition));
                    }
                });
            }
        };
    }

    /**
     *
     * @return The {@link IMenuItemProvider} for adding a menu item to remove a
     *         row group.
     *
     * @since 1.6
     */
    public static IMenuItemProvider removeRowGroupMenuItemProvider() {
        return removeRowGroupMenuItemProvider("%RowGroups.removeRowGroup"); //$NON-NLS-1$
    }

    /**
     *
     * @param menuLabel
     *            The label to be used for showing the menu item.
     * @return The {@link IMenuItemProvider} for adding a menu item to remove a
     *         row group.
     *
     * @since 1.6
     */
    public static IMenuItemProvider removeRowGroupMenuItemProvider(final String menuLabel) {
        return new IMenuItemProvider() {

            @Override
            public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
                MenuItem menuItem = new MenuItem(popupMenu, SWT.PUSH);
                menuItem.setText(Messages.getLocalizedMessage(menuLabel));
                menuItem.setEnabled(true);

                menuItem.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        NatEventData natEventData = getNatEventData(e);
                        int rowPosition = natEventData.getRowPosition();
                        int rowIndex = natEventData.getNatTable().getRowIndexByPosition(rowPosition);
                        natTable.doCommand(new RemoveRowGroupCommand(rowIndex));
                    }
                });
            }
        };
    }

    /**
     *
     * @return The {@link IMenuItemProvider} for adding a menu item to freeze a
     *         column.
     *
     * @since 1.6
     */
    public static IMenuItemProvider freezeColumnMenuItemProvider() {
        return freezeColumnMenuItemProvider("%MenuItemProviders.freezeColumn"); //$NON-NLS-1$
    }

    /**
     *
     * @param menuLabel
     *            The label to be used for showing the menu item.
     * @return The {@link IMenuItemProvider} for adding a menu item to freeze a
     *         column.
     *
     * @since 1.6
     */
    public static IMenuItemProvider freezeColumnMenuItemProvider(final String menuLabel) {
        return new IMenuItemProvider() {

            @Override
            public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
                MenuItem menuItem = new MenuItem(popupMenu, SWT.PUSH);
                menuItem.setText(Messages.getLocalizedMessage(menuLabel));
                menuItem.setEnabled(true);

                menuItem.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        int columnPosition = getNatEventData(e).getColumnPosition();
                        natTable.doCommand(new FreezeColumnCommand(natTable, columnPosition, false, true));
                    }
                });
            }
        };
    }

    /**
     *
     * @return The {@link IMenuItemProvider} for adding a menu item to freeze a
     *         row.
     *
     * @since 1.6
     */
    public static IMenuItemProvider freezeRowMenuItemProvider() {
        return freezeRowMenuItemProvider("%MenuItemProviders.freezeRow"); //$NON-NLS-1$
    }

    /**
     *
     * @param menuLabel
     *            The label to be used for showing the menu item.
     * @return The {@link IMenuItemProvider} for adding a menu item to freeze a
     *         row.
     *
     * @since 1.6
     */
    public static IMenuItemProvider freezeRowMenuItemProvider(final String menuLabel) {
        return new IMenuItemProvider() {

            @Override
            public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
                MenuItem menuItem = new MenuItem(popupMenu, SWT.PUSH);
                menuItem.setText(Messages.getLocalizedMessage(menuLabel));
                menuItem.setEnabled(true);

                menuItem.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        int rowPosition = getNatEventData(e).getRowPosition();
                        natTable.doCommand(new FreezeRowCommand(natTable, rowPosition, false, true));
                    }
                });
            }
        };
    }

    /**
     *
     * @return The {@link IMenuItemProvider} for adding a menu item to freeze a
     *         cell position.
     *
     * @since 1.6
     */
    public static IMenuItemProvider freezePositionMenuItemProvider(boolean include) {
        return freezePositionMenuItemProvider("%MenuItemProviders.freezePosition", include); //$NON-NLS-1$
    }

    /**
     *
     * @param menuLabel
     *            The label to be used for showing the menu item.
     * @param include
     *            whether the selected cell should be included in the freeze
     *            region or not. Include means the freeze borders will be to the
     *            right and bottom, while exclude means the freeze borders are
     *            to the left and top.
     * @return The {@link IMenuItemProvider} for adding a menu item to freeze a
     *         cell position.
     *
     * @since 1.6
     */
    public static IMenuItemProvider freezePositionMenuItemProvider(final String menuLabel, final boolean include) {
        return new IMenuItemProvider() {

            @Override
            public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
                MenuItem menuItem = new MenuItem(popupMenu, SWT.PUSH);
                menuItem.setText(Messages.getLocalizedMessage(menuLabel));
                menuItem.setEnabled(true);

                menuItem.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        int columnPosition = getNatEventData(e).getColumnPosition();
                        int rowPosition = getNatEventData(e).getRowPosition();
                        natTable.doCommand(
                                new FreezePositionCommand(natTable, columnPosition, rowPosition, false, true, include));
                    }
                });
            }
        };
    }

    /**
     *
     * @return The {@link IMenuItemProvider} for adding a menu item to remove a
     *         frozen state.
     *
     * @since 1.6
     */
    public static IMenuItemProvider unfreezeMenuItemProvider() {
        return unfreezeMenuItemProvider("%MenuItemProviders.unfreeze"); //$NON-NLS-1$
    }

    /**
     *
     * @param menuLabel
     *            The label to be used for showing the menu item.
     * @return The {@link IMenuItemProvider} for adding a menu item to remove a
     *         frozen state.
     *
     * @since 1.6
     */
    public static IMenuItemProvider unfreezeMenuItemProvider(final String menuLabel) {
        return new IMenuItemProvider() {

            @Override
            public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
                MenuItem menuItem = new MenuItem(popupMenu, SWT.PUSH);
                menuItem.setText(Messages.getLocalizedMessage(menuLabel));
                menuItem.setEnabled(true);

                menuItem.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        natTable.doCommand(new UnFreezeGridCommand());
                    }
                });
            }
        };
    }

}

/*
 *  LINCE PLUS - Automatizacion de datos observacionales. Inherited from legacy Lince 1.2.
 *  Copyright (C) 2025  Alberto Soto-Fernandez
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.lince.observer.data.legacy.commands.instrumento;

import com.lince.observer.data.legacy.Command;
import com.lince.observer.legacy.instrumentoObservacional.InstrumentoObservacional;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 * Action for modifying mutable tree ordering element in same level
 * creation date: 18/01/2015
 *
 * @author Asoto
 */
public class MoveRuleAction extends Command {

    private DefaultMutableTreeNode parent;
    private JTree tree;
    private boolean isMoveUp;
    private static final String RESOURCE_BUNDLE ="i18n.Bundle";

    /**
     *
     * Common builder for command action
     *
     * @param parent selected node
     * @param tree current tree in swing view
     * @param isMoveUp specify is movement direction is to up or down
     *
     */
    public MoveRuleAction(DefaultMutableTreeNode parent, JTree tree, boolean isMoveUp) {
        this.parent = parent;
        this.tree = tree;
        this.isMoveUp = isMoveUp;
        final String labelKey = isMoveUp ? "actions.rule.moveUp.label" : "actions.rule.moveDown.label";
        final String txtKey = isMoveUp ? "actions.rule.moveUp.description" : "actions.rule.moveDown.description";
        final String commandKey = isMoveUp ? "moveRuleUp" : "moveRuleDown";
        putValue(Action.NAME, java.util.ResourceBundle.getBundle(RESOURCE_BUNDLE).getString(labelKey));
        putValue(Action.ACTION_COMMAND_KEY, commandKey);
        putValue(Action.SHORT_DESCRIPTION, java.util.ResourceBundle.getBundle(RESOURCE_BUNDLE).getString(txtKey));
    }

    @Override
    public void execute() {
        InstrumentoObservacional.getInstance().moveNode(parent,isMoveUp,false);
        //tree update and cascade expand
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        model.reload((DefaultMutableTreeNode) model.getRoot());
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }

    @Override
    public void unExecute() {
        throw new UnsupportedOperationException("Not supported.");
    }
}

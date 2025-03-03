package com.lince.observer.data.bean.linceLegacy;

import java.util.List;

/**
 * Created by Alberto Soto. 17/2/25
 *
 * This class represents a tree node that can have children.
 * It's expected to replace DefaultMutableTreeNode in JavaFX
 * and provide support for legacy instrumentoObservacional.
 *
 * TODO: public class Categoria extends DefaultMutableTreeNode { // implements TreeNodeInterface<Object> {
 */
public interface TreeNodeInterface<T> {

    /**
     * Adds a child node to this node.
     * @param child the child node to add
     */
    void addChild(TreeNodeInterface<T> child);

    /**
     * Removes a child node from this node.
     * @param child the child node to remove
     */
    void removeChild(TreeNodeInterface<T> child);

    /**
     * Gets the list of child nodes.
     * @return List of child nodes
     */
    List<TreeNodeInterface<T>> getChildren();

    /**
     * Gets the data or value associated with this node.
     * @return the value of the node
     */
    T getValue();

    /**
     * Sets the data or value associated with this node.
     * @param value the new value to set
     */
    void setValue(T value);

    /**
     * Checks if the node has any children.
     * @return true if the node has children, false otherwise
     */
    boolean hasChildren();

    /**
     * Retrieves the parent of this node.
     * @return the parent node, or null if there is no parent
     */
    TreeNodeInterface<T> getParent();

    /**
     * Sets the parent of this node.
     * @param parent the parent node
     */
    void setParent(TreeNodeInterface<T> parent);
}

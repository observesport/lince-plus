/*
 *  Lince - Automatizacion de datos observacionales
 *  Copyright (C) 2010  Brais Gabin Moreira
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
package lince.modelo.InstrumentoObservacional;

import com.deicos.lince.data.legacy.datos.ControladorArchivos;
import com.deicos.lince.data.legacy.utiles.MyObservable;
import org.slf4j.LoggerFactory;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Brais
 */
public class InstrumentoObservacional implements TreeModelListener {

    private static InstrumentoObservacional instance = null;
    private static Observable observable = new MyObservable();
    private RootInstrumentoObservacional raiz;
    private transient File path;
    private transient boolean necesarySave;
    private transient DefaultTreeModel modelo = null;
    protected final org.slf4j.Logger log = LoggerFactory.getLogger(this.getClass());

    public static synchronized InstrumentoObservacional getInstance() {
        if (instance == null) {
            loadNewInstance();
        }
        return instance;
    }

    public static void loadNewInstance() {
        instance = new InstrumentoObservacional(getNewRootInstrumentObservacional(), null);
        observable.notifyObservers("new");
    }

    public static void loadInstance(File f) {
        try {
            RootInstrumentoObservacional arbol = (RootInstrumentoObservacional) ControladorArchivos.getInstance().cargar(f);
            instance = new InstrumentoObservacional(arbol, f);
            observable.notifyObservers("load");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(InstrumentoObservacional.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static RootInstrumentoObservacional getNewRootInstrumentObservacional() {
        RootInstrumentoObservacional r = new RootInstrumentoObservacional(java.util.ResourceBundle.getBundle("i18n.Bundle").getString("NUEVO I. OBSERVACIONAL"));
        r.add(new DefaultMutableTreeNode(java.util.ResourceBundle.getBundle("i18n.Bundle").getString("FIJOS")));
        r.add(new DefaultMutableTreeNode(java.util.ResourceBundle.getBundle("i18n.Bundle").getString("MIXTOS")));
        r.add(new DefaultMutableTreeNode(java.util.ResourceBundle.getBundle("i18n.Bundle").getString("VARIABLES")));
        return r;
    }

    public InstrumentoObservacional(RootInstrumentoObservacional arbol, File path) {
        this.raiz = arbol;
        this.path = path;
        this.necesarySave = false;
        this.modelo = null;
    }

    public File getPath() {
        return path;
    }

    public void setPath(File path) {
        this.path = path;
    }

    public static void addObservador(Observer observer) {
        observable.addObserver(observer);
    }

    public static void removeObservador(Observer observer) {
        observable.deleteObserver(observer);
    }


    public void save() {
        try {
            ControladorArchivos.getInstance().guardar(path, raiz);
            necesarySave = false;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(InstrumentoObservacional.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean isNecesarySave() {
        return necesarySave;
    }

    public DefaultTreeModel getModel() {
        if (modelo == null) {
            modelo = new DefaultTreeModel(raiz);
            modelo.addTreeModelListener(this);
        }
        return modelo;
    }

    public void treeNodesChanged(TreeModelEvent e) {
        necesarySave = true;
        observable.notifyObservers();
    }

    public void treeNodesInserted(TreeModelEvent e) {
        necesarySave = true;
        observable.notifyObservers();
    }

    public void treeNodesRemoved(TreeModelEvent e) {
        necesarySave = true;
        observable.notifyObservers();
    }

    public void treeStructureChanged(TreeModelEvent e) {
        necesarySave = true;
        observable.notifyObservers();
    }

    public Criterio[] getCriterios() {
        TreeNode variables = raiz.getChildAt(2);
        int numCriterios = variables.getChildCount();
        Criterio criterios[] = new Criterio[numCriterios];
        for (int i = 0; i < numCriterios; i++) {
            try {
                criterios[i] = (Criterio) variables.getChildAt(i);
            } catch (Exception e) {
                log.error("adding criteria", e);
            }

        }
        return criterios;
    }

    public NodoInformacion[] getDatosFijos() {
        TreeNode variables = raiz.getChildAt(0);
        int numNodoInformacion = variables.getChildCount();
        NodoInformacion nodoInformacion[] = new NodoInformacion[numNodoInformacion];
        for (int i = 0; i < numNodoInformacion; i++) {
            nodoInformacion[i] = (NodoInformacion) variables.getChildAt(i);
        }
        return nodoInformacion;
    }

    public NodoInformacion[] getDatosMixtos() {
        TreeNode variables = raiz.getChildAt(1);
        int numNodoInformacion = variables.getChildCount();
        NodoInformacion nodoInformacion[] = new NodoInformacion[numNodoInformacion];
        for (int i = 0; i < numNodoInformacion; i++) {
            nodoInformacion[i] = (NodoInformacion) variables.getChildAt(i);
        }
        return nodoInformacion;
    }

    public String getName() {
        return raiz.getNombre();
    }

    public String exportToSdisGseq(List<Criterio> criterios) {
        String contenido = "";
        for (Criterio criterio : criterios) {
            contenido += "\r\n($" + criterio.getNombre() + " ="; //FIXME hay que quitar espacios y seguro que mas cosas
            List<Categoria> categorias = criterio.getCategoriasHoja();
            for (Categoria categoria : categorias) {
                contenido += " " + categoria.getCodigo();
            }
            contenido += ")";
        }
        return contenido + ";\r\n";
    }

    public String exportToTheme(List<Criterio> criterios) {
        String contenido = "";
        for (Criterio criterio : criterios) {
            contenido += criterio.getNombre() + "\r\n";
            List<Categoria> categorias = criterio.getCategoriasHoja();
            for (Categoria categoria : categorias) {
                contenido += " " + categoria.getCodigo() + "\r\n";
            }
        }

        return contenido;
    }

    /**
     * Genera nombre adicional
     *
     * @param parent
     * @param nombre
     */
    public void addHijo(DefaultMutableTreeNode parent, String nombre) {
        try{
            String nodeName = nombre;//TODO 2020 asf review -- uuid issue + " " +  (parent.getChildCount() +1);
            if (modelo.getPathToRoot(parent).length > 2) { //
                //pq 2? se trata de una categoría!
                modelo.insertNodeInto(new Categoria(nodeName), parent, parent.getChildCount());
            } else {
                int index = raiz.getIndex(parent);
                switch (index) {
                    case 0:
                    case 1://fijo?
                        modelo.insertNodeInto(new NodoInformacion(nodeName), parent, parent.getChildCount());
                        break;
                    case 2://mixto?
                        modelo.insertNodeInto(new Criterio(nodeName), parent, parent.getChildCount());
                }
            }
        }catch (Exception e){
            log.error("UUID issue adding children to legacy instrument",e);
        }

    }

    public void removeNodo(DefaultMutableTreeNode node) {
        modelo.removeNodeFromParent(node);
    }

    /**
     * Checks children in sourceTree nodes and creates new ones into subRoot
     * <p>
     * Asoto : 18/01/2015
     *
     * @param subRoot    destination mutable tree to place inside data
     * @param sourceTree source mutable tree with interesting inner children
     * @return modified tree
     * @throws CloneNotSupportedException
     */
    public DefaultMutableTreeNode copySubTree(DefaultMutableTreeNode subRoot, DefaultMutableTreeNode sourceTree) throws CloneNotSupportedException {
        if (sourceTree == null) {
            return subRoot;
        }
        for (int i = 0; i < sourceTree.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) sourceTree.getChildAt(i);
            DefaultMutableTreeNode clone = (DefaultMutableTreeNode) child.clone();
            //new DefaultMutableTreeNode(child.getUserObject()); // better than toString()
            subRoot.add(clone);
            copySubTree(clone, child);
        }
        return subRoot;
    }

    /**
     * Move a node inside a tree up or down with their siblings
     * Inner children will be duplicated recursively
     *
     * @param node         current node to move
     * @param isMoveToUp   move up or down this node inside same tree level
     * @param doCopyValues if true, values will be replicated, and original item will be deleted
     */
    public void moveNode(DefaultMutableTreeNode node, boolean isMoveToUp, boolean doCopyValues) {
        int parentPosition = node.getParent().getIndex(node);
        boolean hasCondition = isMoveToUp ? (parentPosition > 0) : ((parentPosition + 1) < node.getParent().getChildCount());
        if (hasCondition) {
            int newPosition = parentPosition - 1;
            if (!isMoveToUp) {
                //si es dirección inferior tenemos que saltar el nodo siguiente
                //o 2 si queremos saltar el que queremos borrar
                newPosition = (doCopyValues) ? (parentPosition + 2) : (parentPosition + 1);
            }
            DefaultMutableTreeNode newObj = null;
            if (doCopyValues) {
                try {
                    newObj = copySubTree((DefaultMutableTreeNode) node.clone(), node);
                } catch (CloneNotSupportedException e) {
                    Logger.getLogger(InstrumentoObservacional.class.getName()).log(Level.SEVERE, null, e);
                }
            }
            modelo.insertNodeInto(doCopyValues ? newObj : node, (DefaultMutableTreeNode) node.getParent(), newPosition);
            if (doCopyValues) {
                //si borramos el nodo, afectamos a los listener, eso implica jtable de datos
                //dejamos como alternativa para otros usos de replica de tablas
                ((DefaultMutableTreeNode) node.getParent()).remove(node.getParent().getIndex(node));
            }
            necesarySave = true;
            observable.notifyObservers("load");
        }
    }


}

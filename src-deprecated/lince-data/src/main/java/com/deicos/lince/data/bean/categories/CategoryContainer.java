package com.deicos.lince.data.bean.categories;

import java.util.ArrayList;
import java.util.List;

/**
 * lince-scientific-desktop
 * com.deicos.lince.data
 * @author berto (alberto.soto@gmail.com)in 29/02/2016.
 * Description:
 */
public class CategoryContainer {
    List<CategoryData> nodes = new ArrayList<>();

    public List<CategoryData> getNodes() {
        return nodes;
    }

    public void setNodes(List<CategoryData> nodes) {
        this.nodes = nodes;
    }

    public void pushCategory(int id, String name, int depth, String type) {
        CategoryData aux = new Category();
        aux.setId(id);
        aux.setName(name);
        aux.setLevel(depth);
        aux.setType(type);
        nodes.add(aux);
        //GenericTree
        /*
        if (this.data.getData() == null) {
            this.data.setData(aux);
        } else {
            this.data.addChild(new GenericTreeNode<CategoryData>(aux));
        }*/
    }




}

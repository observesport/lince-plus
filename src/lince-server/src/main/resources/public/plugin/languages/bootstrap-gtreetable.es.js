/* ========================================================= 
 * bootstrap-gtreetable v2.2.1-alpha
 * https://github.com/gilek/bootstrap-gtreetable
 * ========================================================= 
 * Copyright 2014 Maciej Kłak
 * Licensed under MIT (https://github.com/gilek/bootstrap-gtreetable/blob/master/LICENSE)
 * ========================================================= */

// Farsi Translation by Alberto Soto Fernández
(function( $ ) {
    $.fn.gtreetable.defaults.languages.es = {
        save: 'Guardar',
        cancel: 'Cancelar',
        action: 'Acciones',
        actions: {
            createBefore: 'Insertar un elemento anterior',
            createAfter: 'Insertar un elemento posterior',
            createFirstChild: 'Crear categoría al inicio',
            createLastChild: 'Crear categoría',
            update: 'Actualizar',
            delete: 'Borrar'
        },
        messages: {
            onDelete: 'Vas a eliminar el nodo. ¿Estás seguro?',
            onNewRootNotAllowed: 'Añadir un nuevo nodo como principal no está permitido.',
            onMoveInDescendant: 'El nodo seleccionado no puede tener hijos.',
            onMoveAsRoot: 'Este nodo no puede ser principal.'
        }
    };
}( jQuery ));

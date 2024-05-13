# Title     : Utilities with part of R code around list and data
# Objective : Expose and know whow data can be exposed from java to R
# Created by: Alberto Soto Fernandez
# Created on: 21/08/2017

# Como se genera una matriz y se modifica en R
# ============================================
m <- matrix(1:4, 2); # genera una secuencia
m[1, 2]<-10;   # fila 1 columna 2
print(m);


# Ejemplo a exposicion
print(lince);
print(lince[1,2]);
print(lince[1,'name']);

# Como lo integramos
# ------------------
# StringMatrixBuilder data = new StringMatrixBuilder(2,2);
# data.setValue(0,0,"yuhu");
# data.setValue(0,1,3);
# data.setValue(1,0,"yuhu2");
# data.setValue(1,1,4);
# List<String> colNames = new ArrayList<>();
# colNames.add("name");
# colNames.add("value");
# data.setColNames(colNames);
# engine.put("lince",data.build());
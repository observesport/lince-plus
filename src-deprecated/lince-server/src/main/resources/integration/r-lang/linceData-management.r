a <- as.data.frame(table(linceDataByCategory));
print(a);
#count items: a <- table(numbers);
#get One item: print(a[names(a)==435]);
#export as dataframe: table <- as.data.frame(table(numbers));
#http://www.r-tutor.com/r-introduction/data-frame
#print(mtcars);

#obtener el maximo de las frecuencias del registro de visualización
print(a[which.max(a$Freq), ]);
a[ , grepl( "C" , names( a ) ) ]
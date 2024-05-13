numbers <- c(4,23,4,23,5,43,54,56,657,67,67,435,453,435,324,34,456,56,567,65,34,435);
print(numbers);
#count items
a <- table(numbers);
print(a);
#get One item
print(a[names(a)==435]);
#export as dataframe
table <- as.data.frame(table(numbers));
print(table);
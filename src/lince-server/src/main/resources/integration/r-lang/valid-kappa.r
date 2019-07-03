#http://packages.renjin.org/package/org.renjin.cran/KappaGUI/2.0.2/build/1#test-kappaCohen-examples
scores <- data.frame(Trait1_A = c(1,0,2,1,1,1,0,2,1,1),Trait1_B = c(1,2,0,1,2,1,0,1,2,1),Trait2_A = c(1,4,5,2,3,5,1,2,3,4),Trait2_B = c(2,5,2,2,4,5,1,3,1,4));
print(scores);
print(kappaCohen(scores));
build: Prog1A.class Prog21.class Prog22.class

Prog1A.class: Prog1A.java Record.java Consts.java CSVParser.java BinWriter.java
	javac Prog1A.java

Prog21.class: Prog21.java IndexWriter.java
	javac Prog21.java

Prog22.class: Prog22.java IndexReader.java
	javac Prog22.java

clean:
	rm -f *.class Dataset*.bin lhl.idx

run-all: build
	java Prog1A
	java Prog21
	java Prog22

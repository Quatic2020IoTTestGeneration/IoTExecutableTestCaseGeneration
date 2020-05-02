Executable test case generation tool
=======

This directory contains the test generation tool.
It is organized as follows:
	
	* **config/** : contains the configuration files
	* **mocks/** : contains mock classes for DiaMH
	* **codeparser.py** : contains the code that parses the State Machine diagram
	* **configmanager.py** : contains the code that manages configuration files and builds the header of the Java test suite
	* **graphtools.py** : contains utility functions for graph management
	* **pathgen.py**: the core of the tool: contains the code that builds feasible test paths
	* **main.py**: entry point

The tool requires three parameters: 
	* state machine graph, represented in SCXML format (https://www.w3.org/TR/scxml/)
	* language for test scripts (-j for Java, -p for Python)
	* file name for the generated test suite

To run the tool with the provided model, just type 
	```
	python main.py ../Model/DiaMH_StateMachine.scxml -j ../GeneratedTests/DiaMHTests2_NewModel/src/main/java/DiaMHTestsMaven/DiaMHTests/DiaMHTestClass.java
	```

Dependencies
------
	* **Python 3.7**
	* **PIP 19.0.2**
	* **NetworkX 2.2**, available as PIP package

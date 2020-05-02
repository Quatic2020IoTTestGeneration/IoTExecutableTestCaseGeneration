Executable test case generation tool
=======
This repository contains the tool implementing the approach described in a submission to QUATIC 2020, along with an implementation of the system under test and related artifacts.
It is organized as follows:

	* **DiaMH/** : contains the Diabetes Mobile Health System used as case study for our approach. Instructions for deployment are contained in DiaMH/README.md
	* **GeneratedTests/** : contains the generated test suite, along with wrapper classes and configuration builder
	* **Model/** : contains the Class Diagram and the State Machine Diagram for DiaMH, along with an SCXML representation of the state machine diagram to be used by the tool.
	* **TestGenerationTool/** : contains the actual test generation tool


Information about dependencies and deployment instructions can be found in README files inside such directories.
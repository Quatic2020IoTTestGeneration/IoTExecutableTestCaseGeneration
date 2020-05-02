DiaMH
==============

This directory contains the IoT system prototype used as case study for the proposed approach. It is organized as follows:

	* **DiaMH Node-RED/** : contains the Node-RED flows that implement DiaMH Core, glucose sensor and pump. To deploy, just import the JSON files in Node-RED.
							The 'control.json' flow contains some nodes to manually control the DiaMH system.
	* **DiaMH Mobile/** : contains the Android app for the DiaMH client. Before compiling, you should set the address of the Node-RED host in the "host" field of the strings.xml resource file.
						  (DiaMH Mobile/app/src/main/res/values/strings.xml)


Dependencies
------

	* **Node-RED 0.18.4**
	* **Android Studio 2.3.2 **
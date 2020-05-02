package DiaMHTestsMaven.wrappers;

import annots.CreateAs;
import annots.ModelName;

@ModelName("InsulinPump")
@CreateAs("InsulinPump")
public interface IPump {
	
	@ModelName("reset")
	void reset();
	
	@ModelName("inject")
	void inject();
	
	@ModelName("getErogatedInjections")
	int getErogatedInjections();

}
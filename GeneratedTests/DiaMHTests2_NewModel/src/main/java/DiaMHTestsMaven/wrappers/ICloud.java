package DiaMHTestsMaven.wrappers;

import java.lang.reflect.AnnotatedElement;

import annots.CreateAs;
import annots.ModelName;

@ModelName("Cloud")
@CreateAs("Cloud")
public interface ICloud {
	
	@ModelName("reset")
	void reset();
	
	@ModelName("getCriticalCount")
	int getCriticalCount();
	
	@ModelName("receiveUnder")
	void receiveUnder();
	
	@ModelName("receiveOver")
	void receiveOver();
	
	@ModelName("resetReadings")
	void resetReadings();
	
	@ModelName("discardNext")
	void discardNext(int qty);
	
	@ModelName("getNumReadings")
	int getNumReadings();
	
	@ModelName("getMaxReadings")
	int getMaxReadings();
	
	@ModelName("getThreshold")
	int getThreshold();
	

}
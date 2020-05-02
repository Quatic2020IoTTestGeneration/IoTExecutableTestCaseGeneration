package DiaMHTestsMaven.wrappers;

import annots.CreateAs;
import annots.ModelName;

@ModelName("Sensor")
@CreateAs("GlucoseSensor")
public interface IGlucoseSensor {
	
	public void sendAndWait(int total, int over, int treshold);
	
	void sendMessage(String pattern);

	void stop();
	
	public void setFilter(Integer n);
	
	@ModelName("sendUnder")
	void sendUnder(int treshold);
	
	@ModelName("sendOver")
	void sendOver(int treshold);

}
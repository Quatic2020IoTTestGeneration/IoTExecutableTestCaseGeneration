package DiaMHTestsMaven.wrappers;

import annots.CreateAs;
import annots.ModelName;
import annots.Omitted;

@ModelName("MobileApp")
@CreateAs("MobileAppAppium")
public interface MobileApp {
	
	
	@ModelName("insulinIncrement")
	public void assertInsulinIncrement(int curr_ins, @Omitted int timeout);
	
	@ModelName("isRequiringInjection")
	public void assertRequiringInsulin(@Omitted int timeout);
	
	@ModelName("isNotRequiringInjection")
	public void assertNotRequiringInsulin(@Omitted int timeout);
	
	@ModelName("isAlarmed")
	public void assertAlarmOn(@Omitted int timeout);
	
	@ModelName("isNotAlarmed")
	public void assertAlarmOff(@Omitted int timeout);
	
	@ModelName("shutdownAlarm")
	void shutdownAlarm();
	
	@ModelName("confirmInjection")
	void confirmInsulin();

	boolean isConnectionOK(int timeout) throws InterruptedException;

	boolean isInsulineZero(int timeout) throws InterruptedException;

	boolean isInsulineEqualTo(String val, int timeout) throws InterruptedException;

	boolean isAlarmed(int timeout) throws InterruptedException;

	boolean isRequiringInsulin(int timeout) throws InterruptedException;
	

	
	
	void cancelInsulin();

	boolean isPumpFailed(int timeout);
	
	boolean insulinIncrement(int curr_ins, int timeout);
	


}
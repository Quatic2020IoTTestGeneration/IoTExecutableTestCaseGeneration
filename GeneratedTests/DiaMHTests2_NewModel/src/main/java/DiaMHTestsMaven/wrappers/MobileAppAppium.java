package DiaMHTestsMaven.wrappers;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.HashMap;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServerHasNotBeenStartedLocallyException;

public class MobileAppAppium extends BaseMobileApp {
	
	private static final String insConfBtnId = "org.quatic.diamhmobile:id/insConfBtn";
	public AppiumDriver<WebElement> driver;
    private static AppiumDriverLocalService service;
	
    public MobileAppAppium() {
    	service = AppiumDriverLocalService.buildDefaultService();
        service.start();

        if (service == null || !service.isRunning()) {
            throw new AppiumServerHasNotBeenStartedLocallyException(
                    "An appium server node is not started!");
        }
        
        File classpathRoot = new File(System.getProperty("user.dir"));
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("deviceName","Android Emulator");
        capabilities.setCapability("appPackage", "org.quatic.diamhmobile");
        capabilities.setCapability("appActivity",".MainActivity"); 
        capabilities.setCapability("newCommandTimeout", 300);
        driver = new AndroidDriver<>(service.getUrl(), capabilities);
    }
	
    public boolean locateGlucose() {
    	List<WebElement> txt = driver.findElements(By.id("org.quatic.diamhmobile:id/glcLevel"));
    	return txt.size() != 0;
    }
    
    public String getGlucoseLevelText() {
    	WebElement glc = driver.findElements(By.id("org.quatic.diamhmobile:id/glcLevel")).get(0);
    	return glc.getText();
    	
    }
    

	@Override
	public boolean isConnectionOK(int timeout) {
		waitTimeout(timeout);
		WebElement sensor = driver.findElement(By.id("org.quatic.diamhmobile:id/sensorStat"));
		WebElement pump = driver.findElement(By.id("org.quatic.diamhmobile:id/pumpStat"));
		WebElement cloud = driver.findElement(By.id("org.quatic.diamhmobile:id/cloudStat"));
		
		return sensor.getText().equals("OK") && pump.getText().equals("OK") && cloud.getText().equals("OK");
	}

	private void waitTimeout(int timeout) {
		try {
		Thread.sleep(timeout);
		} catch(Exception e) {
			
		}
	}

	@Override
	public boolean isInsulineZero(int timeout) {
		try {
			new WebDriverWait(driver, timeout)
				.until(ExpectedConditions.textToBe(By.id("org.quatic.diamhmobile:id/insLevel"), "0"));
		} catch(Exception e) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isInsulineEqualTo(String val, int timeout) {
		try {
			new WebDriverWait(driver, timeout)
				.until(ExpectedConditions.textToBe(By.id("org.quatic.diamhmobile:id/insLevel"), val));
		} catch(Exception e) {
			return false;
		}
		return true;
	}
	
	public boolean insulinIncrement(int curr_ins, int timeout) {
		String expected = Integer.toString(curr_ins+1);
		try {
			new WebDriverWait(driver, timeout)
				.until(ExpectedConditions.textToBe(By.id("org.quatic.diamhmobile:id/insLevel"), expected));
		} catch(Exception e) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isAlarmed(int timeout) {
		try {
			new WebDriverWait(driver, timeout)
				.until(ExpectedConditions.visibilityOfElementLocated(By.id("org.quatic.diamhmobile:id/alertIcon")));
		} catch(Exception e) {
			return false;
		}
		return true;
		
	}
	
	public boolean isNotAlarmed(int timeout) {
		try {
			new WebDriverWait(driver, timeout)
				.until(ExpectedConditions.invisibilityOfElementLocated(By.id("org.quatic.diamhmobile:id/alertIcon")));
		} catch(Exception e) {
			return false;
		}
		return true;
		
	}

	@Override
	public boolean isRequiringInsulin(int timeout) {
		try {
			new WebDriverWait(driver, timeout)
				.until(ExpectedConditions.elementToBeClickable(By.id(insConfBtnId)));
		} catch(Exception e) {
			return false;
		}
		return true;
		
	}
	
	@Override
	public void assertRequiringInsulin(int timeout) {
		assertTrue(isRequiringInsulin(timeout));
	}
	
	@Override
	public void assertNotRequiringInsulin(int timeout) {
		assertFalse(isRequiringInsulin(timeout));
	}
	
	@Override
	public void assertInsulinIncrement(int curr_ins, int timeout) {
		assertTrue(insulinIncrement(curr_ins, timeout));
	}
	
	@Override
	public void assertAlarmOn(int timeout) {
		assertTrue(isAlarmed(timeout));
	}
	
	@Override
	public void assertAlarmOff(int timeout) {
		assertTrue(isNotAlarmed(timeout));
	}

	@Override
	public void confirmInsulin() {
		List<WebElement> confBtnList = driver.findElements(By.id(insConfBtnId));
		WebElement confBtn = confBtnList.get(0);
		confBtn.click();
	}

	
	public void doubleClickConf() {
		List<WebElement> confBtnList = driver.findElements(By.id(insConfBtnId));
		WebElement confBtn = confBtnList.get(0);
		confBtn.click();
		confBtn.click();

	}

	@Override
	public void cancelInsulin() {
		List<WebElement> cancBtnList = driver.findElements(By.id("org.quatic.diamhmobile:id/insCancBtn"));
		WebElement cancBtn = cancBtnList.get(0);
		cancBtn.click();

	}

	@Override
	public boolean isPumpFailed(int timeout) {
		try {
			new WebDriverWait(driver, timeout)
				.until(ExpectedConditions.textToBe(By.id("org.quatic.diamhmobile:id/pumpStat"), "FAIL"));
		} catch(Exception e) {
			return false;
		}
		return true;
	}

}

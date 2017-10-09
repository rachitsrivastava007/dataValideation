package testdata;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.Select;

import testdata.ExcelRowDto;
import testdata.CheckLogHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.CellFormat;
import jxl.read.biff.BiffException;
import jxl.write.Colour;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;



public class TestFile extends CheckLogHandler {
	
	WebDriver driver;
	
	List<ExcelRowDto> processedInput = new ArrayList<ExcelRowDto>();
	
	Map<String, Object> storedObjectsMap = new HashMap<String, Object>();
	Set<String> BANs = new HashSet<String>();
	String[] BANsList = new String[1000];
	
	public  ExcelRowDto assign()
	{
		ExcelRowDto excelRowDto = new ExcelRowDto();
		//excelRowDto.setStepEntry("1");
		//excelRowDto.setKeyWord("checkLogs");
		//String[] tempData = {"serviceName","searchCustomerExtendedDetails", "messageLocation", "CLIENT_RESPONSE","accountStatus","storeValue","totalDue", "storeValue","creditClass","storeValue"} ;
		//excelRowDto.setData(tempData);
		return excelRowDto;
		
	}
	
	public void readfile() throws IOException
	{
		 WebDriver driver = null;
		 System.setProperty("webdriver.chrome.driver",Constants.CHROME_DRIVER_PATH);
		 DesiredCapabilities browserCapabilities = DesiredCapabilities.chrome();
		 LoggingPreferences logs = new LoggingPreferences();		
		 logs.enable(LogType.PERFORMANCE, Level.ALL);
		 

		ChromeOptions options = new ChromeOptions();
		options.addArguments(Arrays.asList("allow-running-insecure-content",
				"ignore-certificate-errors", "start-maximized"));
		options.addArguments("test-type");
		browserCapabilities.setCapability(ChromeOptions.CAPABILITY, options);
		browserCapabilities.setCapability(CapabilityType.LOGGING_PREFS, logs);

				//Row r=sheet.getRow(i);
				//Cell cells=r.getCell(i);
				
				
				Workbook script_WorkBook = null;
				WorkbookSettings ws = new WorkbookSettings();
				ws.setEncoding("Cp1252");	
				String keywordFilePath = Constants.FILE_PATH;
				
				try {
					script_WorkBook = Workbook.getWorkbook(new File(keywordFilePath), ws);
				} catch (BiffException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				Sheet testScript_sheet = null;
				testScript_sheet = script_WorkBook.getSheet(0);
				
				 
					
					int rows= testScript_sheet.getRows();
			         int noofBANs=rows-1;
					System.out.println("No of Rows is "+noofBANs);
					
					for(int i=1;i<rows;i++)
					{
						if (""!=testScript_sheet.getCell(0, i).getContents()) {
							BANs.add(testScript_sheet.getCell(0, i).getContents().trim());
						}
					}

					for(String BAN : BANs)
					{
						run_journey(browserCapabilities,BAN);
					}
					
					for(int i=0;(i<ReRunBans.length)&&ReRunBans[i]!=null;i++)
					{   
						String BAN =ReRunBans[i];
						try {
							run_journey(browserCapabilities, BAN);
						} catch (Exception e) {
							// TODO: handle exception
						}
					}
					writeOutputSheet(processedInput, keywordFilePath);
					
		            
				
	            /*linksize = driver.findElements(By.xpath("//span[@class='checkbox-outline']"));
	            for (WebElement myElement : linksize){
			    	if (myElement.getText() !=""){
			    		
			    		try{
			             myElement.click();
			    		}catch(Exception ex){
			    			System.out.println("Excpetion caught");
			    		}
			             //System.out.println("third");
			            }
			    }
	            driver.findElement(By.xpath("//button[text()='Continue']")).click(); 
	            Thread.sleep(2000);*/	    		
	        
							
			
	}
	
	public void writeOutputSheet(List<ExcelRowDto> data, String keywordFilePath){
		
		

		try {
			Workbook workbook = null;
			WorkbookSettings ws = new WorkbookSettings();
			ws.setEncoding("Cp1252");
			WritableWorkbook copyWorkbook = null;
			WritableSheet copySheet = null;
			
			String[] oldPath = keywordFilePath.split(".xls");
			
			String date = new SimpleDateFormat("dd.MM-H_mm_ss").format(new Date());
			String finalName = oldPath[0] +"_Output"+ "(" + "-" + date + ")" + ".xls";
			// name of the user added along with date and time in the output file
			
			workbook = Workbook.getWorkbook(new File(keywordFilePath));
			Sheet testScript_sheet = workbook.getSheet(0);
			
			copyWorkbook = Workbook.createWorkbook(new File(finalName));
			
			copySheet = copyWorkbook.createSheet("Result", 0);
			String temp;
			Label label;
			
			
			int rowcount=1;
			for(String BAN : BANs)
			{   
				label = new Label(0, rowcount, BAN);
				copySheet.addCell(label);
				BANsList[rowcount-1]=BAN;
				rowcount++;
				
			}
			
							
				
				for (int j=0 ; j<testScript_sheet.getColumns();j++){
					
					temp=testScript_sheet.getCell(j, 0).getContents();
					label = new Label(j, 0, temp);
					copySheet.addCell(label);
				}
			
			
			
			int banColumn = getColumnNumber(testScript_sheet,Constants.BAN);
			int accountInfo = getColumnNumber(testScript_sheet,Constants.ACCOUNT_INFO);
			int dueAmount = getColumnNumber(testScript_sheet,Constants.DUE_AMOUNT);
			int customerType = getColumnNumber(testScript_sheet,Constants.CUSTOMER_TYPE);
			int securityPin = getColumnNumber(testScript_sheet,Constants.SECURITY_PIN);
			int securityAnswer = getColumnNumber(testScript_sheet,Constants.SECURITY_ANSWER);
			int noOfAAL = getColumnNumber(testScript_sheet,Constants.NO_OF_AAL);
			int NoOfEarlyUpgradeSub = getColumnNumber(testScript_sheet,Constants.NO_OF_EARLY_UPGRADE);
			int NoOfEligibleSub  = getColumnNumber(testScript_sheet,Constants.TOTAL_NO_OF_UPGRADE);
			int deviceProtection = getColumnNumber(testScript_sheet,Constants.DEVICE_PROTECTION_INFO);
			int comments = getColumnNumber(testScript_sheet,Constants.COMMENTS);
			int JID = getColumnNumber(testScript_sheet,Constants.JID);
			
			for (int j = 0; j < data.size(); j++) {
				/*label = new Label(banColumn, j+1, data.get(j).getBAN());
				
				copySheet.addCell(label); */
				String BANi ;
				BANi = data.get(j).getBAN();
				int BANrow=0;
				for (int i=0;(i<BANsList.length)&&(BANsList[i]!=null);i++){
					if(BANsList[i]==BANi){
						BANrow=i;
						break;
					}
				}
				
				
				int Column;
				
                label = new Label(accountInfo, BANrow+1, data.get(j).getAccountStatus());
				
				copySheet.addCell(label);
				
                label = new Label(dueAmount, BANrow+1, data.get(j).getDueAmount());
				
				copySheet.addCell(label);
				
                label = new Label(customerType, BANrow+1, data.get(j).getCustomerType());
				
				copySheet.addCell(label);
					
				label = new Label(securityPin, BANrow+1, data.get(j).getSecurityPin());
				
				copySheet.addCell(label);
				
				label = new Label(securityAnswer, BANrow+1, data.get(j).getSecurityAnswer());
				
				copySheet.addCell(label);
				
                label = new Label(noOfAAL, BANrow+1, data.get(j).getNoOfAAL());
				
				copySheet.addCell(label);
				
                label = new Label(NoOfEarlyUpgradeSub, BANrow+1, data.get(j).getNoOfEarlyUpgardeSub());
				
				copySheet.addCell(label);
				
                label = new Label(NoOfEligibleSub, BANrow+1, data.get(j).getNoOfNormalUpgradeSub());
				
				copySheet.addCell(label);
				
                label = new Label(deviceProtection, BANrow+1, data.get(j).getdevicePortectionInfo());
				
				copySheet.addCell(label);
				
                label = new Label(JID, BANrow+1, data.get(j).getJID());
				
				copySheet.addCell(label);
				
                if (data.get(j).getComments()!="data is valid for use") {
					label = new Label(comments, BANrow + 1, data.get(j).getComments());
					copySheet.addCell(label);
				}
                else {
                    WritableFont greenFont = new WritableFont(WritableFont.ARIAL);
                    greenFont.setColour(Colour.GREEN);
                    WritableCellFormat cellFormat = new WritableCellFormat(greenFont);
                    
					label = new Label(comments, BANrow + 1, data.get(j).getComments(),cellFormat);
					copySheet.addCell(label);
					
				}
				
			}
			
			copyWorkbook.write();
			copyWorkbook.close();
			workbook.close();
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (BiffException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (RowsExceededException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		
	}
	
	 public static int getColumnNumber(Sheet sheetName, String cellContent) {

	        int columnNumber = 0;
	        Cell cell = sheetName.findCell(cellContent);
	        if (null != cell) {
	            columnNumber = cell.getColumn();
	        } 
	        return columnNumber;
	    }
	 
	 public void run_journey(DesiredCapabilities browserCapabilities,String BAN) {
		      try {
				driver = new ChromeDriver(browserCapabilities);
				
				driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
				
				String cellVal=BAN;				
				System.out.println("Account number "+cellVal);		
				driver.get("http://hb-webapp-env011.10.0.1.5.nip.io/resources/index.html");
				driver.findElement(By.id("storeId")).sendKeys("1418");
				driver.findElement(By.id("submit")).click();
				System.out.println("Clicked submit");
				Thread.sleep(15000);
				List<WebElement> upgradelinks = driver.findElements(By.xpath("//*[@id='upgrade']"));
				try {
					upgradelinks.get(1).click();
				} catch (Exception e) {
					System.out.println( e);
				}
				System.out.println("Clicked upgrade");
				Thread.sleep(10000);
				driver.findElement(By.xpath("//span[text()='Upgrade device or add a line']")).click();
				driver.findElement(By.xpath("//button[@id='submit']")).click();
				Thread.sleep(10000);
				WebElement dropdown;
				try {
					dropdown = driver.findElement(By.id("searchCriteria"));
				} catch (org.openqa.selenium.NoSuchElementException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				dropdown = driver.findElement(By.id("searchCriteria"));
				Select drop= new Select(dropdown);
				drop.selectByValue("Account number");
				
				driver.findElement(By.id("ban")).sendKeys(cellVal);
				Thread.sleep(9000);
				driver.findElement(By.xpath("//button[text()='Search']")).click(); 
				//driver.manage().timeouts().implicitlyWait(250, TimeUnit.SECONDS);
				//driver.manage().timeouts().wait(20000);
				Thread.sleep(30000);
				ExcelRowDto excelRowDto = new ExcelRowDto();
				excelRowDto.setBAN(cellVal);		           
				evaluate(driver, excelRowDto, storedObjectsMap,BAN,browserCapabilities);
				processedInput.add(excelRowDto);
				driver.close();
			} catch ( InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			    
				driver.close();
				
			}
			catch ( Exception  e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
				driver.close();
				
			}
		
	 }
	 
	public static void main(String[] args) throws IOException {
		
		TestFile obj=new TestFile();
		
		obj.readfile();
		System.exit(0);
		

	}

}

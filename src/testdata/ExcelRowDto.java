package testdata;

/**
 * The Class ExcelRowDto holds the strings in the excelRow in the sheet.
 */
public class ExcelRowDto{

    

    private String BAN="";
    
    private String accountStatus="";
    
    private String customerType="";
    
    private String securityPin="";
    
    private String securityAnswer="";
    
    private String noOfAAL="";
    
    private String noOfEarlyUpgradeSub="";
    
    private String totalNoOfEligibleSub="";
    
    private String devicePortectionInfo="";
    
    private String dueAmount="";    
	
    private String comments=""; 
    
    private String creditClass="";
    
    private String JID="";

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getBAN() {
		return BAN;
	}

	public void setBAN(String BAN) {
		this.BAN = BAN;
	}
	
	public String getAccountStatus() {
		return accountStatus;
	}

	public void setAccountStatus(String accountStatus) {
		this.accountStatus = accountStatus;
	}

	public String getDueAmount() {
		return dueAmount;
	}

	public String getCustomerType() {
		return customerType;
	}

	public void setCustomerType(String customerType) {
		this.customerType = customerType;
	}

	public String getSecurityPin() {
		return securityPin;
	}

	public void getNoOfAAL(String noOfAAL) {
		this.noOfAAL = noOfAAL;
	}
	public void setSecurityPin(String securityPin) {
		this.securityPin = securityPin;
	}

	public String getSecurityAnswer() {
		return securityAnswer;
	}

	public void setSecurityAnswer(String securityAnswer) {
		this.securityAnswer = securityAnswer;
	}

	public String getNoOfAAL() {
		return noOfAAL;
	}

	public void setNoOfAAL(String noOfAAL) {
		this.noOfAAL = noOfAAL;
	}
	
	public String getNoOfEarlyUpgardeSub() {
		return noOfEarlyUpgradeSub;
	}
	
	public void setNoOfEarlyUpgardeSub(String noOfEarlyUpgradeSub) {
		this.noOfEarlyUpgradeSub = noOfEarlyUpgradeSub;
	}
	
	public String getNoOfNormalUpgradeSub() {
		return totalNoOfEligibleSub;
	}
	
	public void settotalNoOfEligibleSub(String totalNoOfEligibleSub) {
		this.totalNoOfEligibleSub = totalNoOfEligibleSub;
	}
	
	public String getdevicePortectionInfo() {
		return devicePortectionInfo;
	}
	
	public void setdevicePortectionInfo(String devicePortectionInfo) {
		this.devicePortectionInfo = this.devicePortectionInfo+" "+devicePortectionInfo;
	}
     
	public void setDueAmount(String dueAmount) {
		this.dueAmount = dueAmount;
	}
	
	public String getJID() {
		return JID;
	}

	public void setJID(String JID) {
		this.JID = JID;
	}
	
	

	

   

   

	

   
}


package testdata;

import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.eviware.soapui.impl.WsdlInterfaceFactory;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.WsdlSubmit;
import com.eviware.soapui.impl.wsdl.WsdlSubmitContext;
import com.eviware.soapui.model.iface.Response;

public class CheckLogHandler {

	private MongoClient client = null;

	public String[] ReRunBans = new String[20];

	public DB connectToMongo() throws Exception {

		if (null != client) {
			return client.getDB("hb-mule");
		}
		client = new MongoClient("10.0.18.6", 27017);
		return client.getDB("hb-mule");
	}

	public void evaluate(WebDriver driver, ExcelRowDto excelRowDto, Map<String, Object> storedObjectsMap, String BAN,
			DesiredCapabilities browserCapabilities) {

		String journeyId = null;
		String[] urlDivsion = driver.getCurrentUrl().split("/");

		if (urlDivsion.length > 6 && null != urlDivsion[7].toString()) {
			journeyId = urlDivsion[7].toString();
		} else {

			String key = null;
			key = "SK_JourneyId_";

			// Fetching the entries from Log
			LogEntries les = driver.manage().logs().get(LogType.PERFORMANCE);
			for (LogEntry le : les) {

				if (le.getMessage().contains("GET /api/")) {
					String[] message = le.getMessage().split("url");
					String[] splitMessage = message[1].split("/");
					if (splitMessage.length > 5) {
						journeyId = message[1].split("/")[5];
						System.out.println("Journey Id: " + journeyId);

						break;
					}
				}
			}
			if (null != journeyId) {
				storedObjectsMap.put(key, journeyId);
				excelRowDto.setJID(journeyId);
			}else if(journeyId == null){
				
				for (int i=0;i<ReRunBans.length;i++){
					if (ReRunBans[i]==null){
						ReRunBans[i]=BAN;
						break;
					}
				}
			}
		}
		// excelRowDto.setActualData(journeyId);
		System.out.println("Journey Id: " + journeyId);

		checkInMongo(excelRowDto, journeyId, driver, storedObjectsMap, BAN, browserCapabilities);

	}

	public void checkInMongo(ExcelRowDto excelRowDto, String journeyId, WebDriver driver,
			Map<String, Object> storedObjectsMap, String BAN, DesiredCapabilities browserCapabilities) {

		try {
			int approvedSubscriberCount = 0, activeCount = 0, suspendedCount = 0, reservedCount = 0,
					eligibleSubscriberCount = 0, earlyUpgradeCount = 0, TTEflag = 0, nonEligibleMDNcount = 0;
			String[] nonEligibleMDN = new String[10];
			Arrays.fill(nonEligibleMDN, null);
			String accountSubType = new String("");
			String accountType = new String("");

			DB db = connectToMongo();

			DBCollection collection = db.getCollection("logs");
			BasicDBObject allQuery = new BasicDBObject();
			allQuery.put("journeyId", journeyId);
			allQuery.put("messageLocation", "CLIENT_RESPONSE");

			DBCursor cursor = collection.find(allQuery).sort(new BasicDBObject("_id", -1));// Will
																							// perform
																							// the
																							// sorting
																							// according
																							// to
																							// the
																							// time
																							// stamp
			while (cursor.hasNext()) {

				DBObject fetchDocument = cursor.next();
				System.out.println(fetchDocument.get("serviceName").toString());
				String value = "";

				if (fetchDocument.get("serviceName").toString().equalsIgnoreCase("searchCustomer")) {

					// System.out.println(fetchDocument.get("payload").toString());
					String[] responseList1 = fetchDocument.get("payload").toString().split(":");

					for (int count = 0; count < responseList1.length; count++) {

						if (responseList1[count].contains("message")) {
							String[] valueList = responseList1[count + 1].split(",");
							value = valueList[0].replace("\"", "").replaceAll("}", "").replaceAll("]", "").trim();
							// System.out.println("Value is : "+value);
							if (value.equalsIgnoreCase("Data not found")) {
								excelRowDto.setAccountStatus(value);
								excelRowDto.setComments("data cant be used");
								count = responseList1.length + 1;
							}
						}

						else if (responseList1[count].contains("accountType")) {
							String[] valueList = responseList1[count + 1].split(",");
							value = valueList[0].replace("\"", "").replaceAll("}", "").replaceAll("]", "").trim();
							// System.out.println("Value is : "+value);
							accountType = value;
						} else if (responseList1[count].contains("accountSubType")) {
							String[] valueList = responseList1[count + 1].split(",");
							value = valueList[0].replace("\"", "").replaceAll("}", "").replaceAll("]", "").trim();
							// System.out.println("Value is : "+value);
							accountSubType = value;
						}

						else if (responseList1[count].contains("activeSubscriberCount")) {
							String[] valueList = responseList1[count + 1].split(",");
							value = valueList[0].replace("\"", "").replaceAll("}", "").replaceAll("]", "").trim();
							// System.out.println("Value is : "+value);
							activeCount = Integer.parseInt(value);
						} else if (responseList1[count].contains("reservedSubscriberCount")) {
							String[] valueList = responseList1[count + 1].split(",");
							value = valueList[0].replace("\"", "").replaceAll("}", "").replaceAll("]", "").trim();
							// System.out.println("Value is : "+value);
							reservedCount = Integer.parseInt(value);
						} else if (responseList1[count].contains("suspendedSubscriberCount")) {
							String[] valueList = responseList1[count + 1].split(",");
							value = valueList[0].replace("\"", "").replaceAll("}", "").replaceAll("]", "").trim();
							// System.out.println("Value is : "+value);
							suspendedCount = Integer.parseInt(value);
							int NoOfAAL = approvedSubscriberCount - (activeCount + reservedCount + suspendedCount);
							if (approvedSubscriberCount == 0) {
								NoOfAAL = 0;
							}
							excelRowDto.setNoOfAAL(Integer.toString(NoOfAAL));
							System.out.println(NoOfAAL);

							excelRowDto.settotalNoOfEligibleSub(Integer.toString(eligibleSubscriberCount));
							excelRowDto.setNoOfEarlyUpgardeSub(Integer.toString(earlyUpgradeCount));
						}

						// Checking securityInfoPin in
						// searchCustomerExtendedDetails
						else if (responseList1[count].contains("accountStatus")) {
							String[] valueList = responseList1[count + 1].split(",");
							value = valueList[0].replace("\"", "").replaceAll("}", "").replaceAll("]", "").trim();
							// System.out.println("Value is : "+value);
							if (value.equals("S")) {
								excelRowDto.setAccountStatus("Suspended");
								excelRowDto.setComments("data cant be used");
								count = responseList1.length + 1; // exiting the
																	// current
																	// execution
																	// because
																	// the
																	// account
																	// is
																	// suspended

							} else if (value.equals("T")) {
								excelRowDto.setAccountStatus("Tentative");
							} else if (value.equals("O")) {
								if ((accountType.equalsIgnoreCase("B")) && (accountSubType.equalsIgnoreCase("B"))) {
									excelRowDto.setAccountStatus("Not supported by GST");
									excelRowDto.setComments("data cant be used");
								}

								else if ((accountType.equalsIgnoreCase("C"))
										&& (accountSubType.equalsIgnoreCase("H"))) {
									excelRowDto.setAccountStatus("Corporate");
								} else {
									excelRowDto.setAccountStatus("Open");
								}
							} else if (value.equals("N")) {
								excelRowDto.setAccountStatus("Cancelled");
								excelRowDto.setComments("data cant be used");
							}

						} else if (responseList1[count].contains("totalDue")) {
							String[] valueList = responseList1[count + 1].split(",");
							value = valueList[0].replace("\"", "").replaceAll("}", "").replaceAll("]", "").trim();
							// System.out.println("Value is : "+value);
							excelRowDto.setDueAmount(value);
							if (Float.parseFloat(value) <= 0) {
								if ("Tentative" == excelRowDto.getAccountStatus()
										|| "Open" == excelRowDto.getAccountStatus()
										|| "Corporate" == excelRowDto.getAccountStatus()) {
									excelRowDto.setComments("data is valid for use");
								}
								;
							} else {
								// create new project
								WsdlProject project = new WsdlProject();
								WsdlInterface iface = WsdlInterfaceFactory.importWsdl(project,
										"resources\\PaymentManagementService_1.wsdl", true)[0];
								WsdlOperation operation = (WsdlOperation) iface.getOperationByName("AddPayment");
								// create a new empty request for that operation
								WsdlRequest request = operation.addNewRequest("My request");
								String requestContent = new String(
										Files.readAllBytes(Paths.get("src\\testdata\\requestContent")));

								requestContent = requestContent.replace("116", excelRowDto.getDueAmount());
								requestContent = requestContent.replace("266752971", BAN);
								request.setRequestContent(requestContent);

								String URl = "http://144.226.135.45/services/PaymentManagementService/v1";
								request.setEndpoint(URl);
								request.getResponse();
								// submit the request
								WsdlSubmit submit = (WsdlSubmit) request.submit(new WsdlSubmitContext(null), false);
								// wait for the response
								Response response = submit.getResponse();
								// print the response
								String content = response.getContentAsString();
								if (!content.contains("faultcode")) {
									excelRowDto.setComments("payment done for the " + BAN + " BAN of amount " + " "
											+ excelRowDto.getDueAmount());
									System.out.println("payment done");
								} else {
									excelRowDto.setComments(
											"some problem occured during payment through SOAP for the BAN " + BAN);
								}
								for (int i=0;i<ReRunBans.length;i++){
									if (ReRunBans[i]==null){
										ReRunBans[i]=BAN;
										break;
									}
								}
							}

						} else if (responseList1[count].contains("creditClass")) {
							String[] valueList = responseList1[count + 1].split(",");
							value = valueList[0].replace("\"", "").replaceAll("}", "").replaceAll("]", "").trim();
							// System.out.println("Value is : "+value);
							if (value.equals("H1")) {
								excelRowDto.setCustomerType("Home Credit");
							} else if (excelRowDto.getCustomerType() != "Home Credit") {
								excelRowDto.setCustomerType("Sprint");
							}

						}
					}

				}

				// 1. Checking Client response of searchCustomerExtendedDetails
				if (fetchDocument.get("serviceName").toString().contains("searchCustomerExtendedDetails")) {

					// System.out.println(fetchDocument.get("payload").toString());
					String[] responseList = fetchDocument.get("payload").toString().split(":");

					for (int count = 0; count < responseList.length; count++) {

						// Checking securityInfoPin in
						// searchCustomerExtendedDetails
						if (responseList[count].contains("securityInfoPin")) {
							String[] valueList = responseList[count + 1].split(",");
							value = valueList[0].replace("\"", "").replaceAll("}", "").replaceAll("]", "").trim();
							// System.out.println("Value is : "+value);
							excelRowDto.setSecurityPin(value);
						} else if (responseList[count].contains("answer")) {
							String[] valueList = responseList[count + 1].split(",");
							value = valueList[0].replace("\"", "").replaceAll("}", "").replaceAll("]", "").trim();
							// System.out.println("Value is : "+value);
							excelRowDto.setSecurityAnswer(value);
						} else if (responseList[count].contains("approvedSubscriberCount")) {
							String[] valueList = responseList[count + 1].split(",");
							value = valueList[0].replace("\"", "").replaceAll("}", "").replaceAll("]", "").trim();
							System.out.println("Approved Count is : " + value);
							approvedSubscriberCount = Integer.parseInt(value);
						} else if (responseList[count].contains("TTE")) {
							if (TTEflag == 0) {
								excelRowDto.setdevicePortectionInfo("TTE is active on the account");
								TTEflag = 1;
							}
						} else if (responseList[count].contains("HCFINANCE")) {
							if (TTEflag == 0) {
								excelRowDto.setCustomerType("Home Credit");
								TTEflag = 1;
							}
						}

						else if (responseList[count].contains("subscriberStatus")) {
							String[] valueList = responseList[count + 1].split(",");
							value = valueList[0].replace("\"", "").replaceAll("}", "").replaceAll("]", "").trim();
							if (value.equalsIgnoreCase("Active")) {
								for (int counter = count; !responseList[counter]
										.contains("nextTierEligibilityDate"); counter++) {
									if (responseList[counter].contains("eligibleInd")) {
										String[] valueList1 = responseList[counter + 1].split(",");
										String temp = (valueList1[0].replace("\"", "").replaceAll("}", "")
												.replaceAll("]", "").trim());
										if (temp.equalsIgnoreCase("true")) {
											eligibleSubscriberCount++;
										}
										/*
										 * else
										 * if(temp.equalsIgnoreCase("false")) {
										 * checkEarlyUpgradeEligibility=1; } ;
										 */
									}
									;
								}
								;
							}
							;
						} else if (responseList[count].contains("socCodes")) {
							String[] valueList = responseList[count + 1].split(",");

							try {
								for (int i = 0; i < valueList.length; i++) {
									valueList[i] = valueList[i].replace("\"", "").replaceAll("}", "")
											.replaceAll("]", "").trim();

								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							if (Arrays.asList(valueList).contains("TEPPLUS")) {
								int counter = 0;
								for (counter = count; !responseList[counter].contains("subscriberStatus"); counter++) {
								}
								;
								String[] valueList1 = responseList[counter].split(",");
								String tempMDN = (valueList1[0].replace("\"", "").replaceAll("}", "")
										.replaceAll("]", "").trim());
								String[] valueList2 = responseList[counter + 1].split(",");
								String subscriberStatus = (valueList2[0].replace("\"", "").replaceAll("}", "")
										.replaceAll("]", "").trim());
								for (; !responseList[counter].contains("eligibleInd"); counter++) {
								}
								;
								String[] valueList3 = responseList[counter + 1].split(",");
								String eligibleInd = (valueList3[0].replace("\"", "").replaceAll("}", "")
										.replaceAll("]", "").trim());
								if (subscriberStatus.contains("Active") && eligibleInd.contains("true")) {
									excelRowDto.setdevicePortectionInfo(tempMDN + " has TEPplus, ");
								} else if (subscriberStatus.contains("Active") && eligibleInd.contains("false")) {
									if (Arrays.asList(valueList).contains("tempMDN")) {
										excelRowDto.setdevicePortectionInfo(tempMDN + " has TEPplus, ");
									}

								}

							}
							;
						}
					}

				}

				if (fetchDocument.get("serviceName").toString().contains("checkEarlyUpgradeEligibility")) {

					String[] responseList3 = fetchDocument.get("payload").toString().split(":");
					for (int count2 = 0; count2 < responseList3.length; count2++) {
						if (responseList3[count2].contains("earlyUpgradeEligibilityInd")) {
							String[] valueList4 = responseList3[count2 + 1].split(",");
							value = valueList4[0].replace("\"", "").replaceAll("}", "").replaceAll("]", "").trim();
							if (value.equalsIgnoreCase("true")) {
								earlyUpgradeCount++;

								String[] valueList5 = responseList3[count2 - 1].split(",");
								String TempMDN = valueList5[0].replace("\"", "").replaceAll("}", "").replaceAll("]", "")
										.trim();
								nonEligibleMDN[nonEligibleMDNcount] = TempMDN;
								nonEligibleMDNcount++;
								;

							} else if (value.equalsIgnoreCase("false")) {
							}
						}
					}

				}

			}
			/*
			 * if(!logAvailable){
			 * 
			 * System.out.println(
			 * "No Log available for the mentioned request/response "+
			 * logFormatSplit[0] +" , "+ logFormatSplit[1]);
			 * 
			 * excelRowDto.setFailureReason(
			 * "No Log available for the mentioned request/response "+
			 * logFormatSplit[0] +" , "+ logFormatSplit[1]);
			 * setFinalResult(false, excelRowDto, driver); }
			 * setFinalResult(logResult, excelRowDto, driver);
			 */
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean checkinClientResponseLogs(String fetchDocument, String[] logFormatSplit, ExcelRowDto excelRowDto,
			WebDriver driver, Map<String, Object> storedObjectsMap) {

		boolean logResult = true;
		String[] numberOfInstance = null, valueOfField = null;
		String[] fieldValueInResponse = new String[logFormatSplit.length - 2];
		String[] key = new String[logFormatSplit.length - 2];

		for (int count = 2; count < logFormatSplit.length; count++) {
			valueOfField = logFormatSplit[count].split(":");
			key[count - 2] = valueOfField[0];

			numberOfInstance = fetchDocument.split(",");
			for (int countNew = 0; countNew < numberOfInstance.length; countNew++) {
				if (numberOfInstance[countNew].contains(valueOfField[0])) {
					String[] fieldsArray = numberOfInstance[countNew].split(":");

					fieldValueInResponse[count - 2] = fieldsArray[fieldsArray.length - 1].replace("\"", "")
							.replaceAll("}", "").replaceAll("]", "").trim();
					break;
				}
			}

		}
		if (logFormatSplit[0].split(":")[1].equalsIgnoreCase("searchCustomer")) {
			try {
				if (fieldValueInResponse[0].equalsIgnoreCase("S")) {
					System.out.println("Account is suspeneded");
				} else if (fieldValueInResponse[0].equalsIgnoreCase("O")) {
					System.out.println("Account is open");
				}
				;
				if (Float.parseFloat(fieldValueInResponse[1]) < 0) {
					System.out.println("account is not due");
				} else {
					System.out.println("Account is due with amount " + fieldValueInResponse[1]);
				}
				;
				if (fieldValueInResponse[2].equalsIgnoreCase("H1")) {
					System.out.println("Account is a home credit customer");
				} else {
					System.out.println("Account is sprint customer");
				}
				;

			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (logFormatSplit[0].split(":")[1].equalsIgnoreCase("searchCustomerExtendedDetails")) {
			try {
				if (fieldValueInResponse[0].equalsIgnoreCase("S")) {
					System.out.println("Account is suspeneded");
				} else if (fieldValueInResponse[0].equalsIgnoreCase("O")) {
					System.out.println("Account is open");
				}
				;
				if (Float.parseFloat(fieldValueInResponse[1]) < 0) {
					System.out.println("account is not due");
				} else {
					System.out.println("Account is due with amount " + fieldValueInResponse[1]);
				}
				;
				if (fieldValueInResponse[2].equalsIgnoreCase("H1")) {
					System.out.println("Account is a home credit customer");
				} else {
					System.out.println("Account is sprint customer");
				}
				;

			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return logResult;
	}

}

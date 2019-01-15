# Deprecated Sample Code for XML request/response


## HELLO ORGANISATION EXAMPLE
This is the code we used in our video walking through how to create a new Eclipse project, add your dependencies and make your first API call.
[Watch this video](https://youtu.be/V9SJ8zK0x6I). 

For Public & Partner Apps, you'll implement 3 legged oAuth - Private Apps can skip down to the Data Endpoints (your Consumer Key is your long lived  Access Token)

*RequestTokenServlet.java*
```java
package com.xero.example;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xero.api.Config;
import com.xero.api.JsonConfig;
import com.xero.api.OAuthAuthorizeToken;
import com.xero.api.OAuthRequestToken;

@WebServlet("/RequestTokenServlet")
public class RequestTokenServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public RequestTokenServlet() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		try {
			Config config = JsonConfig.getInstance();
			
			OAuthRequestToken requestToken = new OAuthRequestToken(config);
			requestToken.execute();	
			
			TokenStorage storage = new TokenStorage();
			storage.save(response,requestToken.getAll());

			OAuthAuthorizeToken authToken = new OAuthAuthorizeToken(config, requestToken.getTempToken());
			response.sendRedirect(authToken.getAuthUrl());	
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}

	}
}
```

In your callback Servlet you'll read the query params and swap your temporary for your 30 min access token. 

*CallbackServlet.java*
```java
package com.xero.example;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xero.api.OAuthAccessToken;
import com.xero.api.XeroClient;
import com.xero.model.Organisation;
import com.xero.api.Config;
import com.xero.api.JsonConfig;

@WebServlet("/CallbackServlet")
public class CallbackServlet extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
	
	public CallbackServlet() 
	{
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
			TokenStorage storage = new TokenStorage();
		String verifier = request.getParameter("oauth_verifier");

		try {
			Config config = JsonConfig.getInstance();
			
			OAuthAccessToken accessToken = new OAuthAccessToken(config);
			
			accessToken.build(verifier,storage.get(request,"tempToken"),storage.get(request,"tempTokenSecret")).execute();
			
			if(!accessToken.isSuccess()) {
				storage.clear(response);
				request.getRequestDispatcher("index.jsp").forward(request, response);
			} else {
				storage.save(response,accessToken.getAll());			
				
				XeroClient client = new XeroClient();
				client.setOAuthToken(accessToken.getToken(), accessToken.getTokenSecret());
				
				List<Organisation> newOrganisation = client.getOrganisations();
				System.out.println("Get a Organisation - Name : " + newOrganisation.get(0).getName());		
			}
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}	
}		
```

The TokenStorage class uses cookies to store your temporary token & secret so they can be swapped for 30 min access token & secret.  Of course, you'd want to create your own implementation to store this user information in a database.  This class is merely for demo purposes so you can trying out the SDK.

*TokenStorage.java*
```java
package com.xero.example;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TokenStorage 
{
	
	public  TokenStorage() 
	{
		super();
	}

	public String get(HttpServletRequest request,String key)
	{
		String item = null;
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) 
			{
				if (cookies[i].getName().equals(key)) 
				{
					item = cookies[i].getValue();
				}
			}
		}
		return item;
	}
	
	public boolean tokenIsNull(String token) {
		if (token != null && !token.isEmpty()) { 
			return false;
		} else {
			return true;
		}
	}

	public void clear(HttpServletResponse response)
	{
		HashMap<String,String> map = new HashMap<String,String>();
		map.put("tempToken","");
		map.put("tempTokenSecret","");
		map.put("sessionHandle","");
		map.put("tokenTimestamp","");

		save(response,map);
	}

	public void save(HttpServletResponse response,HashMap<String,String> map)
	{
		Set<Entry<String, String>> set = map.entrySet();
		Iterator<Entry<String, String>> iterator = set.iterator();
	
		while(iterator.hasNext()) {
			Map.Entry<?, ?> mentry = iterator.next();
			String key = (String)mentry.getKey();
			String value = (String)mentry.getValue();

			Cookie t = new Cookie(key,value);
			response.addCookie(t);
		}
	}
}
```


## INIT XEROCLIENT
```java 
	XeroClient client = new XeroClient();
	client.setOAuthToken(accessToken.getToken(), accessToken.getTokenSecret());
```

## ACCOUNTS
```java 
try {
	List<Account> newAccount = client.createAccounts(SampleData.loadAccount().getAccount());
	messages.add("Create a new Account - Name : " + newAccount.get(0).getName() 
			+ " Description : " + newAccount.get(0).getDescription() + "");
	
	List<Account> accountWhere = client.getAccounts(null,"Type==\"BANK\"",null);
	if(accountWhere.size() > 0) {
		
		messages.add("Get a Account with WHERE clause - Name : " + accountWhere.get(0).getName() + "");
	} else {
		messages.add("Get a Account with WHERE clause - No Acccounts of Type BANK found");
	}

	List<Account> accountList = client.getAccounts();
	int num = SampleData.findRandomNum(accountList.size());
	messages.add("Get a random Account - Name : " + accountList.get(num).getName() + "");

	Account accountOne = client.getAccount(accountList.get(num).getAccountID());
	messages.add("Get a single Account - Name : " + accountOne.getName() + "");
	
	newAccount.get(0).setDescription("Monsters Inc.");
	newAccount.get(0).setStatus(null);
	List<Account> updateAccount = client.updateAccount(newAccount);
	messages.add("Update Account - Name : " + updateAccount.get(0).getName() 
			+ " Description : " + updateAccount.get(0).getDescription() + "");

	String status = client.deleteAccount(newAccount.get(0).getAccountID());
	messages.add("Delete Account - Name : " + newAccount.get(0).getName() 
			+ " result : " + status + "");
} catch (XeroApiException e) {
	System.out.println(e.getResponseCode());
	System.out.println(e.getMessage());	
}	
```


## ATTACHMENTS
```java
try {

	List<Invoice> newInvoice = client.createInvoices(SampleData.loadInvoice().getInvoice());
	messages.add("Create a new Invoice ID : " + newInvoice.get(0).getInvoiceID());
	
	InputStream inputStream = JsonConfig.class.getResourceAsStream("/helo-heros.jpg");
	byte[] bytes = IOUtils.toByteArray(inputStream);
	
	String fileName = "sample.jpg";
	Attachment invoiceAttachment = client.createAttachment("Invoices",newInvoice.get(0).getInvoiceID(), fileName, "application/jpeg", bytes,true);
	messages.add("Attachment to Invoice complete - ID: " + invoiceAttachment.getAttachmentID());
	
	List<Attachment> getInvoiceAttachment = client.getAttachments("Invoices", newInvoice.get(0).getInvoiceID());
	messages.add("Get Attachment for Invoice - complete -attachment ID: " + getInvoiceAttachment.get(0).getAttachmentID());	
	
	System.out.println(getInvoiceAttachment.get(0).getFileName() + " --- " +getInvoiceAttachment.get(0).getMimeType());
	
	File f = new File("./");
	String fileName1 = getInvoiceAttachment.get(0).getFileName();
	String dirPath =  f.getCanonicalPath();
	String saveFilePath = dirPath + File.separator + fileName1;

	InputStream in = client.getAttachmentContentById("Invoices",newInvoice.get(0).getInvoiceID(),getInvoiceAttachment.get(0).getAttachmentID(),getInvoiceAttachment.get(0).getMimeType());
	
	OutputStream out = new FileOutputStream(saveFilePath);

	// Transfer bytes from in to out
	byte[] buf = new byte[1024];
	int len;
	while ((len = in.read(buf)) > 0) {
	    out.write(buf, 0, len);
	}
	in.close();
	out.close();
	messages.add("Get Attachment content - save to server - location: " + saveFilePath );				
	
} catch (XeroApiException e) {
	System.out.println(e.getResponseCode());
	System.out.println(e.getMessage());	
}	
```


## BANK TRANSACTIONS
```java 
try {
	List<Account> accountWhere = client.getAccounts(null,"Type==\"BANK\"",null);
	if(accountWhere.size() > 0) {
		
		List<BankTransaction> newBankTransaction = client.createBankTransactions(SampleData.loadBankTransaction().getBankTransaction());
		messages.add("Create a new Bank Transaction - ID : " +newBankTransaction.get(0).getBankTransactionID() + "");
	
		List<BankTransaction> BankTransactionWhere = client.getBankTransactions(null,"Status==\"AUTHORISED\"",null,null);
		messages.add("Get a BankTransaction with WHERE clause - ID : " + BankTransactionWhere.get(0).getBankTransactionID() + "");
		
		List<BankTransaction> BankTransactionList = client.getBankTransactions();
		int num = SampleData.findRandomNum(BankTransactionList.size());
		messages.add("Get a random BankTransaction - ID : " + BankTransactionList.get(num).getBankTransactionID() + "");
	
		BankTransaction BankTransactionOne = client.getBankTransaction(BankTransactionList.get(num).getBankTransactionID());
		messages.add("Get a single BankTransaction - ID : " + BankTransactionOne.getBankTransactionID());
		
		newBankTransaction.get(0).setReference("My Updated Reference");
		List<BankTransaction> updatedBankTransaction = client.updateBankTransactions(newBankTransaction);
		messages.add("Updated a new Bank Transaction - ID : " +updatedBankTransaction.get(0).getBankTransactionID() + "");
		
	} else {
		messages.add("Please create a Bank Acccount before using the BankTransaction Endpoint");
	}

} catch (XeroApiException e) {
	System.out.println(e.getResponseCode());
	System.out.println(e.getMessage());
}
```

## BANK TRANSFERS
```java 
try {
	List<Account> accountWhere = client.getAccounts(null,"Type==\"BANK\"",null);
	if(accountWhere.size() > 0) {
		
		List<BankTransfer> bts = SampleData.loadBankTransfer().getBankTransfer();
		if (bts.size() > 0) {
			List<BankTransfer> newBankTransfer = client.createBankTransfers(SampleData.loadBankTransfer().getBankTransfer());
			messages.add("Create a new Bank Transfer - ID : " +newBankTransfer.get(0).getBankTransferID());
		} else {
			messages.add("Can not create a new Bank Transfer without 2 Bank Accounts"); 
		}
		
		List<BankTransfer> BankTransferWhere = client.getBankTransfers(null,"Amount>Decimal(1.00)",null);
		if(BankTransferWhere.size() > 0) {
			messages.add("Get a BankTransfer with WHERE clause - ID : " + BankTransferWhere.get(0).getBankTransferID());
		}
		
		List<BankTransfer> BankTransferList = client.getBankTransfers();
		if(BankTransferList.size() > 0) {
			int num3 = SampleData.findRandomNum(BankTransferList.size());
			messages.add("Get a random BankTransfer - ID : " + BankTransferList.get(num3).getBankTransferID());
		
			BankTransfer BankTransferOne = client.getBankTransfer(BankTransferList.get(num3).getBankTransferID());
			messages.add("Get a single BankTransfer - ID : " + BankTransferOne.getBankTransferID());
		} else {
			messages.add("No Bank Transfers Found ");
		}
	} else {
		messages.add("Please create a Bank Acccount before using the BankTransfer Endpoint");
	}
} catch (XeroApiException e) {
	System.out.println(e.getResponseCode());
	System.out.println(e.getMessage());	
}	
```

## BRANDING THEMES
```java 
try {
	List<BrandingTheme> newBrandingTheme = client.getBrandingThemes();
	messages.add("Get a Branding Theme - Name : " + newBrandingTheme.get(0).getName());
} catch (XeroApiException e) {
	System.out.println(e.getResponseCode());
	System.out.println(e.getMessage());	
}	
```

## CONTACTS
```java 
try {
	List<Contact> newContact = client.createContact(SampleData.loadContact().getContact());
	messages.add("Create a new Contact - Name : " + newContact.get(0).getName() + " Email : " + newContact.get(0).getEmailAddress());
	
	List<Contact> ContactWhere = client.getContacts(null,"ContactStatus==\"ACTIVE\"",null,null);
	messages.add("Get a Contact with WHERE clause - ID : " + ContactWhere.get(0).getContactID());
	
	List<Contact> ContactList = client.getContacts();
	int num4 = SampleData.findRandomNum(ContactList.size());
	messages.add("Get a random Contact - ID : " + ContactList.get(num4).getContactID());

	Contact ContactOne = client.getContact(ContactList.get(num4).getContactID());
	messages.add("Get a single Contact - ID : " + ContactOne.getContactID());
 	
	newContact.get(0).setEmailAddress("sid.maestre+barney@xero.com");
	List<Contact> updateContact = client.updateContact(newContact);
	messages.add("Update Contact - Name : " + updateContact.get(0).getName() + " email : " + updateContact.get(0).getEmailAddress());
} catch (XeroApiException e) {
	System.out.println(e.getResponseCode());
	System.out.println(e.getMessage());	
}	
```

## CONTACT GROUP
```java 
try {
	List<ContactGroup> newContactGroup = client.createContactGroups(SampleData.loadContactGroup().getContactGroup());
	messages.add("Create a new Contact Group - ID : " + newContactGroup.get(0).getContactGroupID());
	
	List<ContactGroup> newContactGroup2 = client.createContactGroups(SampleData.loadContactGroup().getContactGroup());
	messages.add("Create a new Contact Group 2 - ID : " + newContactGroup2.get(0).getContactGroupID());
	
	List<ContactGroup> ContactGroupWhere = client.getContactGroups(null,"Status==\"ACTIVE\"",null);
	messages.add("Get a ContactGroup with WHERE clause - ID : " + ContactGroupWhere.get(0).getContactGroupID());
	
	List<ContactGroup> ContactGroupList = client.getContactGroups();
	int num = SampleData.findRandomNum(ContactGroupList.size());
	messages.add("Get a random ContactGroup - ID : " + ContactGroupList.get(num).getContactGroupID());
	
	ContactGroup ContactGroupOne = client.getContactGroup(ContactGroupList.get(num).getContactGroupID());
	messages.add("Get a single ContactGroup - ID : " + ContactGroupOne.getContactGroupID());
			
	newContactGroup.get(0).setName("My Updated Group-" + SampleData.loadRandomNum());
	List<ContactGroup> updateContactGroup = client.updateContactGroup(newContactGroup);
	messages.add("Update Contact Group - ID : " + updateContactGroup.get(0).getContactGroupID() + " - Name: " + updateContactGroup.get(0).getName());
	
	List<ContactGroup> deleteContactGroup = client.deleteContactGroup(ContactGroupList.get(num));
	messages.add("Delete Contact Group - Deleted : " + deleteContactGroup.get(0).getContactGroupID());
} catch (XeroApiException e) {
	System.out.println(e.getResponseCode());
	System.out.println(e.getMessage());
}	
```

## CONTACT GROUP CONTACTS
```java 
try {
	List<ContactGroup> newContactGroup = client.createContactGroups(SampleData.loadContactGroup().getContactGroup());
	messages.add("Create a new Contact Group - ID : " + newContactGroup.get(0).getContactGroupID());
	
	ArrayOfContact arrayContact = new ArrayOfContact();
	arrayContact.getContact().add(SampleData.loadSingleContact());
	List<Contact> newContactGroupContacts = client.createContactGroupContacts(arrayContact.getContact(),newContactGroup.get(0).getContactGroupID());
	messages.add("Add Contacts to Contact Group = ContactId : " + newContactGroupContacts.get(0).getContactID());
	
	String deleteSingleContactStatus = client.deleteSingleContactFromContactGroup(newContactGroup.get(0).getContactGroupID(),arrayContact.getContact().get(0).getContactID());
	messages.add("Delete Single Contact from Group - Deleted Status: " + deleteSingleContactStatus);
	
} catch (XeroApiException e) {
	System.out.println("ERROR");
	System.out.println(e.getResponseCode());
	System.out.println(e.getMessage());
}				
```

## CREDIT NOTES
```java 
try {
	List<CreditNote> newCreditNote = client.createCreditNotes(SampleData.loadCreditNote().getCreditNote());
	messages.add("Create a new CreditNote ID : " + newCreditNote.get(0).getCreditNoteID() );
	
	List<Allocation> newAllocation = client.createCreditNoteAllocations(SampleData.loadCreditNoteAllocation().getAllocation(),newCreditNote.get(0).getCreditNoteID());
	messages.add("Create a new Overpayment and Allocate - Applied Amount : " + newAllocation.get(0).getAppliedAmount());

	List<CreditNote> newCreditNote4dp = client.createCreditNotes(SampleData.loadCreditNote4dp().getCreditNote(),"4");
	messages.add("Create a new CreditNote ID 4dp: " + newCreditNote4dp.get(0).getCreditNoteID() );
	
	List<CreditNote> CreditNoteWhere = client.getCreditNotes(null,"Status==\"DRAFT\"",null);
	if(CreditNoteWhere.size() > 0) {
		messages.add("Get a CreditNote with WHERE clause - ID: " + CreditNoteWhere.get(0).getCreditNoteID());
	}
	
	List<CreditNote> CreditNotePage = client.getCreditNotes(null,"Status==\"DRAFT\"",null,"1");
	if(CreditNotePage.size() > 0) {
		messages.add("Get a CreditNote with PAGE=1 clause - ID: " + CreditNoteWhere.get(0).getCreditNoteID());
	}
	
	List<CreditNote> CreditNoteList = client.getCreditNotes();
	int num = SampleData.findRandomNum(CreditNoteList.size());
	messages.add("Get a random CreditNote - ID : " + CreditNoteList.get(num).getCreditNoteID());
			
	CreditNote CreditNoteOne = client.getCreditNote(CreditNoteList.get(num).getCreditNoteID());
	messages.add("Get a single CreditNote - ID : " + CreditNoteOne.getCreditNoteID());
		
	newCreditNote.get(0).setReference("My updated Credit Note");
	List<CreditNote> updateCreditNote = client.updateCreditNote(newCreditNote);
	messages.add("Update CreditNote - ID: " + updateCreditNote.get(0).getCreditNoteID() + " - Reference: " + updateCreditNote.get(0).getReference());
	
	List<CreditNote> CreditNoteListForPdf = client.getCreditNotes();
	
	// GET PDF of CREDIT NOTE
	File f = new File("./");
	String dirPath =  f.getCanonicalPath();
	ByteArrayInputStream input = client.getCreditNotePdfContent(CreditNoteListForPdf.get(0).getCreditNoteID());
	
	String fileName = "creditnote.pdf";
	
	FileOutputStream output = new FileOutputStream(fileName);

	int DEFAULT_BUFFER_SIZE = 1024;
	byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
	int n = 0;

	n = input.read(buffer, 0, DEFAULT_BUFFER_SIZE);

	while (n >= 0) {
	   output.write(buffer, 0, n);
	   n = input.read(buffer, 0, DEFAULT_BUFFER_SIZE);
	}
	
	input.close();
	output.close();
	
	String saveFilePath = dirPath + File.separator + fileName;
	messages.add("Get a PDF copy of CreditNote - save it here: " + saveFilePath);

} catch (XeroApiException e) {
	System.out.println(e.getResponseCode());
	System.out.println(e.getMessage());	
}	
```

## CURRENCIES
```java 
try {
	List<Currency> newCurrency = client.getCurrencies();
	messages.add("GET a Currency - Description: " + newCurrency.get(0).getDescription());
	
	List<Currency> currency = client.createCurrencies(SampleData.loadCurrency().getCurrency());
	messages.add("Create a new Currency : " + currency.get(0).getDescription());
	
} catch (XeroApiException e) {
	System.out.println(e.getResponseCode());
	System.out.println(e.getMessage());	
}	
```


## EMPLOYEES
```java 
try {
	List<Employee> newEmployee = client.createEmployees(SampleData.loadEmployee().getEmployee());
	messages.add("Create a new Employee - ID: " + newEmployee.get(0).getEmployeeID());
	
	List<Employee> EmployeeWhere = client.getEmployees(null,"Status==\"ACTIVE\"",null);
	messages.add("Get a Employee with WHERE clause - FirstName : " + EmployeeWhere.get(0).getFirstName());
	
	List<Employee> EmployeeList = client.getEmployees();
	int num6 = SampleData.findRandomNum(EmployeeList.size());
	messages.add("Get a random Employee - FirstName : " + EmployeeList.get(num6).getFirstName());

	Employee EmployeeOne = client.getEmployee(EmployeeList.get(num6).getEmployeeID());
	messages.add("Get a single Employee - FirstName : " + EmployeeOne.getFirstName());
	
	newEmployee.get(0).setFirstName("David");
	newEmployee.get(0).setStatus(null);
	List<Employee> updateEmployee = client.updateEmployee(newEmployee);
	messages.add("Update the Employee - FirstName : " + updateEmployee.get(0).getFirstName() + " - LastName : " + updateEmployee.get(0).getLastName());
} catch (XeroApiException e) {
	System.out.println(e.getResponseCode());
	System.out.println(e.getMessage());	
}	
```

## EXPENSE CLAIMS
```java 
try {
	
	List<ExpenseClaim> newExpenseClaim = client.createExpenseClaims(SampleData.loadExpenseClaim().getExpenseClaim());
	messages.add("Create a new Expense Claim - ID : " + newExpenseClaim.get(0).getExpenseClaimID() + " Status : " + newExpenseClaim.get(0).getStatus());					
	
	List<ExpenseClaim> ExpenseClaimWhere = client.getExpenseClaims(null,"AmountDue>Decimal(1.00)",null);
	messages.add("Get a ExpenseClaim with WHERE clause - ID" + ExpenseClaimWhere.get(0).getExpenseClaimID());
	
	List<ExpenseClaim> ExpenseClaimList = client.getExpenseClaims();
	int num = SampleData.findRandomNum(ExpenseClaimList.size());
	messages.add("Get a random ExpenseClaim - ID : " + ExpenseClaimList.get(num).getExpenseClaimID());

	ExpenseClaim ExpenseClaimOne = client.getExpenseClaim(ExpenseClaimList.get(num).getExpenseClaimID());
	messages.add("Get a single ExpenseClaim - ID : " + ExpenseClaimOne.getExpenseClaimID());
	
	newExpenseClaim.get(0).setStatus(ExpenseClaimStatus.AUTHORISED);;
	List<ExpenseClaim> updateExpenseClaim = client.updateExpenseClaim(newExpenseClaim);
	messages.add("Update the ExpenseClaim - ID : " + updateExpenseClaim.get(0).getExpenseClaimID() + " Status : " + updateExpenseClaim.get(0).getStatus());
	
} catch (XeroApiException e) {
	System.out.println(e.getResponseCode());
	System.out.println(e.getMessage());	
}	
```

## INVOICES
```java 
try {

	List<Invoice> newInvoice = client.createInvoices(SampleData.loadInvoice().getInvoice());
	newInvoice.get(0).setReference("Just Created my Ref.");
	messages.add("Create a new Invoice ID : " + newInvoice.get(0).getInvoiceID());
	
	List<Invoice> InvoiceWhere = client.getInvoices(null,"Status==\"DRAFT\"",null,null,null);
	messages.add("Get a Invoice with WHERE clause - InvNum : " + InvoiceWhere.get(0).getInvoiceID());
	
	// Set If Modified Since in last 24 hours
	Date date = new Date();
	Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    cal.add(Calendar.DAY_OF_MONTH, -1);
    
    List<Invoice> InvoiceList24hour = client.getInvoices(cal.getTime(),null,null,null,null);
	messages.add("How many invoices modified in last 24 hours?: " + InvoiceList24hour.size());
	
	List<Invoice> InvoiceList = client.getInvoices();
	int num7 = SampleData.findRandomNum(InvoiceList.size());
	messages.add("Get a random Invoice - InvNum : " + InvoiceList.get(num7).getInvoiceID());
	
	Invoice InvoiceOne = client.getInvoice(InvoiceList.get(num7).getInvoiceID());
	messages.add("Get a single Invoice - InvNum : " + InvoiceOne.getInvoiceID());
	
	String ids = InvoiceList.get(0).getInvoiceID() + "," + InvoiceList.get(1).getInvoiceID();
	
	List<Invoice> InvoiceMultiple = client.getInvoices(null,null,null,null,ids);
	messages.add("Get a Muliple Invoices by ID filter : " + InvoiceMultiple.size());
	
	String invNum ="%SIDNEY";
	Map<String, String> filter = new HashMap<>();
	addToMapIfNotNull(filter, "invoicenumbers", invNum);
	List<Invoice> InvoiceMultiple2 = client.getInvoices(null,"Type==\"ACCREC\"",null,null,"4",filter);
	messages.add("Get a Muliple Invoices by ID filter : " + InvoiceMultiple2.size());

	newInvoice.get(0).setReference("Just Updated APRIL my Ref.");
	newInvoice.get(0).setStatus(null);
	List<Invoice> updateInvoice = client.updateInvoice(newInvoice);
	messages.add("Update the Invoice - InvNum : " + updateInvoice.get(0).getInvoiceID() + " - Reference : " + updateInvoice.get(0).getReference());

	// GET PDF of Invoice
	File f = new File("./");
	String dirPath =  f.getCanonicalPath();
	ByteArrayInputStream input = client.getInvoicePdfContent(InvoiceList.get(0).getInvoiceID());
	
	String fileName = "invoice.pdf";
	
	FileOutputStream output = new FileOutputStream(fileName);

	int DEFAULT_BUFFER_SIZE = 1024;
	byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
	int n = 0;

	n = input.read(buffer, 0, DEFAULT_BUFFER_SIZE);

	while (n >= 0) {
	   output.write(buffer, 0, n);
	   n = input.read(buffer, 0, DEFAULT_BUFFER_SIZE);
	}
	
	input.close();
	output.close();
	
	String saveFilePath = dirPath + File.separator + fileName;
	messages.add("Get a PDF copy of Invoice - save it here: " + saveFilePath);

} catch (XeroApiException e) {
	System.out.println(e.getResponseCode());
	System.out.println(e.getMessage());
}

try {
	List<Invoice> InvoiceList = client.getInvoices();
	int num33 = SampleData.findRandomNum(InvoiceList.size());
	OnlineInvoice OnlineInvoice = client.getOnlineInvoice(InvoiceList.get(num33).getInvoiceID());
	messages.add("Get a Online Invoice -  : " + OnlineInvoice.getOnlineInvoiceUrl());
} catch (XeroApiException e) {
	System.out.println(e.getResponseCode());
	System.out.println(e.getMessage());
	messages.add("Error - online invoice: " + e.getMessage());
}	
```

## INVOICE REMINDERS
```java 
try {
	List<InvoiceReminder> newInvoiceReminder = client.getInvoiceReminders();
	messages.add("Get a Invoice Reminder - Is Enabled: " + newInvoiceReminder.get(0).isEnabled() );
} catch (XeroApiException e) {
	System.out.println(e.getResponseCode());
	System.out.println(e.getMessage());
}
```

## ITEMS
```java 
try {
	List<Item> newItem = client.createItems(SampleData.loadItem().getItem());
	messages.add("Create a new Item - ID : " + newItem.get(0).getItemID());
	
	List<Item> ItemWhere = client.getItems(null,"IsSold==true",null);
	messages.add("Get a Item with WHERE clause - Name : " + ItemWhere.get(0).getName());
	
	List<Item> ItemList = client.getItems();
	int num = SampleData.findRandomNum(ItemList.size());
	messages.add("Get a random Item - Name : " + ItemList.get(num).getName());

	Item ItemOne = client.getItem(ItemList.get(num).getItemID());
	messages.add("Get a single Item - Name : " + ItemOne.getName());	
	
	newItem.get(0).setDescription("My Updated Description");
	newItem.get(0).setStatus(null);
	List<Item> updateItem = client.updateItem(newItem);
	messages.add("Update the Item - Name : " + updateItem.get(0).getName() + " - Description : " + updateItem.get(0).getDescription());

	String status = client.deleteItem(newItem.get(0).getItemID());
	messages.add("Delete a new Item - Delete result: " + status);

} catch (XeroApiException e) {
	System.out.println(e.getResponseCode());
	System.out.println(e.getMessage());
}

```

## JOURNALS
```java 
try {
	List<Journal> newJournal = client.getJournals();
	messages.add("Get a Journal - Number : " + newJournal.get(0).getJournalNumber() + " - ID: " + newJournal.get(0).getJournalID());

	List<Journal> newJournalOffset = client.getJournals(null,"10",true);
	messages.add("Get a Journal - Number : " + newJournalOffset.get(0).getJournalNumber() + " - ID: " + newJournalOffset.get(0).getJournalID());

} catch (XeroApiException e) {
	System.out.println(e.getResponseCode());
	System.out.println(e.getMessage());
}

```

## LINKED TRANSACTIONS
```java 
try {
	List<LinkedTransaction> newLinkedTransaction = client.createLinkedTransactions(SampleData.loadLinkedTransaction().getLinkedTransaction());
	System.out.println(newLinkedTransaction.get(0).getLinkedTransactionID());
	messages.add("Create a new LinkedTransaction -  Id:" + newLinkedTransaction.get(0).getContactID());
	
	List<LinkedTransaction> LinkedTransactionWhere = client.getLinkedTransactions(null,"Status==\"BANK\"",null,null);
	messages.add("Get a LinkedTransaction with WHERE clause - ID : " + LinkedTransactionWhere.get(0).getLinkedTransactionID());
	
	List<LinkedTransaction> LinkedTransactionList = client.getLinkedTransactions();
	int num = SampleData.findRandomNum(LinkedTransactionList.size());
	messages.add("Get a random LinkedTransaction - ID : " + LinkedTransactionList.get(num).getLinkedTransactionID());

	LinkedTransaction LinkedTransactionOne = client.getLinkedTransaction(LinkedTransactionList.get(num).getLinkedTransactionID());
	messages.add("Get a single LinkedTransaction - ID : " + LinkedTransactionOne.getLinkedTransactionID());
	
	List<Contact> ContactList2 = client.getContacts();
	int num2 = SampleData.findRandomNum(ContactList2.size());
	newLinkedTransaction.get(0).setContactID(ContactList2.get(num2).getContactID());
	List<LinkedTransaction> updateLinkedTransaction = client.updateLinkedTransaction(newLinkedTransaction);
	messages.add("Update the LinkedTransaction - ID : " + updateLinkedTransaction.get(0).getLinkedTransactionID() + " - Status : " + updateLinkedTransaction.get(0).getStatus());
		
	String status = client.deleteLinkedTransaction(newLinkedTransaction.get(0).getLinkedTransactionID());
	messages.add("Delete a new LinkedTransaction - Delete result: " + status);

} catch (XeroApiException e) {
	System.out.println(e.getResponseCode());
	System.out.println(e.getMessage());
}
```

## MANUAL JOURNALS
```java 
try {
	List<ManualJournal> newManualJournal = client.createManualJournals(SampleData.loadManualJournal().getManualJournal());
	messages.add("Create a new Manual Journal - ID : " + newManualJournal.get(0).getManualJournalID() + " - Narration : " + newManualJournal.get(0).getNarration());
	
	List<ManualJournal> ManualJournalWhere = client.getManualJournals(null,"Status==\"DRAFT\"",null,null);
	if (ManualJournalWhere.size() > 0) {
		messages.add("Get a ManualJournal with WHERE clause - Narration : " + ManualJournalWhere.get(0).getNarration());
	} else {
		messages.add("Get a ManualJournal with WHERE clause - No Manual Journal DRAFT found");
	}
	
	List<ManualJournal> ManualJournalList = client.getManualJournals();
	int num8 = SampleData.findRandomNum(ManualJournalList.size());
	messages.add("Get a random ManualJournal - Narration : " + ManualJournalList.get(num8).getNarration());

	ManualJournal ManualJournalOne = client.getManualJournal(ManualJournalList.get(num8).getManualJournalID());
	messages.add("Get a single ManualJournal - Narration : " + ManualJournalOne.getNarration());
	
	newManualJournal.get(0).setNarration("My Updated Narration");
	newManualJournal.get(0).setStatus(null);
	List<ManualJournal> updateManualJournal = client.updateManualJournal(newManualJournal);
	messages.add("Update the ManualJournal - ID : " + updateManualJournal.get(0).getManualJournalID() + " - Narration : " + updateManualJournal.get(0).getNarration());
	
} catch (XeroApiException e) {
	System.out.println(e.getResponseCode());
	System.out.println(e.getMessage());
}

```

## ORGANISATION
```java 
try {
	List<Organisation> newOrganisation = client.getOrganisations();
	messages.add("Get a Organisation - Name : " + newOrganisation.get(0).getName());
} catch (XeroApiException e) {
	System.out.println(e.getResponseCode());
	System.out.println(e.getMessage());
}
```

## OVERPAYMENT
```java 
try {
	List<Account> accountWhere = client.getAccounts(null,"Type==\"BANK\"",null);
	if(accountWhere.size() > 0) {
		BankTransaction bt = SampleData.loadNewlyCreatedOverpayment();
		List<Overpayment> newOverpayment = client.getOverpayments();
		if (newOverpayment.size() > 0) {
			messages.add("Get a Overpayment - ID" + newOverpayment.get(0).getOverpaymentID());
		}
		
		List<Allocation> newAllocation = client.createOverpaymentAllocations(SampleData.loadAllocation().getAllocation(),bt.getOverpaymentID());
		messages.add("Create a new Overpayment and Allocate - Applied Amount : " + newAllocation.get(0).getAppliedAmount());
	} else {
		messages.add("Please create a Bank Acccount before using the Overpayment Endpoint");
	}

} catch (XeroApiException e) {
	System.out.println(e.getResponseCode());
	System.out.println(e.getMessage());
}
```

## PAYMENT
```java 
try {
	List<Account> accountWhere = client.getAccounts(null,"Type==\"BANK\"",null);
	if(accountWhere.size() > 0) {
		
		List<Payment> newPayment = client.createPayments(SampleData.loadPayment().getPayment());
		messages.add("Create a new Payment - ID : " + newPayment.get(0).getPaymentID() + " : " + newPayment.get(0).getAmount());
		
		List<Payment> PaymentWhere = client.getPayments(null,"Status==\"AUTHORISED\"",null);
		messages.add("Get a Payment with WHERE clause - ID : " + PaymentWhere.get(0).getPaymentID());
		
		List<Payment> PaymentList = client.getPayments();
		int num = SampleData.findRandomNum(PaymentList.size());
		messages.add("Get a random Payment - ID : " + PaymentList.get(num).getPaymentID());
	
		Payment PaymentOne = client.getPayment(PaymentList.get(num).getPaymentID());
		messages.add("Get a single Payment - ID : " + PaymentOne.getPaymentID());
		
		Payment deletePayment = new Payment();
		deletePayment.setPaymentID(newPayment.get(0).getPaymentID());
		deletePayment.setStatus(PaymentStatus.DELETED);
		ArrayOfPayment aPayment = new ArrayOfPayment();
		aPayment.getPayment().add(deletePayment);
		List<Payment> removedPayment = client.deletePayment(aPayment.getPayment());
		messages.add("Delete the Payment - ID : " + removedPayment.get(0).getPaymentID());
	} else {
		messages.add("Please create a Bank Acccount before trying to Apply a Payment to Account Type Bank");
	}

} catch (XeroApiException e) {
	System.out.println(e.getResponseCode());
	System.out.println(e.getMessage());
}
```

## PREPAYMENT
```java 
try {
	List<Account> accountWhere = client.getAccounts(null,"Type==\"BANK\"",null);
	if(accountWhere.size() > 0) {
		BankTransaction bt = SampleData.loadNewlyCreatedPrepayment();
		
		List<Prepayment> newPrepayment = client.getPrepayments();
		messages.add("Get a existing Prepayment - ID : " + newPrepayment.get(0).getPrepaymentID() + " : " + newPrepayment.get(0).getTotal());
		
		List<Allocation> newAllocation = client.createPrepaymentAllocations(SampleData.loadAllocation().getAllocation(),bt.getPrepaymentID());
		messages.add("Create a new Prepayment and Allocate - Applied Amount : " + newAllocation.get(0).getAppliedAmount());
	} else {
		messages.add("Please create a Bank Acccount before using the Prepayment Endpoint");
	}
} catch (XeroApiException e) {
	System.out.println(e.getResponseCode());
	System.out.println(e.getMessage());
}
```

## PURCHASE ORDERS
```java 
try {
	List<PurchaseOrder> newPurchaseOrder = client.createPurchaseOrders(SampleData.loadPurchaseOrder().getPurchaseOrder());
	messages.add("Create a new PurchaseOrder - ID : " + newPurchaseOrder.get(0).getPurchaseOrderID() + " - Reference :" + newPurchaseOrder.get(0).getReference());
		
	List<PurchaseOrder> PurchaseOrderWhere = client.getPurchaseOrders(null,"Status==\"DRAFT\"",null,null);
	messages.add("Get a PurchaseOrder with WHERE clause - ID : " + PurchaseOrderWhere.get(0).getPurchaseOrderID());
	
	List<PurchaseOrder> PurchaseOrderList = client.getPurchaseOrders();
	int num = SampleData.findRandomNum(PurchaseOrderList.size());
	messages.add("Get a random PurchaseOrder - ID : " + PurchaseOrderList.get(num).getPurchaseOrderID());

	PurchaseOrder PurchaseOrderOne = client.getPurchaseOrder(PurchaseOrderList.get(num).getPurchaseOrderID());
	messages.add("Get a single PurchaseOrder - ID : " + PurchaseOrderOne.getPurchaseOrderID());
	
	newPurchaseOrder.get(0).setReference("My Updated Reference");
	newPurchaseOrder.get(0).setStatus(null);
	List<PurchaseOrder> updatePurchaseOrder = client.updatePurchaseOrder(newPurchaseOrder);
	messages.add("Update the PurchaseOrder - ID : " + updatePurchaseOrder.get(0).getPurchaseOrderID() + " - Reference:" + updatePurchaseOrder.get(0).getReference());
	
	// GET PDF of Invoice
	File f = new File("./");
	String dirPath =  f.getCanonicalPath();
	ByteArrayInputStream input = client.getPurchaseOrderPdfContent(PurchaseOrderList.get(0).getPurchaseOrderID());
	
	String fileName = "PurchaseOrder.pdf";
	
	FileOutputStream output = new FileOutputStream(fileName);

	int DEFAULT_BUFFER_SIZE = 1024;
	byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
	int n = 0;

	n = input.read(buffer, 0, DEFAULT_BUFFER_SIZE);

	while (n >= 0) {
	   output.write(buffer, 0, n);
	   n = input.read(buffer, 0, DEFAULT_BUFFER_SIZE);
	}
	
	input.close();
	output.close();
	
	String saveFilePath = dirPath + File.separator + fileName;
	messages.add("Get a PDF copy of PurchaseOrder - save it here: " + saveFilePath);

} catch (XeroApiException e) {
	System.out.println(e.getResponseCode());
	System.out.println(e.getMessage());
}
```

## RECEIPTS
```java 
try {
	List<Receipt> newReceipt = client.createReceipts(SampleData.loadReceipt().getReceipt());
	messages.add("Create a new Receipt - ID : " + newReceipt.get(0).getReceiptID() + " - Reference:" + newReceipt.get(0).getReference());
	
	List<Receipt> ReceiptWhere = client.getReceipts(null,"Status==\"DRAFT\"",null);
	messages.add("Get a Receipt with WHERE clause - ID : " + ReceiptWhere.get(0).getReceiptID());
	
	List<Receipt> ReceiptList = client.getReceipts();
	int num = SampleData.findRandomNum(ReceiptList.size());
	messages.add("Get a random Receipt - ID : " + ReceiptList.get(num).getReceiptID());

	Receipt ReceiptOne = client.getReceipt(ReceiptList.get(num).getReceiptID());
	messages.add("Get a single Receipt - ID : " + ReceiptOne.getReceiptID());
	
	newReceipt.get(0).setReference("You'll get the answer NEXT Saturday");
	newReceipt.get(0).setStatus(null);
	List<Receipt> updateReceipt = client.updateReceipt(newReceipt);
	messages.add("Update the Receipt - ID : " + updateReceipt.get(0).getReceiptID() + " - Reference:" + updateReceipt.get(0).getReference());

} catch (XeroApiException e) {
	System.out.println(e.getResponseCode());
	System.out.println(e.getMessage());
}	

```

## REPEATING INVOICES
```java 
try {
	List<RepeatingInvoice> newRepeatingInvoice = client.getRepeatingInvoices();
	if (newRepeatingInvoice.size() > 0 ){
		messages.add("Get a Repeating Invoice - ID " + newRepeatingInvoice.get(0).getRepeatingInvoiceID());
	} else {
		messages.add("No Repeating Invoices Exists");
	}
} catch (XeroApiException e) {
	System.out.println(e.getResponseCode());
	System.out.println(e.getMessage());
}

```

## REPORTS
```java


try {
	Report newReport = client.getReport("BalanceSheet",null,null);
	messages.add("Get a Reports - " + newReport.getReportID() + " : " + newReport.getReportDate());
	
	List<Contact> AgedPayablesByContactList = client.getContacts();
	int num20 = SampleData.findRandomNum(AgedPayablesByContactList.size());
	
	Report newReportAgedPayablesByContact = client.getReportAgedPayablesByContact(AgedPayablesByContactList.get(num20).getContactID(), null, null, null, "1/1/2016", "1/1/2017");
	messages.add("Get Aged Payables By Contact Reports for " +  AgedPayablesByContactList.get(num20).getName() + " - Report ID " + newReportAgedPayablesByContact.getReportID() );
						
	Report newReportAgedReceivablesByContact = client.getReportAgedReceivablesByContact(AgedPayablesByContactList.get(num20).getContactID(), null, null, null, "1/1/2016", "1/1/2017");
	messages.add("Get Aged Receivables By Contact Reports for " +  AgedPayablesByContactList.get(num20).getName() + " - Report ID " + newReportAgedReceivablesByContact.getReportID() );
	
	Report newReportBalanceSheet = client.getReportBalanceSheet(null, null, "3/3/2017", null, null, true, false, null, null);
	messages.add("Get Balance Sheet Report on " +  newReportBalanceSheet.getReportDate() + " - Name: " + newReportBalanceSheet.getReportTitles().getReportTitle().get(1).toString() );
	
	Report newReportBudgetSummary = client.getReportBudgetSummary(null, null, "1/1/2017", 3, 1);				
	messages.add("Get Budget Summary Report on " +  newReportBudgetSummary.getReportDate() + " - Name: " + newReportBudgetSummary.getReportName() );
	
	Report newExecutiveSummary = client.getExecutiveSummary(null, null,"1/1/2017");			
	messages.add("Get Executive Summary Report on " +  newExecutiveSummary.getReportDate() + " - Name: " + newExecutiveSummary.getReportName() );

	Report newReportProfitLoss = client.getReportProfitLoss(null,null, "9/1/2016", "1/1/2017", null, null, null, null, true, false, null, null);
	messages.add("Get Profit Loss Report on " +  newReportProfitLoss.getReportDate() + " - Name: " + newReportProfitLoss.getReportName() );

	Report newTrialBalance = client.getReportTrialBalance("9/1/2016", true);		
	messages.add("Get Trial Balance Report on " +  newTrialBalance.getReportDate() + " - Name: " + newTrialBalance.getReportName() );

} catch (XeroApiException e) {
	System.out.println(e.getResponseCode());
	System.out.println(e.getMessage());
}
```

## TAX RATES
```java
try {
	List<TaxRate> newTaxRate = client.createTaxRates(SampleData.loadTaxRate().getTaxRate());
	messages.add("Create a new TaxRate - Name : " + newTaxRate.get(0).getName());
	
	List<TaxRate> TaxRateWhere = client.getTaxRates(null,"Status==\"ACTIVE\"",null);
	messages.add("Get a TaxRate with WHERE clause - Name : " + TaxRateWhere.get(0).getName());
	
	List<TaxRate> TaxRateList = client.getTaxRates();
	int num11 = SampleData.findRandomNum(TaxRateList.size());
	messages.add("Get a random TaxRate - Name : " + TaxRateList.get(num11).getName());
	
	newTaxRate.get(0).setName("Yet Another Tax Rate-" + SampleData.loadRandomNum());
	List<TaxRate> updateTaxRate = client.updateTaxRate(newTaxRate);
	messages.add("Update the TaxRate - Type : " + updateTaxRate.get(0).getTaxType() + " - Name : " + updateTaxRate.get(0).getName());
} catch (XeroApiException e) {
	System.out.println(e.getResponseCode());
	System.out.println(e.getMessage());
}
```

## TRACKING CATEGORIES
```java
try {
	List<TrackingCategory> newTrackingCategory = client.createTrackingCategories(SampleData.loadTrackingCategory().getTrackingCategory());
	messages.add("Create a new TrackingCategory - ID : " + newTrackingCategory.get(0).getTrackingCategoryID() + " -  Name : " + newTrackingCategory.get(0).getName());
	
	// TRACKING OPTIONS  
	List<TrackingCategoryOption> newTrackingCategoryOption = client.createTrackingCategoryOption(SampleData.loadTrackingCategoryOption().getOption(),newTrackingCategory.get(0).getTrackingCategoryID());
	messages.add("Create a new TrackingCategory Option - Name : " + newTrackingCategoryOption.get(0).getName());
		
	// MULTIPLE TRACKING OPTIONS 
	List<TrackingCategoryOption> newTrackingCategoryOption2 = client.createTrackingCategoryOption(SampleData.loadTrackingCategoryOptionMulti().getOption(),newTrackingCategory.get(0).getTrackingCategoryID(),false);	
	messages.add("Create a new TrackingCategory MULTI Option - Name : " + newTrackingCategoryOption2.get(0).getName());
	messages.add("Create a new TrackingCategory MULTI Option - Name : " + newTrackingCategoryOption2.get(1).getName());
	
	
	TrackingCategoryOption oneTrackingCategoryOption = new TrackingCategoryOption();
	oneTrackingCategoryOption.setName("Iron Man");
	TrackingCategoryOption updateTrackingCategoryOption = client.updateTrackingCategoryOption(oneTrackingCategoryOption,newTrackingCategory.get(0).getTrackingCategoryID(),newTrackingCategoryOption.get(0).getTrackingOptionID());
	messages.add("Update TrackingCategory Option - Name : " + updateTrackingCategoryOption.getName());

	String deleteTrackingCategoryOption = client.deleteTrackingCategoryOption(newTrackingCategory.get(0).getTrackingCategoryID(),newTrackingCategoryOption.get(0).getTrackingOptionID());
	messages.add("Delete TrackingCategory Option -  : " + deleteTrackingCategoryOption);
	
	List<TrackingCategory> TrackingCategoryWhere = client.getTrackingCategories(null,"Status==\"ACTIVE\"",null, false);
	messages.add("Get a TrackingCategory with WHERE clause - Name : " + TrackingCategoryWhere.get(0).getName());
	
	List<TrackingCategory> TrackingCategoryList = client.getTrackingCategories();
	int num10 = SampleData.findRandomNum(TrackingCategoryList.size());
	messages.add("Get a random TrackingCategory - Name : " + TrackingCategoryList.get(num10).getName());

	TrackingCategory TrackingCategoryOne = client.getTrackingCategory(TrackingCategoryList.get(num10).getTrackingCategoryID());
	messages.add("Get a single TrackingCategory - Name : " + TrackingCategoryOne.getName());
	
	newTrackingCategory.get(0).setName("Lord of the Rings-" + SampleData.loadRandomNum());
	List<TrackingCategory> updateTrackingCategory = client.updateTrackingCategory(newTrackingCategory);
	messages.add("Update the TrackingCategory - ID : " + updateTrackingCategory.get(0).getTrackingCategoryID() + " - Name : " + updateTrackingCategory.get(0).getName());

	String status = client.deleteTrackingCategory(newTrackingCategory.get(0).getTrackingCategoryID());
	messages.add("Delete a new TrackingCategory - Delete result: " + status);

} catch (XeroApiException e) {
	System.out.println(e.getResponseCode());
	System.out.println(e.getMessage());
}
```



## USERS
```java
try {
	List<User> UserWhere = client.getUsers(null,"IsSubscriber==true",null);
	if(UserWhere.size() > 0)  {
		messages.add("Get a User with WHERE clause - Email: " + UserWhere.get(0).getEmailAddress());
	} else {
		messages.add("No User with IsSubscriber True found - must be Demo Company");
	}
	
	List<User> UserList = client.getUsers();
	int num = SampleData.findRandomNum(UserList.size());
	messages.add("Get a random User - Email: " + UserList.get(num).getEmailAddress());

	User UserOne = client.getUser(UserList.get(num).getUserID());
	messages.add("Get a single User - Email: " + UserOne.getEmailAddress());
} catch (XeroApiException e) {
	System.out.println(e.getResponseCode());
	System.out.println(e.getMessage());
}
```



## ERRORS
```java


ArrayOfContact array = new ArrayOfContact();
Contact contact = new Contact();
contact.setName("Sidney Maestre");
array.getContact().add(contact);

// FORCE a 400 Error
try {
	List<Contact> duplicateContact = client.createContact(array.getContact());
	messages.add("Create a new Contact - Name : " + duplicateContact.get(0).getName());
} catch (XeroApiException e) {
	System.out.println(e.getResponseCode());

	messages.add("Error Code : " + e.getResponseCode() );
	
	List<Elements> elements = e.getApiException().getElements();
    Elements element = elements.get(0);
    List<Object> dataContractBase = element.getDataContractBase();
    for (Object dataContract : dataContractBase) {
       Contact failedContact = (Contact) dataContract;
       ArrayOfValidationError validationErrors = failedContact.getValidationErrors();
        
       List<ValidationError> validationErrorList= validationErrors.getValidationError();
       
	   System.out.println(failedContact.getContactID());
       System.out.println(validationErrorList.get(0).getMessage());
       messages.add("Failed Contact ID : " + failedContact.getContactID());
       messages.add("Validation Error : " + validationErrorList.get(0).getMessage());
       
    }
}			

// FORCE a 404 Error
try {
	Contact ContactOne = client.getContact("1234");
	messages.add("Get a single Contact - ID : " + ContactOne.getContactID());
} catch (XeroApiException e) {
	System.out.println(e.getResponseCode());
	System.out.println(e.getMessage());	
	messages.add("Error Code : " + e.getResponseCode() + " Message: " + e.getMessage());
}	

try {
	List<Invoice> listOfInvoices = new ArrayList<Invoice>();
	listOfInvoices.add(SampleData.loadBadInvoice().getInvoice().get(0));
	listOfInvoices.add(SampleData.loadBadInvoice2().getInvoice().get(0));

	List<Invoice> newInvoice = client.createInvoices(listOfInvoices,null,true);
	messages.add("Create a new Invoice ID : " + newInvoice.get(0).getInvoiceID() + " - Reference : " +newInvoice.get(0).getReference());
	
} catch (XeroApiException e) {
	int code = e.getResponseCode();
	
	if (code == 400) {
		List<Elements> elements = e.getApiException().getElements();
		
		if(e.getApiException().getElements().size() > 0) {
			Elements element = elements.get(0);
			List<Object> dataContractBase = element.getDataContractBase();
			for (Object dataContract : dataContractBase) {
				Invoice failedInvoice = (Invoice) dataContract;
				ArrayOfValidationError validationErrors = failedInvoice.getValidationErrors();
		        List<ValidationError> errors = validationErrors.getValidationError();
		        
		        messages.add("Error Code:" + e.getResponseCode() + " message : "  + errors.get(0).getMessage());
		        messages.add("Error invoice Num : " + failedInvoice.getInvoiceNumber());
			}
		}
	}
}

// FORCE a 503 Error
List<Contact> ContactList = client.getContacts();
int num4 = SampleData.findRandomNum(ContactList.size());			
try {
	for(int i=65; i>1; i--){
		Contact ContactOne = client.getContact(ContactList.get(num4).getContactID());
	}
	messages.add("Congrats - you made over 60 calls without hitting rate limit");
} catch (XeroApiException e) {
	System.out.println(e.getResponseCode());
	System.out.println(e.getMessage());
	messages.add("Error Code : " + e.getResponseCode() + " Message: " + e.getMessage());
}		
```

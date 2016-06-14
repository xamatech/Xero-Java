# Xero-Java
A skinny Java wrapper of the Xero API. Supports Accounting API. All third party libraries dependencies managed with Maven

## Xero App
You'll need to decide which type of Xero app you'll be building (Public, Private or Partner). Go to http://app.xero.com to login and create your app.

## Config.json
Located in src/main/resources is the config.json file.  There are examples for public, private and partner - but the Config.java will look in this folder at the config.json file in order to initialize you Java code. 

Below are the unique values you'll set in config.json for each type of Xero app. 

### Public Xero App

* Copy and paste, Consumer Key & Secret from app.xero.com
* Set your callback url at app.xero.com to match CallbackBaseUrl
* Set your callback path - this is appended to the CallbackBaseUrl


### Private Xero App

* Copy and paste, Consumer Key & Secret from app.xero.com
* Upload the public.cer file at app.xero.com
* Copy the public_privatekey.pfx file created with OpenSSL in the certs folder
[Public Private Key Docs](https://developer.xero.com/documentation/advanced-docs/public-private-keypair/)
* Set the private key password


### Partner Xero App

* Copy and paste, Consumer Key & Secret from app.xero.com
* Set your callback url at app.xero.com to match CallbackBaseUrl
* Set your callback path - this is appended to the CallbackBaseUrl
* Upload the public.cer file at app.xero.com
* Copy the public_privatekey.pfx file created with OpenSSL in the certs folder
[Public Private Key Docs](https://developer.xero.com/documentation/advanced-docs/public-private-keypair/)
* Set the Private key password
* Copy the xero-entrus.p12 file set to you by Xero API team.
[Entrust Certificates Docs](https://developer.xero.com/documentation/getting-started/partner-applications/#certificates)
* Set the Entrust Certifcate password
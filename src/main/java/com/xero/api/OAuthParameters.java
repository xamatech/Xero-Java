package com.xero.api;

import com.google.api.client.auth.oauth.OAuthSigner;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.util.escape.PercentEscaper;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OAuthParameters implements HttpExecuteInterceptor, HttpRequestInitializer {
	final static Logger logger = LogManager.getLogger(OAuthParameters.class);
	   
	public OAuthParameters()  {

	}

	/** Secure random number generator to sign requests. */
	private static final SecureRandom RANDOM = new SecureRandom();

	/** Required OAuth signature algorithm. */
	public OAuthSigner signer;

	/**
	 * Absolute URI back to which the server will redirect the resource owner when the Resource Owner
	 * Authorization step is completed.
	 */
	public String callback;

	/**
	 * Required identifier portion of the client credentials (equivalent to a username).
	 */
	public String consumerKey;

	/** Required nonce value. Should be computed using {@link #computeNonce()}. */
	public String nonce;

	/** Realm. */
	public String realm;

	/** Signature. Required but normally computed using {@link #computeSignature}. */
	public String signature;

	/**
	 * Name of the signature method used by the client to sign the request. Required, but normally
	 * computed using {@link #computeSignature}.
	 */
	public String signatureMethod;

	/**
	 * Required timestamp value. Should be computed using {@link #computeTimestamp()}.
	 */
	public String timestamp;

	/**
	 * Token value used to associate the request with the resource owner or {@code null} if the
	 * request is not associated with a resource owner.
	 */
	public String token;

	/**
	 * Session handle.
	 */
	public String sessionHandle;

	/** The verification code received from the server. */
	public String verifier;

	/**
	 * Must either be "1.0" or {@code null} to skip. Provides the version of the authentication
	 * process as defined in this specification.
	 */
	public String version;

    /**
     * A value of true indicates that server is sitting behind and application firewall (e.g. L7)
     * Values for hostname and URL's will be adjusted when calculating oauth signatures as the
     * application firewall will be changing the request
     */
    public boolean usingAppFirewall = false;

    /**
     * The hostname used for communicating through the application firewall.  If the url hostname
     * matches this, the value of "api.xero.com" will be used instead when calculating the oauth
     * signature.
     */
    public String appFirewallHostname = "";
    private static final String XERO_API_HOST = "api.xero.com";

    /**
     * The prefix the application firewall uses to identify mappings for it's own URL rewriting.
     * The value of the prefix will be stripped from the URL when calculating the oauth signature.
     */
    public String appFirewallUrlPrefix = "";

    private static final PercentEscaper ESCAPER = new PercentEscaper("-_.~", false);

	/**
	 * Computes a nonce based on the hex string of a random non-negative long, setting the value of
	 * the {@link #nonce} field.
	 */
	public void computeNonce() {
		nonce = Long.toHexString(Math.abs(RANDOM.nextLong()));
	}

	/**
	 * Computes a timestamp based on the current system time, setting the value of the
	 * {@link #timestamp} field.
	 */
	public void computeTimestamp() {
		timestamp = Long.toString(System.currentTimeMillis() / 1000);
	}

	/**
	 * Computes a new signature based on the fields and the given request method and URL, setting the
	 * values of the {@link #signature} and {@link #signatureMethod} fields.
     * @param requestUrl The full URL including paramters
     * @param requestMethod The REST verb GET, PUT, POST, DELETE
	 * @throws GeneralSecurityException general security exception
	 */
	public void computeSignature(String requestMethod, GenericUrl requestUrl)
			throws GeneralSecurityException 
	{
		OAuthSigner signer = this.signer;
		String signatureMethod = this.signatureMethod = signer.getSignatureMethod();
		// oauth_* parameters (except oauth_signature)
		TreeMap<String, String> parameters = new TreeMap<String, String>();
		putParameterIfValueNotNull(parameters, "oauth_callback", callback);
		putParameterIfValueNotNull(parameters, "oauth_consumer_key", consumerKey);
		putParameterIfValueNotNull(parameters, "oauth_nonce", nonce);
		putParameterIfValueNotNull(parameters, "oauth_signature_method", signatureMethod);
		putParameterIfValueNotNull(parameters, "oauth_timestamp", timestamp);
		putParameterIfValueNotNull(parameters, "oauth_token", token);
		putParameterIfValueNotNull(parameters, "oauth_verifier", verifier);
		putParameterIfValueNotNull(parameters, "oauth_version", version);
		putParameterIfValueNotNull(parameters, "oauth_session_handle", sessionHandle);

		// parse request URL for query parameters
		for (Map.Entry<String, Object> fieldEntry : requestUrl.entrySet()) {
			Object value = fieldEntry.getValue();
			if (value != null) {
				String name = fieldEntry.getKey();
				if (value instanceof Collection<?>) {
					for (Object repeatedValue : (Collection<?>) value) {
						putParameter(parameters, name, repeatedValue);
					}
				} else {
					putParameter(parameters, name, value);
				}
			}
		}
		// normalize parameters
		StringBuilder parametersBuf = new StringBuilder();
		boolean first = true;
		for (Map.Entry<String, String> entry : parameters.entrySet()) {
			if (first) {
				first = false;
			} else {
				parametersBuf.append('&');
			}
			parametersBuf.append(entry.getKey());
			String value = entry.getValue();
			if (value != null) {
				parametersBuf.append('=').append(value);
			}
		}
		String normalizedParameters = parametersBuf.toString();
		// normalize URL, removing any query parameters and possibly port
		GenericUrl normalized = new GenericUrl();
		String scheme = requestUrl.getScheme();
		normalized.setScheme(scheme);
		if (usingAppFirewall && (requestUrl.getHost().equals(appFirewallHostname))) {
			normalized.setHost(XERO_API_HOST);
		} else {
		    normalized.setHost(requestUrl.getHost());
		}
		if (usingAppFirewall &&(requestUrl.getRawPath().startsWith(appFirewallUrlPrefix))) {
		    String modifiedPath = requestUrl.getRawPath().replace(appFirewallUrlPrefix, "");
            normalized.setPathParts(requestUrl.toPathParts(modifiedPath));
        } else {
            normalized.setPathParts(requestUrl.getPathParts());
        }
		int port = requestUrl.getPort();
		if ("http".equals(scheme) && port == 80 || "https".equals(scheme) && port == 443) {
			port = -1;
		}
		normalized.setPort(port);
		String normalizedPath = normalized.build();
		// signature base string
		StringBuilder buf = new StringBuilder();
		buf.append(escape(requestMethod)).append('&');
		buf.append(escape(normalizedPath)).append('&');
		buf.append(escape(normalizedParameters));
		String signatureBaseString = buf.toString();
		signature = signer.computeSignature(signatureBaseString);
	}

	/**
	 * Returns the {@code Authorization} header value to use with the OAuth parameter values found in
	 * the fields.
     * @return String
	 */
	public String getAuthorizationHeader() 
	{
		StringBuilder buf = new StringBuilder("OAuth");
		appendParameter(buf, "realm", realm);
		appendParameter(buf, "oauth_callback", callback);
		appendParameter(buf, "oauth_consumer_key", consumerKey);
		appendParameter(buf, "oauth_nonce", nonce);
		appendParameter(buf, "oauth_signature", signature);
		appendParameter(buf, "oauth_signature_method", signatureMethod);
		appendParameter(buf, "oauth_timestamp", timestamp);
		appendParameter(buf, "oauth_token", token);
		appendParameter(buf, "oauth_verifier", verifier);
		appendParameter(buf, "oauth_version", version);
		appendParameter(buf, "oauth_session_handle", sessionHandle);

		return buf.substring(0, buf.length() - 1);
	}

	private void appendParameter(StringBuilder buf, String name, String value) 
	{
		if (value != null) 
		{
			buf.append(' ').append(escape(name)).append("=\"").append(escape(value)).append("\",");
		}
	}

	private void putParameterIfValueNotNull(TreeMap<String, String> parameters, String key, String value) 
	{
		if (value != null) {
			putParameter(parameters, key, value);
		}
	}

	private void putParameter(TreeMap<String, String> parameters, String key, Object value) 
	{
		parameters.put(escape(key), value == null ? null : escape(value.toString()));
	}

	/** Returns the escaped form of the given value using OAuth escaping rules. 
    * @param value The string value you wish to escape
    * @return String
	*/
	public static String escape(String value) 
	{
		return ESCAPER.escape(value);
	}

	public void initialize(HttpRequest request) throws IOException 
	{
		request.setInterceptor(this);
	}

	public void intercept(HttpRequest request) throws IOException 
	{
		computeNonce();
		computeTimestamp();
		try {
			computeSignature(request.getRequestMethod(), request.getUrl());
		} catch (GeneralSecurityException e) {
			logger.error(e);
			IOException io = new IOException();
			io.initCause(e);
			throw io;
		}
		request.getHeaders().setAuthorization(getAuthorizationHeader());
	}
	
	public void intercept(HttpGet request, GenericUrl url) throws IOException 
	{
		computeNonce();
		computeTimestamp();
	
		try {
			computeSignature(request.getMethod(), url);
		} catch (GeneralSecurityException e) {
			logger.error(e);
			IOException io = new IOException();
			io.initCause(e);
			throw io;
		}
		request.addHeader("Authorization", getAuthorizationHeader());
	}
	
	public void intercept(HttpPost request, GenericUrl url) throws IOException 
	{
		computeNonce();
		computeTimestamp();
	
		try {
			computeSignature(request.getMethod(), url);
		} catch (GeneralSecurityException e) {
			logger.error(e);
			IOException io = new IOException();
			io.initCause(e);
			throw io;
		};
		request.addHeader("Authorization", getAuthorizationHeader());
	}
	
	public void intercept(HttpPut request, GenericUrl url) throws IOException 
	{
		computeNonce();
		computeTimestamp();
	
		try {
			computeSignature(request.getMethod(), url);
		} catch (GeneralSecurityException e) {
			logger.error(e);
			IOException io = new IOException();
			io.initCause(e);
			throw io;
		}
		request.addHeader("Authorization", getAuthorizationHeader());
	}
	
	public void intercept(HttpDelete request, GenericUrl url) throws IOException 
	{
		computeNonce();
		computeTimestamp();
	
		try {
			computeSignature(request.getMethod(), url);
		} catch (GeneralSecurityException e) {
			logger.error(e);
			IOException io = new IOException();
			io.initCause(e);
			throw io;
		}
		request.addHeader("Authorization", getAuthorizationHeader());
	}

}

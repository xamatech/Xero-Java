/*
 * Accounting API
 * No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
 *
 * OpenAPI spec version: 2.0.0
 * Contact: api@xero.com
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package com.xero.models.accounting;

import java.util.Objects;
import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.xero.models.accounting.TaxType;
import com.xero.models.accounting.TrackingCategory;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * ManualJournalLine
 */

public class ManualJournalLine {
  
  @JsonProperty("LineAmount")
  private Float lineAmount = null;

  
  @JsonProperty("AccountCode")
  private String accountCode = null;

  
  @JsonProperty("Description")
  private String description = null;

  
  @JsonProperty("TaxType")
  private TaxType taxType = null;

  
  @JsonProperty("Tracking")
  private List<TrackingCategory> tracking = null;

  
  @JsonProperty("TaxAmount")
  private Float taxAmount = null;

  
  @JsonProperty("IsBlank")
  private Boolean isBlank = null;

  public ManualJournalLine lineAmount(Float lineAmount) {
    this.lineAmount = lineAmount;
    return this;
  }

   /**
   * total for line. Debits are positive, credits are negative value
   * @return lineAmount
  **/
  @ApiModelProperty(example = "-2569.0", value = "total for line. Debits are positive, credits are negative value")
  public Float getLineAmount() {
    return lineAmount;
  }

  public void setLineAmount(Float lineAmount) {
    this.lineAmount = lineAmount;
  }

  public ManualJournalLine accountCode(String accountCode) {
    this.accountCode = accountCode;
    return this;
  }

   /**
   * See Accounts
   * @return accountCode
  **/
  @ApiModelProperty(example = "720", value = "See Accounts")
  public String getAccountCode() {
    return accountCode;
  }

  public void setAccountCode(String accountCode) {
    this.accountCode = accountCode;
  }

  public ManualJournalLine description(String description) {
    this.description = description;
    return this;
  }

   /**
   * Description for journal line
   * @return description
  **/
  @ApiModelProperty(example = "Coded incorrectly Office Equipment should be Computer Equipment", value = "Description for journal line")
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public ManualJournalLine taxType(TaxType taxType) {
    this.taxType = taxType;
    return this;
  }

   /**
   * Get taxType
   * @return taxType
  **/
  @ApiModelProperty(value = "")
  public TaxType getTaxType() {
    return taxType;
  }

  public void setTaxType(TaxType taxType) {
    this.taxType = taxType;
  }

  public ManualJournalLine tracking(List<TrackingCategory> tracking) {
    this.tracking = tracking;
    return this;
  }

  public ManualJournalLine addTrackingItem(TrackingCategory trackingItem) {
    if (this.tracking == null) {
      this.tracking = new ArrayList<TrackingCategory>();
    }
    this.tracking.add(trackingItem);
    return this;
  }

   /**
   * Optional Tracking Category – see Tracking. Any JournalLine can have a maximum of 2 &lt;TrackingCategory&gt; elements.
   * @return tracking
  **/
  @ApiModelProperty(value = "Optional Tracking Category – see Tracking. Any JournalLine can have a maximum of 2 <TrackingCategory> elements.")
  public List<TrackingCategory> getTracking() {
    return tracking;
  }

  public void setTracking(List<TrackingCategory> tracking) {
    this.tracking = tracking;
  }

   /**
   * The calculated tax amount based on the TaxType and LineAmount
   * @return taxAmount
  **/
  @ApiModelProperty(example = "0.0", value = "The calculated tax amount based on the TaxType and LineAmount")
  public Float getTaxAmount() {
    return taxAmount;
  }

  public ManualJournalLine isBlank(Boolean isBlank) {
    this.isBlank = isBlank;
    return this;
  }

   /**
   * is the line blank
   * @return isBlank
  **/
  @ApiModelProperty(value = "is the line blank")
  public Boolean getIsBlank() {
    return isBlank;
  }

  public void setIsBlank(Boolean isBlank) {
    this.isBlank = isBlank;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ManualJournalLine manualJournalLine = (ManualJournalLine) o;
    return Objects.equals(this.lineAmount, manualJournalLine.lineAmount) &&
        Objects.equals(this.accountCode, manualJournalLine.accountCode) &&
        Objects.equals(this.description, manualJournalLine.description) &&
        Objects.equals(this.taxType, manualJournalLine.taxType) &&
        Objects.equals(this.tracking, manualJournalLine.tracking) &&
        Objects.equals(this.taxAmount, manualJournalLine.taxAmount) &&
        Objects.equals(this.isBlank, manualJournalLine.isBlank);
  }

  @Override
  public int hashCode() {
    return Objects.hash(lineAmount, accountCode, description, taxType, tracking, taxAmount, isBlank);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ManualJournalLine {\n");
    
    sb.append("    lineAmount: ").append(toIndentedString(lineAmount)).append("\n");
    sb.append("    accountCode: ").append(toIndentedString(accountCode)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    taxType: ").append(toIndentedString(taxType)).append("\n");
    sb.append("    tracking: ").append(toIndentedString(tracking)).append("\n");
    sb.append("    taxAmount: ").append(toIndentedString(taxAmount)).append("\n");
    sb.append("    isBlank: ").append(toIndentedString(isBlank)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}


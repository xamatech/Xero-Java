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
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * AccountsReceivable
 */

public class AccountsReceivable {
  
  @JsonProperty("Outstanding")
  private String outstanding = null;

  
  @JsonProperty("Overdue")
  private String overdue = null;

  public AccountsReceivable outstanding(String outstanding) {
    this.outstanding = outstanding;
    return this;
  }

   /**
   * Get outstanding
   * @return outstanding
  **/
  @ApiModelProperty(value = "")
  public String getOutstanding() {
    return outstanding;
  }

  public void setOutstanding(String outstanding) {
    this.outstanding = outstanding;
  }

  public AccountsReceivable overdue(String overdue) {
    this.overdue = overdue;
    return this;
  }

   /**
   * Get overdue
   * @return overdue
  **/
  @ApiModelProperty(value = "")
  public String getOverdue() {
    return overdue;
  }

  public void setOverdue(String overdue) {
    this.overdue = overdue;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AccountsReceivable accountsReceivable = (AccountsReceivable) o;
    return Objects.equals(this.outstanding, accountsReceivable.outstanding) &&
        Objects.equals(this.overdue, accountsReceivable.overdue);
  }

  @Override
  public int hashCode() {
    return Objects.hash(outstanding, overdue);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AccountsReceivable {\n");
    
    sb.append("    outstanding: ").append(toIndentedString(outstanding)).append("\n");
    sb.append("    overdue: ").append(toIndentedString(overdue)).append("\n");
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


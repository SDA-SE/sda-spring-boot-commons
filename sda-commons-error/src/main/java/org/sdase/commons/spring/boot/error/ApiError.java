/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.error;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;

/** Error Entity object for transferring error information between server and client. */
@Schema(
    description =
        "Describes an api error object to transfer error information between server and client.")
public class ApiError {

  @Schema(
      description = "The human readable description of the error.",
      example = "Request parameters are not valid")
  private String title;

  @Schema(
      description =
          "Contains a list of invalid parameters in case of validation errors. Parameters in "
              + "this case can be for example fields in a Json of the request body or query params.",
      example = "[]")
  private List<ApiInvalidParam> invalidParams;

  public ApiError() {
    // default constructor
  }

  public ApiError(String title) {
    this.title = title;
    this.invalidParams = new ArrayList<>();
  }

  public ApiError(String title, List<ApiInvalidParam> invalidParams) {
    this.title = title;
    this.invalidParams = invalidParams;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public List<ApiInvalidParam> getInvalidParams() {
    return invalidParams;
  }

  public void addInvalidParameter(ApiInvalidParam parameter) {
    invalidParams.add(parameter);
  }
}

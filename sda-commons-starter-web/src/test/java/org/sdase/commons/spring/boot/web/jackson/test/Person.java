/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.jackson.test;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;
import java.util.List;

public class Person {

  private String name;
  private Title title;
  private LocalDate dob;
  private List<String> addresses;
  private Person partner;
  @JsonIgnore private String idCardNumber;

  private Profession profession;

  public String getName() {
    return name;
  }

  public Person setName(String name) {
    this.name = name;
    return this;
  }

  public Title getTitle() {
    return title;
  }

  public Person setTitle(Title title) {
    this.title = title;
    return this;
  }

  public LocalDate getDob() {
    return dob;
  }

  public Person setDob(LocalDate dob) {
    this.dob = dob;
    return this;
  }

  public List<String> getAddresses() {
    return addresses;
  }

  public Person setAddresses(List<String> addresses) {
    this.addresses = addresses;
    return this;
  }

  public Person getPartner() {
    return partner;
  }

  public Person setPartner(Person partner) {
    this.partner = partner;
    return this;
  }

  public String getIdCardNumber() {
    return idCardNumber;
  }

  public Person setIdCardNumber(String idCardNumber) {
    this.idCardNumber = idCardNumber;
    return this;
  }

  public Profession getProfession() {
    return profession;
  }

  public Person setProfession(Profession profession) {
    this.profession = profession;
    return this;
  }

  public enum Title {
    PROFESSOR,
    DOCTOR
  }

  public enum Profession {
    IT,
    FINANCE,
    LEGAL,
    @JsonEnumDefaultValue
    OTHER
  }
}

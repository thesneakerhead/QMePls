package com.cz3002.diseasesclinicalapp;

import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PatientUser extends User {
    public ArrayList<SymptomCard> symptomCards;
    public String name;
    public PatientUser()
    {
        this.isClinicAcc = false;
    }
}

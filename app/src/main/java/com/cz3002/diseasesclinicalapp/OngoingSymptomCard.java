package com.cz3002.diseasesclinicalapp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OngoingSymptomCard extends SymptomCard {
    public String status;//ONGOING,COMPLETED or CANCELLED
    public String clinicUID;//Clinic patient is queueing for
}

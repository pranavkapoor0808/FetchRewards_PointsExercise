package edu.umass.fetch.request;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AddTransaction {

  String payer;
  double points;
  LocalDateTime timestamp;
}

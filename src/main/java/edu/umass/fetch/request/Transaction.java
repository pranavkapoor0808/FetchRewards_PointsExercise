package edu.umass.fetch.request;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

  private String payer;
  private double points;
  private LocalDateTime timestamp;
}

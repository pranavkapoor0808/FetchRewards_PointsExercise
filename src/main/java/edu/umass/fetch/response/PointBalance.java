package edu.umass.fetch.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PointBalance {
  String payer;
  double points;
}

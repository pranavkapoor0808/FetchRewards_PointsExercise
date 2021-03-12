package edu.umass.fetch.controller;

import com.google.gson.Gson;
import edu.umass.fetch.request.AddTransaction;
import edu.umass.fetch.request.SpendTransaction;
import edu.umass.fetch.response.PointBalance;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class ApiController {

  private static final Gson gson = new Gson();

  @PostMapping(path = "/addTransactions",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.TEXT_PLAIN_VALUE)
  @ResponseBody ResponseEntity<String> addTransactions(@RequestBody List<AddTransaction> addTransactions) {
    log.info(addTransactions.toString());
    return ResponseEntity.ok("Transactions added successfully");
  }

  @PostMapping(path = "/spendPoints",
      consumes= MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody ResponseEntity<String> spendPoints(@RequestBody SpendTransaction spendTransaction) {
    log.info(spendTransaction.toString());
    List<PointBalance> payerPointsBalances = new ArrayList<>();
    payerPointsBalances.add(new PointBalance("ABC", 500.0));
    return ResponseEntity.ok(gson.toJson(payerPointsBalances));
  }

  @GetMapping(path = "getPointsBalance",
    produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody ResponseEntity<String> getPointsBalance() {
    Map<String, Double> pointsBalances = new HashMap<>();
    pointsBalances.put("ABC", 200.0);
    return ResponseEntity.ok(gson.toJson(pointsBalances));
  }

}

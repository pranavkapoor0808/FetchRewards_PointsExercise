package edu.umass.fetch.controller;

import com.google.gson.Gson;
import edu.umass.fetch.request.Transaction;
import edu.umass.fetch.request.SpendTransaction;
import edu.umass.fetch.response.PointBalance;
import edu.umass.fetch.service.PointsService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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

  private final Gson gson;
  private final PointsService pointsService;

  public ApiController(final PointsService pointsService) {
    this.pointsService = pointsService;
    gson = new Gson();
  }

  @PostMapping(path = "/addTransactions",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.TEXT_PLAIN_VALUE)
  @ResponseBody ResponseEntity<String> addTransactions(@RequestBody List<Transaction> transactions) {
    try {
      pointsService.addPoints(transactions);
      return ResponseEntity.ok("Transactions added successfully");
    } catch (IllegalStateException e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
    }
  }

  @PostMapping(path = "/spendPoints",
      consumes= MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody ResponseEntity<String> spendPoints(@RequestBody SpendTransaction spendTransaction) {
    try {
      Set<PointBalance> payerPointsBalances = pointsService.spendPoints(spendTransaction);
      return ResponseEntity.ok(gson.toJson(payerPointsBalances));
    } catch (IllegalStateException e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
    }
  }

  @GetMapping(path = "/getPointsBalance",
    produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody ResponseEntity<String> getPointsBalance() {
    Map<String, Double> pointsBalances = pointsService.getPointsBalance();
    return ResponseEntity.ok(gson.toJson(pointsBalances));
  }

}

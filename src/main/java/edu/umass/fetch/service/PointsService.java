package edu.umass.fetch.service;

import edu.umass.fetch.request.SpendTransaction;
import edu.umass.fetch.request.Transaction;
import edu.umass.fetch.response.PointBalance;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class PointsService {

  private double availablePointsBalance = 0;
  private final Queue<Transaction> pointsQueue = new PriorityQueue<>(Comparator.comparing(Transaction::getTimestamp));
  private final Map<String, Double> pointsBalance = new HashMap<>();

  public void addPoints(List<Transaction> transactions) throws IllegalStateException {
    Map<String, Double> pointsAddedPerPayer = transactions.stream()
        .collect(Collectors.groupingBy(Transaction::getPayer, Collectors.summingDouble(Transaction::getPoints)));

    for (Map.Entry<String, Double> entry: pointsAddedPerPayer.entrySet()) {
      if(entry.getValue() + pointsBalance.getOrDefault(entry.getKey(), 0.0) < 0) {
        throw new IllegalStateException("Processing these transactions will cause " + entry.getKey() + " balance to become negative");
      }
    }

    pointsQueue.addAll(transactions);
    transactions.forEach(transaction -> {
      pointsBalance.put(transaction.getPayer(),
          transaction.getPoints() + pointsBalance.getOrDefault(transaction.getPayer(), 0.0));
      availablePointsBalance += transaction.getPoints();
    });
  }

  public Set<PointBalance> spendPoints(SpendTransaction spendTransaction) throws IllegalStateException {

    double spendPoints = spendTransaction.getPoints();
    if (spendPoints > availablePointsBalance) {
      throw new IllegalStateException("Insufficient Point Balance. Points available: " + availablePointsBalance + " Redemption  request: " + spendPoints);
    }

    Map<String, Double> payerToSpentPoints = new HashMap<>();

    while(spendPoints > 0) {
      Transaction oldestTransaction = pointsQueue.poll();
      String payer = oldestTransaction.getPayer();

      double transactionPoints = oldestTransaction.getPoints();
      double pointsSpentTillNow = payerToSpentPoints.getOrDefault(payer, 0.0);
      if (transactionPoints < spendPoints) {
        pointsSpentTillNow += transactionPoints;
        availablePointsBalance -= transactionPoints;
        pointsBalance.put(payer, pointsBalance.getOrDefault(payer, 0.0) - transactionPoints);
        spendPoints -= transactionPoints;
      } else {
        pointsSpentTillNow += spendPoints;
        availablePointsBalance -= spendPoints;
        pointsQueue.add(new Transaction(payer, transactionPoints - spendPoints, oldestTransaction.getTimestamp()));
        pointsBalance.put(oldestTransaction.getPayer(), pointsBalance.getOrDefault(payer, 0.0) - spendPoints);
        spendPoints = 0;
      }

      payerToSpentPoints.put(payer, pointsSpentTillNow);
    }

    return payerToSpentPoints.entrySet()
        .stream()
        .map(entry -> new PointBalance(entry.getKey(), -1 * entry.getValue()))
        .collect(Collectors.toSet());
  }

  public Map<String, Double> getPointsBalance() {
    return pointsBalance;
  }



}

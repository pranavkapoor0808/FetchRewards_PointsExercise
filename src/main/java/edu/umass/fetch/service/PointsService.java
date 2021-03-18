package edu.umass.fetch.service;

import com.google.common.annotations.VisibleForTesting;
import edu.umass.fetch.request.SpendTransaction;
import edu.umass.fetch.request.Transaction;
import edu.umass.fetch.response.PointBalance;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

@Service
public class PointsService {

  @VisibleForTesting
  @Getter
  private double availablePointsBalance = 0;

  @VisibleForTesting
  @Getter
  private final Queue<Transaction> pointsQueue = new PriorityQueue<>((o1, o2) -> {
    if ((o1.getPoints() <= 0 && o2.getPoints() <= 0)
        || o1.getPoints() > 0 && o2.getPoints() > 0) {
      return o1.getTimestamp().compareTo(o2.getTimestamp());
    } else if (o1.getPoints() <= 0) {
      return -1;
    }
    return 1;
  });

  @Getter
  private final Map<String, Double> pointsBalance = new HashMap<>();

  public void addPoints(List<Transaction> transactions) throws IllegalStateException {

    if (CollectionUtils.isNotEmpty(transactions)) {
      Map<String, Double> pointsAddedPerPayer = transactions.stream()
          .collect(Collectors.groupingBy(Transaction::getPayer, Collectors.summingDouble(Transaction::getPoints)));

      for (Map.Entry<String, Double> entry : pointsAddedPerPayer.entrySet()) {
        if (entry.getValue() + pointsBalance.getOrDefault(entry.getKey(), 0.0) < 0) {
          throw new IllegalStateException(
              "Processing these transactions will cause " + entry.getKey() + " balance to become negative");
        }
      }

      pointsQueue.addAll(transactions);
      transactions.forEach(transaction -> {
        pointsBalance.put(transaction.getPayer(),
            transaction.getPoints() + pointsBalance.getOrDefault(transaction.getPayer(), 0.0));
        availablePointsBalance += transaction.getPoints();
      });
    }
  }

  public Set<PointBalance> spendPoints(SpendTransaction spendTransaction) throws IllegalStateException {
    double spendPoints = spendTransaction.getPoints();

    if (spendPoints <= 0) {
      throw new IllegalStateException("Spending negative points is not allowed. Try a positive number greater than zero");
    }
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
        pointsBalance.put(payer, pointsBalance.get(payer) - transactionPoints);

        spendPoints -= transactionPoints;
      } else {
        pointsSpentTillNow += spendPoints;
        availablePointsBalance -= spendPoints;

        if (transactionPoints - spendPoints > 0) {
          pointsQueue.add(new Transaction(payer, transactionPoints - spendPoints, oldestTransaction.getTimestamp()));
        }
        pointsBalance.put(oldestTransaction.getPayer(), pointsBalance.get(payer) - spendPoints);
        spendPoints = 0;
      }

      payerToSpentPoints.put(payer, pointsSpentTillNow);
    }

    return payerToSpentPoints.entrySet()
        .stream()
        .map(entry -> new PointBalance(entry.getKey(), -1 * entry.getValue()))
        .collect(Collectors.toSet());
  }

}

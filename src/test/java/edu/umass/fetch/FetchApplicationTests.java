package edu.umass.fetch;

import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import edu.umass.fetch.request.SpendTransaction;
import edu.umass.fetch.request.Transaction;
import edu.umass.fetch.response.PointBalance;
import edu.umass.fetch.service.PointsService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FetchApplicationTests {

  PointsService pointsService;

  @BeforeEach
  public void setUp() {
    this.pointsService = new PointsService();
  }

  /**
   * ADD POINTS UNIT TESTS - START
   **/

  @Test
  void testAddPoints_EmptyList() {
    List<Transaction> transactionList = new ArrayList<>();
    pointsService.addPoints(transactionList);

    assertTrue("The points per payer map is not empty", MapUtils.isEmpty(pointsService.getPointsBalance()));
    assertTrue("The transaction queue is not empty", CollectionUtils.isEmpty(pointsService.getPointsQueue()));
    assertEquals("The available points balance is not zero", 0.0, pointsService.getAvailablePointsBalance());
  }

  @Test
  void testAddPoints_SinglePayer_SingleTransaction_PositivePoints() {
    List<Transaction> transactionList = new ArrayList<>();
    transactionList.add(new Transaction("PAYER-1", 1000.0, LocalDateTime.now()));
    pointsService.addPoints(transactionList);

    assertEquals("The points per payer map is not size 1", 1, pointsService.getPointsBalance().size());
    assertEquals("The points for Payer 1 is not 1000.0", 1000.0, pointsService.getPointsBalance().get("PAYER-1"));
    assertEquals("The transaction queue is not size 1", 1, pointsService.getPointsQueue().size());
    assertEquals("The available points balance is not 1000.0", 1000.0, pointsService.getAvailablePointsBalance());
  }


  @Test
  void testAddPoints_SinglePayer_PositiveNegativePoints_Successful() {

    List<Transaction> transactionList = new ArrayList<>();
    LocalDateTime timestamp1 = LocalDateTime.now();
    LocalDateTime timestamp2 = timestamp1.plusSeconds(20);
    transactionList.add(new Transaction("PAYER-1", 1000.0, timestamp1));
    transactionList.add(new Transaction("PAYER-1", -800.0, timestamp2));
    pointsService.addPoints(transactionList);

    assertEquals("The points per payer map is not size 1", 1, pointsService.getPointsBalance().size());
    assertEquals("The points for Payer 1 is not 200.0", 200.0, pointsService.getPointsBalance().get("PAYER-1"));
    assertEquals("The transaction queue is not size 2", 2, pointsService.getPointsQueue().size());
    assertEquals("The top transaction in queue does not have " + timestamp2 + " as timestamp", timestamp2, pointsService.getPointsQueue().peek().getTimestamp());
    assertEquals("The available points balance is not 200.0", 200.0, pointsService.getAvailablePointsBalance());
  }

  @Test
  void testAddPoints_SinglePayer_PositiveNegativePoints_Unsuccessful() {

    List<Transaction> transactionList = new ArrayList<>();
    LocalDateTime timestamp1 = LocalDateTime.now();
    LocalDateTime timestamp2 = timestamp1.plusSeconds(20);
    transactionList.add(new Transaction("PAYER-1", 1000.0, timestamp1));
    transactionList.add(new Transaction("PAYER-1", -1200.0, timestamp2));

    IllegalStateException e = assertThrows(IllegalStateException.class, () -> pointsService.addPoints(transactionList));
    Assertions.assertEquals("Processing these transactions will cause PAYER-1 balance to become negative", e.getMessage());
    assertTrue("The points per payer map is not empty", MapUtils.isEmpty(pointsService.getPointsBalance()));
    assertTrue("The transaction queue is not empty", CollectionUtils.isEmpty(pointsService.getPointsQueue()));
    assertEquals("The available points balance is not zero", 0.0, pointsService.getAvailablePointsBalance());
  }

  @Test
  void testAddPoints_MultiplePayers_SingleTransaction_PositivePoints() {
    List<Transaction> transactionList = new ArrayList<>();
    LocalDateTime timestamp1 = LocalDateTime.now();
    LocalDateTime timestamp2 = timestamp1.minusSeconds(20);
    transactionList.add(new Transaction("PAYER-1", 1000.0, timestamp1));
    transactionList.add(new Transaction("PAYER-2", 300.0, timestamp2));
    pointsService.addPoints(transactionList);

    assertEquals("The points per payer map is not size 2", 2, pointsService.getPointsBalance().size());
    assertEquals("The points for Payer 1 is not 1000.0", 1000.0, pointsService.getPointsBalance().get("PAYER-1"));
    assertEquals("The points for Payer 2 is not 300.0", 300.0, pointsService.getPointsBalance().get("PAYER-2"));
    assertEquals("The transaction queue is not size 2", 2, pointsService.getPointsQueue().size());
    assertEquals("The top transaction in queue does not have " + timestamp2 + " as timestamp", timestamp2, pointsService.getPointsQueue().peek().getTimestamp());
    assertEquals("The available points balance is not 1300.0", 1300.0, pointsService.getAvailablePointsBalance());
  }

  @Test
  void testAddPoints_MultiplePayers_PositiveNegativePoints_Successful() {
    List<Transaction> transactionList = new ArrayList<>();
    LocalDateTime timestamp1 = LocalDateTime.now();
    LocalDateTime timestamp2 = timestamp1.plusSeconds(20);
    LocalDateTime timestamp3 = timestamp1.plusSeconds(30);
    LocalDateTime timestamp4 = timestamp1.plusSeconds(400);
    transactionList.add(new Transaction("PAYER-1", 1000.0, timestamp1));
    transactionList.add(new Transaction("PAYER-2", -300.0, timestamp2));
    transactionList.add(new Transaction("PAYER-1", -200.0, timestamp3));
    transactionList.add(new Transaction("PAYER-2", 500.0, timestamp4));
    pointsService.addPoints(transactionList);

    assertEquals("The points per payer map is not size 2", 2, pointsService.getPointsBalance().size());
    assertEquals("The points for Payer 1 is not 800.0", 800.0, pointsService.getPointsBalance().get("PAYER-1"));
    assertEquals("The points for Payer 2 is not 200.0", 200.0, pointsService.getPointsBalance().get("PAYER-2"));
    assertEquals("The transaction queue is not size 4", 4, pointsService.getPointsQueue().size());
    assertEquals("The top transaction in queue does not have " + timestamp2 + " as timestamp", timestamp2, pointsService.getPointsQueue().peek().getTimestamp());
    assertEquals("The available points balance is not 1000.0", 1000.0, pointsService.getAvailablePointsBalance());
  }

  @Test
  void testAddPoints_MultiplePayers_PositiveNegativePoints_Unsuccessful() {
    List<Transaction> transactionList = new ArrayList<>();
    LocalDateTime timestamp1 = LocalDateTime.now();
    LocalDateTime timestamp2 = timestamp1.plusSeconds(20);
    LocalDateTime timestamp3 = timestamp1.plusSeconds(30);
    LocalDateTime timestamp4 = timestamp1.plusSeconds(400);
    transactionList.add(new Transaction("PAYER-1", 1000.0, timestamp1));
    transactionList.add(new Transaction("PAYER-2", -300.0, timestamp2));
    transactionList.add(new Transaction("PAYER-1", -200.0, timestamp3));
    transactionList.add(new Transaction("PAYER-2", 100.0, timestamp4));

    assertThrows(IllegalStateException.class, () -> pointsService.addPoints(transactionList));
    assertTrue("The points per payer map is not empty", MapUtils.isEmpty(pointsService.getPointsBalance()));
    assertTrue("The transaction queue is not empty", CollectionUtils.isEmpty(pointsService.getPointsQueue()));
    assertEquals("The available points balance is not zero", 0.0, pointsService.getAvailablePointsBalance());
  }

  /**
   * ADD POINTS UNIT TESTS - END
   **/

  /**
   * SPEND POINTS UNIT TESTS - START
   **/

  @Test
  void testSpendPoints_ZeroPoints() {
    List<Transaction> transactionList = new ArrayList<>();
    transactionList.add(new Transaction("PAYER-1", 1000.0, LocalDateTime.now()));
    pointsService.addPoints(transactionList);

    SpendTransaction spendTransaction = new SpendTransaction(0.0);

    IllegalStateException e = assertThrows(IllegalStateException.class, () -> pointsService.spendPoints(spendTransaction));
    Assertions.assertEquals("Spending negative points is not allowed. Try a positive number greater than zero", e.getMessage());
  }

  @Test
  void testSpendPoints_NegativePoints() {
    List<Transaction> transactionList = new ArrayList<>();
    transactionList.add(new Transaction("PAYER-1", 1000.0, LocalDateTime.now()));
    pointsService.addPoints(transactionList);

    SpendTransaction spendTransaction = new SpendTransaction(-40.0);

    IllegalStateException e = assertThrows(IllegalStateException.class, () -> pointsService.spendPoints(spendTransaction));
    Assertions.assertEquals("Spending negative points is not allowed. Try a positive number greater than zero", e.getMessage());
  }

  @Test
  void testSpendPoints_MoreThanAvailablePoints() {
    List<Transaction> transactionList = new ArrayList<>();
    transactionList.add(new Transaction("PAYER-1", 1000.0, LocalDateTime.now()));
    pointsService.addPoints(transactionList);

    SpendTransaction spendTransaction = new SpendTransaction(4000.0);

    IllegalStateException e = assertThrows(IllegalStateException.class, () -> pointsService.spendPoints(spendTransaction));
    Assertions.assertEquals("Insufficient Point Balance. Points available: " + 1000.0 + " Redemption  request: " + 4000.0, e.getMessage());
  }

  @Test
  void testSpendPoints_DeductionSinglePayer() {
    List<Transaction> transactionList = new ArrayList<>();

    LocalDateTime timestamp1 = LocalDateTime.now();
    transactionList.add(new Transaction("PAYER-1", 1000.0, timestamp1));
    pointsService.addPoints(transactionList);

    SpendTransaction spendTransaction1 = new SpendTransaction(800.0);
    Set<PointBalance> pointBalances1 = pointsService.spendPoints(spendTransaction1);

    assertEquals("The points were not deducted from only 1 payer", pointBalances1.size(), 1);
    assertEquals("Payer is not PAYER-1", "PAYER-1", pointBalances1.stream().map(PointBalance::getPayer).findAny().get());
    assertEquals("Points deducted for PAYER-1 is not -800.0", -800.0, pointBalances1.stream().map(PointBalance::getPoints).findAny().get());
    assertEquals("The points per payer map is not size 1", 1, pointsService.getPointsBalance().size());
    assertEquals("The points for Payer 1 is not 200.0", 200.0, pointsService.getPointsBalance().get("PAYER-1"));
    assertEquals("The transaction queue is not size 1", 1, pointsService.getPointsQueue().size());
    assertEquals("The top transaction in queue does not have " + timestamp1 + " as timestamp", timestamp1, pointsService.getPointsQueue().peek().getTimestamp());
    assertEquals("The available points balance is not 200.0", 200.0, pointsService.getAvailablePointsBalance());

    SpendTransaction spendTransaction2 = new SpendTransaction(200.0);
    Set<PointBalance> pointBalances2 = pointsService.spendPoints(spendTransaction2);

    assertEquals("The points were not deducted from only 1 payer", pointBalances2.size(), 1);
    assertEquals("Payer is not PAYER-1", "PAYER-1", pointBalances2.stream().map(PointBalance::getPayer).findAny().get());
    assertEquals("Points deducted for PAYER-1 is not -200.0", -200.0, pointBalances2.stream().map(PointBalance::getPoints).findAny().get());
    assertEquals("The points per payer map is not size 1", 1, pointsService.getPointsBalance().size());
    assertEquals("The points for Payer 1 is not 0.0", 0.0, pointsService.getPointsBalance().get("PAYER-1"));
    assertTrue("The transaction queue is not empty", CollectionUtils.isEmpty(pointsService.getPointsQueue()));
    assertEquals("The available points balance is not 0.0", 0.0, pointsService.getAvailablePointsBalance());
  }

  @Test
  void testSpendPoints_DeductionSinglePayer_NegativeQueueEntries() {
    List<Transaction> transactionList = new ArrayList<>();

    LocalDateTime timestamp1 = LocalDateTime.now();
    LocalDateTime timestamp2 = LocalDateTime.now().plusSeconds(20);
    transactionList.add(new Transaction("PAYER-1", -200.0, timestamp1));
    transactionList.add(new Transaction("PAYER-1", 1200.0, timestamp2));
    pointsService.addPoints(transactionList);

    SpendTransaction spendTransaction1 = new SpendTransaction(800.0);
    Set<PointBalance> pointBalances1 = pointsService.spendPoints(spendTransaction1);

    assertEquals("The points were not deducted from only 1 payer", pointBalances1.size(), 1);
    assertEquals("Payer is not PAYER-1", "PAYER-1", pointBalances1.stream().map(PointBalance::getPayer).findAny().get());
    assertEquals("Points deducted for PAYER-1 is not -800.0", -800.0, pointBalances1.stream().map(PointBalance::getPoints).findAny().get());
    assertEquals("The points per payer map is not size 1", 1, pointsService.getPointsBalance().size());
    assertEquals("The points for PAYER-1 is not 200.0", 200.0, pointsService.getPointsBalance().get("PAYER-1"));
    assertEquals("The transaction queue is not size 1", 1, pointsService.getPointsQueue().size());
    assertEquals("The top transaction in queue does not have " + timestamp2 + " as timestamp", timestamp2, pointsService.getPointsQueue().peek().getTimestamp());
    assertEquals("The available points balance is not 200.0", 200.0, pointsService.getAvailablePointsBalance());

    SpendTransaction spendTransaction2 = new SpendTransaction(200.0);
    Set<PointBalance> pointBalances2 = pointsService.spendPoints(spendTransaction2);

    assertEquals("The points were not deducted from only 1 payer", pointBalances2.size(), 1);
    assertEquals("Payer is not PAYER-1", "PAYER-1", pointBalances2.stream().map(PointBalance::getPayer).findAny().get());
    assertEquals("Points deducted for PAYER-1 is not -200.0", -200.0, pointBalances2.stream().map(PointBalance::getPoints).findAny().get());
    assertEquals("The points per payer map is not size 1", 1, pointsService.getPointsBalance().size());
    assertEquals("The points for PAYER-1 is not 0.0", 0.0, pointsService.getPointsBalance().get("PAYER-1"));
    assertTrue("The transaction queue is not empty", CollectionUtils.isEmpty(pointsService.getPointsQueue()));
    assertEquals("The available points balance is not 0.0", 0.0, pointsService.getAvailablePointsBalance());
  }

  @Test
  void testSpendPoints_DeductionMultiplePayer() {
    List<Transaction> transactionList1 = new ArrayList<>();
    LocalDateTime timestamp1 = LocalDateTime.now();
    LocalDateTime timestamp2 = LocalDateTime.now().plusSeconds(20);
    LocalDateTime timestamp3 = LocalDateTime.now().plusSeconds(30);
    transactionList1.add(new Transaction("PAYER-1", 200.0, timestamp1));
    transactionList1.add(new Transaction("PAYER-2", 1200.0, timestamp2));
    transactionList1.add(new Transaction("PAYER-1", -100.0, timestamp3));
    pointsService.addPoints(transactionList1);

    SpendTransaction spendTransaction1 = new SpendTransaction(800.0);
    Set<PointBalance> pointBalances1 = pointsService.spendPoints(spendTransaction1);

    assertEquals("The points were not deducted from 2 payers", 2, pointBalances1.size());
    assertEquals("Payer is not PAYER-1", "PAYER-1", pointBalances1.stream().map(PointBalance::getPayer)
        .filter(payer -> payer.equals("PAYER-1")).findAny().get());
    assertEquals("Points deducted for PAYER-1 is not -100.0", -100.0, pointBalances1.stream().filter(pointBalance -> pointBalance.getPayer().equals("PAYER-1"))
        .map(PointBalance::getPoints).findAny().get());
    assertEquals("Payer is not PAYER-2", "PAYER-2", pointBalances1.stream().map(PointBalance::getPayer)
        .filter(payer -> payer.equals("PAYER-2")).findAny().get());
    assertEquals("Points deducted for PAYER-2 is not -700.0", -700.0, pointBalances1.stream().filter(pointBalance -> pointBalance.getPayer().equals("PAYER-2"))
        .map(PointBalance::getPoints).findAny().get());
    assertEquals("The points per payer map is not size 2", 2, pointsService.getPointsBalance().size());
    assertEquals("The points for Payer 1 is not 0.0", 0.0, pointsService.getPointsBalance().get("PAYER-1"));
    assertEquals("The points for Payer 2 is not 500.0", 500.0, pointsService.getPointsBalance().get("PAYER-2"));
    assertEquals("The transaction queue is not size 1", 1, pointsService.getPointsQueue().size());
    assertEquals("The top transaction in queue does not have " + timestamp2 + " as timestamp", timestamp2, pointsService.getPointsQueue().peek().getTimestamp());
    assertEquals("The available points balance is not 500.0", 500.0, pointsService.getAvailablePointsBalance());

    SpendTransaction spendTransaction2 = new SpendTransaction(200.0);
    Set<PointBalance> pointBalances2 = pointsService.spendPoints(spendTransaction2);


    assertEquals("The points were not deducted from 1 payer", 1, pointBalances2.size());
    assertEquals("Payer is not PAYER-2", "PAYER-2", pointBalances2.stream().map(PointBalance::getPayer)
        .filter(payer -> payer.equals("PAYER-2")).findAny().get());
    assertEquals("Points deducted for PAYER-2 is not -200.0", -200.0, pointBalances2.stream().filter(pointBalance -> pointBalance.getPayer().equals("PAYER-2"))
        .map(PointBalance::getPoints).findAny().get());
    assertEquals("The points per payer map is not size 2", 2, pointsService.getPointsBalance().size());
    assertEquals("The points for Payer 1 is not 0.0", 0.0, pointsService.getPointsBalance().get("PAYER-1"));
    assertEquals("The points for Payer 2 is not 300.0", 300.0, pointsService.getPointsBalance().get("PAYER-2"));
    assertEquals("The transaction queue is not size 1", 1, pointsService.getPointsQueue().size());
    assertEquals("The top transaction in queue does not have " + timestamp2 + " as timestamp", timestamp2, pointsService.getPointsQueue().peek().getTimestamp());
    assertEquals("The available points balance is not 300.0", 300.0, pointsService.getAvailablePointsBalance());

    List<Transaction> transactionList2 = new ArrayList<>();
    LocalDateTime timestamp4 = timestamp1.minusSeconds(100);
    LocalDateTime timestamp5 = timestamp1.minusSeconds(200);
    transactionList2.add(new Transaction("PAYER-1", 300.0, timestamp4));
    transactionList2.add(new Transaction("PAYER-2", -100.0, timestamp5));
    pointsService.addPoints(transactionList2);

    assertEquals("The points per payer map is not size 2", 2, pointsService.getPointsBalance().size());
    assertEquals("The points for Payer 1 is not 300.0", 300.0, pointsService.getPointsBalance().get("PAYER-1"));
    assertEquals("The points for Payer 2 is not 200.0", 200.0, pointsService.getPointsBalance().get("PAYER-2"));
    assertEquals("The transaction queue is not size 3", 3, pointsService.getPointsQueue().size());
    assertEquals("The top transaction in queue does not have " + timestamp5 + " as timestamp", timestamp5, pointsService.getPointsQueue().peek().getTimestamp());
    assertEquals("The available points balance is not 500.0", 500.0, pointsService.getAvailablePointsBalance());

    SpendTransaction spendTransaction3 = new SpendTransaction(400.0);
    Set<PointBalance> pointBalances3 = pointsService.spendPoints(spendTransaction3);

    assertEquals("The points were not deducted from 2 payers", 2, pointBalances3.size());
    assertEquals("Payer is not PAYER-1", "PAYER-1", pointBalances3.stream().map(PointBalance::getPayer)
        .filter(payer -> payer.equals("PAYER-1")).findAny().get());
    assertEquals("Points deducted for PAYER-1 is not -300.0", -300.0, pointBalances3.stream().filter(pointBalance -> pointBalance.getPayer().equals("PAYER-1"))
        .map(PointBalance::getPoints).findAny().get());
    assertEquals("Payer is not PAYER-2", "PAYER-2", pointBalances3.stream().map(PointBalance::getPayer)
        .filter(payer -> payer.equals("PAYER-2")).findAny().get());
    assertEquals("Points deducted for PAYER-2 is not -100.0", -100.0, pointBalances3.stream().filter(pointBalance -> pointBalance.getPayer().equals("PAYER-2"))
        .map(PointBalance::getPoints).findAny().get());
    assertEquals("The points per payer map is not size 2", 2, pointsService.getPointsBalance().size());
    assertEquals("The points for Payer 1 is not 0.0", 0.0, pointsService.getPointsBalance().get("PAYER-1"));
    assertEquals("The points for Payer 2 is not 100.0", 100.0, pointsService.getPointsBalance().get("PAYER-2"));
    assertEquals("The transaction queue is not size 1", 1, pointsService.getPointsQueue().size());
    assertEquals("The top transaction in queue does not have " + timestamp2 + " as timestamp", timestamp2, pointsService.getPointsQueue().peek().getTimestamp());
    assertEquals("The available points balance is not 100.0", 100.0, pointsService.getAvailablePointsBalance());

  }

  /**
   * SPEND POINTS UNIT TESTS - START
   **/

}

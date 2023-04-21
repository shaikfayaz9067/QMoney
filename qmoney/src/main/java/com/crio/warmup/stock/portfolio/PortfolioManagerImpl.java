
package com.crio.warmup.stock.portfolio;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.management.RuntimeErrorException;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager {


 
  private RestTemplate restTemplate;

  // Caution: Do not delete or modify the constructor, or else your build will break!
  // This is absolutely necessary for backward compatibility
  protected PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }


  //TODO: CRIO_TASK_MODULE_REFACTOR
  // 1. Now we want to convert our code into a module, so we will not call it from main anymore.
  //    Copy your code from Module#3 PortfolioManagerApplication#calculateAnnualizedReturn
  //    into #calculateAnnualizedReturn function here and ensure it follows the method signature.
  // 2. Logic to read Json file and convert them into Objects will not be required further as our
  //    clients will take care of it, going forward.

  // Note:
  // Make sure to exercise the tests inside PortfolioManagerTest using command below:
  // ./gradlew test --tests PortfolioManagerTest

  //CHECKSTYLE:OFF




  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }

  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Extract the logic to call Tiingo third-party APIs to a separate function.
  //  Remember to fill out the buildUri function and use that.


  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException {
      if(from.compareTo(to)>=0){
        throw new RuntimeException();
      }
      
      
      String url=buildUri(symbol, from, to);

      TiingoCandle[] tinCandles=restTemplate.getForObject(url, TiingoCandle[].class);

      // return Arrays.asList(tinCandles);
      if(tinCandles==null){
        return new ArrayList<Candle>();
      }
      else{
        return Arrays.asList(tinCandles);
      }

  }
  
  public static String getToken() {
    return "2e833dc8c89136ed202e71e60175910b2d7a0900";
  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
       String uriTemplate = "https:api.tiingo.com/tiingo/daily/$SYMBOL/prices?"
            + "startDate=$STARTDATE&endDate=$ENDDATE&token=$APIKEY";
            
        String url =uriTemplate.replace("$APIKEY", getToken()).replace("$ENDDATE", endDate.toString()).replace("$STARTDATE",startDate.toString()).replace("$SYMBOL",symbol);

        return url;
  }


  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades,
      LocalDate endDate) {

        AnnualizedReturn annualizedReturn ;
        List<AnnualizedReturn> annualizedReturns=new ArrayList<>();

        for(int i=0;i<portfolioTrades.size();i++){
          annualizedReturn=getAnnualizedReturn(portfolioTrades.get(i),endDate);
          annualizedReturns.add(annualizedReturn);
        }

       Collections.sort(annualizedReturns,getComparator());



    // TODO Auto-generated method stub
    return annualizedReturns;
  }


  private AnnualizedReturn getAnnualizedReturn(PortfolioTrade portfolioTrade,LocalDate endDate) {

    AnnualizedReturn annualizedReturn;
    String symbol =portfolioTrade.getSymbol();
    LocalDate startdDate=portfolioTrade.getPurchaseDate();
     
    try{
      List<Candle> tCandles;
      tCandles=getStockQuote(symbol, startdDate, endDate);
      Candle stockstratdate= tCandles.get(0);
      Candle stockEndDate= tCandles.get(tCandles.size()-1);

      Double startprice =stockstratdate.getOpen();
      Double endprice =stockEndDate.getClose();

      Double totalReturn = (endprice-startprice)/startprice;
        
      double numDays = ChronoUnit.DAYS.between(portfolioTrade.getPurchaseDate(), endDate);

        
      double annualizedreturn =Math.pow((1+totalReturn), (1/(numDays/365)))-1;
      annualizedReturn=new AnnualizedReturn(symbol, annualizedreturn, totalReturn);


    }
    catch(JsonProcessingException e){
      annualizedReturn=new AnnualizedReturn(symbol, Double.NaN, Double.NaN);


    }


    return annualizedReturn;
  }
}

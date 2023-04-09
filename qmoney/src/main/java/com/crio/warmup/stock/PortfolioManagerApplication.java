
package com.crio.warmup.stock;


import com.crio.warmup.stock.dto.*;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;


public class PortfolioManagerApplication {

   public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {
      
      File file=resolveFileFromResources(args[0]);
      ObjectMapper objectMapper=getObjectMapper();
      PortfolioTrade[] trades=objectMapper.readValue(file,PortfolioTrade[].class);
      List<String> symbols=new ArrayList<String>();
      for(PortfolioTrade trade:trades)
        symbols.add(trade.getSymbol());
     return symbols;
   }



  // TODO: CRIO_TASK_MODULE_REST_API
  //  Find out the closing price of each stock on the end_date and return the list
  //  of all symbols in ascending order by its close value on end date.

  // Note:
  // 1. You may have to register on Tiingo to get the api_token.
  // 2. Look at args parameter and the module instructions carefully.
  // 2. You can copy relevant code from #mainReadFile to parse the Json.
  // 3. Use RestTemplate#getForObject in order to call the API,
  //    and deserialize the results in List<Candle>



  // public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {
    
  //   //list of portfolio trade 
  //   //args[0] file name
  //   String token="2e833dc8c89136ed202e71e60175910b2d7a0900";
    
  //  //  ObjectMapper om=getObjectMapper();
  //  //  PortfolioTrade[] portfolioTrades=om.readValue(resolveFileFromResources(args[0]), PortfolioTrade[].class);
  //   List<PortfolioTrade> list=readTradesFromJson(args[0]);
   
  //  //  list=Arrays.asList(portfolioTrades);
  //  RestTemplate restTemplate=new RestTemplate();
  //    List<TotalReturnsDto> totalReturnsDtos=new ArrayList<>();

  //    for(PortfolioTrade item:list){

  //     // String uri=prepareUrl(item, args[1], token);
  //     String uri= "https://api.tiingo.com/tiingo/daily/"+item.getSymbol()+"prices?startDate="+item.getPurchaseDate().toString()+"&endDate="+args[1]+"&token=2e833dc8c89136ed202e71e60175910b2d7a0900";

  //       TiingoCandle[] tiingoCandles=restTemplate.getForObject(uri, TiingoCandle[].class);
       
  //       if(tiingoCandles!=null){
  //        totalReturnsDtos.add(new TotalReturnsDto(item.getSymbol(), tiingoCandles[tiingoCandles.length-1].getClose()));
  //       }

  //    }
  //    //sort the total return DTO
  //    //creat a list of string and iterate over sorted list and save getSymbol of every item 
  //    //return the list
  //    List<String> list2=new ArrayList<>();
  //   final Comparator<TotalReturnsDto> closingComparator=new Comparator<TotalReturnsDto>() {
  //     public int compare(TotalReturnsDto td1,TotalReturnsDto td2){
  //        return (int)(td1.getClosingPrice().compareTo(td2.getClosingPrice()));
  //     }
  //    };

  //    Collections.sort(totalReturnsDtos,closingComparator);

  //    for(TotalReturnsDto item:totalReturnsDtos){
  //           list2.add(item.getSymbol());
  //    }
  //    return list2;
  // }

  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {
    RestTemplate restTemplate = new RestTemplate();
    List<TotalReturnsDto> totalReturnsDto = new ArrayList<TotalReturnsDto>();
    List<PortfolioTrade> portfolioTrade = readTradesFromJson(args[0]);
    for(PortfolioTrade t : portfolioTrade){
      LocalDate endDate = LocalDate.parse(args[1]);
      String url = prepareUrl(t, endDate, getToken());
      System.out.println(url);
      TiingoCandle[] results = restTemplate.getForObject(url, TiingoCandle[].class);
      if(results != null){
        totalReturnsDto.add(new TotalReturnsDto(t.getSymbol(), results[results.length - 1].getClose()));
      }
    }
    Collections.sort(totalReturnsDto, new Comparator<TotalReturnsDto>(){
      
      @Override
      public int compare(TotalReturnsDto o1, TotalReturnsDto o2){
        return (int) (o1.getClosingPrice().compareTo(o2.getClosingPrice()));
      }

    });

    List<String> listAnswer = new ArrayList<>();
    for(int i = 0; i < totalReturnsDto.size(); i++){
      listAnswer.add(totalReturnsDto.get(i).getSymbol());
    }
    return listAnswer;
  }

  public static String getToken() {
    return "f8f43e95887cfbd3079e95cc1df68412e608e7ae";
  }




  // TODO:
  //  After refactor, make sure that the tests pass by using these two commands
  //  ./gradlew test --tests PortfolioManagerApplicationTest.readTradesFromJson
  //  ./gradlew test --tests PortfolioManagerApplicationTest.mainReadFile
  public static List<PortfolioTrade> readTradesFromJson(String filename) throws IOException, URISyntaxException {

   ObjectMapper objectMapper=getObjectMapper();

   // List<PortfolioTrade> portfolioTrades= new ArrayList<>();

   PortfolioTrade[] portfolioTrades2=objectMapper.readValue(readFileAsString(filename), PortfolioTrade[].class);
   
     return Arrays.asList(portfolioTrades2);
  }

  private static String readFileAsString(String filename) throws URISyntaxException, IOException {

    return new String(Files.readAllBytes(resolveFileFromResources(filename).toPath()), "UTF-8");

  }




  // TODO:
  //  Build the Url using given parameters and use this function in your code to cann the API.
  public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) {

   // RestTemplate restTemplate=new RestTemplate();

    String uri= "https://api.tiingo.com/tiingo/daily/"+trade.getSymbol()+"/prices?startDate="+trade.getPurchaseDate().toString()+"&endDate="+endDate+"&token="+token;
   //  TiingoCandle[] result=restTemplate.getForObject(uri, TiingoCandle[].class);
     return uri;
  }


   private static ObjectMapper getObjectMapper() {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.registerModule(new JavaTimeModule());
      return objectMapper;
   }



   public static List<String> debugOutputs() {
        String valueOfArgument0 = "trades.json";
        String resultOfResolveFilePathArgs0 = "/home/crio-user/workspace/prashant-criodo-ME_QMONEY_V2/qmoney/bin/main/trades.json";
        String toStringOfObjectMapper = "com.fasterxml.jackson.databind.ObjectMapper@5542c4ed";
        String functionNameFromTestFileInStackTrace = "PortfolioManagerApplicationTest.mainReadFile()";
        String lineNumberFromTestFileInStackTrace = "29:1";

        return Arrays.asList(new String[]{valueOfArgument0, resultOfResolveFilePathArgs0,
                toStringOfObjectMapper, functionNameFromTestFileInStackTrace,
                lineNumberFromTestFileInStackTrace});
    }

    private static File resolveFileFromResources(String filename) throws URISyntaxException {
      return Paths.get(
          Thread.currentThread().getContextClassLoader().getResource(filename).toURI()).toFile();
    }



  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());


    printJsonObject(mainReadQuotes(args));


  }

  private static void printJsonObject(List<String> mainReadQuotes) {}
}


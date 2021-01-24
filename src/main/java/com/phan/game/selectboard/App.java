package com.phan.game.selectboard;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.json.simple.JSONArray;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.ApplicationContext;import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.EnableScheduling;

//import com.phan.game.fcm.PushNotificationService;
import com.phan.game.pojo.GridData;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
/**
 * Hello world!
 *
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.phan.game.selectboard", "com.phan.game.fcm"})
@EnableScheduling
public class App 
{	
	public static void main( String[] args )
	{
		System.out.println( "Command Line arguments: " + args.length );
		for (String cmdArg : args) {
			System.out.println(cmdArg);
		}
		SpringApplication.run(App.class, args);
	}


	@Bean
	public CommandLineRunner commandLineRunner(final ApplicationContext ctx) {
		return args -> {

//			System.out.println("Let's inspect the beans provided by Spring Boot:");
//
//			String[] beanNames = ctx.getBeanDefinitionNames();
//			Arrays.sort(beanNames);
//			for (String beanName : beanNames) {
//				System.out.println(beanName);
//			}
			//CacheData.pushNotificationService = (PushNotificationService)ctx.getBean(PushNotificationService.class);
			readfileGridData("CoDGotTalentv3.csv");
			CacheData.populateInputArgMap(args);
			GameManager.buildStateGraph();			
		};
	}

	public static void readfileGridData(String fileName) 
	{

		InputStream inputStream = null;
		try {
			System.out.println( "run started! File: " + fileName );
			Resource resource = new ClassPathResource("classpath:" + fileName);
			inputStream = resource.getInputStream();
			List<GridData> data = CSVConverter.convertToGridData2(inputStream);
			inputStream.close();
			CacheData.createCategoryMap(data);
			System.out.println( "run ended !" );
		} catch (Exception e) {
			System.out.println("Exception" + e.getMessage());
		} 
	}
	
	public static void readfileJSON(String fileName) 
	{

		InputStream inputStream = null;
		try {
			System.out.println( "run started! File: " + fileName );
			Resource resource = new ClassPathResource("classpath:" + fileName);
			inputStream = resource.getInputStream();
			JSONArray json = CSVConverter.convert(inputStream);
			System.out.println("Loaded file: \n" + json.toJSONString());
			inputStream.close();
			System.out.println( "run ended !" );
		} catch (Exception e) {
			System.out.println("Exception" + e.getMessage());
		} 
	}
	
	private static String readfileBuffer(String fileName) {
		InputStream inputStream = null;
		try {
			System.out.println( "run started !" );
			Resource resource = new ClassPathResource("classpath:" + fileName);
			inputStream = resource.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			StringBuilder out = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				out.append(line);
			}
			System.out.println(out.toString());   //Prints the string content read from input stream
			reader.close();
			return out.toString();
		} catch (Exception e) {
			System.out.println("Exception" + e.getMessage());
		}
		return null;
	}
}




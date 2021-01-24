package com.phan.game.selectboard;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.csv.CsvSchema.ColumnType;
import com.phan.game.pojo.GridData;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

public class CSVConverter {
	public static JSONArray convert(InputStream input) throws Exception {

		JSONParser parser = new JSONParser();
		CsvSchema csvSchema = CsvSchema.builder().setUseHeader(true).build();
		CsvMapper csvMapper = new CsvMapper();

		// Read data from CSV file
		List<? extends Object> readAll = csvMapper.readerFor(Map.class).with(csvSchema).readValues(input).readAll();
		ObjectMapper mapper = new ObjectMapper();
		JSONArray jsonObject = (JSONArray) parser.parse(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(readAll));
//		System.out.print(jsonObject.toString());

		return jsonObject;

	}

	// Doesn't work, columns and data don't match
	public static List<GridData> convertToCategoryEntry(InputStream input) throws Exception {
		CsvMapper csvMapper = new CsvMapper();
		List<GridData> gridData = new ArrayList<>();
		CsvSchema schema = csvMapper
				.schemaFor(GridData.class)
				.withSkipFirstDataRow(true)
				.withUseHeader(false)
				.withEscapeChar('\\')
				.withLineSeparator("\n")
				.withColumnSeparator(',');
				
		final ObjectReader reader;
		reader = csvMapper.readerFor(GridData.class).with(schema);
		final MappingIterator<GridData> mappingIterator = reader.readValues(input);
		while(mappingIterator.hasNext()) {
			GridData result = mappingIterator.next();
			gridData.add(result);
			System.out.println(
					"title=" + result.getTitle() + 
					" : URL=" + result.getUrl() + 
					" : embeddedUrl=" + result.getEmbeddedUrl() + 
					" : category=" + result.getCategory() + 
					" : author=" + result.getAuthor() +
					" : points=" + result.getPoints());
		}

		System.out.println("number of gridData objects in list: " + gridData.size());
		return gridData;
	}
	
	public static List<GridData> convertToGridData2(InputStream input) throws Exception {
		CsvMapper csvMapper = new CsvMapper();
		List<GridData> gridData = new ArrayList<>();
		CsvSchema schema = CsvSchema.builder()
				.addColumn("title", ColumnType.STRING)
				.addColumn("url", ColumnType.STRING)
				.addColumn("embeddedUrl", ColumnType.STRING)
				.addColumn("category", ColumnType.STRING)
				.addColumn("author", ColumnType.STRING)
				.addColumn("points", ColumnType.STRING)
				.setSkipFirstDataRow(true)
				.setUseHeader(false)
				.setColumnSeparator(',')
				.setLineSeparator("\n")
				.build();
				
		final ObjectReader reader;
		reader = csvMapper.readerFor(GridData.class).with(schema);
		final MappingIterator<GridData> mappingIterator = reader.readValues(input);
		while(mappingIterator.hasNext()) {
			GridData result = mappingIterator.next();
			gridData.add(result);
//			System.out.println(
//					"title=" + result.getTitle() + 
//					" : URL=" + result.getUrl() + 
//					" : embeddedUrl=" + result.getEmbeddedUrl() + 
//					" : category=" + result.getCategory() + 
//					" : author=" + result.getAuthor() +
//					" : points=" + result.getPoints());
		}

		System.out.println("number of gridData objects in list: " + gridData.size());
		return gridData;
	}


}
